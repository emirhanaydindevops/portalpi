package com.canovate.uploader.service.inter;

import com.canovate.uploader.core.exception.ObjectStorageException;

import java.io.FileNotFoundException;
import java.util.List;

public interface IUploadService {
    String getUploadPath();

    String getUploadedPath();

    String getBucketName();

    String getBucketFolderName();

    void uploadUpdate(String osPath, String folderPath, IUploadProgress uploadProgress) throws ObjectStorageException, FileNotFoundException;

    List<String> getServiceListFromOS() throws ObjectStorageException;

    List<String> getVersionListFromOS(String serviceName) throws ObjectStorageException;

    void deleteVersion(String serviceName, String versionName, IDeleteProgress deleteProgress) throws ObjectStorageException;
}
