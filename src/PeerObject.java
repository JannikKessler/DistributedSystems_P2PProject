import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;

public class PeerObject {

    private byte[] port = new byte[2];
    private byte[] ip = new byte[4];
    private byte[] id = new byte[2];
    private Socket socket;
    private Date timestamp;

    public PeerObject() {
    }

    public PeerObject(byte[] msg) {
        initPeerObject(Arrays.copyOfRange(msg, 0, 4),
            Arrays.copyOfRange(msg, 4, 6),
            Arrays.copyOfRange(msg, 6, 8));
    }

    public PeerObject(byte[] ip, byte[] port, byte[] id) {
        initPeerObject(ip, port, id);
    }

    protected void initPeerObject(byte[] ip, byte[] port, byte[] id) {

        this.ip = ip;
        this.port = port;
        this.id = id;
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

    public byte[] getId() {
        return id;
    }

    public int getPortAsInt() {
        return Utilities.byteArrayToChar(port);
    }

    public String getIpAsString() {
        return Utilities.byteArrayToIp(ip);
    }

    public void closeStreams() {
        try {
            if (socket != null && !socket.isClosed())
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

    public void setId(byte[] id) {
        this.id = id;
    }

    public int getIdAsInt() {
        return Utilities.byteArrayToChar(id);
    }

    public String toString() {
        return getIpAsString() + ":" + getPortAsInt() + "; " + getIdAsInt();
    }

    public byte[] getPeerPackage() {

        byte[] msg = new byte[8];

        for (int i = 0; i < ip.length; i++)
            msg[i] = ip[i];

        for (int i = 0; i < port.length; i++)
            msg[i + ip.length] = port[i];

        for (int i = 0; i < id.length; i++)
            msg[i + ip.length + port.length] = id[i];

        return msg;
    }
}
