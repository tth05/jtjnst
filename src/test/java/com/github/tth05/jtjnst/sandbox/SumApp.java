package com.github.tth05.jtjnst.sandbox;

import com.github.tth05.jtjnst.cmd.JavaCompilerHelper;
import com.github.tth05.jtjnst.transpiler.JTJNSTranspiler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This is a very simple client-server app with three available commands for the client:
 * <br>
 * <ul>
 *     <li>p <number> : Adds the given number to the global sum</li>
 *     <li>r 0 : Resets the sum to 0</li>
 *     <li>e 0 : Closes the connection</li>
 *     <li>Unknown commands will just send back the global sum</li>
 * </ul>
 * <br>
 * Use {@link ServerMain} to run the server and afterwards {@link ClientMain} to start a client. All classes will be
 * transpiled into one by JTJNST. The actual project is just a demonstration and does not consist of great code.
 */
public class SumApp {

    //language=JAVA
    private static final String CUSTOM_SOCKET = """
            package util;
            import java.io.IOException;
            import java.io.InputStreamReader;
            import java.io.BufferedReader;
                        
            public class Socket {
                private String remoteHostIP;
                private int remotePort;
                private java.net.Socket socket;
                        
                private BufferedReader reader;
                        
                public Socket(String remoteHostIP, int remotePort) {
                    this.remoteHostIP = remoteHostIP;
                    this.remotePort = remotePort;
                }
                        
                public Socket(java.net.Socket socket) throws IOException {
                    this.socket = socket;
                    this.remotePort = socket.getPort();
                    this.remoteHostIP = socket.getRemoteSocketAddress().toString();
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                }
                        
                public boolean connect() {
                    try {
                        socket = new java.net.Socket(remoteHostIP, remotePort);
                        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                    return true;
                }
                        
                public int dataAvailable() throws IOException {
                    return socket.getInputStream().available();
                }
                        
                public int read() throws IOException {
                    return socket.getInputStream().read();
                }
                        
                public int read(byte[] b, int len) throws IOException {
                    return socket.getInputStream().read(b, 0, len);
                }
                        
                public String readLine() throws IOException {
                    return reader.readLine();
                }
                        
                public void write(int value) throws IOException {
                    socket.getOutputStream().write(value);
                }
                        
                public void write(byte[] b, int len) throws IOException {
                    socket.getOutputStream().write(b, 0, len);
                }
                        
                public void write(String s) throws IOException {
                    socket.getOutputStream().write(s.getBytes());
                }
                        
                public void close() throws IOException {
                    reader.close();
                    socket.close();
                }
            }
            """;

    //language=JAVA
    private static final String CUSTOM_SERVERSOCKET = """
            package util;
            import java.io.IOException;
                        
            public class ServerSocket {
                        
                private final java.net.ServerSocket serverSocket;
                        
                public ServerSocket(int localPort) {
                    try {
                        serverSocket = new java.net.ServerSocket(localPort);
                    } catch (Throwable t) {
                        throw new RuntimeException();
                    }
                }
                        
                public Socket accept() {
                    try {
                        return new Socket(serverSocket.accept());
                    } catch (Throwable t) {
                        return null;
                    }
                }
                        
                public void close() throws IOException {
                    serverSocket.close();
                }
            }
            """;

    public static void runClient() throws IOException {
        Path tmpDir = Files.createTempDirectory("jtjtnst");

        // language=Java
        String input1 = """
                package client;
                import util.Socket;
                import java.io.IOException;
                public class SumClient {
                    private Socket clientSocket;
                                
                    public boolean verbinden(String host, int port) {
                        this.clientSocket = new Socket(host, port);
                        return this.clientSocket.connect();
                    }
                                
                    public void senden(String text) {
                        try {
                            this.clientSocket.write(text + "\\n");
                        } catch (IOException e) {
                        }
                    }
                                
                    public String empfangen() {
                        try {
                            if (this.clientSocket.dataAvailable() > 0)
                                return this.clientSocket.readLine();
                            return null;
                        } catch (IOException e) {
                            return null;
                        }
                    }
                                
                    public void abmelden() {
                        if (this.clientSocket == null)
                            return;
                        try {
                            this.clientSocket.close();
                        } catch (IOException e) {
                        }
                    }
                }
                                
                """;

        //language=Java
        String input2 = """
                package client;
                import java.io.BufferedReader;
                import java.io.IOException;
                import java.io.InputStreamReader;
                                
                public class SumClientUI {
                    public static void main(String[] args) throws IOException, InterruptedException {
                	    System.out.println("Start");
                        SumClient sumClient = new SumClient();
                                
                        System.out.println("Connect");
                        boolean value = sumClient.verbinden("localhost", 15000);
                        System.out.println("After connect");
                        if(!value)
                            return;
                        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                                
                        while (true) {
                            if (reader.ready()) {
                                String command = reader.readLine();
                                System.out.println(command);
                                sumClient.senden(command);
                            }
                                
                            String empfangen = sumClient.empfangen();
                            if (empfangen == null) {
                                Thread.sleep(100);
                                continue;
                            }
                                
                            System.out.println(empfangen);
                                
                            if (empfangen.equals("Ende vom SumServer."))
                                break;
                            System.out.print("Geben Sie ein Kommando ein: ");
                        }
                    }
                }
                """;

        String code = new JTJNSTranspiler(input1, input2, CUSTOM_SOCKET).getTranspiledCode();
        JavaCompilerHelper.compile("Main", code, tmpDir);

        InputStream errStream = JavaCompilerHelper.run("Main", tmpDir, System.in, System.out, true);
        if (errStream != null)
            System.out.println(new String(errStream.readAllBytes()));
    }

