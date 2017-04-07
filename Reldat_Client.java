import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Scanner;

public class Reldat_Client {

	public static LinkedList<Packet> updateAck(LinkedList<Packet> window) {
		if (window.isEmpty()) {
			return window;
		}
		int ack = window.get(0).getSeqNum();
		for (Packet p : window) {
			p.setAck(ack);
		}
		return window;
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length != 3) // Test for correct # of args
			throw new IllegalArgumentException("Parameter(s): <Server> <port #> <max window size>");
		
		Scanner reader = new Scanner(System.in);
		
		InetAddress server = InetAddress.getByName(args[0]);// Server name or IP address

		int port = Integer.parseInt(args[1]);  //port number
		int max_window = Integer.parseInt(args[2]);
		//set initial state to connect;
		State currentState = State.CONNECT;
		//buffer to store packet
		LinkedList<Packet> windowBuffer = new LinkedList<>();
		
		//the first packet in the window, initially set to zero for connection initialization;
		int base = 0;
		//initially set the next sequence number to 0
		int nextSeqNum = 0;
		
		Encoder encoder = new Encoder();
		
		Decoder decoder = new Decoder();
		
		//end of file, all file data has been transmitted
		boolean EOF = false;
		
		//ready to send
		
		DatagramSocket sock = new DatagramSocket(); // UDP socket for sending
		
