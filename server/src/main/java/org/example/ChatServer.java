package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class ChatServer {
    static int PORT = 12345;
    static String LOG_FILE;
    static Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) {
        loadConfig();
        System.out.println("Chat server started on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
            PORT = Integer.parseInt(properties.getProperty("port", "12345"));
            LOG_FILE = properties.getProperty("logFile", "file.log");
        } catch (IOException ex) {
            ex.printStackTrace();
            PORT = 12345;
            LOG_FILE = "file.log";
        }
    }

    static class ClientHandler extends Thread {
        Socket socket;
        PrintWriter out;
        BufferedReader in;
        private String userName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                userName = in.readLine();
                log("User connected: " + userName);

                String message;
                while ((message = in.readLine()) != null) {
                    log(userName + ": " + message);
                    sendMessageToClients(userName + ": " + message);
                    if (message.equalsIgnoreCase("/exit")) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
                log("User disconnected: " + userName);
            }
        }

        void sendMessageToClients(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }

        private void log(String message) {
            try (FileWriter fw = new FileWriter(LOG_FILE, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                out.println(timestamp + " - " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}