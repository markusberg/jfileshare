package com.sectra.jfileshare.objects;

import java.util.Date;

public class FileLog {
    private String ipAddr;
    private Date dateAccess;

    FileLog(Date dateAccess, String ipAddr) {
        this.dateAccess = dateAccess;
        this.ipAddr = ipAddr;
    }

    public String getIp() {
        return this.ipAddr;
    }

    public Date getTime() {
        return this.dateAccess;
    }
}
