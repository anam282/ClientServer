import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

/**
 * Created by a2shadab on 24/09/17.
 */
public class Server {

    /**
     *
     * @param reqCodeServer
     * This function runs the UDP connection. When a client sends a request_code, it matches the code with its req_code
     * and if there is a match, sets up a tcp connection for transfer of data. If the req_code does not match it does
     * nothing
     */
    public static final int BUFFER_LENGTH = 1024;
    public static final String OK = "ok";
    public static final String NO = "no";
    public static final int NUM_PARAMETERS = 1;
    public static final int REQ_CODE = 0;

    public static void serverUDP(String reqCodeServer) {

        DatagramSocket udpSocket = null;
        try {
            // Create a UDP connection
            udpSocket = new DatagramSocket();
            System.out.println("SERVER_PORT=" + udpSocket.getLocalPort());
            byte[] buffer = new byte[BUFFER_LENGTH];
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
                DatagramPacket reply = new DatagramPacket(rPort.getBytes(),
                        rPort.length(),
                        request.getAddress(),
                        request.getPort());
                udpSocket.send(reply);
                // Receive the confirmation from the client
                udpSocket.receive(request);
                String tcpPort = new String(request.getData(), request.getOffset(), request.getLength());
                // Check if the port number received by client matches port number sent by server and send acknowledgement
                if(tcpPort.equals(rPort)) {
                    DatagramPacket ackReply = new DatagramPacket(OK.getBytes(),
                            OK.length(),
                            request.getAddress(),
                            request.getPort());
                    udpSocket.send(ackReply);
                    // Establish TCP connection accept message from the client
                    serverTCP(tcpSocket);
                }
                else {
                    DatagramPacket ackReply = new DatagramPacket(NO.getBytes(),
                            NO.length(),
                            request.getAddress(),
                            request.getPort());
                    udpSocket.send(ackReply);
                    System.out.println("TCP port received from client did not match the original port number");
                }
            }
        }
        catch (Exception e) {
            System.out.println("EXCEPTION:" + e.getMessage());
        }
        finally {
            if(udpSocket!= null) {
                udpSocket.close();
            }
        }
    }

    /**
     *
     * @param str
     * @return reversed str
     * Takes a string as input and returns the reversed string
     */
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

    /**
     *
     * @param tcpSocket
     * @throws IOException
     * Waits for client to connect over TCP socket. When the connection is established, accepts a string input from the
     * client and sends back reversed string and closes the connection.
     */
    public static  void  serverTCP(ServerSocket tcpSocket) throws IOException{
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
        // Close input output streams
        inputStream.close();
        outputStream.close();
        client.close();
        tcpSocket.close();
    }

    /**
     *
     * @param args
     * Runs the server program
     */
    public static void main(String[] args) {
        // start server
        if(args.length != NUM_PARAMETERS) {
            System.out.println("Incorrect number of parameters");
            System.exit(1);
        }
        try {
            int reqCode = Integer.parseInt(args[REQ_CODE]);
            serverUDP(String.valueOf(reqCode));
        }
        catch (Exception e) {
            System.out.println("EXCEPTION:" + e.getMessage());
        }
    }
}
