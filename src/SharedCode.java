import java.util.ArrayList;

public class SharedCode {

    public static final int INSERT = 1;
    public static final int REMOVE = 2;

    public static byte[] responeMsg(ArrayList<PeerObject> peerListe, int tag) {

        byte[] msg = new byte[26];
        msg[0] = (byte) tag; //Tag
        msg[1] = 0; //Version

        for (int i = 0; i < 4; i++) {

            PeerObject o = null;

            if (peerListe.size() > i + 1)
                o = peerListe.get(i + 1);

            byte[] ip = {0, 0, 0, 0};
            byte[] port = {0, 0};
            if (o != null) {
                ip = o.getIp();
                port = o.getPort();
            }
            packIpPackage(msg, i * 6 + 2, ip, port);
        }

        return msg;
    }

    public static void packIpPackage(byte[] destination, int offset, byte[] ip, byte[] port) {

        try {

            for (int i = 0; i < ip.length; i++)
                destination[offset + i] = ip[i];

            for (int i = 0; i < port.length; i++)
                destination[offset + i + ip.length] = port[i];

        } catch (Exception e) {
            Utilities.errorMessage(e);
        }
    }

    public static void deletePeersFromPeerList(ArrayList<PeerObject> peerListe, ArrayList<PeerObject> delete) {

        for (PeerObject p : delete) {
            modifyPeerList(peerListe, REMOVE, p);
        }
    }

    public static void modifyPeerList(ArrayList<PeerObject> peerList, int action, PeerObject insertORemove) {

        synchronized (Variables.getObject("syn_object")) {
            switch (action) {
                case INSERT:
                    peerList.add(0, insertORemove);
                    break;
                case REMOVE:
                    peerList.remove(insertORemove);
                    break;
                default:
                    Utilities.switchDefault();
            }

        }

    }
}
