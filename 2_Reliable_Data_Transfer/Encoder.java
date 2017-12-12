import java.io.*;
public class Encoder {
	public byte[] encode(Packet p) throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buf);
		
		out.writeInt(p.getSeqNum());
		out.writeInt(p.getAck());
		out.writeInt(p.getCheckSum());
		out.writeByte(p.getSYN());
		out.writeByte(p.getFIN());
		
		//write the length of the actual data
		out.writeInt(p.getData().length);
		
		out.write(p.getData());
		
		out.flush();
		return buf.toByteArray();
	}
}
