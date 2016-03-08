package com.inmobi.storm.monitor;

import com.inmobi.storm.monitor.storm.StormClusterState;

import org.apache.commons.mail.EmailException;
import org.apache.thrift7.transport.TTransportException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import backtype.storm.Config;
import backtype.storm.generated.ClusterSummary;
import backtype.storm.generated.ExecutorSummary;
import backtype.storm.generated.TopologyInfo;
import backtype.storm.generated.TopologySummary;
import backtype.storm.utils.NimbusClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClusterStateChecker {

    private StormMonitorAppConfiguration.ClusterConfig clusterConfig;
    private NimbusClient nimbusClient;
    private AlertMessageBuilder alertMessageBuilder;
    private Mailer mailer;

    private StormClusterState stormClusterState;

    public ClusterStateChecker(StormMonitorAppConfiguration.ClusterConfig clusterConfig,
                               StormMonitorAppConfiguration.EmailConfig emailConfig) {
        this.clusterConfig = clusterConfig;
        Map nimbusConf = new HashMap<>();
        nimbusConf.put(Config.STORM_THRIFT_TRANSPORT_PLUGIN, "backtype.storm.security.auth.SimpleTransportPlugin");
        try {
            this.nimbusClient = new NimbusClient(nimbusConf, clusterConfig.getNimbusHost(), clusterConfig.getNimbusThriftPort());
        } catch (TTransportException e) {
            throw new RuntimeException(String.format("Failed to initialize nimbus client for cluster: %s", clusterConfig.toString()), e);
        }
        alertMessageBuilder = new AlertMessageBuilder(clusterConfig);
        mailer = new Mailer(emailConfig);
    }

    public void execute() {
        try {
            StormClusterState currentStormClusterState = new StormClusterState();

            // Initially mark all the topologies killed
            for (String topology : clusterConfig.getTopologies()) {
                StormClusterState.TopologyState topologyState = new StormClusterState.TopologyState();
                topologyState.setTopologyStatus(StormClusterState.TopologyState.Status.KILLED);
                currentStormClusterState.getTopologyStateMap().put(topology, topologyState);
            }

            ClusterSummary clusterSummary = this.nimbusClient.getClient().getClusterInfo();
            for (TopologySummary topologySummary : clusterSummary.get_topologies()) {
                StormClusterState.TopologyState
                    topologyState =
                    currentStormClusterState.getTopologyStateMap().get(topologySummary.get_name());

                if (null == topologyState) {
                    log.info("{} is not being monitored in name {}", topologySummary.get_name(), clusterConfig.getName());
                    continue;
                }

                topologyState.setTopologyStatus(StormClusterState.TopologyState.Status.valueOf(topologySummary.get_status()));
                if (topologyState.getTopologyStatus() == StormClusterState.TopologyState.Status.ACTIVE) {
                    TopologyInfo topologyInfo = nimbusClient.getClient().getTopologyInfo(topologySummary.get_id());
                    topologyState.setWorkerSet(new HashSet<StormClusterState.Worker>());
                    for (ExecutorSummary executorSummary : topologyInfo.get_executors()) {
                        StormClusterState.Worker
                            worker =
                            new StormClusterState.Worker(executorSummary.get_host(), executorSummary.get_port(),
                                                         executorSummary.get_uptime_secs());
                        topologyState.getWorkerSet().add(worker);
                    }
                    //TODO: Clear errors in the second run
                    topologyState.setTopologyErrors(topologyInfo.get_errors());
                }
            }

            String alertMsg = alertMessageBuilder.buildMessage(stormClusterState, currentStormClusterState);
            String subject = "Photon cluster state alert " + clusterConfig.getName();
            if (null != alertMsg) {
                try {
                    mailer.sendMail(subject, alertMsg);
                } catch (EmailException ex) {
                    log.error("Error in sending the email", ex);
                }
            }

            stormClusterState = currentStormClusterState;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


}
