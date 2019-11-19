import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;


public class Peer {

    private PeerObject entryServer;
    private ArrayList<PeerObject> peerList;

    public Peer() {
        peerList = new ArrayList<>();
        Variables.putObject("syn_object", this);
    }

    private void startPeer() {
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

            Thread keepAlive = new Thread(() -> {
                while (true) {
                    try {
                        sendKeepAlive();
                        Utilities.printPeerList(peerList);
                        Thread.sleep(Variables.getIntValue("time_send_keep_alive"));
                    } catch (Exception e) {
                        Utilities.errorMessage(e);
                    }
                }
            });
            keepAlive.start();

            ServerSocket myServer = new ServerSocket(Utilities.getPeerPort());

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
                                    SharedCode.modifyPeerList(peerList, SharedCode.INSERT, p);
                                    p.getOutToPeerStream().write(SharedCode.responeMsg(peerList, 4));
                                    break;
                                case 4:
                                    msg = new byte[26];
                                    msgErr2 = inFromPeer.read(msg, 0, 26);
                                    processResponeMessage(msg);
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
            SharedCode.packIpPackage(msg, 2, Utilities.getMyIpAsByteArray(), Utilities.getPeerPortAsByteArray());
            entryServer.getOutToPeerStream().write(msg);
            entryServer.closeStreams();
        } catch (Exception e) {
            //Utilities.errorMessage(e); Fehlermeldungen werden hier nicht ausgegeben, da KeepAlive
            //sowieso durchgängig gesendet wird; da darf auch ein Packet mal nicht ankommen
        }
    }

    private void processResponeMessage(byte[] entyResponeMessage) {

        int tag = entyResponeMessage[0];
        int version = entyResponeMessage[1];

        if (!(tag == 3 || tag == 4)) {
            Utilities.fehlermeldungBenutzerdefiniert("Fehlerhafter tag übergeben");
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
                    SharedCode.modifyPeerList(peerList, SharedCode.INSERT, po);
                po.closeStreams();
                peerList.add(po);
            }
        }
    }

    private boolean sendMyIp(PeerObject po, int tag) {

        try {
            byte[] entryMsg = new byte[8];
            entryMsg[0] = (byte) tag; //Tag
            entryMsg[1] = 0; //Version
            SharedCode.packIpPackage(entryMsg, 2, Utilities.getMyIpAsByteArray(), Utilities.getPeerPortAsByteArray());
            po.getOutToPeerStream().write(entryMsg);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        Peer p = new Peer();
        p.startPeer();
    }


}
