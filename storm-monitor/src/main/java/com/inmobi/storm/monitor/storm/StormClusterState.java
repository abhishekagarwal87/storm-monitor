package com.inmobi.storm.monitor.storm;

import java.util.*;

import backtype.storm.generated.ErrorInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class StormClusterState {
    private Map<String, TopologyState> topologyStateMap;

    public StormClusterState() {
        topologyStateMap = new HashMap<>();
    }

    @Data
    public static class TopologyState {
        private Set<Worker> workerSet;
        private Map<String, List<ErrorInfo>> topologyErrors;
        private Status TopologyStatus;

        public TopologyState() {
            topologyErrors = new HashMap<>();
            workerSet = new HashSet<>();
        }

        public static enum Status {
            ACTIVE ,
            KILLED,
            INACTIVE;
        }
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(exclude = {"upTimeSeconds"})
    public static class Worker {
        private String host;
        private Integer port;
        private Integer upTimeSeconds;

        @Override
        public String toString() {
            return String.format("%s:%d", host, port);
        }
    }
}
