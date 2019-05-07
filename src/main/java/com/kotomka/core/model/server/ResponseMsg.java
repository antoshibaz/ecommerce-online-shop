package com.kotomka.core.model.server;

public class ResponseMsg {

    private String status;
    private String description;
    private Long returnedParam;

    public ResponseMsg(String status) {
        this.status = status;
    }

    public ResponseMsg(String status, Long returnedParam) {
        this.status = status;
        this.returnedParam = returnedParam;
    }

    public ResponseMsg(String status, String description) {
        this.status = status;
        this.description = description;
    }

    public ResponseMsg(String status, String description, Long returnedParam) {
        this.status = status;
        this.description = description;
        this.returnedParam = returnedParam;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getReturnedParam() {
        return returnedParam;
    }

    public void setReturnedParam(Long returnedParam) {
        this.returnedParam = returnedParam;
    }
}