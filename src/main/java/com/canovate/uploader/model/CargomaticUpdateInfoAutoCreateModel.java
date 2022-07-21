package com.canovate.uploader.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CargomaticUpdateInfoAutoCreateModel {
    private String applicationName;
    private String version;
    private String groupName;
}
