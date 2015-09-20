package com.sprouts.alarmer;

import com.sprouts.alarmer.service.TaskReceiver;
import com.sprouts.spm_framework.utils.ConfigUtils;

public class Main {

    public static void main(String[] args) {
        String configPath = "alarm-config.xml";
        ConfigUtils.initExternalConfig(configPath);
        TaskReceiver taskReceiver = new TaskReceiver();
        taskReceiver.initReceiver();
    }
}