		outerloop:
		while(true) {
			switch (currentState) {
			case CONNECT: {
				//In connection state:
				sock.setSoTimeout(3000);  //time out 3 seconds
				
				//first send a packet with syn bit set as well as the max window size
				byte[] data = Integer.toString(max_window).getBytes();
				Packet initial = new Packet(0,0, data);
				initial.setSYN((byte) 1);
				
				initial.updateCheckSum();
				
				byte[] encoded = encoder.encode(initial);
				
				
				DatagramPacket send_initial = new DatagramPacket(encoded, encoded.length, server, port);
				DatagramPacket rec = new DatagramPacket(new byte[1000],1000);
				boolean receivedReply = false;
				int tries = 0;
				
				do{
					sock.send(send_initial);
					System.out.println("very first packet sent");
					try {
						sock.receive(rec);

						receivedReply = true;
					} catch (InterruptedIOException e) {
						tries++;
						System.out.println("The server has not answered in the last 3 seconds.");
						System.out.println("retrying... " + (3 - tries) + " more tries.");
					}
				} while ((!receivedReply) && (tries < 3));
				
				if (receivedReply) {
					Packet ack1 = decoder.decode(rec);
					//also check checksum
					int checksum = ack1.getCheckSum() + ack1.getDataSum();
					if (ack1.getSYN() == 1 && ack1.getSeqNum() == 0 && checksum == -1) {
						byte[] window = ack1.getData();
						String server_window = new String(window);
						if (Integer.parseInt(server_window) < max_window) {
							max_window = Integer.parseInt(server_window);
							
						}
						System.out.println("max window size: " + max_window);
						
						//connection establishment successful, enter next state
						currentState = State.DATA_TRANSFER;
						break;
						
					} else {
						//packet received but corrupted
						System.out.println("first ack is corrupted");
						//resend by re-entering the connect state
						break;
					}
					
					
					
				} else {
					throw new Exception("An error has occured and connection establishment failed");
				}
				
				
			}
			case DATA_TRANSFER: {
				//first listen for user input
				System.out.println("Enter you next command:  ");
				String command = reader.nextLine();
				if (command.equals("disconnect")) {
					currentState = State.CLOSE;
					break;
				}
				String[] userinput = command.split(" ");
				String file_name = userinput[1];
				FileInputStream in = new FileInputStream(file_name);
				String newfile_name = file_name.substring(0, file_name.indexOf("."))
						+"-received" + ".txt";
				
				File file = new File(newfile_name);
				file.createNewFile();
				
				FileOutputStream out = new FileOutputStream(file);
				
				//now start reading file and transmit packet
				boolean eof = false;
				byte[] buffer = new byte[980];  //980 because max size is 1000, we have 16 bytes of header data
				readloop:
				while(true) {
//					if (in.read(buffer) == -1) {
//						//read end of line 
//						eof = true;
//						System.out.println("reached end of file");
//						break;
//					}
					for (int i = 0; i < max_window - windowBuffer.size(); i++) {
						//put packet into windowBuffer
						
						
						int check = in.read(buffer);
						if (check == -1) {
							System.out.println("reached end of file");
							
							
							eof = true;
							break;
						}
						
						Packet p = new Packet(nextSeqNum,nextSeqNum, buffer);
						//flush the buffer
						buffer = new byte[980];
						p.updateCheckSum();
						nextSeqNum++;
						windowBuffer.add(p);
						Reldat_Client.updateAck(windowBuffer);
						byte[] encoded = encoder.encode(p);
						DatagramPacket send = new DatagramPacket(encoded, encoded.length, server, port);
						System.out.println("sending packet with data, seq: " + (nextSeqNum - 1));
						sock.send(send);
					}
					
					if (eof) {
						//send out all the packets in the buffer and we're done
						int tries = 0;
//						System.out.println("printing out all the data inside window");
//						for (Packet p : windowBuffer) {
//							System.out.println("packet in window with seq:  "+ p.getSeqNum());
//							System.out.println("---------------------");
//						}
						while (!windowBuffer.isEmpty()) {
							if (tries == 5) {
								throw new Exception("server crashed during the last part of data transfer");
							}
							//System.out.println("we need to get the last few ack to ack data in the window");
							while (true) {
								//get ack
								if (windowBuffer.isEmpty()) {
									System.out.println("finished tranforming this file");
									out.flush();
									out.close();
									break readloop;
								}
									
								sock.setSoTimeout(3000);
								DatagramPacket rec = new DatagramPacket(new byte[1000],1000);
								try {
									sock.receive(rec);
									tries = 0;
								} catch (Exception e) {
									tries++;
									//System.out.println("didn't get the last few packets, tries: " + tries);
									break;
								}
								
								Packet accPacket = decoder.decode(rec);
								int checksum = accPacket.getCheckSum() + accPacket.getDataSum();
								if (checksum == -1 && accPacket.getSeqNum() == base && accPacket.getSYN() == 0) {
									System.out.println("correct last few acc from server with acc num: " + base);
									base++;
									windowBuffer.remove();
									windowBuffer = Reldat_Client.updateAck(windowBuffer);
									System.out.println("writing to file");
									out.write(accPacket.getData());
								} else {
									//System.out.println("ack wrong for the last few packet, current base: " + base
									//		+ "but received seq number with: " + accPacket.getSeqNum());
									break;
									//resend the whole window
//									for (Packet p : windowBuffer) {
//										byte[] encoded = encoder.encode(p);
//										DatagramPacket send = new DatagramPacket(encoded, encoded.length, server, port);
//										System.out.println("!!!REsending packet with data because data corrupted");
//										sock.send(send);
									}
								}
							
							//System.out.println("didn't get all the last acks for packet in window,"
							//		+ "still need how many?   " + windowBuffer.size());
							for (Packet p : windowBuffer) {
								byte[] encoded = encoder.encode(p);
								DatagramPacket send = new DatagramPacket(encoded, encoded.length, server, port);
								//System.out.println("sending last few packet in window");
								sock.send(send);
							}
							
								
							}
							break readloop;
						}
					
					//after sending the whole window, need to wait for ack
					if (nextSeqNum == base + max_window) {
						//System.out.println("in waiting ack phase");
						//System.out.println("waiting for ack: " + base);
						//need ack 
						DatagramPacket rec = new DatagramPacket(new byte[1000],1000);
						
						sock.setSoTimeout(1000);
						boolean receivedReply = false;
						int tries = 0;
						int incorrect = 0;
						
						
						
						
						
						
						//test!!!!!!!
//						while (incorrect < 8) {
//							while (tries < 3) {
//								try {
//									sock.receive(rec);
//									receivedReply = true;
//									break;
//								} catch (Exception e) {
//									System.out.println("didn't receive ack with data, ack number: " + base);
//									System.out.println("resend the whole window..., current tries: " + tries );
//									tries++;
//									for (Packet p : windowBuffer) {
//										byte[] encoded = encoder.encode(p);
//										DatagramPacket send = new DatagramPacket(encoded, encoded.length, server, port);
//										System.out.println("!!!REsending packet number:  " + p.getSeqNum()+ "  with data");
//										sock.send(send);
//									}
//									
//									
//								}
//							}
//							if (tries == 5) {
//								throw new Exception("server no response");
//							}
//							
//							Packet aPacket = decoder.decode(rec);
//							int check = aPacket.getCheckSum() + aPacket.getDataSum();
//							if (check == -1 && aPacket.getSeqNum() == base && aPacket.getSYN() == 0) {
//								System.out.println("correct acc from server");
//								base++;
//								windowBuffer.remove();
//								windowBuffer = Sender.updateAck(windowBuffer);
//								System.out.println("writing to file");
//								out.write(aPacket.getData());
//								break;
//							} else {
//								System.out.println("ack wrong packet, current base: " + base);
//								System.out.println("checksum is :" + check +"and seq num : " + aPacket.getSeqNum());
//								incorrect++;
//							
//						}
//						}
//						
//						if (incorrect == 8) {
//							System.out.println("new condition");
//							for (Packet p : windowBuffer) {
//							byte[] encoded = encoder.encode(p);
//							DatagramPacket send = new DatagramPacket(encoded, encoded.length, server, port);
//							System.out.println("!!!REsending packet:  " + p.getSeqNum());
//							sock.send(send);
//						}
//						}
						
						
						//________________
						
						while (tries < 5) {
							try {
								sock.receive(rec);
								receivedReply = true;
								break;
							} catch (Exception e) {
								//System.out.println("didn't receive ack with data, ack number: " + base);
								//System.out.println("resend the whole window..., current tries: " + tries );
								tries++;
								for (Packet p : windowBuffer) {
									byte[] encoded = encoder.encode(p);
									DatagramPacket send = new DatagramPacket(encoded, encoded.length, server, port);
									//System.out.println("!!!REsending packet number:  " + p.getSeqNum()+ "  with data");
									sock.send(send);
								}
								
								
							}
						}
						if (tries == 5) {
							throw new Exception("server no response");
						}

						if (receivedReply) {
							//received an ack, might be the correct ack
							Packet accPacket = decoder.decode(rec);
							int checksum = accPacket.getCheckSum() + accPacket.getDataSum();
							if (checksum == -1 && accPacket.getSeqNum() == base && accPacket.getSYN() == 0) {
								System.out.println("correct acc:  " + base +  "  from server");
								base++;
								windowBuffer.remove();
								windowBuffer = Reldat_Client.updateAck(windowBuffer);
								System.out.println("writing to file");
								out.write(accPacket.getData());
							} else {
								//System.out.println("ack wrong packet, current base: " + base);
								//System.out.println("checksum is :" + checksum +"and seq num : " + accPacket.getSeqNum());
								//resend the whole window
								for (Packet p : windowBuffer) {
									byte[] encoded = encoder.encode(p);
									DatagramPacket send = new DatagramPacket(encoded, encoded.length, server, port);
									//System.out.println("!!!REsending packet:  " + p.getSeqNum());
									sock.send(send);
								}
							}
							
							
							
						} else {
							//System.out.println("no response from server in data transfer state, posibly crashed");
							throw new Exception("server possibly crashed");
						}
					}
					
					}
					break;					
				}

				
				
				
			case CLOSE: {
				sock.setSoTimeout(3000);  //time out 3 seconds
				
				// send a packet with fin bit set 
				byte[] data = "CLOSE_CONNECTION".getBytes();
				Packet last = new Packet(-1, -1, data);
				last.setFIN((byte) 1);
				
				last.updateCheckSum();
				
				byte[] encoded = encoder.encode(last);
				
				DatagramPacket send_final = new DatagramPacket(encoded, encoded.length, server, port);
				DatagramPacket rec = new DatagramPacket(new byte[1000],1000);
				boolean receivedReply = false;
				int tries = 0;
				
				do{
					sock.send(send_final);
					//System.out.println("final packet sent");
					try {
						sock.receive(rec);
						receivedReply = true;
					} catch (InterruptedIOException e) {
						tries++;
						//System.out.println("The server has not answered in the last 3 seconds.");
						//System.out.println("retrying... " + (3 - tries) + " more tries.");
					}
				} while ((!receivedReply) && (tries < 3));
				
				if (receivedReply) {
					Packet ack1 = decoder.decode(rec);
					//also check checksum
					int checksum = ack1.getCheckSum() + ack1.getDataSum();
					if (ack1.getFIN() == 1 && ack1.getSeqNum() == -1 && checksum == -1) {
						System.out.println("ready to close connection");
						
						// terminate, all done
						currentState = State.FIN_WAIT;
						break;
						
					} else {
						//packet received but corrupted
						//System.out.println("last ack is corrupted");
						//resend by re-entering the closing state
						break;
					}
					
				} else {
					throw new Exception("server crashed, no response for closing connection");
				}
			}
			
			case FIN_WAIT: {
				//
				//System.out.println("in FIN_wait CONDITION");
				sock.setSoTimeout(30000); //wait for 30 secs
				byte[] data = "FIN_WAIT".getBytes();
				Packet last = new Packet(-2, -2, data);
				last.setFIN((byte) 1);
				last.updateCheckSum();
				byte[] encoded = encoder.encode(last);
				DatagramPacket send_final2 = new DatagramPacket(encoded, encoded.length, server, port);
				DatagramPacket rec = new DatagramPacket(new byte[1000],1000);
				sock.send(send_final2);
				try {
					sock.receive(rec);
					Packet p = decoder.decode(rec);
					if (p.getFIN() == 1 && p.getSeqNum() == -1) {
						//re-enter time wait state
						//System.out.println("not the fin close packet");
						break;
					} else {
						System.out.println("very wierd condition");
					}
				} catch (InterruptedIOException e) {
					System.out.println("connection is closed, time waited 30 secs");
					break outerloop;
				}
				break;
			}
			
			}
		}
		
		System.out.println("all successful, done");

	}

}
