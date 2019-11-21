import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;


public class Peer extends Application {

    private PeerObject entryServer;
    private ArrayList<PeerObject> peerList;
    private Gui gui;
    private int myPort;
    private Thread keepAlive;
    private boolean exit = false;

    public Peer() {
        init(Utilities.getStandardPeerPort(), null);
    }

    public Peer(int port) {
        init(port, null);
    }

    public Peer(int port, Point location) {
        init(port, location);
    }

    private void init(int port, Point location) {
        peerList = new ArrayList<>();
        myPort = port;
        Variables.putObject("syn_object", this);
        gui = new Gui("Peer " + Utilities.getMyIpAsString() + ":" + port, location, this);
    }

    public void setMyPort(int myPort) {
        Variables.putInt("peer_port", myPort);
    }

    public void startPeer() {
        try {

            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

            entryServer = new PeerObject(Utilities.getServerIpAsByteArray(), Utilities.getServerPortAsByteArray());

            System.out.println("Client gestartet");
            Utilities.printMyIp();

            while (!sendMyIp(entryServer, 1)) {
                System.err.println("EntryServer konnte nicht erreicht werden\nVersuche es erneut...");
                Thread.sleep(1000);
            }

            byte[] entyResponeMessage = new byte[26];
            int msgErr;
            msgErr = entryServer.getInFromPeerStream().read(entyResponeMessage, 0, 26);
            processResponeMessage(entyResponeMessage);
            entryServer.closeStreams();

            AnzeigeThread at = new AnzeigeThread(gui, peerList);
            at.start();

            keepAlive = new Thread(() -> {
                while (true) {
                    try {
                        sendKeepAlive();
                        Thread.sleep(Variables.getIntValue("time_send_keep_alive"));

                        if (exit)
                            return;
                    } catch (Exception e) {
                        Utilities.errorMessage(e);
                    }
                }
            });
            keepAlive.start();

            ServerSocket myServer = new ServerSocket(myPort);

            while (true) {

                Socket connectionSocket = myServer.accept();

                Thread t = new Thread(() -> {

                    try {

                        InputStream inFromPeer = connectionSocket.getInputStream();
                        OutputStream outToPeer = connectionSocket.getOutputStream();

                        int msgErr2;
                        byte[] msg = new byte[1];

                        msgErr2 = inFromPeer.read(msg, 0, 1);
                        int tag = msg[0];

                        msgErr2 = inFromPeer.read(msg, 0, 1);
                        int version = msg[0];

                        if (version == 0) {

                            switch (tag) {
                                case 3:
                                    msg = new byte[6];
                                    msgErr2 = inFromPeer.read(msg, 0, 6);
                                    PeerObject p = new PeerObject(msg);
                                    SharedCode.addPeer(peerList, p);
                                    p.getOutToPeerStream().write(SharedCode.responeMsg(peerList, 4));
                                    break;
                                case 4:
                                    msg = new byte[26];
                                    msg[0] = (byte) tag;
                                    msg[1] = (byte) version;
                                    msgErr2 = inFromPeer.read(msg, 2, 24);
                                    processResponeMessage(msg);
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

    private void sendKeepAlive() {

        try {
            byte[] msg = new byte[8];
            msg[0] = 5;
            msg[1] = 0;
            SharedCode.packIpPackage(msg, 2, Utilities.getMyIpAsByteArray(), Utilities.convertPortToByteArray(myPort));
            entryServer.getOutToPeerStream().write(msg);
            entryServer.closeStreams();
        } catch (Exception e) {
            Utilities.errorMessage(e);
        }
    }

    private void processResponeMessage(byte[] entyResponeMessage) {

        int tag = entyResponeMessage[0];
        int version = entyResponeMessage[1];

        if (!(tag == 2 || tag == 4)) {
            Utilities.fehlermeldungBenutzerdefiniert("Fehlerhafter tag übergeben: " + tag);
            return;
        }

        if (!(version == 0)) {
            Utilities.fehlermeldungVersion();
            return;
        }

        for (int i = 2; i < entyResponeMessage.length; i += Utilities.getIpPackLength()) {

            byte[] ipPack = Arrays.copyOfRange(entyResponeMessage, i, i + Utilities.getIpPackLength());
            if (!Utilities.isArrayEmty(ipPack)) {

                PeerObject po = new PeerObject(ipPack);

                if (tag == 4 || sendMyIp(po, 3))
                    SharedCode.addPeer(peerList, po);

                po.closeStreams();
            }
        }
    }

    private boolean sendMyIp(PeerObject po, int tag) {

        try {
            byte[] entryMsg = new byte[8];
            entryMsg[0] = (byte) tag; //Tag
            entryMsg[1] = 0; //Version
            SharedCode.packIpPackage(entryMsg, 2, Utilities.getMyIpAsByteArray(), Utilities.convertPortToByteArray(myPort));
            po.getOutToPeerStream().write(entryMsg);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void exit() {
        exit = true;
    }

    public static void main(String[] args) {
        Peer p = new Peer();
        p.startPeer();
    }
}
