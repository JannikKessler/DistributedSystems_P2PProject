import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;

public class PeerObject {

    private byte[] port = new byte[2];
    private byte[] ip = new byte[4];
    private Socket socket;
    private Date timestamp;

    public PeerObject(byte[] msg) {

        ip = Arrays.copyOfRange(msg, 0, 4);
        port = Arrays.copyOfRange(msg, 4, 6);
        timestamp = new Date();
    }

    public PeerObject(byte[] ip, byte[] port) {
        this.ip = ip;
        this.port = port;
        timestamp = new Date();
    }

    public OutputStream getOutToPeerStream() throws Exception {

        if (isSocketClosed())
            createSocket();
        return socket.getOutputStream();
    }

    private void createSocket() throws Exception {

        socket = new Socket(Utilities.getInetAdressFromByteArray(ip), Utilities.byteArrayToChar(port));
    }

    private boolean isSocketClosed() {

        return (socket == null || socket.isClosed());
    }

    public InputStream getInFromPeerStream() {

        try {
            if (isSocketClosed())
                createSocket();
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

    public void closeStreams() {
        try {
            socket.close();
        } catch (Exception e) {
            Utilities.errorMessage(e);
        }
    }

    public void updateTimestamp() {
        timestamp = new Date();
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
