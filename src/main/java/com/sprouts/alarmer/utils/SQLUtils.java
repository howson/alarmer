package com.sprouts.alarmer.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.sprouts.spm_framework.sql.SQLService;

public class SQLUtils extends SQLService {

    /**
     * 
     * @param monitorType 0为web，1为app server
     * @param sql
     * @return
     */
    public String[] getMonitorInfo(int monitorType, String sql) {
        String[] info = new String[5];
        ResultSet set = attachList(sql);
        if (set != null) {
            try {
                while (set.next()) {
                    if (monitorType == 0) {
                        info[0] = set.getString("monitorname");
                        info[1] = set.getString("destination");
                    } else if (monitorType == 1) {
                        info[0] = set.getString("monitorname");
                        info[1] = set.getString("destination");
                        info[2] = String.valueOf(set.getInt("port"));
                    }
                }
            } catch (SQLException e) {
                logger.error("Get MonitorName Error:", e);
            }
        }
        return info;
    }

}
