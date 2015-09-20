package com.sprouts.alarmer.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;

import com.sprouts.alarmer.utils.SQLUtils;
import com.sprouts.spm_framework.amq.message.AlarmTaskMessage;
import com.sprouts.spm_framework.utils.AsyncTaskUtils;
import com.sprouts.spm_framework.utils.Logger;

/**
 * 警报执行线程，定时检查该线程负责的监控数据，若最后n条数据异常则报警
 * 
 * @author howson
 * 
 */
public class AlarmerThread implements Runnable {

    private AlarmTaskMessage alarmMessage;
    public String threadName;
    private ArrayList<String> email_list;
    private SQLUtils sqlUtils = new SQLUtils();
    private Logger logger = new Logger();
    private ScheduledFuture mailInstance = null;
    private int mailThreadNum = 0;

    public AlarmerThread(AlarmTaskMessage alarmMessage, ArrayList<String> email_list) {
        this.alarmMessage = alarmMessage;
        this.email_list = email_list;
        threadName = alarmMessage.getUserId() + "-" + alarmMessage.getMonitorId();
    }

    @Override
    public void run() {
        watchAbnormal();
    }

    public void watchAbnormal() {
        String nameSql = "SELECT * FROM %s WHERE id=%d;";
        String querySql =
                "SELECT * FROM (SELECT * FROM %s s WHERE name='%s' ORDER BY id DESC LIMIT %d) AS a ORDER BY a.id;";
        String quertAppSql =
                "SELECT * FROM (SELECT * FROM %s s WHERE ip='%s' AND port=%d ORDER BY id DESC LIMIT %d) AS a ORDER BY a.id;";
        String content_base =
                "您好，紧急情况！<br>您的服务名为%s的监控出现了问题，请及时处理！<br>出错信息：%s <br>最后一次出错时间：%s <br><br>来自Sprout云客服";
        int count = 0;
        String[] monitorInfo = new String[10];
        String monitorName = null;
        ResultSet set = null;
        try {
            switch (alarmMessage.getType()) {
                case HTTP:
                    monitorInfo =
                            sqlUtils.getMonitorInfo(
                                    0,
                                    String.format(nameSql, "list_spm_web",
                                            alarmMessage.getMonitorId()));
                    monitorName = monitorInfo[0];
                    set =
                            sqlUtils.attachList(String.format(querySql, "collect_http",
                                    monitorName, alarmMessage.getRetry_time()));
                    while (set.next()) {
                        if (set.getInt("status") == 0 || set.getInt("status_code") != 200
                                || set.getFloat("average_res_time") > alarmMessage.getThreshold()) {
                            count++;
                            if (count == alarmMessage.getRetry_time()) {
                                String status_str =
                                        String.format("Warning:[URL=%s, 状态码 = %d, 平均响应时间 = %f]",
                                                set.getString("site"), set.getInt("status_code"),
                                                set.getFloat("average_res_time"));
                                String content =
                                        String.format(content_base, monitorName, status_str,
                                                set.getString("timestamp"));

                                if (mailThreadNum == 0) {
                                    AsyncTaskUtils asyncHandler = AsyncTaskUtils.getInstance();
                                    mailInstance =
                                            asyncHandler.dispatchScheduleTask(new MailThread(
                                                    email_list, content), 1, alarmMessage
                                                    .getFrequency());
                                    mailThreadNum++;
                                }

                            }
                        }
                    }
                    break;
                case PING:
                    monitorInfo =
                            sqlUtils.getMonitorInfo(
                                    0,
                                    String.format(nameSql, "list_spm_web",
                                            alarmMessage.getMonitorId()));
                    monitorName = monitorInfo[0];
                    set =
                            sqlUtils.attachList(String.format(querySql, "collect_ping",
                                    monitorName, alarmMessage.getRetry_time()));
                    while (set.next()) {
                        if (set.getInt("status") == 0
                                || set.getFloat("loss_rate") > alarmMessage.getThreshold()) {
                            count++;
                            if (count == alarmMessage.getRetry_time()) {
                                String status_str =
                                        String.format(
                                                "Warning:[URL=%s, 发送的包个数 = %d个, 成功接收个数 = %d个, 丢包率 = %f]",
                                                monitorInfo[1], set.getInt("pack_send"),
                                                set.getInt("pack_receive"),
                                                set.getFloat("loss_rate"));
                                String content =
                                        String.format(content_base, monitorName, status_str,
                                                set.getString("timestamp"));

                                if (mailThreadNum == 0) {
                                    AsyncTaskUtils asyncHandler = AsyncTaskUtils.getInstance();
                                    mailInstance =
                                            asyncHandler.dispatchScheduleTask(new MailThread(
                                                    email_list, content), 1, alarmMessage
                                                    .getFrequency());
                                    mailThreadNum++;
                                }
                            }
                        }
                    }
                    break;
                case APPS_TOMCAT:
                case APPS_JETTY:
                    monitorInfo =
                            sqlUtils.getMonitorInfo(
                                    1,
                                    String.format(nameSql, "list_spm_appserver",
                                            alarmMessage.getMonitorId()));
                    monitorName = monitorInfo[0];
                    set =
                            sqlUtils.attachList(String.format(quertAppSql, "collect_appserver",
                                    monitorName, monitorInfo[2], alarmMessage.getRetry_time()));
                    while (set.next()) {
                        if (set.getFloat("cpuUsage") > alarmMessage.getThreshold()) {
                            count++;
                            if (count == alarmMessage.getRetry_time()) {
                                String status_str =
                                        String.format(
                                                "Warning:[IP=%s, 应用服务器类型= %s, CPU使用率 = %f, 内存占用 = %f]",
                                                monitorInfo[1], alarmMessage.getType().type,
                                                set.getFloat("cpuUsage"), set.getFloat("memused"));
                                String content =
                                        String.format(content_base, monitorName, status_str,
                                                set.getString("timestamp"));

                                if (mailThreadNum == 0) {
                                    AsyncTaskUtils asyncHandler = AsyncTaskUtils.getInstance();
                                    mailInstance =
                                            asyncHandler.dispatchScheduleTask(new MailThread(
                                                    email_list, content), 1, alarmMessage
                                                    .getFrequency());
                                    mailThreadNum++;
                                }
                            }
                        }
                    }
                    break;
                default:
                    logger.warn("The monitor type is " + alarmMessage.getType()
                            + "; It is not supported.");
                    break;

            }
        } catch (SQLException e) {
            logger.error("", e);
        }
    }

    public void cancelMailThread() {
        mailInstance.cancel(true);
    }
}
