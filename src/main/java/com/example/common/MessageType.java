package com.example.common;


/**
 * @author: Linda
 * @date: 2026/3/18 17:00
 * @description:
 */

public enum MessageType {
    TEXT,           // 普通文本消息
    PRIVATE,        // 私聊消息
    FILE,           // 文件消息
    SYSTEM,         // 系统消息
    USER_LIST,      // 用户列表
    LOGIN,          // 登录消息
    LOGOUT          // 登出消息
}