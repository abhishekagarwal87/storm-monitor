package com.inmobi.storm.monitor;

import com.google.common.collect.Sets;

import com.inmobi.storm.monitor.storm.StormClusterState;

import java.util.List;
import java.util.Map;
import java.util.Set;

import backtype.storm.generated.ErrorInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class AlertMessageBuilder {

    private StormMonitorAppConfiguration.ClusterConfig clusterConfig;

    public AlertMessageBuilder(StormMonitorAppConfiguration.ClusterConfig clusterConfig) {
        this.clusterConfig = clusterConfig;
    }

    String buildMessage(StormClusterState previousState, StormClusterState currentState) {
        if (null == previousState) {
            log.info("First run of the service, no alert to be sent");
            return null;
        }
        boolean isCritical = false;
        StringBuilder clusterAlertMessage = new StringBuilder().append("\n");
        for (Map.Entry<String, StormClusterState.TopologyState> topologyStateEntry : currentState.getTopologyStateMap().entrySet()) {
            StringBuilder topologyAlertMessage = new StringBuilder();
            StormClusterState.TopologyState currentTopologyState = topologyStateEntry.getValue();
            StormClusterState.TopologyState previousTopologyState = null;
            String topologyName = topologyStateEntry.getKey();

            previousTopologyState = previousState.getTopologyStateMap().get(topologyName);
            if (currentTopologyState.getTopologyStatus() == StormClusterState.TopologyState.Status.KILLED
                || currentTopologyState.getTopologyStatus() == StormClusterState.TopologyState.Status.INACTIVE) {
                topologyAlertMessage
                    .append("<h4>")
                    .append(String.format("%s topology is in %s state", topologyName, currentTopologyState.getTopologyStatus()))
                    .append("</h4>");
                isCritical = true;
            } else if (null != currentTopologyState.getTopologyErrors() && !currentTopologyState.getTopologyErrors().isEmpty()) {
                for (Map.Entry<String, List<ErrorInfo>> entry : currentTopologyState.getTopologyErrors().entrySet()) {
                    Set<ErrorInfo> distinctErrors = Sets.newHashSet(entry.getValue());
                    String component = entry.getKey();
                    for (ErrorInfo errorInfo : distinctErrors) {
                        if (null != previousTopologyState && previousTopologyState.getTopologyErrors().containsKey(component)
                            && previousTopologyState.getTopologyErrors().get(component).contains(errorInfo)) {
                            // Error is already reported
                            continue;
                        }
                        isCritical = true;
                        topologyAlertMessage.append("<h4>")
                            .append(
                                String
                                    .format("Machine - %s:%d - component: %s", errorInfo.get_host(), errorInfo.get_port(), entry.getKey()))
                            .append("</h4>");
                        topologyAlertMessage.append("<pre>").append(errorInfo.get_error()).append("</pre>");
                    }
                }
            } else if (null != previousTopologyState && !previousTopologyState.getWorkerSet().equals(currentTopologyState.getWorkerSet())) {
                topologyAlertMessage.append("Worker state was changed for topology ").append(topologyName).append("\n");
                topologyAlertMessage.append("Killed workers: ")
                    .append(Sets.difference(previousTopologyState.getWorkerSet(), currentTopologyState.getWorkerSet())).append("\n");
                topologyAlertMessage.append("New workers: ")
                    .append(Sets.difference(currentTopologyState.getWorkerSet(), previousTopologyState.getWorkerSet())).append("\n");
                topologyAlertMessage.append("\n");
                isCritical = true;
            }

            if (topologyAlertMessage.length() != 0) {
                clusterAlertMessage.append("<h3>").append(String.format("Topology %s is in critical state", topologyName)).append("</h3>");
                clusterAlertMessage.append(topologyAlertMessage.toString());
            }

            //TODO: check up time of worker as well
        }

        log.info(clusterAlertMessage.toString());
        if (isCritical) {
            return clusterAlertMessage.toString();
        } else {
            return null;
        }
    }
}
