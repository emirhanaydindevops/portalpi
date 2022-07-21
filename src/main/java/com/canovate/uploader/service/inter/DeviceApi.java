package com.canovate.uploader.service.inter;

import com.canovate.uploader.model.CargomaticUpdateInfoAutoCreateModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "deviceApi", url = "${device-api.url}")
@RequestMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
public interface DeviceApi {
    @RequestMapping(method = RequestMethod.POST, value = "/api/v1/cargomatic-update/create-auto-update")
    String createUpdate(@RequestHeader(value = "Authorization", required = true) String bearerToken, @RequestBody CargomaticUpdateInfoAutoCreateModel createModel, @RequestParam(name = "force", defaultValue = "false") boolean force);
}
