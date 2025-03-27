package net.hearnsoft.tcm.infrastructure.adapter.http;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultResponse {
    private String status;
    private String message;
    private int error_code;
}
