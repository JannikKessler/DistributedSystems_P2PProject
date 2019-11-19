import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

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

            Thread cleanPeerList = new Thread(() -> {
                try {
                    long grenzwert = new Date().getTime() - 60000;
                    ArrayList<PeerObject> delete = new ArrayList<>();
                    for (PeerObject p : peerListe) {
                        if (p.getTimestamp().getTime() < grenzwert)
                            delete.add(p);
                    }
                    Utilities.deletePeers(peerListe, delete);
                    Thread.sleep(50000);
                } catch (Exception e) {
                    Utilities.errorMessage(e);
                }
            });
            cleanPeerList.start();

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
                                    addPeer(new PeerObject(msg));
                                    outToClient.write(SharedCode.responeMsg(peerListe, 3));
                                    break;
                                case 5:
                                    msg = new byte[6];
                                    msgErr = inFromClient.read(msg, 0, 6);
                                    addPeer(new PeerObject(msg));
                                    break;
                                default:
                                    System.err.println("Es wurde ein falscher Tag übergeben");
                            }
                        }

                        connectionSocket.close();

                    } catch (Exception ioe) {
                        Utilities.errorMessage(ioe);
                    }
                });
                t.start();
            }

        } catch (Exception e) {
            Utilities.errorMessage(e);
        }
    }

    private void addPeer(PeerObject peerObject) {
        for (PeerObject p : peerListe) {
            if (p.getIp() == peerObject.getIp()) {
                peerObject.updateTimestamp();
            }
        }
        peerListe.add(0, peerObject);
    }

    public static void main(String[] args) {
        Server s = new Server();
        s.startServer();
    }
}
