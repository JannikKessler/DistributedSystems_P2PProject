import java.net.ConnectException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class PeerUtilities extends Utilities {

    private Peer i;
    private SimpleDateFormat df;

    public PeerUtilities(Peer i) {
        this.i = i;
        df = new SimpleDateFormat("HH:mm:ss");
    }

    public String getMyIpAsString() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            errorMessage(e);
        }
        return null;
    }

    public void printMyIp() {
        printLogInformation(getMyIpAsString());
    }

    public void errorMessage(Exception e) {
        printLogInformation(e.getLocalizedMessage());
        for (StackTraceElement s : e.getStackTrace()) {
            printLogInformation("" + s);
        }
    }

    @SuppressWarnings("unused")
    public void printByteArrayAsBinaryCode(byte[] array) {
        StringBuilder s = new StringBuilder();
        for (byte b : array) {
            s.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0')).append(" ");
        }
        printLogInformation(s.toString());
    }

    public byte[] getMyIpAsByteArray() {
        try {
            return InetAddress.getLocalHost().getAddress();
        } catch (Exception e) {
            errorMessage(e);
        }
        return null;
    }

    public void printPeerList(Peer i, boolean printOnConsole) {
        if (isShowGui())
            i.getGui().setPeerList(i.getPeerList());
        if (printOnConsole)
            printLogInformation("Anzahl Peers in der Liste: " + i.getPeerList().size());
    }

    public void switchDefault() {
        printLogInformation("Im Switch wurde ein falscher Parameter übergeben");
    }

    public byte[] getServerIpAsByteArray() {
        return Objects.requireNonNull(getInetAdressFromString(getServerIp())).getAddress();
    }

    public void modificationException(@SuppressWarnings("unused") Exception e) {
        printLogInformation("Modification-Exception");
        //errorMessage(e);
    }

    private String getTimeStamp() {
        return "(" + df.format(new Date()) + ")";
    }

    private void printOnGuiMsgPanel(String s) {
        Gui g = i.getGui();
        if (isShowGui() && g != null)
            g.addTextToChat(s);
    }

    private void printOnGuiLogPanel(String s) {
        Gui g = i.getGui();
        if (isShowGui() && g != null)
            g.addTextToConsole(getTimeStamp() + " " + s);
    }

    private void printOnConsole(String s) {
        System.out.println(i.getMyPeer().getIdAsInt() + ": " + getTimeStamp() + " " + s);
    }

    //Consolenausgaben; i = mein Peer-Objekt
    public void printLogInformation(String s) {
        printOnGuiLogPanel(s);
        printOnConsole(s);
    }

    public void printMsg(String s) {
        printOnGuiMsgPanel(s);
        printOnConsole(s);
    }

    public void printMetaInfo(PeerObject from, int tag, int version) {
        String s = "[Von ID " + from.getIdAsInt() + "] Tag " + tag + " in Version " + version + " erhalten";
        printOnGuiLogPanel(s);
        printOnConsole(s);
    }

    public void setLeader(int id) {
        printLogInformation("Leader ist " + id);
        if (isShowGui())
            i.getGui().setLeaderId(id);
    }

    public void connectException(@SuppressWarnings("unused") ConnectException c) {
        printLogInformation("Connection refused: connect");
        //errorMessage(c);
    }
}
