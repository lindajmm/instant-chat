package com.example.server;


import com.example.common.Message;
import com.example.common.MessageType;

import java.io.*;
import java.net.Socket;

/**
 * @author: Linda
 * @date: 2026/3/18 17:03
 * @description:
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private ChatServer server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private boolean running = true;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // 接收用户名
            Message loginMsg = (Message) in.readObject();
            this.username = loginMsg.getSender();

            // 添加到服务器
            server.addClient(username, this);

            // 处理消息
            while (running) {
                try {
                    Message message = (Message) in.readObject();
                    processMessage(message);
                } catch (EOFException | ClassNotFoundException e) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("客户端 " + username + " 连接异常: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection();
        }
    }

    private void processMessage(Message message) {
        switch (message.getType()) {
            case TEXT:
                // 群发文本消息
                server.broadcastMessage(message, username);
                break;

            case PRIVATE:
                // 私聊消息
                server.sendPrivateMessage(message);
                break;

            case FILE:
                // 文件消息
                if (message.getReceiver() == null) {
                    // 群发文件
                    server.broadcastMessage(message, username);
                } else {
                    // 私发文件
                    server.sendPrivateMessage(message);
                }
                break;

            case LOGOUT:
                closeConnection();
                break;
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.out.println("发送消息给 " + username + " 失败: " + e.getMessage());
        }
    }

    public void closeConnection() {
        running = false;
        try {
            if (username != null) {
                server.removeClient(username);
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
