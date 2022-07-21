package com.canovate.uploader.service.inter;

import java.io.File;

public interface IDeleteProgress {
    boolean beforeDelete(String osPath);

    void deleted(String osPath, int deletedCount, int totalCount);
}
