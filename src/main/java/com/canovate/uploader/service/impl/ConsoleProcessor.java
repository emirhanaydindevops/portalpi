package com.canovate.uploader.service.impl;

import com.canovate.uploader.config.ApplicationConfig;
import com.canovate.uploader.core.exception.ObjectStorageException;
import com.canovate.uploader.service.inter.IDeleteProgress;
import com.canovate.uploader.service.inter.IUploadProgress;
import com.canovate.uploader.service.inter.IUploadService;
import com.canovate.uploader.util.CommonUtils;
import com.canovate.uploader.util.FileOperationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Slf4j
@Component
public class ConsoleProcessor implements CommandLineRunner {

    @Autowired
    IUploadService uploadService;

    @Autowired
    ApplicationConfig applicationConfig;

    @Value("${console-processor.enable}")
    private Boolean CONSOLE_ENABLE;

    BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in));


    private String readLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            return "";
        }
    }

    private void print(String txt) {
        System.out.print(txt);
    }

    private void println(String txt) {
        print(txt + "\n");
    }

    private void printFileNames(List<String> files) {
        for (String name : files)
            println("-- " + name);
    }

    private void main() {
        while (CONSOLE_ENABLE) {
            clearConsole();
            println("Bucket-Name: " + uploadService.getBucketName());
            println("");
            println("1: Upload");
//            println("2: Generate Url");
            println("2: Delete Version");
            println("0: Exit");
            print("\n# ");
            String select = readLine();

            int exitCode = -1;

            switch (select) {
                case "1":
                    uploadUpdate();
                    break;
//                case "2":
//                    print("Os Path: ");
//                    String osPath = readLine();
//                    try {
//                        println(uploadService.getTestUrl(osPath));
//                    } catch (ObjectStorageException e) {
//                        e.printStackTrace();
//                    }
//                    break;
                case "2":
                    exitCode = updateDelete();
                    break;
                case "0":
                    System.exit(0);
                    break;
                default:
                    println("Wrong input.");
                    break;
            }

            if (exitCode != 0) {
                println("Press enter to open menu...");
                readLine();
            }
        }
    }

    private void uploadUpdate() {
        String folderPath = uploadService.getUploadPath();
        String uploadedPath = uploadService.getUploadedPath();
        println("Upload-Folder: " + folderPath);

        List<String> folders = FileOperationUtils.getFolders(folderPath);
        if (folders.isEmpty()) {
            println("Folder empty");
            return;
        }
        printFileNames(folders);

        print("Application Name: ");
        String app = readLine();
        String osPath = app;

        folderPath += "\\" + app;

        if (Strings.isEmpty(app))
            return;
        if (!FileOperationUtils.exist(folderPath)) {
            println("Application Folder Not Exist");
            return;
        }

        folders = FileOperationUtils.getFolders(folderPath);
        if (folders.isEmpty()) {
            println("Folder empty");
            return;
        }
        printFileNames(folders);

        print("Version: ");
        String response = readLine();
        osPath += "\\" + response;

        folderPath += "\\" + response;

        if (Strings.isEmpty(response) || !FileOperationUtils.exist(folderPath)) {
            println("Version Folder Not Exist");
            return;
        }


        String jarName = applicationConfig.getApplicationJarName(app);
        CommonUtils.checkAndChangeJarName(jarName, folderPath);

        try {
            uploadService.uploadUpdate(osPath, folderPath, new IUploadProgress() {
                @Override
                public boolean beforeUpload(File file, int totalFileCount, int downloadedFileCount) {
                    print(file.getName() + " (" + downloadedFileCount + "\\" + totalFileCount + ") ...");
                    return true;
                }

                @Override
                public void uploaded(File file) {
                    println("\b\b\b");
                    try {
                        String to = file.getPath().substring(uploadedPath.length() - 1);
                        FileOperationUtils.moveFile(file.getPath(), uploadedPath + "\\" + to);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            println("\nFiles Uploaded. Os Path: " + uploadService.getBucketFolderName() + "\\" + osPath);
        } catch (Exception e) {
            log.error("uploadUpdate::error", e);
            return;
        }
        try {
            FileOperationUtils.deleteFolder(folderPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        println("Upload Done");
    }

    private int updateDelete() {
        try {
            while (true) {
                clearConsole();
                println("------ Delete Version ------");
                List<String> serviceList = uploadService.getServiceListFromOS();
                printList(serviceList);
                println("\n0: Back");
                print("# ");
                String serviceName = readLine();

                if (serviceName.equals("0"))
                    return 0;
                if (!serviceList.stream().anyMatch(x -> x.equals(serviceName))) {
                    println("Service not exist.");
                    continue;
                }

                while (true) {
                    clearConsole();
                    println("ServicName: " + serviceName);
                    List<String> versionList = uploadService.getVersionListFromOS(serviceName);
                    println("");
                    printList(versionList);
                    println("\n0: Back");
                    print("# ");
                    String versionName = readLine();
                    if (versionName.equals("0"))
                        break;

                    if (!versionList.stream().anyMatch(x -> x.equals(versionName))) {
                        println("Version not exist.");
                        continue;
                    }

                    uploadService.deleteVersion(serviceName, versionName, new IDeleteProgress() {
                        @Override
                        public boolean beforeDelete(String osPath) {
                            return true;
                        }

                        @Override
                        public void deleted(String osPath, int deletedCount, int totalCount) {
                            println(osPath + " (" + deletedCount + "\\" + totalCount + ") deleted");
                        }
                    });
                    println(uploadService.getBucketFolderName() + "\\" + serviceName + "\\" + versionName + "  is deleted.");

                    println("Press enter to continue...");
                    readLine();
                }

            }
        } catch (ObjectStorageException e) {
            log.error("updateDelete::error Error on object storage.");
        }
        return -1;
    }

    private void printList(List<String> list) {
        for (String serviceName : list) {
            println(serviceName);
        }
    }

    public static void clearConsole() {
        try {
            String operatingSystem = System.getProperty("os.name");
            if (operatingSystem.contains("Windows")) {
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "cls");
                Process startProcess = pb.inheritIO().start();
                startProcess.waitFor();
            } else {
                ProcessBuilder pb = new ProcessBuilder("clear");
                Process startProcess = pb.inheritIO().start();

                startProcess.waitFor();
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void run(String... args) throws Exception {
        new Thread(() -> main()).start();
    }
}