package com.inmobi.storm.monitor;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class StormMonitorAppConfiguration {

    @NotEmpty
    @JsonProperty("port")
    private Integer port;

    @NotEmpty
    @JsonProperty("threads")
    private Integer threads;

    @NotEmpty
    @JsonProperty("clusters")
    private List<ClusterConfig> clusterConfigs;

    @JsonProperty("email")
    private EmailConfig emailConfig;

    @JsonProperty("state.update.interval.seconds")
    private Integer stateUpdateIntervalInSecs = 60;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ClusterConfig {

        @NotEmpty
        @JsonProperty("name")
        private String name;

        @NotEmpty
        @JsonProperty("nimbus.host")
        private String nimbusHost;

        @JsonProperty("topologies")
        private List<String> topologies;

        @JsonProperty("nimbus.thrift.port")
        private Integer nimbusThriftPort = 6627;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EmailConfig {

        @NotEmpty
        @JsonProperty("from")
        private String from;

        @NotEmpty
        @JsonProperty("to")
        private String to;

        @NotEmpty
        @JsonProperty("host.name")
        private String hostName;
    }
}
