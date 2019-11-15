import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    private ArrayList<PeerObject> peerListe;

    public Server() {
        peerListe = new ArrayList<>();
    }

    private void startServer() {

        try {
            ServerSocket serverSocket = new ServerSocket(Utilities.getServerPort());
            System.out.println("Server gestartet");
            Utilities.printMyIp();

            while (true) {

                Socket connectionSocket = serverSocket.accept();

                Thread t = new Thread(() -> {

                    try {

                        InputStream inFromClient = connectionSocket.getInputStream();
                        OutputStream outToClient = connectionSocket.getOutputStream();

                        int msgErr;
                        byte[] msg = new byte[1];

                        msgErr = inFromClient.read(msg, 0, 1);
                        char tag = (char) msg[0];

                        msgErr = inFromClient.read(msg, 0, 1);
                        char version = (char) msg[0];

                        if (version == 0) {

                            switch (tag) {
                                case 1:
                                    msg = new byte[6];
                                    msgErr = inFromClient.read(msg, 0, 6);
                                    peerListe.add(0, new PeerObject(msg));
                                    outToClient.write(SharedCode.responeMsg(peerListe, 3));
                                    break;
                            }
                        }

                        connectionSocket.close();

                    } catch (Exception ioe) {
                        ioe.printStackTrace();
                    }
                });
                t.start();
            }

        } catch (Exception e) {
            Utilities.errorMessage(e);
        }
    }

    public static void main(String[] args) {

        Server s = new Server();
        s.startServer();
    }
}