    public static void runServer() throws IOException {
        Path tmpDir = Files.createTempDirectory("jtjtnst");

        // language=JAVA
        String input1 = """
                package server;
                import util.ServerSocket;
                import util.Socket;
                import java.io.IOException;
                                
                public class SumServer {
                                
                    private ServerSocket socket;
                    private int localPort;
                                
                    private long sum;
                                
                    public SumServer(int localPort) {
                        this.localPort = localPort;
                    }
                                
                    public void runServer() {
                        try {
                            this.socket = new ServerSocket(localPort);
                        } catch (IOException e) {
                            return;
                        }
                                
                        while (true) {
                            try {
                                System.out.println("Waiting for client...");
                                Socket client = this.socket.accept();
                                client.write("OK vom SumServer.\\n");
                                System.out.println("Accepted client and sent welcome message");
                                new SumServerThread(this, client).run();
                            } catch (IOException e) {
                                System.out.println("Fehler beim verbinden!");
                            }
                        }
                    }
                                
                    public void beendeServer() {
                        if (this.socket == null)
                            return;
                        try {
                            this.socket.close();
                        } catch (IOException e) {
                        }
                    }
                                
                    public synchronized long getSum() {
                        return sum;
                    }
                                
                    public synchronized void setSum(long sum) {
                        this.sum = sum;
                    }
                }
                """;

        // language=JAVA
        String input2 = """
                package server;
                
                import util.Socket;
                
                import java.io.IOException;
                
                public class SumServerThread {
                
                    private Socket clientSocket;
                    private final SumServer server;
                
                    public SumServerThread(SumServer server, Socket clientSocket) {
                        this.clientSocket = clientSocket;
                        this.server = server;
                    }
                
                    public void run() {
                        new Thread(() -> loop()).start();
                    }
                
                    public void loop() {
                        while (this.clientSocket != null) {
                            try {
                                execute(this.clientSocket.readLine());
                            } catch (IOException e) {
                                break;
                            }
                        }
                    }
                
                    private void execute(String kommando) {
                        String[] cmd = kommando.split(" ");
                
                        long number;
                        try {
                            number = Long.parseLong(cmd[1]);
                        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                            sendSum();
                            return;
                        }
                
                        if ("p".equals(cmd[0])) {
                            server.setSum(server.getSum() + number);
                            sendSum();
                        } else if ("r".equals(cmd[0]) && number == 0) {
                            server.setSum(0);
                            sendSum();
                        } else if ("e".equals(cmd[0]) && number == 0) {
                            try {
                                this.clientSocket.write("Ende vom SumServer.\\n");
                            } catch (IOException ignored) {
                            }
                            this.clientSocket = null;
                        } else {
                            sendSum();
                        }
                    }
                
                    private void sendSum() {
                        try {
                            this.clientSocket.write(server.getSum() + "\\n");
                        } catch (IOException ignored) {
                        }
                    }
                }
                                
                """;

        // language=JAVA
        String input3 = """
                package server;
                public class SumServerUI {
                    public static void main(String[] args) {
                    	System.out.println("Start server");
                        new SumServer(15000).runServer();
                    }
                }
                """;

        String code = new JTJNSTranspiler(input1, input2, input3, CUSTOM_SOCKET, CUSTOM_SERVERSOCKET).getTranspiledCode();
        JavaCompilerHelper.compile("Main", code, tmpDir);

        InputStream errStream = JavaCompilerHelper.run("Main", tmpDir, System.in, System.out, true);
        if (errStream != null)
            System.out.println(new String(errStream.readAllBytes()));
    }

    private static final class ClientMain {
        public static void main(String[] args) throws IOException {
            SumApp.runClient();
        }
    }

    private static final class ServerMain {
        public static void main(String[] args) throws IOException {
            SumApp.runServer();
        }
    }
}
