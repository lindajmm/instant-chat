package com.example.client;


import com.example.common.Message;
import com.example.common.MessageType;

import java.io.*;
import java.net.Socket;

/**
 * @author: Linda
 * @date: 2026/3/18 17:04
 * @description:
 */

public class ChatClient {
    private String serverHost;
    private int serverPort;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private ClientGUI gui;
    private boolean connected = false;
    private MessageListener listener;

    public ChatClient(String serverHost, int serverPort, ClientGUI gui) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.gui = gui;
    }

    public boolean connect(String username) {
        try {
            this.username = username;
            socket = new Socket(serverHost, serverPort);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // 发送登录信息
            Message loginMsg = new Message();
            loginMsg.setType(MessageType.LOGIN);
            loginMsg.setSender(username);
            sendMessage(loginMsg);

            connected = true;

            // 启动消息监听线程
            listener = new MessageListener();
//            new Thread(listener).start();
            Thread thread = new Thread(listener);
            thread.setName("MessageListener-Thread");
            thread.start();

            // 添加短暂休眠，让新线程有机会启动
            try {
                Thread.sleep(1000);  // 等待100ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return true;
        } catch (IOException e) {
            gui.showError("连接服务器失败: " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        try {
            connected = false;
            if (listener != null) {
                listener.stop();
            }

            // 发送登出消息
            Message logoutMsg = new Message();
            logoutMsg.setType(MessageType.LOGOUT);
            logoutMsg.setSender(username);
            sendMessage(logoutMsg);

            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            gui.showError("发送消息失败: " + e.getMessage());
        }
    }

    // 发送文本消息
    public void sendTextMessage(String content) {
        Message msg = Message.createTextMessage(username, content);
        sendMessage(msg);
    }

    // 发送私聊消息
    public void sendPrivateMessage(String receiver, String content) {
        Message msg = Message.createPrivateMessage(username, receiver, content);
        sendMessage(msg);
    }

    // 发送文件
    public void sendFile(String filePath, String receiver) {
        try {
            byte[] fileData = com.example.common.FileTransfer.fileToBytes(filePath);
            String fileName = com.example.common.FileTransfer.getFileName(filePath);
            long fileSize = com.example.common.FileTransfer.getFileSize(filePath);

            Message msg = Message.createFileMessage(username, receiver, fileName, fileData, fileSize);
            sendMessage(msg);

            gui.displayMessage("系统", "文件发送中: " + fileName);
        } catch (IOException e) {
            gui.showError("读取文件失败: " + e.getMessage());
        }
    }

    public String getUsername() {
        return username;
    }

    public boolean isConnected() {
        return connected;
    }

    // 消息监听器内部类
    private class MessageListener implements Runnable {
        private boolean running = true;

        @Override
        public void run() {
            while (running && connected) {
                try {
                    Message message = (Message) in.readObject();
                    processIncomingMessage(message);
                } catch (IOException | ClassNotFoundException e) {
                    if (running) {
                        gui.showError("与服务器连接断开");
                        disconnect();
                    }
                    break;
                }
            }
        }

        private void processIncomingMessage(Message message) {
            switch (message.getType()) {
                case TEXT:
                    gui.displayMessage(message.getSender(), message.getContent());
                    break;

                case PRIVATE:
                    gui.displayPrivateMessage(message.getSender(), message.getContent());
                    break;

                case FILE:
                    handleFileMessage(message);
                    break;

                case SYSTEM:
                    gui.displaySystemMessage(message.getContent());
                    break;

                case USER_LIST:
                    String[] users = message.getContent().split(",");
                    gui.updateUserList(users);
                    break;
            }
        }

        private void handleFileMessage(Message message) {
            String savePath = gui.chooseSaveLocation(message.getFileName());
            if (savePath != null) {
                try {
                    com.example.common.FileTransfer.bytesToFile(message.getFileData(), savePath);
                    gui.displaySystemMessage("文件已保存: " + savePath);
                } catch (IOException e) {
                    gui.showError("保存文件失败: " + e.getMessage());
                }
            }
        }

        public void stop() {
            running = false;
        }
    }
}
