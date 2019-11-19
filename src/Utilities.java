import java.net.Inet4Address;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    public static byte[] getServerPortAsByteArray() {
        return charToByteArray((char) getServerPort());
    }

    public static void printMyIp() {
        try {
            System.out.println(InetAddress.getLocalHost().getHostAddress() + "\n");
        } catch (Exception e) {
            errorMessage(e);
        }
    }

    public static void errorMessage(Exception e) {
        e.printStackTrace();
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

    public static void printByteArrayAsBinaryCode(byte[] array) {

        for (byte b : array) {
            System.out.print(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0') + " ");
        }
        System.out.println();
    }

    public static boolean isArrayEmty(byte[] array) {

        for (byte b : array) {

            if (b != 0)
                return false;
        }
        return true;
    }

    public static InetAddress getInetAdressFromString(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (Exception e) {
            errorMessage(e);
        }
        return null;
    }

    public static InetAddress getInetAdressFromByteArray(byte[] ip) {
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

    public static byte[] getServerIpAsByteArray() {
        return getInetAdressFromString(getServerIp()).getAddress();
    }
}
