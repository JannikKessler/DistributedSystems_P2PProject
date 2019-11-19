import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;


public class Peer {

    private final int IPPACKLENGTH = 6;
    private PeerObject entryServer;
    private ArrayList<PeerObject> peerListe;

    public Peer() {
        peerListe = new ArrayList<>();
    }

    private void startPeer() {
        try {

            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

            InetAddress inetAddress = InetAddress.getLocalHost(); //Eventuel muss ausgetauscht werden durch Server IP
            entryServer = new PeerObject(inetAddress.getAddress(), Utilities.charToByteArray(Utilities.getServerPort()));

            System.out.println("Client gestartet");
            Utilities.printMyIp();

            sendMyIp(entryServer, 1);

            byte[] entyResponeMessage = new byte[26];
            int msgErr;
            msgErr = entryServer.getInFromPeerStream().read(entyResponeMessage, 0, 26);
            processEntryResponeMessage(entyResponeMessage);
            entryServer.closeStreams();

            Thread keepAlive = new Thread(() -> {
                while (true) {
                    try {
                        sendKeepAlive();
                        Thread.sleep(30000);
                    } catch (Exception e) {
                        Utilities.errorMessage(e);
                    }
                }
            });
            keepAlive.start();

            ServerSocket myServer = new ServerSocket(Utilities.getPeerPort());
            myServer.setReuseAddress(true);

            while (true) {

                Socket connectionSocket = myServer.accept();

                Thread t = new Thread(() -> {

                    try {

                        InputStream inFromPeer = connectionSocket.getInputStream();
                        OutputStream outToPeer = connectionSocket.getOutputStream();

                        int msgErr2;
                        byte[] msg = new byte[1];

                        msgErr2 = inFromPeer.read(msg, 0, 1);
                        char tag = Utilities.byteArrayToChar(msg);

                        msgErr2 = inFromPeer.read(msg, 0, 1);
                        char version = Utilities.byteArrayToChar(msg);

                        if (version == 0) {

                            switch (tag) {
                                case 3:
                                    msg = new byte[6];
                                    msgErr2 = inFromPeer.read(msg, 0, 6);
                                    peerListe.add(0, new PeerObject(msg));
                                    outToPeer.write(SharedCode.responeMsg(peerListe, 4));
                                    break;
                            }
                        }

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
            Utilities.packIpPackage(msg, 3, Utilities.getMyIpAsByteArray(), Utilities.getPeerPortAsByteArray());
            entryServer.getOutToPeerStream().write(msg);
            entryServer.closeStreams();
        } catch (Exception e) {
            Utilities.errorMessage(e);
        }
    }

    private void processEntryResponeMessage(byte[] entyResponeMessage) {

        int tag = entyResponeMessage[0];
        int version = entyResponeMessage[1];

        for (int i = 2; i < entyResponeMessage.length; i += IPPACKLENGTH) {

            byte[] ipPack = Arrays.copyOfRange(entyResponeMessage, i, i + IPPACKLENGTH);
            if (!Utilities.isArrayEmty(ipPack)) {
                PeerObject po = new PeerObject(ipPack);
                sendMyIp(po, 3);
                po.closeStreams();
                peerListe.add(po);
            }
        }
    }

    private void sendMyIp(PeerObject po, int tag) {

        try {
            byte[] entryMsg = new byte[8];
            entryMsg[0] = (byte) tag; //Tag
            entryMsg[1] = 0; //Version
            Utilities.packIpPackage(entryMsg, 2, Utilities.getMyIpAsByteArray(), Utilities.getPeerPortAsByteArray());
            po.getOutToPeerStream().write(entryMsg);
        } catch (Exception e) {
            Utilities.errorMessage(e);
        }
    }

    public static void main(String[] args) {
        Peer p = new Peer();
        p.startPeer();
    }


}
