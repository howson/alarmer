package com.sprouts.alarmer.handler;

import java.util.ArrayList;

import com.sprouts.spm_framework.alarmer.MailService;

public class MailThread implements Runnable {

    private ArrayList<String> emailList;
    private String content = null;

    public MailThread(ArrayList<String> emailList, String content) {
        this.emailList = emailList;
        this.content = content;
    }

    @Override
    public void run() {
        MailService mailService = MailService.getInstance();
        mailService.send(emailList, content);
    }

}
