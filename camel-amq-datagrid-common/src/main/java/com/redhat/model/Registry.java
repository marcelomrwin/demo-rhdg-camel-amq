package com.redhat.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Registry {
    private Integer id;
    private String roleCode;
    private List<Application> applications;
    private List<Herd> herds;

    public Registry(Integer id){
        this();
        this.id = id;
    }
}
