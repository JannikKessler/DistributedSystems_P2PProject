import java.util.Arrays;

public class PeerObject {

    private byte[] port = new byte[2];
    private byte[] ip = new byte[4];

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

    public byte[] getPort() {
        return port;
    }

    public byte[] getIp() {
        return ip;
    }
}
