package com.canovate.uploader.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Slf4j
public class CommonUtils {

    private CommonUtils() {
    }

    public static final DateFormat yyyyMMddHHmmssSSSZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final DateTimeFormatter DAY_AND_MONTH_AND_YEAR_WITH_POINT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final ObjectMapper objectMapper;
    private static final ObjectMapper objectMapperDateFormatted;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(new JavaTimeModule());

        objectMapperDateFormatted = new ObjectMapper();
        objectMapperDateFormatted.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapperDateFormatted.registerModule(new JavaTimeModule());
        objectMapperDateFormatted.setDateFormat(yyyyMMddHHmmssSSSZ);
    }

    public static <T> T readValue(String value, Class<T> valueClass) throws IOException {
        try {
            return objectMapper.readValue(value, valueClass);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static String writeValueAsString(Object object) throws JsonProcessingException {
        if (Objects.isNull(object)) return null;
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static String writeValueAsStringWithoutException(Object object) {
        try {
            return writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("writeValueAsStringWithoutException:: error on parsing json object", e);
            return null;
        }
    }

    public static String writeValueAsStringDateFormattedWithoutException(Object object) {
        try {
            return writeValueAsStringDateFormatted(object);
        } catch (JsonProcessingException e) {
            log.error("writeValueAsStringDateFormattedWithoutException:: error on parsing json object", e);
            return null;
        }
    }

    public static String writeValueAsStringDateFormatted(Object object) throws JsonProcessingException {
        if (Objects.isNull(object)) return null;
        try {
            return objectMapperDateFormatted.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static String unixPath(String path) {
        return path.replace("\\", "/");
    }

    public static boolean checkAndChangeJarName(String jarName, String folderPath) {
        String jarPath = folderPath + "\\" + jarName;
        if (Objects.nonNull(jarName) && !FileOperationUtils.exist(jarPath)) {
            List<String> jarFiles = FileOperationUtils.getFilesWithExtension(folderPath, ".jar");
            if (jarFiles.isEmpty()) {
                return false;
            }
            if (jarFiles.size() != 1) {
                return false;
            } else {
                try {
                    FileOperationUtils.rename(folderPath + "\\" + jarFiles.get(0), jarPath);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }
}
