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
    private static final String CUSTOM_SOCKET = "package util;\n" +
                                                "import java.io.IOException;\n" +
                                                "import java.io.InputStreamReader;\n" +
                                                "import java.io.BufferedReader;\n" +
                                                "\n" +
                                                "public class Socket {\n" +
                                                "    private String remoteHostIP;\n" +
                                                "    private int remotePort;\n" +
                                                "    private java.net.Socket socket;\n" +
                                                "\n" +
                                                "    private BufferedReader reader;\n" +
                                                "\n" +
                                                "    public Socket(String remoteHostIP, int remotePort) {\n" +
                                                "        this.remoteHostIP = remoteHostIP;\n" +
                                                "        this.remotePort = remotePort;\n" +
                                                "    }\n" +
                                                "\n" +
                                                "    public Socket(java.net.Socket socket) throws IOException {\n" +
                                                "        this.socket = socket;\n" +
                                                "        this.remotePort = socket.getPort();\n" +
                                                "        this.remoteHostIP = socket.getRemoteSocketAddress().toString();\n" +
                                                "        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));\n" +
                                                "    }\n" +
                                                "\n" +
                                                "    public boolean connect() {\n" +
                                                "        try {\n" +
                                                "            socket = new java.net.Socket(remoteHostIP, remotePort);\n" +
                                                "            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));\n" +
                                                "        } catch (Exception e) {\n" +
                                                "            e.printStackTrace();\n" +
                                                "            return false;\n" +
                                                "        }\n" +
                                                "        return true;\n" +
                                                "    }\n" +
                                                "\n" +
                                                "    public int dataAvailable() throws IOException {\n" +
                                                "        return socket.getInputStream().available();\n" +
                                                "    }\n" +
                                                "\n" +
                                                "    public int read() throws IOException {\n" +
                                                "        return socket.getInputStream().read();\n" +
                                                "    }\n" +
                                                "\n" +
                                                "    public int read(byte[] b, int len) throws IOException {\n" +
                                                "        return socket.getInputStream().read(b, 0, len);\n" +
                                                "    }\n" +
                                                "\n" +
                                                "    public String readLine() throws IOException {\n" +
                                                "        return reader.readLine();\n" +
                                                "    }\n" +
                                                "\n" +
                                                "    public void write(int value) throws IOException {\n" +
                                                "        socket.getOutputStream().write(value);\n" +
                                                "    }\n" +
                                                "\n" +
                                                "    public void write(byte[] b, int len) throws IOException {\n" +
                                                "        socket.getOutputStream().write(b, 0, len);\n" +
                                                "    }\n" +
                                                "\n" +
                                                "    public void write(String s) throws IOException {\n" +
                                                "        socket.getOutputStream().write(s.getBytes());\n" +
                                                "    }\n" +
                                                "\n" +
                                                "    public void close() throws IOException {\n" +
                                                "        reader.close();\n" +
                                                "        socket.close();\n" +
                                                "    }\n" +
                                                "}\n";

    //language=JAVA
    private static final String CUSTOM_SERVERSOCKET = "package util;\n" +
                                                      "import java.io.IOException;\n" +
                                                      "\n" +
                                                      "public class ServerSocket {\n" +
                                                      "\n" +
                                                      "    private final java.net.ServerSocket serverSocket;\n" +
                                                      "\n" +
                                                      "    public ServerSocket(int localPort) {\n" +
                                                      "        try {\n" +
                                                      "            serverSocket = new java.net.ServerSocket(localPort);\n" +
                                                      "        } catch (Throwable t) {\n" +
                                                      "            throw new RuntimeException();\n" +
                                                      "        }\n" +
                                                      "    }\n" +
                                                      "\n" +
                                                      "    public Socket accept() {\n" +
                                                      "        try {\n" +
                                                      "            return new Socket(serverSocket.accept());\n" +
                                                      "        } catch (Throwable t) {\n" +
                                                      "            return null;\n" +
                                                      "        }\n" +
                                                      "    }\n" +
                                                      "\n" +
                                                      "    public void close() throws IOException {\n" +
                                                      "        serverSocket.close();\n" +
                                                      "    }\n" +
                                                      "}\n";

    public static void runClient() throws IOException {
        Path tmpDir = Files.createTempDirectory("jtjtnst");

        // language=Java
        String input1 = "package client;\n" +
                        "import util.Socket;\n" +
                        "import java.io.IOException;\n" +
                        "public class SumClient {\n" +
                        "    private Socket clientSocket;\n" +
                        "\n" +
                        "    public boolean verbinden(String host, int port) {\n" +
                        "        this.clientSocket = new Socket(host, port);\n" +
                        "        return this.clientSocket.connect();\n" +
                        "    }\n" +
                        "\n" +
                        "    public void senden(String text) {\n" +
                        "        try {\n" +
                        "            this.clientSocket.write(text + \"\\n\");\n" +
                        "        } catch (IOException e) {\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    public String empfangen() {\n" +
                        "        try {\n" +
                        "            if (this.clientSocket.dataAvailable() > 0)\n" +
                        "                return this.clientSocket.readLine();\n" +
                        "            return null;\n" +
                        "        } catch (IOException e) {\n" +
                        "            return null;\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    public void abmelden() {\n" +
                        "        if (this.clientSocket == null)\n" +
                        "            return;\n" +
                        "        try {\n" +
                        "            this.clientSocket.close();\n" +
                        "        } catch (IOException e) {\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n" +
                        "\n";

        //language=Java
        String input2 = "package client;\n" +
                        "import java.io.BufferedReader;\n" +
                        "import java.io.IOException;\n" +
                        "import java.io.InputStreamReader;\n" +
                        "\n" +
                        "public class SumClientUI {\n" +
                        "    public static void main(String[] args) throws IOException, InterruptedException {\n" +
                        "	    System.out.println(\"Start\");\n" +
                        "        SumClient sumClient = new SumClient();\n" +
                        "\n" +
                        "        System.out.println(\"Connect\");\n" +
                        "        boolean value = sumClient.verbinden(\"localhost\", 15000);\n" +
                        "        System.out.println(\"After connect\");\n" +
                        "        if(!value)\n" +
                        "            return;\n" +
                        "        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));\n" +
                        "\n" +
                        "        while (true) {\n" +
                        "            if (reader.ready()) {\n" +
                        "                String command = reader.readLine();\n" +
                        "                System.out.println(command);\n" +
                        "                sumClient.senden(command);\n" +
                        "            }\n" +
                        "\n" +
                        "            String empfangen = sumClient.empfangen();\n" +
                        "            if (empfangen == null) {\n" +
                        "                Thread.sleep(100);\n" +
                        "                continue;\n" +
                        "            }\n" +
                        "\n" +
                        "            System.out.println(empfangen);\n" +
                        "\n" +
                        "            if (empfangen.equals(\"Ende vom SumServer.\"))\n" +
                        "                break;\n" +
                        "            System.out.print(\"Geben Sie ein Kommando ein: \");\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n";

        String code = new JTJNSTranspiler(input1, input2, CUSTOM_SOCKET).getTranspiledCode();
        JavaCompilerHelper.compile("Main", code, tmpDir);

        InputStream errStream = JavaCompilerHelper.run("Main", tmpDir, System.in, System.out, true);
        if (errStream != null)
            System.out.println(new String(errStream.readAllBytes()));
    }

    public static void runServer() throws IOException {
        Path tmpDir = Files.createTempDirectory("jtjtnst");

        // language=JAVA
        String input1 = "package server;\n" +
                        "import util.ServerSocket;\n" +
                        "import util.Socket;\n" +
                        "import java.io.IOException;\n" +
                        "\n" +
                        "public class SumServer {\n" +
                        "\n" +
                        "    private ServerSocket socket;\n" +
                        "    private int localPort;\n" +
                        "\n" +
                        "    private long sum;\n" +
                        "\n" +
                        "    public SumServer(int localPort) {\n" +
                        "        this.localPort = localPort;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void runServer() {\n" +
                        "        try {\n" +
                        "            this.socket = new ServerSocket(localPort);\n" +
                        "        } catch (IOException e) {\n" +
                        "            return;\n" +
                        "        }\n" +
                        "\n" +
                        "        while (true) {\n" +
                        "            try {\n" +
                        "                System.out.println(\"Waiting for client...\");\n" +
                        "                Socket client = this.socket.accept();\n" +
                        "                client.write(\"OK vom SumServer.\\n\");\n" +
                        "                System.out.println(\"Accepted client and sent welcome message\");\n" +
                        "                new SumServerThread(this, client).run();\n" +
                        "            } catch (IOException e) {\n" +
                        "                System.out.println(\"Fehler beim verbinden!\");\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    public void beendeServer() {\n" +
                        "        if (this.socket == null)\n" +
                        "            return;\n" +
                        "        try {\n" +
                        "            this.socket.close();\n" +
                        "        } catch (IOException e) {\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    public synchronized long getSum() {\n" +
                        "        return sum;\n" +
                        "    }\n" +
                        "\n" +
                        "    public synchronized void setSum(long sum) {\n" +
                        "        this.sum = sum;\n" +
                        "    }\n" +
                        "}\n";

        // language=JAVA
        String input2 = "package server;\n" +
                        "\n" +
                        "import util.Socket;\n" +
                        "\n" +
                        "import java.io.IOException;\n" +
                        "\n" +
                        "public class SumServerThread {\n" +
                        "\n" +
                        "    private Socket clientSocket;\n" +
                        "    private final SumServer server;\n" +
                        "\n" +
                        "    public SumServerThread(SumServer server, Socket clientSocket) {\n" +
                        "        this.clientSocket = clientSocket;\n" +
                        "        this.server = server;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void run() {\n" +
                        "        new Thread(() -> loop()).start();\n" +
                        "    }\n" +
                        "\n" +
                        "    public void loop() {\n" +
                        "        while (this.clientSocket != null) {\n" +
                        "            try {\n" +
                        "                execute(this.clientSocket.readLine());\n" +
                        "            } catch (IOException e) {\n" +
                        "                break;\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    private void execute(String kommando) {\n" +
                        "        String[] cmd = kommando.split(\" \");\n" +
                        "\n" +
                        "        long number;\n" +
                        "        try {\n" +
                        "            number = Long.parseLong(cmd[1]);\n" +
                        "        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {\n" +
                        "            sendSum();\n" +
                        "            return;\n" +
                        "        }\n" +
                        "\n" +
                        "        if (\"p\".equals(cmd[0])) {\n" +
                        "            server.setSum(server.getSum() + number);\n" +
                        "            sendSum();\n" +
                        "        } else if (\"r\".equals(cmd[0]) && number == 0) {\n" +
                        "            server.setSum(0);\n" +
                        "            sendSum();\n" +
                        "        } else if (\"e\".equals(cmd[0]) && number == 0) {\n" +
                        "            try {\n" +
                        "                this.clientSocket.write(\"Ende vom SumServer.\\n\");\n" +
                        "            } catch (IOException ignored) {\n" +
                        "            }\n" +
                        "            this.clientSocket = null;\n" +
                        "        } else {\n" +
                        "            sendSum();\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    private void sendSum() {\n" +
                        "        try {\n" +
                        "            this.clientSocket.write(server.getSum() + \"\\n\");\n" +
                        "        } catch (IOException ignored) {\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n" +
                        "\n";

        // language=JAVA
        String input3 = "package server;\n" +
                        "public class SumServerUI {\n" +
                        "    public static void main(String[] args) {\n" +
                        "    	System.out.println(\"Start server\");\n" +
                        "        new SumServer(15000).runServer();\n" +
                        "    }\n" +
                        "}\n";

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
