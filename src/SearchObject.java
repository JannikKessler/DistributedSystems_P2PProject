import java.util.Date;

public class SearchObject extends PeerObject {

	private byte[] port = new byte[2];
    private byte[] ip = new byte[4];
    private byte[] id = new byte[2];
    private byte[] searchId = new byte[2];
    private byte[] destId = new byte[2];
    private Date timestamp;

    //TODO Konstruktoren Implementieren mit ip, port, id, searchid, timestamp
    
    public SearchObject(byte[] ip, byte[] port, byte[] id, byte[] searchId, byte[] destId) {
    	for (int i = 0; i < port.length; i++)
    		this.port[i] = port[i];
    	for (int j = 0; j < ip.length; j++)
    		this.ip[j] = ip[j];
    	for (int k = 0; k < id.length; k++)
    		this.id[k] = id[k];
    	for (int l = 0; l < searchId.length; l++)
    		this.searchId[l] = searchId[l];
    	for (int m = 0; m < destId.length; m++)
    		this.destId[m] = destId[m];
    }
    
    public SearchObject(byte[] nodeSearchMsg) {
    	int offset = 0;
    	for (int i = 0; i < ip.length; i++) {
    		this.ip[i] = nodeSearchMsg[i];
    		offset++;
    	}
    	for (int j = 0; j < port.length; j++) {
    		this.port[j] = nodeSearchMsg[j + offset];
    		offset++;
    	}
    	for (int k = 0; k < id.length; k++) {
    		this.id[k] = nodeSearchMsg[k + offset];
    		offset++;
    	}
    	for (int l = 0; l < searchId.length; l++) {
    		this.searchId[l] = nodeSearchMsg[l + offset];
    		offset++;
    	}
    	for (int m = 0; m < destId.length; m++) {
    		this.destId[m] = nodeSearchMsg[m + offset];
    		offset++;
    	}
    }
    
    public byte[] getDestId() {
    	return destId;
    }
    
    public byte[] getSearchId() {
    	return searchId;
    }
}
