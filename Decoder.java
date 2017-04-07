import java.io.*;
import java.net.DatagramPacket;
public class Decoder {

	
	public Packet decode(DatagramPacket p) throws IOException {

		ByteArrayInputStream wire = new ByteArrayInputStream(p.getData(), p.getOffset(), p.getLength());
		DataInputStream src = new DataInputStream(wire);
		int seq = src.readInt();
		int ack = src.readInt();
		int checksum = src.readInt();
		byte SYN = src.readByte();
		byte FIN = src.readByte();
		int dataLength = src.readInt();
		byte[] stringBuffer = new byte[dataLength];
		src.readFully(stringBuffer);
		Packet packet = new Packet(seq, ack, stringBuffer);
		packet.setSYN(SYN);
		packet.setFIN(FIN);
		packet.setCheckSum(checksum);
		
		return packet;
	}
}
