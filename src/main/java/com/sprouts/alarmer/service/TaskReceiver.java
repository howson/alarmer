package com.sprouts.alarmer.service;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import com.sprouts.alarmer.handler.AlarmerThread;
import com.sprouts.alarmer.utils.AlarmerUtils;
import com.sprouts.spm_framework.amq.AmqConfig;
import com.sprouts.spm_framework.amq.AmqReceiver;
import com.sprouts.spm_framework.amq.message.AlarmTaskMessage;
import com.sprouts.spm_framework.enums.MonitorType;
import com.sprouts.spm_framework.utils.AsyncTaskUtils;
import com.sprouts.spm_framework.utils.Logger;
import com.sprouts.spm_framework.utils.SPMConstants;

/**
 * 接收派来的Alarmer任务
 * 
 * @author howson
 * 
 */
public class TaskReceiver {

    public static AmqReceiver receiver;
    private Logger logger = new Logger();
    AsyncTaskUtils asynHandler = null;

    public void initReceiver() {
        asynHandler = AsyncTaskUtils.getInstance();
        AmqConfig config =
                AmqConfig.getDefaultConfig(SPMConstants.ALARMER_BROKER, SPMConstants.ALARMER_QUEUE,
                        AlarmTaskMessage.class, new AlarmerListener());
        receiver = AmqReceiver.initAmqReceiver(config);
        receiver.receive();
    }

    /**
     * ActiveMQ接收消息的监听器
     * 
     * @author howson
     * 
     */
    class AlarmerListener implements MessageListener {

        @SuppressWarnings("rawtypes")
        @Override
        public void onMessage(Message arg0) {
            try {
                if (arg0 != null) {

                    AlarmTaskMessage message = new AlarmTaskMessage();
                    message = (AlarmTaskMessage) message.parseMapMsgToBaseMsg((MapMessage) arg0);

                    // 判断消息是要取消告警线程还是创建告警线程
                    if (message.getType() != MonitorType.CANCEL) {
                        String[] emails = message.getEmail().split(";");
                        ArrayList<String> email_list = new ArrayList<String>(10);
                        if (emails.length > 0) {
                            for (String email : emails) {
                                email_list.add(email);
                            }
                        }

                        String key = message.getUserId() + "-" + message.getMonitorId();
                        AlarmerThread thread = new AlarmerThread(message, email_list);
                        if (AlarmerUtils.containsValue(key)) {
                            // 若已存在线程，则取消掉原来的线程重新创建
                            AlarmerUtils.removeFromTaskMap(key);
                        }
                        ScheduledFuture futureTask = asynHandler.dispatchScheduleTask(thread, 0, 3);
                        AlarmerUtils.insertToTaskMap(thread.threadName, thread, futureTask); // 将线程对象添加到本地任务Map中

                    } else {
                        String key = message.getUserId() + "-" + message.getMonitorId();
                        if (AlarmerUtils.containsValue(key)) {
                            // 取消任务
                            AlarmerUtils.removeFromTaskMap(key);
                        }
                    }
                } else {
                    logger.error("The message received is null.");
                }
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }
}
