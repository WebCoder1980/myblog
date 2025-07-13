package org.myblog.users.dto;

import lombok.Data;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@Data
public class AppResponse<T> {
    private AppResponseStatusEnum status;
    private T data;
    private Map<String, Set<String>> errors;

    public AppResponse() {

    }
    public AppResponse(T data) {
        this.status = AppResponseStatusEnum.OK;
        this.data = data;
    }
    public AppResponse(Map<String, Set<String>> errors) {
        this.status = AppResponseStatusEnum.ERROR;
        this.errors = errors;
    }

    public AppResponse<T> addErrorFluent(String error) {
        if (errors == null) {
            errors = new TreeMap<>();
        }
        errors.computeIfAbsent("general", i -> new TreeSet<>());
        errors.get("general").add(error);

        status = AppResponseStatusEnum.ERROR;

        return this;
    }
}
