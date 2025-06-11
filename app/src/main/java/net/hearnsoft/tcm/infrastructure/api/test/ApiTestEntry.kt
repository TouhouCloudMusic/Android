package net.hearnsoft.tcm.beans;

import java.util.List;

import lombok.Getter;

@Getter
public class ApiTestEntry {
    private List<ApiTestConfig> apiTestConfigs;

    public ApiTestEntry(List<ApiTestConfig> apiTestConfigs) {
        this.apiTestConfigs = apiTestConfigs;
    }
}
