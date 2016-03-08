package com.inmobi.storm.monitor;

import com.google.common.base.Preconditions;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MonitorApp {
    public static void main(String[] args) {
        log.info("Starting the MonitorApp");
        Preconditions.checkArgument(args.length > 0, "Config file path is not specified");
        StormMonitorAppConfiguration configuration = ConfigurationStore.loadConfiguration(args[0]);
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(configuration.getClusterConfigs().size());

        for (StormMonitorAppConfiguration.ClusterConfig clusterConfig : configuration.getClusterConfigs()) {
            final ClusterStateChecker clusterStateChecker = new ClusterStateChecker(clusterConfig, configuration.getEmailConfig());
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    clusterStateChecker.execute();
                }
            }, 0, configuration.getStateUpdateIntervalInSecs(), TimeUnit.SECONDS);
        }
    }

}
