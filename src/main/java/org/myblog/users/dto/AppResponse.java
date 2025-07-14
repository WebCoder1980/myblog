package org.myblog.users.dto;

import lombok.Data;
import org.myblog.users.appenum.AppResponseStatusEnum;

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

    public AppResponse<T> addErrorFluent(String message) {
        return addErrorFluent("general", message);
    }

    public AppResponse<T> addErrorFluent(String field, String message) {
        if (errors == null) {
            errors = new TreeMap<>();
        }
        errors.computeIfAbsent(field, i -> new TreeSet<>());
        errors.get(field).add(message);

        status = AppResponseStatusEnum.ERROR;

        return this;
    }
}
