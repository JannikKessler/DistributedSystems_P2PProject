import java.sql.Date;

public class TimeObject extends PeerObject {

	private byte[] time = new byte[8];
	private Date date;
	
	public TimeObject(byte[] ip, byte[] port, byte[] id) {
		date = new Date(0);//Bei mir meckert der Compiler wenn ich keinen Parameter angebe.
		byte[] tm = longToByteArr(date.getTime());
		initTimeObject(ip, port, id, tm);
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
		long t = 0;
		int cnt = 56;
		for (int i = 0; i < 8; i++) {
			t += (this.time[i] << cnt);
			cnt -= 8;
		}
		return t;
	}
		
	public byte[] longToByteArr(long msec) {
		byte[] tm = new byte[8];
		int cnt = 56;
		for (int i = 0; i < tm.length; i++) {
			tm[i] = (byte) (msec >> cnt);
			cnt -= 8;
		}		
		return tm;
	}
}
