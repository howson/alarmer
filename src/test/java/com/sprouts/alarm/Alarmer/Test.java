package com.sprouts.alarm.Alarmer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;

import com.sprouts.spm_framework.utils.AsyncTaskUtils;


public class Test {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // String name =
        // String.format("I really love %s", "asshole！<br><br>&nbsp;&nbsp;&nbsp;God Bless ME!");
        // MailService mailService = MailService.getInstance();
        // ArrayList<String> list = new ArrayList<String>();
        // list.add("haoshen.liu@foxmail.com");
        // mailService.send(list, name);

        AsyncTaskUtils utils = AsyncTaskUtils.getInstance();
        ScheduledFuture<Fuck> future = utils.dispatchScheduleTask(new Fuck(), 0, 2);
    }

    static class Fuck implements Runnable {

        String name = "fuck wudan";

        public void print() {
            System.out.println(name);
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub

        }

    }
}
