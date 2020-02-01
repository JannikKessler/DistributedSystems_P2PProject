//Es werden nur Offsets gespeichert!
public class TimeObject extends PeerObject {

    private byte[] time = new byte[8];

    public TimeObject(PeerObject p, long time) {
        initTimeObject(p.getIp(), p.getPort(), p.getId(), Utilities.longToByteArray(time));
    }

    public TimeObject(byte[] ip, byte[] port, byte[] id, byte[] tm) {
        initTimeObject(ip, port, id, tm);
    }

    private void initTimeObject(byte[] ip, byte[] port, byte[] id, byte[] tm) {
        initPeerObject(ip, port, id);
        this.time = tm;
    }

    public byte[] getTime() {
        return this.time;
    }

    public long getTimeAsLong() {
        return Utilities.byteArrayToLong(time);
    }
}
