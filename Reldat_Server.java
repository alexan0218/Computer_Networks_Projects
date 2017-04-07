import java.util.ArrayList;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.nio.*;

public class Reldat_Server {

	public static void main(String[] args) throws Exception {
		if (args.length!= 2) {
			throw new IllegalArgumentException("incorrect argument");
		}

		int port = Integer.parseInt(args[0]);
		int max_window = Integer.parseInt(args[1]);
		int currentSeq = 1;
		
		Encoder encoder = new Encoder();
		Decoder decoder = new Decoder();
		
		DatagramSocket sock = new DatagramSocket(port);
		
		State currentState = State.CONNECT;
		DatagramPacket prev = new DatagramPacket(new byte[1000], 1000);
		DatagramPacket fin = new DatagramPacket(new byte[1000], 1000);
		outermost:
		while(true) {
			switch (currentState) {			
			case CONNECT: {
				//hearing mode
				//sock = new DatagramSocket(port);
				currentSeq = 1;
				System.out.println("hearing mode !!!");
				DatagramPacket rec = new DatagramPacket(new byte[1000], 1000);
				sock.setSoTimeout(0);
				//first get the very first connect from server
				while(true) {
					sock.receive(rec);
					
					Packet p = decoder.decode(rec);
					int checksum = p.getCheckSum() + p.getDataSum();
					if (checksum == -1 && p.getSeqNum() == 0 && p.getSYN() == 1) {
						//correct format, should send ack0
						System.out.println("received correct very first packet");
						//sock.bind(rec.getSocketAddress());
						int client_window = Integer.parseInt(new String(p.getData()));
						max_window = Math.min(client_window, max_window);
						break;
					}
				}
				
				System.out.println("now sending ack 0");
				while (true) {
					//System.out.println("in loop, sending first ack0");
					Packet goPacket = new Packet(0,0,Integer.toString(max_window).getBytes());
					goPacket.setSYN((byte)1);
					goPacket.updateCheckSum();
					DatagramPacket ack0 = new DatagramPacket(encoder.encode(goPacket),
							encoder.encode(goPacket).length, rec.getAddress(), rec.getPort());
					sock.send(ack0);
					System.out.println("sent ack0 again");
					sock.setSoTimeout(25000);  //50secs if timeout, sender crashed
					
					DatagramPacket rec2 = new DatagramPacket(new byte[1000], 1000);
					try {
						sock.receive(rec2);
					} catch (InterruptedIOException e){
						throw new Exception("waited for 25 secs, sender crashed");
					}
					
					Packet seq0 = decoder.decode(rec2);

					int checksum = seq0.getCheckSum() + seq0.getDataSum();
					if (checksum == -1 && seq0.getSYN() == 0 && seq0.getSeqNum() == 0) {
						System.out.println("received first packet with actual data");
						//send the modified data back
						String text = new String(seq0.getData());
						byte[] data = text.toUpperCase().getBytes();
						Packet mod1 = new Packet(0, 0, data);
						mod1.updateCheckSum();
						DatagramPacket send0 = new DatagramPacket(encoder.encode(mod1),
								encoder.encode(mod1).length, rec2.getAddress(),rec2.getPort());
						sock.send(send0);
						prev = send0;
						System.out.println("sended back first packet with modified data");
						currentState = State.DATA_TRANSFER;
						break;
					} else {
						//System.out.println("we received a packet but not the one we expect with data");
						//System.out.println(seq0);
					}
					//else it will reenter this while loop
					
				}
				break;

			}
			
			
			case DATA_TRANSFER: {
				System.out.println("in data_transfer mode");
				sock.setSoTimeout(20000); //set 20 secs time out for receive
				DatagramPacket rec = new DatagramPacket(new byte[1000], 1000);
				while (true) {
					try {
						sock.receive(rec);
					} catch (InterruptedIOException e){
						throw new Exception("waited for 10 secs, sender crashed");
					}
					Packet client_data = decoder.decode(rec);
					int checksum = client_data.getCheckSum() + client_data.getDataSum();
					int seq = client_data.getSeqNum();
					int ack = client_data.getAck();
					if (checksum == -1 && client_data.getFIN() == 1 && seq == -1) {
						System.out.println("received FIN request from client");
						currentState = State.CLOSE;
						Packet fin1 = new Packet(-1, -1, "CLOSE_CONNECTION".getBytes());
						fin1.setFIN((byte)1);
						fin1.updateCheckSum();
						fin = new DatagramPacket(encoder.encode(fin1), 
								encoder.encode(fin1).length, rec.getAddress(), rec.getPort());
						break;
					}
					if (checksum == -1) {
						if (seq==currentSeq) {
//							System.out.println("received expected packet with data, seq: " + currentSeq);
//							System.out.println("actual data in this packet is:    !!!!!!!!!!");
//							System.out.println(client_data);
//							System.out.println("................");
							String text = new String(client_data.getData());
							Packet mod = new Packet(seq, seq, text.toUpperCase().getBytes());
							mod.updateCheckSum();
							DatagramPacket send = new DatagramPacket(encoder.encode(mod),
									encoder.encode(mod).length, rec.getAddress(),rec.getPort());
							sock.send(send);
							prev = send;
							currentSeq++;
						} else if (seq < currentSeq) {
							//System.out.println("seq is :  " + seq +  "which is < currentSeq:  " + currentSeq +", already received,");
							String text = new String(client_data.getData());
							Packet mod = new Packet(seq, seq, text.toUpperCase().getBytes());
							mod.updateCheckSum();
							DatagramPacket send = new DatagramPacket(encoder.encode(mod),
									encoder.encode(mod).length, rec.getAddress(),rec.getPort());
							sock.send(send);
							prev = send;

							currentSeq = seq + 1;
//							sock.send(prev);
//							System.out.println("sent previous packet");
							
							
						} else {
							if (seq == ack) {
								//System.out.println("even though out of order, but is"
								// 		+ "the expected one");
								String text = new String(client_data.getData());
								Packet mod = new Packet(seq, seq, text.toUpperCase().getBytes());
								mod.updateCheckSum();
								DatagramPacket send = new DatagramPacket(encoder.encode(mod),
										encoder.encode(mod).length, rec.getAddress(),rec.getPort());
								sock.send(send);
								prev = send;
								currentSeq = seq + 1;
								
							} else {
								//System.out.println("out of order packet:  "
								//		+ seq + "expected seq:  " + currentSeq);
							}

						}
					} else {
						System.out.println("packet corrupted, waiting for retransmit");
						System.out.println("it is corrupted because checksum is: " + checksum);
					}
					
					
					
				}
				
				break;
			}
			
			
			case CLOSE: {
				//
				//System.out.println("entering close state, already received fin request");
				sock.send(fin);
				DatagramPacket rec = new DatagramPacket(new byte[1000], 1000);
				sock.setSoTimeout(3000);
				//need to get the final ack
				boolean received = false;
				try {
					sock.receive(rec);
					received = true;
				} catch (Exception e) {
					//didnt receive anything
					//System.out.println("resending the last ack");
					sock.send(fin);
				}
				if (received) {
					//System.out.println("received something");
					Packet f = decoder.decode(rec);
					int checksum = f.getCheckSum() + f.getDataSum();
					//System.out.println("but checksum is: " + checksum + "and ack: " + f.getSeqNum());
					if (checksum == -1 && f.getFIN() == 1 && f.getSeqNum() == -2) {
						System.out.println("connection closed successfully");
						currentState = State.CONNECT;
						//sock.close();
						System.out.println("successful file transform");
						System.out.println();
						System.out.println();
						//break outermost;
						break;
					}
				}
				
				break;
			}
			
			
			
			
			
			
			}

		}
		
		

	}

}
