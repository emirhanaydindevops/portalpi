package com.canovate.uploader.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.canovate.uploader.core.exception.ObjectStorageException;
import com.canovate.uploader.security.SecurityUtils;
import com.canovate.uploader.service.inter.IDeleteProgress;
import com.canovate.uploader.service.inter.IObjectStorageProcessor;
import com.canovate.uploader.service.inter.IUploadProgress;
import com.canovate.uploader.util.CommonUtils;
import com.canovate.uploader.util.FileOperationUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ObjectStorageProcessor implements IObjectStorageProcessor {

    private AmazonS3 osClient;

    private static final String DEFAULT_CONTENT_TYPE = "text/plain";
    private static final int DOWNLOAD_RETRY_COUNT = 10;
    private static final int RECONNECT_COUNT = 25;

    private AwsClientBuilder.EndpointConfiguration endpointConfiguration;
    private String bucketName;
    private AWSStaticCredentialsProvider credentialsProvider;
    private Region region;
    private int getOsClientCounter = 0;

    public ObjectStorageProcessor(String access, String secret, String endpoint, String bucketName) {
        credentialsProvider = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(access, secret));
        endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(endpoint, null);
        this.bucketName = bucketName;
    }

    public ObjectStorageProcessor(String access, String secret, Region region, String bucketName) {
        credentialsProvider = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(access, secret));
        this.region = region;
        this.bucketName = bucketName;
    }

    private AmazonS3 getOsClient() {

        if (getOsClientCounter-- <= 0) {
            if (Objects.nonNull(osClient))
                osClient.shutdown();
            if (Objects.nonNull(endpointConfiguration))
                osClient = AmazonS3ClientBuilder.standard()
                        .withCredentials(credentialsProvider)
                        .withEndpointConfiguration(endpointConfiguration)
                        .withPathStyleAccessEnabled(true)
                        .build();
            else
                osClient = AmazonS3ClientBuilder.standard()
                        .withCredentials(credentialsProvider)
                        .withPathStyleAccessEnabled(true)
                        .withRegion(region.getName())
                        .build();
            getOsClientCounter = RECONNECT_COUNT;
        }

        return osClient;
    }

    public String getUrl(String bucketName, String fileName) {
        if (!getOsClient().doesBucketExistV2(bucketName))
            createBucket();
        return getOsClient().getUrl(bucketName, fileName).toString();
    }

    public void upload(String keyName, String filePath, String contentType) throws SdkClientException, AmazonServiceException, FileNotFoundException {
        createBucket();

        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.length());
        objectMetadata.setContentType(contentType);
        getOsClient().putObject(bucketName, keyName, fileInputStream, objectMetadata);
    }

    public void upload(String keyName, String filePath) throws SdkClientException, AmazonServiceException, FileNotFoundException {
        upload(keyName, filePath, DEFAULT_CONTENT_TYPE);
    }

    public void uploadString(String keyName, String data, String contentType) throws SdkClientException, AmazonServiceException {
        InputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(data.length());
        objectMetadata.setContentType(contentType);
        getOsClient().putObject(bucketName, keyName, inputStream, objectMetadata);
    }

    public void uploadString(String keyName, String data) throws SdkClientException, AmazonServiceException {
        uploadString(keyName, data, DEFAULT_CONTENT_TYPE);
    }

    public void uploadFolderCheckForExists(String osPath, String folderPath, IUploadProgress uploadProgress) throws FileNotFoundException {
        createBucket();
        File folder = new File(folderPath);
        List<File> list = FileOperationUtils.getFileListRecursive(folder);
        int downloadCount = 0;
        for (File file : list) {
            boolean uploadFile = file.isFile();
            if (!uploadFile)
                continue;
            uploadFile = uploadProgress.beforeUpload(file, list.size(), ++downloadCount);

            if (!uploadFile)
                continue;

            String keyName = file.getPath().substring(folderPath.length() + 1);
            keyName = CommonUtils.unixPath(keyName);
            uploadIfDifferent(osPath + "/" + keyName, file);

            uploadProgress.uploaded(file);
        }
    }

    private void uploadIfDifferent(String keyName, File file) throws FileNotFoundException {
        boolean objExist = getOsClient().doesObjectExist(bucketName, keyName);
        if (objExist) {
            S3Object obj = getOsClient().getObject(bucketName, keyName);
            boolean sameFile = false;
            try {
                sameFile = obj.getObjectMetadata().getETag().equals(SecurityUtils.getFileMD5(file));
            } catch (IOException e) {
                log.error("uploadIfDifferent::error MD5 error bucket: {}, keyName: {}, filePath: {}", bucketName, keyName, file.getPath(), e);
            }
            if (!sameFile) {
                upload(keyName, file.getPath());
            }
        } else
            upload(keyName, file.getPath());
    }

    public byte[] downloadAsBytes(String awsFilePath) throws SdkClientException, AmazonServiceException, IOException, ObjectStorageException {
        if (!getOsClient().doesBucketExistV2(bucketName))
            throw new ObjectStorageException("Bucket not exist BucketName: " + bucketName);

        S3Object o = getOsClient().getObject(bucketName, awsFilePath);
        S3ObjectInputStream s3is = o.getObjectContent();
        return s3is.readAllBytes();
    }

    public void download(String awsFilePath, File localFilePath) throws ObjectStorageException, IOException {
        if (!getOsClient().doesBucketExistV2(bucketName))
            throw new ObjectStorageException("Bucket not exist BucketName: " + bucketName);

        onlyDownload(awsFilePath, localFilePath);
    }

    private void onlyDownload(String awsFilePath, File localFilePath) throws IOException {
        int retryCount = DOWNLOAD_RETRY_COUNT;
        while (true) {
            try {
                GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, awsFilePath);
                getOsClient().getObject(getObjectRequest, localFilePath);
                break;
            } catch (Exception e) {
                retryCount--;
                if (retryCount <= 0) {
                    throw new IOException("onlyDownload::error Error downloading file after " + DOWNLOAD_RETRY_COUNT + " try, File: " + awsFilePath, e);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
            }
        }
        if (retryCount != DOWNLOAD_RETRY_COUNT)
            log.info("Test: File downloaded after " + (DOWNLOAD_RETRY_COUNT - retryCount) + " try, File: " + awsFilePath);
    }

    public void downloadFolder(String awsFilePath, String localFolderPath) throws ObjectStorageException, InterruptedException {
        if (!getOsClient().doesBucketExistV2(bucketName))
            throw new ObjectStorageException("Bucket not exist BucketName: " + bucketName);

        TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(getOsClient()).build();

        File desktop = new File(localFolderPath);

        MultipleFileDownload download = transferManager.downloadDirectory(bucketName, awsFilePath, desktop);
        download.waitForCompletion();
    }

    public void deleteRemovedFiles(String awsFolderPath, String localFolderPath) throws ObjectStorageException {
        ListObjectsRequest lor = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix(awsFolderPath);
        ObjectListing objectListing = getOsClient().listObjects(lor);

        List<S3ObjectSummary> objectList = objectListing.getObjectSummaries();
        if (Objects.isNull(objectListing) || objectList.size() <= 0)
            throw new ObjectStorageException("Object Storage Folder doesn't contain any file. Bucket = " + bucketName + " Folder = " + awsFolderPath);

        List<File> deletedFiles = findDeletedFiles(objectList, awsFolderPath, localFolderPath);
        deleteFiles(deletedFiles);
    }

    private void deleteFiles(List<File> deleteFiles) {
        for (File file : deleteFiles)
            file.delete();
    }

    private List<File> findDeletedFiles(List<S3ObjectSummary> objectList, String awsFolderPath, String localFolderPath) {
        List<File> fileList = getFileList(new File(localFolderPath));
        for (S3ObjectSummary summary : objectList) {
            if (summary.getSize() <= 0)
                continue;

            String awsFileName = summary.getKey().substring(awsFolderPath.length());

            File file = new File(localFolderPath + "/" + awsFileName);

            if (fileList.contains(file)) {
                fileList.remove(file);
            }
        }
        return fileList;
    }

    private List<File> getFileList(File localFile) {
        List<File> listFile = new ArrayList<>();
        return getFileList(localFile, listFile);
    }

    private List<File> getFileList(File localFile, List<File> list) {

        for (final File fileEntry : localFile.listFiles()) {
            if (fileEntry.isDirectory()) {
                list = getFileList(fileEntry, list);
            } else {
                list.add(fileEntry);
            }
        }
        return list;
    }

    public void createBucket() throws SdkClientException, AmazonServiceException {
        if (!getOsClient().doesBucketExistV2(bucketName))
            getOsClient().createBucket(bucketName);
    }

    public String generatePreSignedUrl(String awsFilePath, Date expireDate) {
        return getOsClient().generatePresignedUrl(bucketName, awsFilePath, expireDate).toString();
    }

    public List<String> getDirectoriesList(String awsFolderPath) {
        String delimiter = "/";
        String prefix = awsFolderPath;
        if (!prefix.endsWith(delimiter)) {
            prefix += delimiter;
        }

        ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
                .withBucketName(bucketName).withPrefix(prefix)
                .withDelimiter(delimiter);

        ListObjectsV2Result listObjectsV2Result = getOsClient().listObjectsV2(listObjectsRequest);
        List<String> objectListing = listObjectsV2Result.getCommonPrefixes();

        String finalPrefix = prefix;
        objectListing = objectListing.stream().map(x -> x.substring(finalPrefix.length(), x.length() - 1)).collect(Collectors.toList());
        Collections.reverse(objectListing);
        return objectListing;
    }

    public void deleteFolder(String folder, IDeleteProgress deleteProgress) {
        ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
                .withBucketName(bucketName).withPrefix(folder);

        ListObjectsV2Result listObjectsV2Result = getOsClient().listObjectsV2(listObjectsRequest);

        List<S3ObjectSummary> s3ObjectSummary = listObjectsV2Result.getObjectSummaries();

        int deletedCount = 0;
        int totalCount = s3ObjectSummary.size();
        for (S3ObjectSummary summary : s3ObjectSummary) {
            deletedCount++;
            String key = summary.getKey();
            if (deleteProgress.beforeDelete(key)) {
                getOsClient().deleteObject(bucketName, key);
                deleteProgress.deleted(key, deletedCount, totalCount);
            }
        }
    }

    public String getBucketName() {
        return bucketName;
    }
}