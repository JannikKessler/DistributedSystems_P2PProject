import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class PeerObject {

    private byte[] port = new byte[2];
    private byte[] ip = new byte[4];
    private Socket socket;

    public PeerObject(byte[] msg) {

        ip = Arrays.copyOfRange(msg, 0, 4);
        port = Arrays.copyOfRange(msg, 4, 6);
        Utilities.printByteArray(ip);
        Utilities.printByteArray(port);
    }

    public PeerObject(byte[] ip, byte[] port) {
        this.ip = ip;
        this.port = port;
    }

    public OutputStream getOutToPeerStream() {

        try {
            if (isSocketClosed())
                socket = new Socket(Utilities.getInetAdress(ip), Utilities.byteArrayToInt(port));
            return socket.getOutputStream();
        } catch (Exception e) {
            Utilities.errorMessage(e);
        }
        return null;
    }

    private boolean isSocketClosed() {

        return (socket == null || socket.isClosed());
    }

    public InputStream getInFromPeerStream() {

        try {
            if (isSocketClosed())
                socket = new Socket(Utilities.getInetAdress(ip), Utilities.byteArrayToInt(port));
            return socket.getInputStream();
        } catch (Exception e) {
            Utilities.errorMessage(e);
        }
        return null;
    }

    public byte[] getPort() {
        return port;
    }

    public byte[] getIp() {
        return ip;
    }
}
