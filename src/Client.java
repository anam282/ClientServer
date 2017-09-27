import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

/**
 * Created by a2shadab on 24/09/17.
 */
public class Client {
    public static final int MAX_PORT = 65535;
    public static final int BUFFER_LENGTH = 1024;
    public static final int TIMEOUT = 30000;
    public static final String NO = "no";
    public static final int NAME = 0;
    public static final int PORT = 1;
    public static final int REQ_CODE = 2;
    public static final int MSG = 3;
    public static final int NUM_PARAMETERS = 4;

    /**
     *
     * @param hostName
     * @param serverPort
     * @param reqCode
     * @return tcpPort
     * Opens a UDP socket and sends req_code to the server. After the server validates the req_code and sends a TCP
     * port number, it send a confirmation and waits for the acknowledgement from the server
     */
    public static String udpClient(InetAddress hostName, int serverPort, String reqCode) throws Exception{
        // Create a UDP socket
        DatagramSocket udpSocket = new DatagramSocket();
        // Set time out of 30 seconds
        udpSocket.setSoTimeout(TIMEOUT);
        // Create a UDP request, send req_code to the server
        DatagramPacket request = new DatagramPacket(reqCode.getBytes(), reqCode.length(), hostName, serverPort);
        udpSocket.send(request);
        // Initialize buffer to receive response from the Server
        byte[] buffer = new byte[BUFFER_LENGTH];
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
        // Get the response from server. Server sends the random port for establishing TCP connection
        udpSocket.receive(reply);
        // Get port from the response
        String tcpPort = new String(reply.getData(), reply.getOffset(), reply.getLength());
        System.out.println("TCP_PORT=" + tcpPort);
        // Send confirmation to the Server. Confirmation has the post number sent by the server
        DatagramPacket confirmation = new DatagramPacket(tcpPort.getBytes(), tcpPort.length(), hostName, serverPort);
        udpSocket.send(confirmation);
        // Receive acknowledgement from the server
        udpSocket.receive(reply);
        String acknowledgement = new String(reply.getData(), reply.getOffset(), reply.getLength());
        System.out.println("ACKNOWLEDGEMENT_RECEIVED=" + acknowledgement);
        if(acknowledgement.equals(NO)) tcpPort = null;
        udpSocket.close();
        // Return the TCP Port received from server
        return tcpPort;
    }

    /**
     *
     * @param hostName
     * @param serverPort
     * @param msg
     * @return
     * @throws IOException
     * Establishes TCP connection with the server and sends a string to the server. Receives reversed string from
     * the server and closes the connection
     */
    public static String tcpClient(InetAddress hostName, int serverPort, String msg) throws IOException {
        // Create a new socket for TCP connection
        Socket tcpSocket = new Socket(hostName, serverPort);
        // Initialize input and output streams
        DataInputStream inputStream = new DataInputStream(tcpSocket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(tcpSocket.getOutputStream());
        // Write the message to the output stream to send it to server
        outputStream.writeUTF(msg);
        // Update revMsg with the response from the Sever
        String revMsg = inputStream.readUTF();
        tcpSocket.close();
        return revMsg;
    }

    /**
     *
     * @param args
     * Runs the client program
     */
    public static void main(String[] args) {
        // If there are less than 4 arguments program should exit
        if(args.length != NUM_PARAMETERS) {
            System.out.println("Incorrect number of parameters");
            System.exit(1);
        }
        try {
            // Get connection details from the arguments
            InetAddress hostName = InetAddress.getByName(args[NAME]);
            Integer serverPort = Integer.parseInt(args[PORT]);
            if( serverPort < 0 || serverPort > MAX_PORT ) {
                throw new Exception("Incorrect port number");
            }
            Integer reqCode = Integer.parseInt(args[REQ_CODE]);
            String msg = args[MSG];
            // Get the tcp port number from the server
            String tcpPort = udpClient(hostName, serverPort, String.valueOf(reqCode));
            if(tcpPort == null) {
                throw new IOException("Could not establish tcp connection with server");
            }
            // Get the response from the server over tcp connection
            String revMsg = tcpClient(hostName, Integer.valueOf(tcpPort), msg);
            // Print the response
            System.out.println("REVERSE=" + revMsg);
        }
        catch (Exception e) {
            System.out.println("EXCEPTION:" + e.getMessage());
        }
    }

}
