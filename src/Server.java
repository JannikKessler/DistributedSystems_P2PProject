import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
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

                System.out.println("test2");

                Thread t = new Thread(() -> {

                    try {

                        InputStream inFromClient = connectionSocket.getInputStream();
                        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

                        int msgErr;
                        byte[] msg = new byte[1];

                        msgErr = inFromClient.read(msg, 0, 1);
                        int tag = Utilities.byteArrayToInt(msg);

                        msgErr = inFromClient.read(msg, 0, 1);
                        int version = Utilities.byteArrayToInt(msg);

                        if (version == 0) {

                            switch (tag) {
                                case 1:
                                    msg = new byte[6];
                                    msgErr = inFromClient.read(msg, 0, 6);
                                    peerListe.add(0, new PeerObject(msg));
                                    outToClient.write(entryResponeMsg(outToClient));
                                    break;
                            }
                        }

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

    private byte[] entryResponeMsg(DataOutputStream outToClient) {

        byte[] msg = new byte[26];
        msg[0] = 2;
        msg[1] = 0;

        for (int i = 0; i < 4; i++) {

            PeerObject o = null;

            if (peerListe.size() > i + 1)
                o = peerListe.get(i); //TODO Abfangen wenn index nicht vorhanden

            byte[] ip = {0, 0, 0, 0};
            byte[] port = {0, 0};
            if (o != null) {
                ip = o.getIp();
                port = o.getPort();
            }
            Utilities.packIpPackage(msg, i * 6 + 2, ip, port);
        }

        return msg;
    }

    public static void main(String[] args) {

        Server s = new Server();
        s.startServer();
    }
}
