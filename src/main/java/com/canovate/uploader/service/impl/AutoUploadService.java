package com.canovate.uploader.service.impl;

import com.canovate.uploader.config.ApplicationConfig;
import com.canovate.uploader.core.UpdateStatusCode;
import com.canovate.uploader.model.AutoUploadDeployModel;
import com.canovate.uploader.model.CargomaticUpdateInfoAutoCreateModel;
import com.canovate.uploader.model.ResponseModel;
import com.canovate.uploader.security.ITokenManager;
import com.canovate.uploader.service.inter.DeviceApi;
import com.canovate.uploader.service.inter.IDeleteProgress;
import com.canovate.uploader.service.inter.IUploadProgress;
import com.canovate.uploader.util.CommonUtils;
import com.canovate.uploader.util.FileOperationUtils;
import feign.Feign;
import feign.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class AutoUploadService {

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private ITokenManager tokenManager;

    @Value("${auto-upload.start-delay-second:30}")
    private int START_DELAY;

    private boolean autoUploadInProgress;

    @Autowired
    Feign.Builder feignBuilder;

    private DeviceApi createDeviceClient(String deviceApiUrl) {
        return feignBuilder
                .logLevel(Logger.Level.FULL)
                .target(DeviceApi.class, deviceApiUrl);
    }

    private void createUpdateOnDeviceApi(AutoUploadDeployModel autoUploadDeployModel, String version) {
        if (!autoUploadDeployModel.createUpdateInfoOnDeviceEnable())
            return;

        try {

            tokenManager.checkToken();
            String bearerToken = tokenManager.getBearerToken();
            DeviceApi deviceApi = createDeviceClient(autoUploadDeployModel.getDeviceServiceAddress());

            CargomaticUpdateInfoAutoCreateModel cargomaticUpdateInfoAutoCreateModel = CargomaticUpdateInfoAutoCreateModel.builder()
                    .applicationName(autoUploadDeployModel.getAppName())
                    .groupName(autoUploadDeployModel.getDeviceGroupName())
                    .version(version)
                    .build();

            String createUpdateModel = deviceApi.createUpdate(bearerToken, cargomaticUpdateInfoAutoCreateModel, true);
            log.info("createUpdateOnDeviceApi::info createUpdateInfo on deviceApi request: {}, response: {}", CommonUtils.writeValueAsStringDateFormattedWithoutException(cargomaticUpdateInfoAutoCreateModel), createUpdateModel);
        }catch (Exception e){
            log.error("createUpdateOnDeviceApi::error createUpdateInfo on deviceApi autoDeployModel: {}", CommonUtils.writeValueAsStringDateFormattedWithoutException(autoUploadDeployModel), e);
        }
    }

    @Scheduled(cron = "0 0/5 * * * *")
    public void uploadSchedule() {
        if (applicationConfig.getAutoDeployConfig().isEmpty() || autoUploadInProgress)
            return;
        try {
            autoUploadInProgress = true;

            List<AutoUploadDeployModel> autoUploadDeployModelList = applicationConfig.getAutoDeployConfig();
            for (AutoUploadDeployModel deployModel : autoUploadDeployModelList) {
                try {
                    if (FileOperationUtils.checkFolderIsEmpty(deployModel.getLocalFilePath()))
                        continue;

                    try {
                        Thread.sleep(START_DELAY);
                    } catch (Exception e) {
                    }

                    String version = "0";
                    if (Objects.nonNull(deployModel.getJarName())) {
                        List<String> folderJarFiles = FileOperationUtils.getFilesWithExtension(deployModel.getLocalFilePath(), ".jar");
                        if (folderJarFiles.size() == 0) {
                            log.error("uploadSchedule::error [{}] Deploy file has no jar file. Clearing {} folder...", deployModel.getAppName(), deployModel.getLocalFilePath());
                            continue;
                        } else if (folderJarFiles.size() > 1) {
                            log.error("uploadSchedule::error [{}] Deploy file has more than one jar file. Clearing {} folder...", deployModel.getAppName(), deployModel.getLocalFilePath());
                            continue;
                        }

                        try {
                            String jarName = folderJarFiles.get(0);
                            String[] rawVersion = jarName.split("-")[1].split(".jar")[0].split("\\.");
                            version = rawVersion[0] + rawVersion[1] + rawVersion[2];
                            FileOperationUtils.rename(deployModel.getLocalFilePath()+"\\"+jarName, deployModel.getLocalFilePath()+"\\"+deployModel.getJarName());
                        } catch (Exception e) {
                            log.error("uploadSchedule::error [{}] Can't get version from jar name error. jarName: {}, Clearing {} folder...", deployModel.getAppName(), folderJarFiles.get(0), deployModel.getLocalFilePath(), e);
                            continue;
                        }
                    }
                    for (ObjectStorageProcessor osProcessor : deployModel.getOsProcessorList()) {
                        try {
                            String osPath = deployModel.getOsFolder() + "\\" + version;
                            osProcessor.deleteFolder(osPath, new IDeleteProgress() {
                                @Override
                                public boolean beforeDelete(String osPath) {
                                    return true;
                                }

                                @Override
                                public void deleted(String osPath, int deletedCount, int totalCount) {
                                }
                            });
                            osProcessor.uploadFolderCheckForExists(osPath, deployModel.getLocalFilePath(), new IUploadProgress() {
                                @Override
                                public boolean beforeUpload(File file, int totalFileCount, int downloadedFileCount) {
                                    return true;
                                }

                                @Override
                                public void uploaded(File file) {
                                }
                            });
                            log.info("uploadSchedule::info [{}] Files Uploaded. bucketName: {}, Os Path: {}", deployModel.getAppName(), deployModel.getJarName(), osProcessor.getBucketName(), deployModel.getOsFolder());

                            createUpdateOnDeviceApi(deployModel, version);

                        } catch (Exception ex) {
                            log.error("uploadSchedule::error [{}] Os upload error. bucketName: {}, Os Path: {}", deployModel.getAppName(), deployModel.getJarName(), osProcessor.getBucketName(), deployModel.getOsFolder(), ex);
                        }
                    }
                } catch (Exception e) {
                    log.error("uploadSchedule::error [{}] Unknown error. jarName: {}, Os Path: {}", deployModel.getAppName(), deployModel.getJarName(), deployModel.getOsFolder(), e);
                    return;
                } finally {
                    try {
                        FileOperationUtils.clearDirectory(deployModel.getLocalFilePath());
                    } catch (Exception ee) {
                    }
                }
            }
        } finally {
            autoUploadInProgress = false;
        }
    }
}
