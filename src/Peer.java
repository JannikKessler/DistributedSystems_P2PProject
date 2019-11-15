import java.io.*;
import java.net.InetAddress;
import java.net.Socket;


public class Peer {

    private final int PORT = 3333;

    public Peer() {
    }

    private void startPeer() {
        try {

            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            Socket clientSocket = new Socket("localhost", PORT);

            OutputStream outToServer = clientSocket.getOutputStream();
            InputStream inFromServer = clientSocket.getInputStream();

            System.out.println("Client gestartet");
            Utilities.printMyIp();

            byte[] entryMsg = new byte[8];
            entryMsg[0] = 1; //Tag
            entryMsg[1] = 0; //Version
            Utilities.packIpPackage(entryMsg, 2, InetAddress.getLocalHost().getAddress(), Utilities.intToByteArray(PORT));

            outToServer.write(entryMsg);

        } catch (Exception e) {
            Utilities.errorMessage(e);
        }

    }

    public static void main(String[] args) {

        Peer p = new Peer();
        p.startPeer();
    }


}
