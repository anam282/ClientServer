import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

/**
 * Created by a2shadab on 24/09/17.
 */
public class Client {

    /**
     *
     * @param hostName
     * @param serverPort
     * @param reqCode
     * @return tcpPort
     * Opens a UDP socket and sends req_code to the server. After the server validates the req_code and sends a TCP
     * port number, it send a confirmation and waits for the acknowledgement from the server
     */
    public static String udpClient(InetAddress hostName, int serverPort, String reqCode) {
        DatagramSocket udpSocket = null;
        String tcpPort = null;
        try {
            // Create a UDP socket
            udpSocket = new DatagramSocket();
            // Set time out of 30 seconds
            udpSocket.setSoTimeout(30000);
            // Create a UDP request, send req_code to the server
            DatagramPacket request = new DatagramPacket(reqCode.getBytes(), reqCode.length(), hostName, serverPort);
            udpSocket.send(request);
            // Initialize buffer to receive response from the Server
            byte[] buffer = new byte[1024];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            // Get the response from server. Server sends the random port for establishing TCP connection
            udpSocket.receive(reply);
            // Get port from the response
            tcpPort = new String(reply.getData(), reply.getOffset(), reply.getLength());
            System.out.println("TCP_PORT=" + tcpPort);
            // Send confirmation to the Server. Confirmation has the post number sent by the server
            DatagramPacket confirmation = new DatagramPacket(tcpPort.getBytes(), tcpPort.length(), hostName, serverPort);
            udpSocket.send(confirmation);
            // Receive acknowledgement from the server
            udpSocket.receive(reply);
            String acknowledgement = new String(reply.getData(), reply.getOffset(), reply.getLength());
            System.out.println("ACKNOWLEDGEMENT RECEIVED=" + acknowledgement);
            if(acknowledgement.equals("no")) tcpPort = null;
        }
        catch (Exception e) {
            System.out.println("EXCEPTION:" + e.getMessage());
        }
        finally {
            // Close the UDP socket
            if(udpSocket != null) {
                udpSocket.close();
            }
            // Return the TCP Port received from server
            return tcpPort;
        }
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
        if(args.length != 4) {
            System.out.println("Incorrect number of parameters");
            System.exit(1);
        }
        try {
            // Get connection details from the arguments
            InetAddress hostName = InetAddress.getByName(args[0]);
            Integer serverPort = Integer.parseInt(args[1]);
            if( serverPort < 0 || serverPort > 65535 ) {
                throw new Exception("Incorrect port number");
            }
            Integer reqCode = Integer.parseInt(args[2]);
            String msg = args[3];
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
