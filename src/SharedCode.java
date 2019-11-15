import java.util.ArrayList;

public class SharedCode {

    public static byte[] responeMsg(ArrayList<PeerObject> peerListe, int tag) {

        byte[] msg = new byte[26];
        msg[0] = (byte) tag; //Tag
        msg[1] = 0; //Version

        for (int i = 0; i < 4; i++) {

            PeerObject o = null;

            if (peerListe.size() > i+1)
                o = peerListe.get(i+1); //TODO Abfangen wenn index nicht vorhanden

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
}
