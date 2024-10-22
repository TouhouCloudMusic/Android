package net.hearnsoft.tcm.api;

public enum ApiMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE");

    private final String method;

    ApiMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
