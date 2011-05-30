package com.sectra.jfileshare.objects;

import java.util.Date;

public class FileLog {
    private String ipAddress;
    private Date dateAccess;

    FileLog(Date dateAccess, String ipAddress) {
        this.dateAccess = dateAccess;
        this.ipAddress = ipAddress;
    }

    public String getIp() {
        return this.ipAddress;
    }

    public Date getTime() {
        return this.dateAccess;
    }
}
