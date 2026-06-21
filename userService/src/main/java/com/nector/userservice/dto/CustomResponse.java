package com.nector.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomResponse {

    private Integer status;
    private Object data;
    private String message;

    public static CustomResponse success(Object data, String message) {
        return new CustomResponse(200, data, message);
    }

    public static CustomResponse error(Integer status, String message) {
        return new CustomResponse(status, null, message);
    }
}
