package com.canovate.uploader.model;

import com.canovate.uploader.service.impl.ObjectStorageProcessor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AutoUploadDeployModel {
    private List<ObjectStorageProcessor> osProcessorList;
    private String appName;
    private String deviceServiceAddress;
    private String deviceGroupName;
    private String osFolder;
    private String localFilePath;
    private String jarName;

    public AutoUploadDeployModel(List<ObjectStorageProcessor> osProcessorList, String appName, String osFolder, String localFilePath, String jarName) {
        this.osProcessorList = osProcessorList;
        this.appName = appName;
        this.osFolder = osFolder;
        this.localFilePath = localFilePath;
        this.jarName = jarName;
    }

    public AutoUploadDeployModel(List<ObjectStorageProcessor> osProcessorList, String appName, String osFolder, String localFilePath) {
        new AutoUploadDeployModel(osProcessorList, appName, osFolder, localFilePath, null);
    }

    public AutoUploadDeployModel(ObjectStorageProcessor osProcessor, String appName, String osFolder, String localFilePath, String jarName) {
        this.osProcessorList = new ArrayList<>();
        this.appName = appName;
        this.osProcessorList.add(osProcessor);
        this.osFolder = osFolder;
        this.localFilePath = localFilePath;
        this.jarName = jarName;
    }

    public AutoUploadDeployModel(ObjectStorageProcessor osProcessor, String appName, String osFolder, String localFilePath) {
        new AutoUploadDeployModel(osProcessor, appName, osFolder, localFilePath, null);
    }

    public void setAutoUpdateCreate(String deviceUrl, String deviceGroupName) {
        this.deviceServiceAddress = deviceUrl;
        this.deviceGroupName = deviceGroupName;
    }

    public boolean createUpdateInfoOnDeviceEnable() {
        return !StringUtils.isBlank(this.deviceServiceAddress) && !StringUtils.isBlank(this.deviceGroupName);
    }
}
