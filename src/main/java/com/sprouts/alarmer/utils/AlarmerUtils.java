package com.sprouts.alarmer.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import com.sprouts.alarmer.handler.AlarmerThread;
import com.sprouts.spm_framework.utils.Logger;

/**
 * Alarmer的工具类
 * 
 * @author howson
 * 
 */
public class AlarmerUtils {

    @SuppressWarnings("rawtypes")
    public static Map<String, ScheduledFuture> localTasksMap =
            new HashMap<String, ScheduledFuture>(); // 本地任务Map，用来取消任务和创建任务
    public static Map<String, AlarmerThread> localThreadMap = new HashMap<String, AlarmerThread>();// 本地线程Map，用来关闭MailThread
    private static Logger logger = new Logger();

    @SuppressWarnings("rawtypes")
    public static void insertToTaskMap(String name, AlarmerThread thread, ScheduledFuture task) {
        try {
            insertToThreadMap(name, thread);
            localTasksMap.put(name, task);
        } catch (Exception e) {
            logger.error("Error in inserting element from task map:", e);
        }
    }

    public static void insertToThreadMap(String name, AlarmerThread task) {
        try {
            localThreadMap.put(name, task);
        } catch (Exception e) {
            logger.error("Error in inserting element from thread map:", e);
        }
    }

    public static void removeFromTaskMap(String name) {
        try {
            removeFromThreadMap(name);
            ScheduledFuture cancelTask = getValue(name);
            cancelTask.cancel(true);
            localTasksMap.remove(name);
        } catch (Exception e) {
            logger.error("Error in removing element from task map:", e);
        }
    }

    public static void removeFromThreadMap(String name) {
        try {
            AlarmerThread thread = localThreadMap.get(name);
            thread.cancelMailThread();
            localThreadMap.remove(name);
        } catch (Exception e) {
            logger.error("Error in removing element from thread map:", e);
        }
    }

    public static boolean containsValue(String key) {
        boolean find = false;
        try {
            find = localTasksMap.containsKey(key);
        } catch (Exception e) {
            logger.error("Error in finding element in task map:", e);
        }
        return find;
    }

    @SuppressWarnings("rawtypes")
    public static ScheduledFuture getValue(String key) {
        ScheduledFuture future = null;
        try {
            future = localTasksMap.get(key);
        } catch (Exception e) {
            logger.error("Error in getting element from task map:", e);
        }
        return future;
    }
}
