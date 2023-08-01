package com.redhat.model;

import com.redhat.model.Registry;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse {

    private ApiResponseType type;
    private Registry registry;
    private String message;
    public enum ApiResponseType {
        DATA, LOAD
    }
}
