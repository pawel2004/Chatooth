package com.example.grybos.chatooth;

public class Message {

    private String device_name;
    private String msg;

    public Message(String device_name, String msg) {
        this.device_name = device_name;
        this.msg = msg;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
