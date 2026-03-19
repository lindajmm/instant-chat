package com.example.server;

import com.example.common.Message;
import com.example.common.MessageType;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Linda
 * @date: 2026/3/18 17:02
 * @description:
 */
public class ChatServer {
    private static final int PORT = 8888;
    private ServerSocket serverSocket;
    private boolean running = false;

    // 存储在线客户端
    private ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private ServerGUI gui;

    public ChatServer() {
        this.gui = new ServerGUI(this);
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            gui.logMessage("服务器启动在端口: " + PORT);

            while (running) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, this);
                Thread thread = new Thread(handler);
                thread.setName("ClientHandler-Thread");
                thread.start();

                // 添加短暂休眠，让新线程有机会启动
                try {
                    Thread.sleep(1000);  // 等待100ms
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            gui.logMessage("服务器错误: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        try {
            // 通知所有客户端
            Message shutdownMsg = new Message();
            shutdownMsg.setType(MessageType.SYSTEM);
            shutdownMsg.setContent("服务器关闭");
            broadcastMessage(shutdownMsg, null);

            // 关闭所有客户端连接
            for (ClientHandler handler : clients.values()) {
                handler.closeConnection();
            }
            clients.clear();

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            gui.logMessage("服务器已关闭");
        } catch (IOException e) {
            gui.logMessage("关闭服务器错误: " + e.getMessage());
        }
    }

    // 添加客户端
    public void addClient(String username, ClientHandler handler) {
        clients.put(username, handler);
        gui.updateUserList(getUserList());

        // 广播用户加入消息
        Message joinMsg = new Message();
        joinMsg.setType(MessageType.SYSTEM);
        joinMsg.setContent("用户 " + username + " 加入聊天室");
        broadcastMessage(joinMsg, username);

        // 发送当前用户列表给新用户
        sendUserList(handler);
    }

    // 移除客户端
    public void removeClient(String username) {
        clients.remove(username);
        gui.updateUserList(getUserList());

        // 广播用户离开消息
        Message leaveMsg = new Message();
        leaveMsg.setType(MessageType.SYSTEM);
        leaveMsg.setContent("用户 " + username + " 离开聊天室");
        broadcastMessage(leaveMsg, null);
    }

    // 获取用户列表
    public List<String> getUserList() {
        return new ArrayList<>(clients.keySet());
    }

    // 发送用户列表给特定客户端
    private void sendUserList(ClientHandler handler) {
        Message listMsg = new Message();
        listMsg.setType(MessageType.USER_LIST);
        listMsg.setContent(String.join(",", getUserList()));
        handler.sendMessage(listMsg);
    }

    // 广播消息
    public void broadcastMessage(Message message, String excludeUser) {
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            if (excludeUser == null || !entry.getKey().equals(excludeUser)) {
                entry.getValue().sendMessage(message);
            }
        }
    }

    // 发送私聊消息
    public void sendPrivateMessage(Message message) {
        ClientHandler receiver = clients.get(message.getReceiver());
        if (receiver != null) {
            receiver.sendMessage(message);
            // 同时发送给发送者（显示在自己的聊天窗口）
            ClientHandler sender = clients.get(message.getSender());
            if (sender != null) {
                sender.sendMessage(message);
            }
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}
