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
public class App1Model {
    private Integer id;
    private String attribute;
    private String appId;
    private List<Loan> loans;
    private List<Flock> flocks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Loan {
        private String status;
        private Double value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Flock {
        private String type;
        private String location;
        private Integer total;
    }
}