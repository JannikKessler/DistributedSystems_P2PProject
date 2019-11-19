import java.net.Inet4Address;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Utilities {

    public static String getServerIp() {
        return Variables.getStringValue("server_ip");
    }

    public static int getServerPort() {
        return Variables.getIntValue("server_port");
    }

    public static int getPeerPort() {
        return Variables.getIntValue("peer_port");
    }

    public static int getIpPackLength() {
        return Variables.getIntValue("ippack_length");
    }

    public static byte[] getPeerPortAsByteArray() {
        return charToByteArray((char) getPeerPort());
    }

    public static void printMyIp() {
        try {
            System.out.println(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            errorMessage(e);
        }
    }

    public static void errorMessage(Exception e) {
        e.printStackTrace();
    }

    public static void packIpPackage(byte[] destination, int offset, byte[] ip, byte[] port) {

        try {

            for (int i = 0; i < ip.length; i++)
                destination[offset + i] = ip[i];

            for (int i = 0; i < port.length; i++)
                destination[offset + i + ip.length] = port[i];

        } catch (Exception e) {
            errorMessage(e);
        }
    }

    public static byte[] charToByteArray(char i) {

        byte[] result = new byte[2];
        result[0] = (byte) (i >> 8);
        result[1] = (byte) (i);
        return result;
    }

    public static char byteArrayToChar(byte[] array) {
        return (char) (array[1] | array[0] << 8);
    }

    public static void printByteArray(byte[] array) {

        for (int i = 0; i < array.length; i++) {
            System.out.print(String.format("%8s", Integer.toBinaryString(array[i] & 0xFF)).replace(' ', '0') + " ");
        }
        System.out.println();
    }

    public static boolean isArrayEmty(byte[] array) {

        for (int i = 0; i < array.length; i++) {

            if (array[i] != 0)
                return false;
        }

        return true;
    }

    public static InetAddress getInetAdress(byte[] ip) {
        try {
            return InetAddress.getByAddress(ip);
        } catch (Exception e) {
            errorMessage(e);
        }
        return null;
    }

    public static byte[] getMyIpAsByteArray() {
        try {
            return InetAddress.getLocalHost().getAddress();
        } catch (Exception e) {
            errorMessage(e);
        }
        return null;
    }

    public static int getPortFromByteArray() {
        return 0;
    }

    public static void printPeerList(ArrayList<PeerObject> peerListe) {

        System.out.println("Ausdruck PeerListe:");
        for (PeerObject p : peerListe) {
            System.out.println("Peer " + (peerListe.indexOf(p) + 1) + ": " + byteArrayToIp(p.getIp()));
        }
        System.out.println();
    }

    private static String byteArrayToIp(byte[] ipAsByteArray) {
        try {
            InetAddress i = Inet4Address.getByAddress(ipAsByteArray);
            return i.getHostAddress();
        } catch (Exception e) {
            errorMessage(e);
        }
        return null;
    }

    public static void switchDefault() {
        System.err.println("Im Switch wurde ein falscher Parameter übergeben");
    }

    public static void printTimestamp(long timestamp) {

        SimpleDateFormat s = new SimpleDateFormat("d.M.y H:m:s S");
        System.out.println(s.format(new Date(timestamp)));
    }

    public static void fehlermeldungBenutzerdefiniert(String s) {
        System.err.println(s);
    }

    public static void fehlermeldungVersion() {
        fehlermeldungBenutzerdefiniert("Falsche Version übergeben");
    }
}
