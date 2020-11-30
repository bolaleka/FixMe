package broker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.Scanner;

public class BrokerConnHandler extends Thread {

	public static final String ANSI_RESET = "\u001B[0m";
	CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
	CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
	Selector selector = null;
	SocketChannel socket = null;
	SelectionKey clientKey = null;
	Scanner INPUT = new Scanner(System.in);
	FixMessages tag = new FixMessages();

	// implementing client connection
	public BrokerConnHandler() throws InterruptedException {
		try {
			// Create a Selector
			selector = Selector.open();

			// Create a Socket and register
			socket = SocketChannel.open();
			socket.configureBlocking(false);
			clientKey = socket.register(selector, SelectionKey.OP_CONNECT);

			// connect to the remote address
			InetSocketAddress ip = new InetSocketAddress("localhost", 5000);
			socket.connect(ip);
		} catch (IOException e) {
			logger("this is close");
			System.exit(1);
		}

	}

	// read event
	public void run() {
		try {
			while (true) {
				selector.select();

				Iterator<SelectionKey> selectedKey = selector.selectedKeys().iterator();
				while (selectedKey.hasNext()) {
					SelectionKey key = selectedKey.next();
					selectedKey.remove();

					if (key.isConnectable()) {
						SocketChannel channel = (SocketChannel) key.channel();
						if (channel.isConnectionPending())
							channel.finishConnect();
						channel.register(selector, SelectionKey.OP_READ);
						logger("Connected to the server successfully!");

					} else if (key.isReadable()) {
						SocketChannel channel = (SocketChannel) key.channel();
						// read the data
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						channel.read(buffer);
						buffer.flip();
						String msg = decoder.decode(buffer).toString();
						if(msg.contains(":") && !msg.contains("|")) {
							logger(msg + "\n");
							logger("Available options [" + "\u001b[32m" +"BUY, SELL, EXIT" + ANSI_RESET + "] to enter trade:");
							tag.setSenderID(msg.split(":")[1].trim());
						}else if(msg.contains("|")) {
							receivedLog(msg);
							String newMsg = msg.replace("|", " ");
							tag.setMsgfromID(newMsg.split("\\s")[2]);
							tag.setReceivedMsg(newMsg);
							if(tag.getReceivedMsg().split("\\s")[3].contains("35=8")) {
								logger("\n"+ "\u001b[32m" +"Executed Successfully" + ANSI_RESET);
							}else {
								logger("\n" + "\u001b[31m" +"Rejected: Transaction not Successful" + ANSI_RESET);
							}
							logger("\nWould you like to Buy,Sell or Exit?");
						}
							
					}
				}
			}
		} catch (IOException e) {
			logger("No server found, please check your server connection");
			System.exit(0);
		} finally {
			try {
				selector.close();
				socket.close();
				System.exit(1);
			} catch (IOException e) {
				System.exit(1);
			}
		}
	}

	// Send a message to server
	public void send(String msg) {
		try {
			SocketChannel client = (SocketChannel) clientKey.channel();
			client.write(encoder.encode(CharBuffer.wrap(msg)));
		} catch (Exception e) {
			logger("Message not successful");
		}
	}

	public void sendMessage() throws IOException {
		
		SocketChannel client = (SocketChannel) clientKey.channel();
		try {
			INPUT = new Scanner(System.in);
			String input = INPUT.nextLine();
			byte[] newInput;
			ByteBuffer buffer;

			if (true) {
				if(input.isEmpty()) {
					logger("No input found");
					sendMessage();
				}else if (!input.equalsIgnoreCase("Sell") && !input.equalsIgnoreCase("Buy") && !input.equalsIgnoreCase("exit")) {
					logger("[" + "\u001B[33m"+"WARNING"+ "\u001B[0m" + "]" + "   Enter BUY/SELL to trade");
					sendMessage();
				} else {
					if (input.equalsIgnoreCase("exit")) {
						newInput = new String(input).getBytes();
						buffer = ByteBuffer.wrap(newInput);
						client.write(buffer);
						buffer.clear();
						client.close();
						System.exit(1);
					} else if (input.equalsIgnoreCase("Buy")) {
						addInput(input);
						sendLog(tag.fixMsgBuy());
						newInput = new String(tag.fixMsgBuy()).getBytes();
						buffer = ByteBuffer.wrap(newInput);
						client.write(buffer);
						buffer.clear();
						sendMessage();
					} else if (input.equalsIgnoreCase("Sell")) {
						addInput(input);
						sendLog(tag.fixMsgSell());
						newInput = new String(tag.fixMsgSell()).getBytes();
						buffer = ByteBuffer.wrap(newInput);
						client.write(buffer);
						buffer.clear();
						sendMessage();
					}else {
						logger("Enter: BUY/SELL to trade");
					}
				}
				
			}
			INPUT.close();
		} catch (Exception e) {
			System.err.println("Something went wrong");
			System.exit(1);

		}
	}

	private static void logger(String str) {
		System.out.println(str);
	}

	public void addInput(String input) {
		System.out.print("Instrument: " );
		input = INPUT.nextLine();
		tag.setInstrument(input);
		System.out.print("Price: ");
		int price = INPUT.nextInt();
		tag.setPrice(price);
		System.out.print("Quantity: ");
		int quantity = INPUT.nextInt();
		tag.setQuantity(quantity);
	}

	private void sendLog(String msg) {
		System.out.println("[" + "\u001B[32m" +  "Send_Message"+ ANSI_RESET +  "] " +  msg);
		
	}

	private void receivedLog(String msg) {
		System.out.println("[" + "\u001B[32m" +  "Received_Message"+ ANSI_RESET +  "] " +  msg);
	}
}