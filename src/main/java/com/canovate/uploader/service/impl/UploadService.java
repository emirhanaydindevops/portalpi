package com.canovate.uploader.service.impl;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.canovate.uploader.config.ApplicationConfig;
import com.canovate.uploader.core.exception.ObjectStorageException;
import com.canovate.uploader.service.inter.IDeleteProgress;
import com.canovate.uploader.service.inter.IObjectStorageProcessor;
import com.canovate.uploader.service.inter.IUploadProgress;
import com.canovate.uploader.service.inter.IUploadService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class UploadService implements IUploadService {

    @Value("${object-storage.aws.access}")
    private String AWS_ACCESS;

    @Value("${object-storage.aws.secret}")
    private String AWS_SECRET;

    @Value("${object-storage.aws.region:#{null}}")
    private String AWS_REGION;

    @Value("${object-storage.aws.endpoint:#{null}}")
    private String AWS_ENDPOINT;

    @Value("${object-storage.update.aws.bucket}")
    private String AWS_BUCKET;

    @Value("${object-storage.update.aws.bucket-folder}")
    private String AWS_BUCKET_FOLDER;

    @Value("${upload-service.upload-path}")
    private String UPLOAD_PATH;

    @Value("${upload-service.uploaded-path}")
    private String UPLOADED_PATH;


    @Autowired
    private ApplicationConfig applicationConfig;

    private IObjectStorageProcessor objectStorageProcessor;

    private IObjectStorageProcessor getS3Connection() throws IllegalArgumentException, ObjectStorageException {
        if (Objects.nonNull(objectStorageProcessor))
            return objectStorageProcessor;

        if (!Strings.isEmpty(AWS_ENDPOINT)) {
            objectStorageProcessor = new ObjectStorageProcessor(AWS_ACCESS, AWS_SECRET, AWS_ENDPOINT, AWS_BUCKET);
        } else if (!Strings.isEmpty(AWS_REGION)) {
            Region awsRegion = Region.getRegion(Regions.fromName(AWS_REGION));

            objectStorageProcessor = new ObjectStorageProcessor(AWS_ACCESS, AWS_SECRET, awsRegion, AWS_BUCKET);
        } else {
            throw new ObjectStorageException("region or endpoint not found in properties file");
        }
        return objectStorageProcessor;
    }

    public String getUploadPath() {
        return UPLOAD_PATH;
    }

    public String getUploadedPath() {
        return UPLOADED_PATH;
    }

    public String getBucketName() {
        return AWS_BUCKET;
    }

    public String getBucketFolderName() {
        return AWS_BUCKET_FOLDER;
    }

    public String getTestUrl(String osPath) throws ObjectStorageException {
        Date expireDate = java.sql.Timestamp.valueOf(LocalDateTime.now().plusMinutes(300));
        return getS3Connection().generatePreSignedUrl(osPath, expireDate);
    }

    public void uploadUpdate(String osPath, String folderPath, IUploadProgress uploadProgress) throws ObjectStorageException, FileNotFoundException {
        osPath = osPath.replace("\\", "/");
        getS3Connection().uploadFolderCheckForExists(AWS_BUCKET_FOLDER + "/" + osPath, folderPath, uploadProgress);
    }

    public List<String> getServiceListFromOS() throws ObjectStorageException {
        return getS3Connection().getDirectoriesList(AWS_BUCKET_FOLDER);
    }

    public List<String> getVersionListFromOS(String serviceName) throws ObjectStorageException {
        return getS3Connection().getDirectoriesList(AWS_BUCKET_FOLDER + "/" + serviceName);
    }

    public void deleteVersion(String serviceName, String versionName, IDeleteProgress deleteProgress) throws ObjectStorageException {
        getS3Connection().deleteFolder(AWS_BUCKET_FOLDER + "/" + serviceName + "/" + versionName, deleteProgress);
    }

}