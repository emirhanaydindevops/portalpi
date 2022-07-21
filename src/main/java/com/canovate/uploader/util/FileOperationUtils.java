package com.canovate.uploader.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FileOperationUtils {

    public static boolean exist(String folderPath) {
        return new File(folderPath).exists();
    }

    public static void deleteFolder(String pathName) throws IOException {
        File folder = new File(pathName);
        if (!folder.exists() || folder.isFile())
            return;
        FileUtils.deleteDirectory(folder);
    }

    public static List<String> getFolders(String folderPath) {
        File file = new File(folderPath);
        List<String> names = new ArrayList<>();
        file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                if (!new File(current.getPath() + "\\" + name).isFile())
                    names.add(name);
                return false;
            }
        });
        return names;
    }

    public static List<String> getFiles(String folderPath) {
        File file = new File(folderPath);
        List<String> names = new ArrayList<>();
        file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                if (new File(current.getPath() + "\\" + name).isFile())
                    names.add(name);
                return false;
            }
        });
        return names;
    }

    public static List<String> getFilesWithExtension(String folderPath, String extension) {
        File file = new File(folderPath);
        List<String> names = new ArrayList<>();
        file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                if (name.endsWith(extension) && new File(current.getPath() + "\\" + name).isFile())
                    names.add(name);
                return false;
            }
        });
        return names;
    }

    public static List<File> getFileListRecursive(File folderName) {
        List<File> files = new ArrayList<>();
        return getFileListRecursive(folderName, files);
    }

    private static List<File> getFileListRecursive(File localFile, List<File> list) {
        for (final File fileEntry : localFile.listFiles()) {
            if (fileEntry.isDirectory()) {
                list = getFileListRecursive(fileEntry, list);
            } else {
                list.add(fileEntry);
            }
        }
        return list;
    }

    public static boolean transferDirectory(String from, String to) throws IOException {
        File fromFile = new File(from);
        File toFile = new File(to);

        if (fromFile.isFile() || toFile.isFile())
            return false;

        if (!fromFile.exists() || !(toFile.exists() || toFile.mkdirs()) || fromFile.list().length <= 0)
            return false;

        FileUtils.copyDirectory(fromFile, toFile);
        return true;
    }

    public static boolean transferDirectoryWithBackup(String from, String to, String backupName) throws IOException {
        File fromFile = new File(from);
        File toFile = new File(to);

        if (fromFile.isFile() || toFile.isFile())
            return false;

        if (!fromFile.exists() || !(toFile.exists() || toFile.mkdirs()) || fromFile.list().length <= 0)
            return false;

        List<File> fileList = getFileList(to);
        for (File file : fileList) {
            if (!file.exists())
                continue;
            Path filePath = file.toPath();
            Path toPath = filePath.resolveSibling(file.getName() + backupName);

            if (Files.exists(toPath))
                Files.delete(toPath);

            Files.move(filePath, filePath.resolveSibling(file.getName() + backupName));
        }

        FileUtils.copyDirectory(fromFile, toFile);
        return true;
    }

    public static boolean clearDirectory(String path) throws IOException {
        File file = new File(path);
        if (!file.exists() || !file.isDirectory())
            return false;

        FileUtils.cleanDirectory(file);
        return true;
    }

    public static boolean moveDirectory(String from, String to) throws IOException {
        File fromFile = new File(from);
        File toFile = new File(to);

        if (!fromFile.exists())
            return false;

        FileUtils.moveDirectory(fromFile, toFile);
        return true;
    }

    public static boolean moveFile(String from, String to) throws IOException {
        File fromFile = new File(from);
        File toFile = new File(to);

        if (!fromFile.exists())
            return false;

        FileUtils.copyFileToDirectory(fromFile, toFile.getParentFile(), true);
        fromFile.delete();
        return true;
    }

    public static boolean rename(String filePath, String fileNewPath) throws IOException {
        File file = new File(filePath);

        if (!file.exists())
            return false;

        File toFile = new File(fileNewPath);

        FileUtils.moveFile(file, toFile);
        return true;
    }


    public static List<File> getFileList(String folderPath) {
        List<File> listFile = new ArrayList<>();
        File folder = new File(folderPath);

        if (!folder.exists())
            return listFile;

        return getFileList(folder, listFile);
    }

    private static List<File> getFileList(File localFile, List<File> list) {

        for (final File fileEntry : localFile.listFiles()) {
            if (fileEntry.isDirectory()) {
                list = getFileList(fileEntry, list);
            } else {
                list.add(fileEntry);
            }
        }
        return list;
    }

    public static boolean checkFolderIsEmpty(String filePath) {
        File file = new File(filePath);
        if (!file.exists())
            return true;
        return file.list().length == 0;
    }
}
