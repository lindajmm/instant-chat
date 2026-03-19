package com.example.common;

import java.io.Serializable;
/**
 * @author: Linda
 * @date: 2026/3/18 17:00
 * @description:
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageType type;
    private String sender;
    private String receiver;  // null表示群聊
    private String content;
    private byte[] fileData;
    private String fileName;
    private long fileSize;
    private long timestamp;

    public Message() {
        this.timestamp = System.currentTimeMillis();
    }

    // 文本消息构造器
    public static Message createTextMessage(String sender, String content) {
        Message msg = new Message();
        msg.setType(MessageType.TEXT);
        msg.setSender(sender);
        msg.setContent(content);
        return msg;
    }

    // 私聊消息构造器
    public static Message createPrivateMessage(String sender, String receiver, String content) {
        Message msg = new Message();
        msg.setType(MessageType.PRIVATE);
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setContent(content);
        return msg;
    }

    // 文件消息构造器
    public static Message createFileMessage(String sender, String receiver,
                                            String fileName, byte[] data, long fileSize) {
        Message msg = new Message();
        msg.setType(MessageType.FILE);
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setFileName(fileName);
        msg.setFileData(data);
        msg.setFileSize(fileSize);
        msg.setContent("发送文件: " + fileName);
        return msg;
    }

    // Getters and Setters
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", type, sender, content);
    }
}
