import java.util.Arrays;

public class SearchObject extends PeerObject {

    private byte[] searchId = new byte[2];
    private byte[] destId = new byte[2];

    public SearchObject(byte[] ip, byte[] port, byte[] id, byte[] searchId, byte[] destId) {
        initSearchObject(ip, port, id, searchId, destId);
    }

    public SearchObject(PeerObject p, byte[] nodeSearchMsg) {

        initSearchObject(
            p.getIp(),
            p.getPort(),
            p.getId(),
            Arrays.copyOfRange(nodeSearchMsg, 0, 2),
            Arrays.copyOfRange(nodeSearchMsg, 2, 4));
    }

    private void initSearchObject(byte[] ip, byte[] port, byte[] id, byte[] searchId, byte[] destId) {

        initPeerObject(ip, port, id);
        this.searchId = searchId;
        this.destId = destId;
    }

    @SuppressWarnings("unused")
    public byte[] getDestId() {
        return destId;
    }

    public int getDestIdAsInt() {
        return Utilities.byteArrayToCharToInt(destId);
    }

    public byte[] getSearchId() {
        return searchId;
    }

    public int getSearchIdAsInt() {
        return Utilities.byteArrayToCharToInt(searchId);
    }

    public String toString() {
        return super.toString() + "; SearchID: " + getSearchIdAsInt() + "; DestID: " + getDestIdAsInt();
    }
}
