package org.example;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientTest {
    private static ServerSocket serverSocket;
    private static final int SERVER_PORT = 12345;

    @BeforeAll
    public static void setUp() throws IOException {
        serverSocket = new ServerSocket(SERVER_PORT);
        new Thread(() -> {
            try {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        serverSocket.close();
    }

    private static void handleClient(Socket clientSocket) {
        new Thread(() -> {
            try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                String userName = in.readLine();
                out.println(userName + " присоединился к чату!");

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(userName + ": " + message);
                    out.println(userName + ": " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Test
    public void testChatClients() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<String> client1Future = executor.submit(() -> runClient("User1"));
        Future<String> client2Future = executor.submit(() -> runClient("User2"));

        String client1Message = client1Future.get();
        String client2Message = client2Future.get();

        assertEquals("User1: Hello from User1", client2Message.trim());
        assertEquals("User2: Hello from User2", client1Message.trim());
    }

    private String runClient(String userName) {
        StringBuilder result = new StringBuilder();
        try (Socket socket = new Socket("localhost", SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(userName);
            out.println("Hello from " + userName);
            result.append(in.readLine());

            out.println("/exit");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}