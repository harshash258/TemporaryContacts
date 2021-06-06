package com.mycompany.createtemporarycontact.model;

public class Logs {
    String number, datetime, callType;

    public Logs(String number, String datetime, String callType) {
        this.number = number;
        this.datetime = datetime;
        this.callType = callType;
    }

    public String getNumber() {
        return number;
    }

    public String getDatetime() {
        return datetime;
    }

    public String getCallType() {
        return callType;
    }
}
