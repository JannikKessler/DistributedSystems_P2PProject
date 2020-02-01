import javax.swing.*;
import java.awt.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Random;

public class Utilities {

    public static String getServerIp() {
        return Variables.getStringValue("server_ip");
    }

    public static void setServerIp(String serverip) {
        Variables.putObject("server_ip", serverip);
    }

    public static int getStandardPort() {
        return Variables.getIntValue("standard_port");
    }

    public static int getPeerPackLength() {
        return Variables.getIntValue("peer_pack_length");
    }

    public static InetAddress getInetAdressFromString(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (Exception e) {
            staticErrorMessage(e);
        }
        return null;
    }

    public static String byteArrayToIp(byte[] ipAsByteArray) {
        try {
            InetAddress i = Inet4Address.getByAddress(ipAsByteArray);
            return i.getHostAddress();
        } catch (Exception e) {
            staticErrorMessage(e);
        }
        return null;
    }

    public static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            staticErrorMessage(new IllegalArgumentException("max must be greater than min"));
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public static void staticErrorMessage(Exception e) {

        if (isShowGui())
            JOptionPane.showMessageDialog(null, e.toString(), "Fehler", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    public static InetAddress getInetAdressFromByteArray(byte[] ip) {
        try {
            return InetAddress.getByAddress(ip);
        } catch (Exception e) {
            staticErrorMessage(e);
        }
        return null;
    }

    public static byte[] longToByteArray(long msec) {
        byte[] tm = new byte[8];
        for (int i = 0, cnt = 56; i < tm.length; i++, cnt -= 8)
            tm[i] = (byte) (msec >> cnt);
        return tm;
    }

    public static long byteArrayToLong(byte[] bytes) {

        if (bytes.length == 8) {
            long result = 0;
            for (int i = 0; i < 8; i++) {
                result <<= 8;
                result |= (bytes[i] & 0xFF);
            }
            return result;
        } else {
            staticErrorMessage(new Exception("Es wurde keine ByteArray übergeben, der in einen Long umgewandelt werden könnte"));
            return -1;
        }
    }

    public static byte[] charToByteArray(char i) {

        byte[] result = new byte[2];
        result[0] = (byte) (i >> 8);
        result[1] = (byte) (i);
        return result;
    }

    public static int byteArrayToCharToInt(byte[] bytes) {
        return
            ((bytes[0] & 0xFF) << 8) |
                ((bytes[1] & 0xFF));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isArrayEmty(byte[] array) {

        for (byte b : array) {

            if (b != 0)
                return false;
        }
        return true;
    }

    public static int getScreenUpdateTime() {
        return Variables.getIntValue("screen_update_time");
    }

    public static byte[] addAll(final byte[] array1, byte[] array2) {
        byte[] joinedArray = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    public static Dimension getGuiSize() {
        return (Dimension) Variables.getObject("gui_size");
    }

    public static Font getNormalFont() {
        return (Font) Variables.getObject("font");
    }

    public static Font getHeadlineFont() {
        return (Font) Variables.getObject("font_headline");
    }

    public static Font getHeadlineFontThin() {
        return (Font) Variables.getObject("font_headline_thin");
    }

    public static Dimension getScreenDimension() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    public static byte[] getStandardPortAsByteArray() {
        return charToByteArray((char) getStandardPort());
    }

    public static void setShowGui(boolean showGui) {
        Variables.putObject("show_gui", showGui);
    }

    public static boolean isShowGui() {
        return (Boolean) Variables.getObject("show_gui");
    }
}
