import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

/**
 * Created by a2shadab on 24/09/17.
 */
public class Client {

    public static String udpClient(String serverAdd, String serverPort, String reqCode) {
        DatagramSocket udpSocket = null;
        String tcpPort = null;
        try {
            // Create a UDP socket
            udpSocket = new DatagramSocket();
            // Set time out of 30 seconds
            udpSocket.setSoTimeout(30000);
            // Get host name and port number
            InetAddress hostName = InetAddress.getByName(serverAdd);
            int reqServerPort = Integer.valueOf(serverPort);
            // Create a UDP request, send req_code to the server
            DatagramPacket request = new DatagramPacket(reqCode.getBytes(), reqCode.length(), hostName, reqServerPort);
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
            DatagramPacket confirmation = new DatagramPacket(tcpPort.getBytes(), tcpPort.length(), hostName, reqServerPort);
            udpSocket.send(confirmation);
            // Receive acknowledgement from the server
            DatagramPacket ack = new DatagramPacket(buffer, buffer.length);
            udpSocket.receive(ack);
            String acknowledgement = new String(ack.getData(), ack.getOffset(), ack.getLength());
            System.out.println("ACKNOWLEDGEMENT RECEIVED=" + acknowledgement);
            if(acknowledgement.equals("no")) tcpPort = null;
        }
        catch (SocketException e) {
            System.out.println("Socket:" + e.getMessage());
        }
        catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
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

    public static String tcpClient(String serverAdd, String serverPort, String msg) throws IOException {
        Socket tcpSocket = null;
        String revMsg = null;
        try {
            // Create a new socket for TCP connection
            tcpSocket = new Socket(serverAdd, Integer.parseInt(serverPort));
            // Initialize input and output streams
            DataInputStream inputStream = new DataInputStream(tcpSocket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(tcpSocket.getOutputStream());
            // Write the message to the output stream to send it to server
            outputStream.writeUTF(msg);
            // Initialize buffer for reading from the input stream
            revMsg = inputStream.readUTF();
            // Update revMsg with the response from the Sever
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        finally {
            // Close TCP socket
            if(tcpSocket != null) {
                tcpSocket.close();
            }
            // return the message received from Server
            return revMsg;
        }
    }

    public static void main(String[] args) {
        // If there are less than 4 arguments program should exit
        if(args.length != 4) {
            System.out.println("Incorrect number of parameters");
            System.exit(1);
        }
        // Get connection details from the arguments
        String serverAdd = args[0];
        String serverPort = args[1];
        String reqCode = args[2];
        String msg = args[3];
        try {
            // Get the tcp port number from the server
            String tcpPort = udpClient(serverAdd, serverPort, reqCode);
            if(tcpPort == null) {
                throw new IOException("Could not establish tcp connection with server");
            }
            // Get the response from the server over tcp connection
            String revMsg = tcpClient(serverAdd, tcpPort, msg);
            // Print the response
            System.out.println("REVERSE=" + revMsg);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
