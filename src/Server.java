import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

/**
 * Created by a2shadab on 24/09/17.
 */
public class Server {

    public static void serverUDP(String reqCodeServer) {

        DatagramSocket udpSocket = null;
        try {
            // Create a UDP connection
            udpSocket = new DatagramSocket();
            System.out.println("SERVER_PORT=" + udpSocket.getLocalPort());
            byte[] buffer = new byte[1024];
            while(true) {
                // Server receives request code from the client
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(request);
                String reqCodeClient = new String(request.getData(), request.getOffset(), request.getLength());
                // Server checks if the request code matches with the server's request code
                if(!reqCodeClient.equals(reqCodeServer)) {
                    System.out.println("REQUEST CODE DID NOT MATCH");
                    continue;
                }
                // Find a free port to establish TCP connection
                ServerSocket tcpSocket = new ServerSocket(0);
                String rPort = String.valueOf(tcpSocket.getLocalPort());
                System.out.println("SERVER_TCP_PORT=" + rPort);
                // Send the TCP port to the client
                buffer = rPort.getBytes();
                DatagramPacket reply = new DatagramPacket(buffer,
                        rPort.length(),
                        request.getAddress(),
                        request.getPort());
                udpSocket.send(reply);
                // Receive the confirmation from the client
                udpSocket.receive(request);
                String tcpPort = new String(request.getData(), request.getOffset(), request.getLength());
                // Check if the port number received by client matches port number sent by server and send acknowledgement
                if(tcpPort.equals(rPort)) {
                    DatagramPacket ackReply = new DatagramPacket("ok".getBytes(),
                            "ok".length(),
                            request.getAddress(),
                            request.getPort());
                    udpSocket.send(ackReply);
                    // Establish TCP connection accept message from the client
                    serverTCP(tcpSocket);
                }
                else {
                    DatagramPacket ackReply = new DatagramPacket("no".getBytes(),
                            "no".length(),
                            request.getAddress(),
                            request.getPort());
                    udpSocket.send(ackReply);
                    System.out.println("TCP port received from client did not match the original port number");
                }
            }
        }
        catch (SocketException e) {
            System.out.println("Socket:" + e.getMessage());
        }
        catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        }
        catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
        }
        finally {
            if(udpSocket!= null) {
                udpSocket.close();
            }
        }
    }

    public static String reverseString(String str) {
        if(str == null || str.length() == 0) return str;
        char[] strArr = str.toCharArray();
        int start = 0;
        int end = strArr.length-1;
        while(start < end) {
            char temp = strArr[start];
            strArr[start] = strArr[end];
            strArr[end] = temp;
            start++;
            end--;
        }
        return new String(strArr);
    }

    public static  void  serverTCP(ServerSocket tcpSocket) throws IOException{
        try {
            // Wait for client to connect
            Socket client = tcpSocket.accept();
            // Open data input/output streams
            DataInputStream inputStream = new DataInputStream(client.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());
            // Get message from client
            String msg = inputStream.readUTF();
            System.out.println("SERVER_RCV_MSG=" + msg);
            // Reverse string
            String revMsg = reverseString(msg);
            // Write output to output stream
            outputStream.writeUTF(revMsg);
            inputStream.close();
            outputStream.close();
            client.close();
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        finally {
            if(tcpSocket != null) {
                tcpSocket.close();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        // start server
        if(args.length != 1) {
            System.out.println("Incorrect number of parameters");
            System.exit(1);
        }
        try {
            int reqCode = Integer.parseInt(args[0]);
            serverUDP(String.valueOf(reqCode));
        }
        catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
        }
    }

}
