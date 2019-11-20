import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Server extends Application {

    private Gui gui;
    private ArrayList<PeerObject> peerList;
    private Thread cleanPeerList;
    private boolean exit;

    public Server() {
        peerList = new ArrayList<>();
        Variables.putObject("syn_object", this);
        gui = new Gui("Server " + Utilities.getMyIpAsString() + ":" + Utilities.getServerPort(), null, this);
    }

    public void startServer() {

        try {
            ServerSocket serverSocket = new ServerSocket(Utilities.getServerPort());
            System.out.println("Server gestartet");
            Utilities.printMyIp();

            cleanPeerList = new Thread(() -> {
                try {
                    while (true) {
                        long grenzwert = new Date().getTime() - Variables.getIntValue("time_server_max_without_keep_alive");
                        ArrayList<PeerObject> deleteList = new ArrayList<>();
                        for (PeerObject p : peerList) {
                            if (p.getTimestamp().getTime() < grenzwert)
                                deleteList.add(p);
                        }
                        SharedCode.deletePeersFromPeerList(peerList, deleteList);
                        Utilities.printPeerList(gui, peerList);
                        Thread.sleep(Variables.getIntValue("time_serverlist_clean"));

                        if (exit)
                            return;
                    }
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
                                case 5:
                                    msg = new byte[6];
                                    msgErr = inFromClient.read(msg, 0, 6);
                                    SharedCode.addPeer(peerList, new PeerObject(msg));
                                    if (tag == 1)
                                        outToClient.write(SharedCode.responeMsg(peerList, 2));
                                    break;
                                default:
                                    Utilities.switchDefault();
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

    public void exit() {
        exit = true;
    }

    public static void main(String[] args) {
        Server s = new Server();
        s.startServer();
    }

}
