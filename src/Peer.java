import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;


public class Peer {

    private final int MYPORT = 3333;
    private final int IPPACKLENGTH = 6;
    private ArrayList<PeerObject> peerListe;

    public Peer() {
        peerListe = new ArrayList<>();
    }

    private void startPeer() {
        try {

            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

            Inet4Address inet4Address = (Inet4Address) Inet4Address.getLocalHost(); //Eventuel muss ausgetauscht werden durch Server IP
            System.out.println(inet4Address.getHostAddress());
            PeerObject server = new PeerObject(inet4Address.getAddress(), Utilities.intToByteArray(Utilities.getServerPort()));

            System.out.println("Client gestartet");
            Utilities.printMyIp();

            byte[] entryMsg = new byte[8];
            entryMsg[0] = 1; //Tag
            entryMsg[1] = 0; //Version
            Utilities.packIpPackage(entryMsg, 2, InetAddress.getLocalHost().getAddress(), Utilities.intToByteArray(MYPORT));
            server.getOutToPeerStream().write(entryMsg);

            System.out.println("Test");

            byte[] entyResponeMessage = server.getInFromPeerStream().readAllBytes();
            processEntryResponeMessage(entyResponeMessage);

        } catch (Exception e) {
            Utilities.errorMessage(e);
        }

    }

    private void processEntryResponeMessage(byte[] entyResponeMessage) {

        int tag = entyResponeMessage[0];
        int version = entyResponeMessage[1];

        for (int i = 2; i<entyResponeMessage.length; i+=IPPACKLENGTH) {

            byte[] ipPack = Arrays.copyOfRange(entyResponeMessage, i, i+IPPACKLENGTH);
            if (!Utilities.isArrayEmty(ipPack)) {
                PeerObject po = new PeerObject(ipPack);
                //TODOpo.getOutToPeerStream().write();
                peerListe.add(po);
            }
        }

    }

    private void sendMyIp(PeerObject po, int tag) {

    }

    public static void main(String[] args) {

        Peer p = new Peer();
        p.startPeer();
    }


}
