package com.canovate.uploader.security;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SecurityUtils {

    public static String getFileMD5(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        String strMd5;
        try {
            strMd5 = DigestUtils.md5Hex(inputStream);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }
        return strMd5;
    }
}
