package com.canovate.uploader.config;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.canovate.uploader.model.AutoUploadDeployModel;
import com.canovate.uploader.service.impl.ObjectStorageProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Configuration
public class ApplicationConfig {

    private HashMap<String, String> applicationJarName = new HashMap<>();

    private List<AutoUploadDeployModel> autoUploadDeployConfig = new ArrayList<>();

    @Value("${object-storage.aws.access}")
    private String AWS_ACCESS;

    @Value("${object-storage.aws.secret}")
    private String AWS_SECRET;

    @Value("${object-storage.aws.region:#{null}}")
    private String AWS_REGION;

    @Value("${object-storage.update.aws.bucket}")
    private String AWS_BUCKET;

    @Value("${object-storage.update.aws.bucket-folder}")
    private String AWS_FOLDER;

    @Value("${application-config.auto-deploy-enable:false}")
    private boolean AUTO_DEPLOY_ENABLE;

    @Value("${device-api.url}")
    private String DEVICE_API_URL;

    @Value("${auto-upload.group-name}")
    private String DEVICE_API_UPDATE_GROUP_NAME;

    @Value("${auto-upload.hcs-group-name}")
    private String DEVICE_API_UPDATE_HCS_GROUP_NAME;

    @Value("${application-config.deploy-path-api}")
    private String DEPLOY_PATH_API;
    @Value("${application-config.app-name-api}")
    private String APP_NAME_API = "api";

    @Value("${application-config.deploy-path-hcs}")
    private String DEPLOY_PATH_HCS;
    @Value("${application-config.app-name-hcs}")
    private String APP_NAME_HCS = "hcs";

    @Value("${application-config.deploy-path-sync}")
    private String DEPLOY_PATH_SYNC;
    @Value("${application-config.app-name-sync}")
    private String APP_NAME_SYNC = "sync";

    @Value("${application-config.deploy-path-updater}")
    private String DEPLOY_PATH_UPDATER;
    @Value("${application-config.app-name-updater}")
    private String APP_NAME_UPDATER = "updater";

    @Value("${application-config.app-name-html}")
    private String APP_NAME_HTML = "html";
    @Value("${application-config.deploy-path-html}")
    private String DEPLOY_PATH_HTML;

    @PostConstruct
    private void init() {
        initApplicationName();
        if (AUTO_DEPLOY_ENABLE)
            initAutoDeployConfigs();
    }

    private void initApplicationName() {
        applicationJarName.put(APP_NAME_API, APP_NAME_API + ".jar");
        applicationJarName.put(APP_NAME_HCS, APP_NAME_HCS + ".jar");
        applicationJarName.put(APP_NAME_SYNC, APP_NAME_SYNC + ".jar");
        applicationJarName.put(APP_NAME_UPDATER, APP_NAME_UPDATER + ".jar");
    }

    private void initAutoDeployConfigs() {
        Region awsRegion = Region.getRegion(Regions.fromName(AWS_REGION));
        ObjectStorageProcessor objectStorageProcessor = new ObjectStorageProcessor(AWS_ACCESS, AWS_SECRET, awsRegion, AWS_BUCKET);

        AutoUploadDeployModel apiAutoDeployConfig = new AutoUploadDeployModel(objectStorageProcessor, APP_NAME_API, AWS_FOLDER + "/" + APP_NAME_API, DEPLOY_PATH_API, applicationJarName.get(APP_NAME_API));
        apiAutoDeployConfig.setAutoUpdateCreate(DEVICE_API_URL, DEVICE_API_UPDATE_GROUP_NAME);
        autoUploadDeployConfig.add(apiAutoDeployConfig);
        log.info("initAutoDeployConfigs::info [{}] {} --> {}  CreateUpdateInfoEnable: {}",apiAutoDeployConfig.getAppName(), apiAutoDeployConfig.getLocalFilePath(), apiAutoDeployConfig.getOsFolder(), apiAutoDeployConfig.createUpdateInfoOnDeviceEnable());

        AutoUploadDeployModel syncAutoDeployConfig = new AutoUploadDeployModel(objectStorageProcessor, APP_NAME_SYNC, AWS_FOLDER + "/" + APP_NAME_SYNC, DEPLOY_PATH_SYNC, applicationJarName.get(APP_NAME_SYNC));
        syncAutoDeployConfig.setAutoUpdateCreate(DEVICE_API_URL, DEVICE_API_UPDATE_GROUP_NAME);
        autoUploadDeployConfig.add(syncAutoDeployConfig);
        log.info("initAutoDeployConfigs::info [{}] {} --> {}  CreateUpdateInfoEnable: {}",syncAutoDeployConfig.getAppName(), syncAutoDeployConfig.getLocalFilePath(), syncAutoDeployConfig.getOsFolder(), syncAutoDeployConfig.createUpdateInfoOnDeviceEnable());

        AutoUploadDeployModel updaterAutoDeployConfig = new AutoUploadDeployModel(objectStorageProcessor, APP_NAME_UPDATER, AWS_FOLDER + "/" + APP_NAME_UPDATER, DEPLOY_PATH_UPDATER, applicationJarName.get(APP_NAME_UPDATER));
        updaterAutoDeployConfig.setAutoUpdateCreate(DEVICE_API_URL, DEVICE_API_UPDATE_GROUP_NAME);
        autoUploadDeployConfig.add(updaterAutoDeployConfig);
        log.info("initAutoDeployConfigs::info [{}] {} --> {}  CreateUpdateInfoEnable: {}",updaterAutoDeployConfig.getAppName(), updaterAutoDeployConfig.getLocalFilePath(), updaterAutoDeployConfig.getOsFolder(), updaterAutoDeployConfig.createUpdateInfoOnDeviceEnable());

        AutoUploadDeployModel htmlAutoDeployConfig = new AutoUploadDeployModel(objectStorageProcessor, APP_NAME_HTML, AWS_FOLDER + "/" + APP_NAME_UPDATER, DEPLOY_PATH_HTML);
        htmlAutoDeployConfig.setAutoUpdateCreate(DEVICE_API_URL, DEVICE_API_UPDATE_GROUP_NAME);
        autoUploadDeployConfig.add(htmlAutoDeployConfig);
        log.info("initAutoDeployConfigs::info [{}] {} --> {}  CreateUpdateInfoEnable: {}",htmlAutoDeployConfig.getAppName(), htmlAutoDeployConfig.getLocalFilePath(), htmlAutoDeployConfig.getOsFolder(), htmlAutoDeployConfig.createUpdateInfoOnDeviceEnable());

        AutoUploadDeployModel hcsAutoDeployConfig = new AutoUploadDeployModel(objectStorageProcessor, APP_NAME_HCS, AWS_FOLDER + "/" + APP_NAME_HCS, DEPLOY_PATH_HCS, applicationJarName.get(APP_NAME_HCS));
        hcsAutoDeployConfig.setAutoUpdateCreate(DEVICE_API_URL, DEVICE_API_UPDATE_HCS_GROUP_NAME);
        autoUploadDeployConfig.add(hcsAutoDeployConfig);
    }

    public String getApplicationJarName(String app) {
        return applicationJarName.get(app);
    }

    public List<AutoUploadDeployModel> getAutoDeployConfig() {
        return autoUploadDeployConfig;
    }
}
