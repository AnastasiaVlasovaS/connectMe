package org.example;

import org.junit.jupiter.api.*;
import java.io.*;
import java.net.Socket;
import static org.mockito.Mockito.*;

public class ServerTest {
    private PrintWriter mockOut;
    private BufferedReader mockIn;
    private Socket mockSocket;
    ChatServer.ClientHandler clientHandler = new ChatServer.ClientHandler(mockSocket);

    @BeforeEach
    public void setUp() throws IOException {
        mockSocket = mock(Socket.class);
        mockOut = new PrintWriter(new StringWriter(), true);
        mockIn = new BufferedReader(new StringReader("User1\nMessage1\n/exit\n"));

        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream("User1\nMessage1\n/exit\n".getBytes()));

        clientHandler = new ChatServer.ClientHandler(mockSocket);
        clientHandler.out = mockOut;
        clientHandler.in = mockIn;
    }

    @AfterEach
    public void tearDown() throws IOException {
        clientHandler.socket.close();
    }

    @Test
    public void testUserConnection() throws IOException {
        clientHandler.start();
        Assertions.assertTrue(ChatServer.clientWriters.contains(mockOut));
    }

    @Test
    public void testSendMessageToClients() throws IOException {
        PrintWriter mockOut2 = new PrintWriter(new StringWriter(), true);
        ChatServer.clientWriters.add(mockOut2);
        clientHandler.sendMessageToClients("Hello");
        mockOut.println("Hello");
        verify(mockOut).println("Hello");
        verify(mockOut2).println("Hello");
    }

    @Test
    public void testUserDisconnection() throws IOException {
        clientHandler.start();
        clientHandler.run();
        Assertions.assertFalse(ChatServer.clientWriters.contains(mockOut));

    }
}

