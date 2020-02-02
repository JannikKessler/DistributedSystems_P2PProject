//Es werden nur Offsets gespeichert!
public class TimeOffsetObject extends PeerObject {

    private byte[] time = new byte[8];

    public TimeOffsetObject(PeerObject p, long offset) {
        initTimeObject(p.getIp(), p.getPort(), p.getId(), Utilities.longToByteArray(offset));
    }

    private void initTimeObject(byte[] ip, byte[] port, byte[] id, byte[] tm) {
        initPeerObject(ip, port, id);
        this.time = tm;
    }

    @SuppressWarnings("unused")
    public byte[] getTime() {
        return this.time;
    }

    public long getTimeAsLong() {
        return Utilities.byteArrayToLong(time);
    }
}
