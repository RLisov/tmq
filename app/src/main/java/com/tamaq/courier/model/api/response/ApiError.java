package com.tamaq.courier.model.api.response;


import android.support.annotation.NonNull;

import com.fasterxml.jackson.databind.JsonNode;

public class ApiError {

    private String cause;
    private JsonNode jsonNode;
    private int httpCode;
    private String explanation;

    public ApiError() {
    }

    public ApiError(String cause, JsonNode jsonNode) {
        this.cause = cause;
        this.jsonNode = jsonNode;
    }

    public ApiError(String cause, JsonNode jsonNode, int httpCode) {
        this.cause = cause;
        this.jsonNode = jsonNode;
        this.httpCode = httpCode;
    }

    public String getErrorMessage() {
        if (jsonNode != null) {
            if (jsonNode.isArray() && jsonNode.size() > 0) {
                return jsonNode.get(0).textValue();
            } else {
                return jsonNode.toString();
            }
        }
        return explanation;
    }

    @NonNull
    public String getCauseSafe() {
        if (getCause() == null)
            return "";
        return getCause();
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public JsonNode getJsonNode() {
        return jsonNode;
    }

    public void setJsonNode(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
