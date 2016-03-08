package com.inmobi.storm.monitor;

import com.google.common.base.Preconditions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class ConfigurationStore {

    public static StormMonitorAppConfiguration loadConfiguration(String configPath) {
        File file = new File(configPath);
        Preconditions.checkArgument(file.exists(), "%s path doesn't exist", configPath);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(file, StormMonitorAppConfiguration.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
