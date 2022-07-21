package com.canovate.uploader.service.inter;

import java.io.File;

public interface IUploadProgress {
    boolean beforeUpload(File file, int totalFileCount, int downloadedFileCount);

    void uploaded(File file);
}
