package com.canovate.uploader.model;


import lombok.Data;

@Data
public class ResponseModel<D, M> {
    public boolean success = false;
    public M status = null;
    public D data = null;

    public ResponseModel() {
    }

    public ResponseModel(M status) {
        this.status = status;
    }

    public ResponseModel(M status, boolean success) {
        this.status = status;
        this.success = success;
    }

    public ResponseModel(M status, boolean success, D data) {
        this.status = status;
        this.success = success;
        this.data = data;
    }
}
