import java.nio.*;

public class Packet {
	
	//max size 1000 byte;
	private int seq_num;  //sequence number, used for client ack as well
	private int ack;  //acknumber 
	private int checkSum;
	private byte SYN = 0;
	private byte FIN = 0;
	private byte[] data;  //actual data; size should be 1000 - 4 - 4 -4 - 2 - 2 -4  = 980
	public Packet(int seq, int ack, byte[] input) {
		//constructor, with sequence number and byte[] data;
		seq_num = seq;
		this.ack = ack;
		data = input;

	}
	int getAck() {
		return ack;
	}
	void setAck(int ack) {
		this.ack = ack;
	}
	void setSYN(byte SYN) {
		this.SYN = SYN;
	}
	
	void setFIN(byte FIN) {
		this.FIN = FIN;
	}
	void setCheckSum(int checksum) {
		this.checkSum = checksum;
	}
	int getCheckSum() {
		return checkSum;
	}
	
	int getSeqNum() {
		return seq_num;
	}
	
	byte getSYN() {
		return SYN;
	}
	
	byte getFIN() {
		return FIN;
	}
	
	byte[] getData() {
		return data;
	}
	
	public String toString() {
		
		String result = "Sequence Number: " + seq_num;
		String text = new String(data);
		return result + "  " +  text;
	}
	
	
	public void updateCheckSum() {
		int sum = 0;
		for (byte word : data) {
			sum += word;
		}
		sum += SYN;
		sum += FIN;
		for (byte word: ByteBuffer.allocate(4).putInt(seq_num).array()) {
			sum += word;
		}
		checkSum = ~sum;
		//System.out.println("in update checksum: " + checkSum);
	}
	
	public int getDataSum() {
		int sum = 0;
		for (byte word : data) {
			sum += word;
		}
		sum += SYN;
		sum += FIN;
		for (byte word: ByteBuffer.allocate(4).putInt(seq_num).array()) {
			sum += word;
		}
		return sum;
	}
	
}
