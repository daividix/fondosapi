package com.btgpactual.fondosapi.dto;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public ApiResponse() {}
    public ApiResponse(boolean success, String message, T data) {
        this.success = success; this.message = message; this.data = data;
    }
    // getters/setters

    public boolean isSuccess() {
        return this.success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMesssage() {
        return this.message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return this.data;
    }
    public void setDate(T data) {
        this.data = data;
    }
}
