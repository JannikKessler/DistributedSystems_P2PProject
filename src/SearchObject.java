import java.util.Arrays;
import java.util.Date;

public class SearchObject extends PeerObject {

    private byte[] searchId = new byte[2];
    private byte[] destId = new byte[2];

    //TODO Konstruktoren Implementieren mit ip, port, id, searchid, timestamp

    public SearchObject(byte[] ip, byte[] port, byte[] id, byte[] searchId, byte[] destId) {
        initSearchObject(ip, port, id, searchId, destId);
    }

    public SearchObject(PeerObject p, byte[] nodeSearchMsg) {

        initSearchObject(
            p.getIp(),
            p.getPort(),
            p.getId(),
            Arrays.copyOfRange(nodeSearchMsg, 8, 10),
            Arrays.copyOfRange(nodeSearchMsg, 10, 12));

        //System.out.println(toString());
    }

    private void initSearchObject(byte[] ip, byte[] port, byte[] id, byte[] searchId, byte[] destId) {

        initPeerObject(ip, port, id);
        this.searchId = searchId;
        this.destId = destId;
    }

    public byte[] getDestId() {
        return destId;
    }

    public int getDestIdAsInt() {
        return Utilities.byteArrayToChar(destId);
    }

    public byte[] getSearchId() {
        return searchId;
    }

    public int getSearchIdAsInt() {
        return Utilities.byteArrayToChar(searchId);
    }

    public String toString() {
        return super.toString() + "; SearchID: " + getSearchIdAsInt() + "; DestID: " + getDestIdAsInt();
    }
}
