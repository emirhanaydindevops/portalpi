package com.canovate.uploader.service.inter;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.canovate.uploader.core.exception.ObjectStorageException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface IObjectStorageProcessor {
    String getUrl(String bucketName, String fileName);

    void upload(String keyName, String filePath, String contentType) throws SdkClientException, AmazonServiceException, FileNotFoundException;

    void upload(String keyName, String filePath) throws SdkClientException, AmazonServiceException, FileNotFoundException;

    void uploadString(String keyName, String data, String contentType) throws SdkClientException, AmazonServiceException;

    void uploadString(String keyName, String data) throws SdkClientException, AmazonServiceException;

    void uploadFolderCheckForExists(String osPath, String folderPath, IUploadProgress uploadProgress) throws FileNotFoundException;

    byte[] downloadAsBytes(String awsFilePath) throws SdkClientException, AmazonServiceException, IOException, ObjectStorageException;

    void download(String awsFilePath, File localFilePath) throws ObjectStorageException, IOException;

    void downloadFolder(String awsFilePath, String localFolderPath) throws ObjectStorageException, InterruptedException;

    void deleteRemovedFiles(String awsFolderPath, String localFolderPath) throws ObjectStorageException;

    void createBucket() throws SdkClientException, AmazonServiceException;

    String generatePreSignedUrl(String awsFilePath, Date expireDate);

    List<String> getDirectoriesList(String awsFolderPath);

    void deleteFolder(String folder, IDeleteProgress deleteProgress);

    String getBucketName();
}
