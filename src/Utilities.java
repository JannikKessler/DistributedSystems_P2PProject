import java.net.Inet4Address;
import java.net.InetAddress;

public class Utilities {

    public static String getServerIp() {
        return "localhost";
    }

    public static int getServerPort() {
        return 3333;
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

    public static byte[] intToByteArray(int i) {

        byte[] result = new byte[2];
        result[0] = (byte) (i >> 8);
        result[1] = (byte) (i);
        return result;
    }

    public static int byteArrayToInt(byte[] array) {

        int res = 0;
        for (int i = 0; i < array.length; i++) {
            res = (res * 10) + (array[i] & 0xFF);
        }
        return res;
    }

    public static void printByteArray(byte[] array) {

        for (int i = 0; i < array.length; i++) {
            System.out.print(String.format("%8s", Integer.toBinaryString(array[i] & 0xFF)).replace(' ', '0') + " ");
        }
        System.out.println();
    }

    public static boolean isArrayEmty(byte[] array) {

        for (int i = 0; i<array.length; i++) {

            if (array[i] !=0)
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

    public static int getPortFromByteArray() {
        return 0;
    }
}
