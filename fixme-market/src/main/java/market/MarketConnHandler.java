package market;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class MarketConnHandler extends Thread {

	public static final String ANSI_RESET = "\u001B[0m";
	CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
	CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
	Selector selector = null;
	SocketChannel socket = null;
	SelectionKey clientKey = null;
	Scanner INPUT = new Scanner(System.in);
	FixMessages tag = new FixMessages();

	// implementing client connection
	public MarketConnHandler() throws InterruptedException {
		try {
			// Create a Selector
			selector = Selector.open();

			// Create a Socket and register
			socket = SocketChannel.open();
			socket.configureBlocking(false);
			clientKey = socket.register(selector, SelectionKey.OP_CONNECT);

			// connect to the remote address
			InetSocketAddress ipAddress = new InetSocketAddress("localhost", 5001);
			socket.connect(ipAddress);
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

						try {
							tag.marketList();
						} catch (Exception e) {
							logger("File not successful");
						}

					} else if (key.isReadable()) {
						SocketChannel channel = (SocketChannel) key.channel();
						// read the data
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						channel.read(buffer);
						buffer.flip();
						String msg = decoder.decode(buffer).toString();
						if (msg.contains(":") && !msg.contains("|")) {
							logger(msg);
							tag.setServerID(msg.split(":")[1].trim());
						} else if (msg.contains("|")) {
							receivedLog(msg);
							String newMsg = msg.replace("|", " ");
							tag.setRecieverID(newMsg.split("\\s")[2]);
							tag.setInstrumentName(newMsg.split("\\s")[4]);
							tag.setReceivedMsg(newMsg);
							tag.setPrice(newMsg.split("\\s")[7]);
							tag.setQuantity(newMsg.split("\\s")[5]);
							if (tag.getReceivedMsg().split("\\s")[8].contains("54=1") ) {
								sendLog(tag.fixExecutedMsg());
								if (validateBuy(channel) == true) {
									byte[] newInput = new String(tag.fixExecutedMsg()).getBytes();
									ByteBuffer bufferd = ByteBuffer.wrap(newInput);
									channel.write(bufferd);
									bufferd.clear();
								} else {
									byte[] newInput = new String(tag.fixRejectMsg()).getBytes();
									ByteBuffer bufferd = ByteBuffer.wrap(newInput);
									channel.write(bufferd);
									bufferd.clear();
								}
							} else if (tag.getReceivedMsg().split("\\s")[8].contains("54=2")) {
								sendLog(tag.fixExecutedMsg());
								if (validateSell(channel) == true) {
									byte[] newInput = new String(tag.fixExecutedMsg()).getBytes();
									ByteBuffer bufferd = ByteBuffer.wrap(newInput);
									channel.write(bufferd);
									bufferd.clear();
								} else {
									byte[] newInput = new String(tag.fixRejectMsg()).getBytes();
									ByteBuffer bufferd = ByteBuffer.wrap(newInput);
									channel.write(bufferd);
									bufferd.clear();
								}
							}

						}
					}
				}
			}
		} catch (IOException e) {
			System.out.println("No server found, please check your server connection");
			System.exit(0);
		} finally {
			try {
				selector.close();
				socket.close();
			} catch (IOException e) {
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
			Scanner INPUT = new Scanner(System.in);
			String input = INPUT.nextLine();
			byte[] newInput;
			ByteBuffer buffer;

			if (true) {
				if (!input.equalsIgnoreCase("exit")) {
					logger("[" + "\u001B[33m" + "WARNING" + "\u001B[0m" + "]" + "  Only [EXIT] option is allowed..");
					sendMessage();
				} else {
					if (input.equalsIgnoreCase("exit")) {
						newInput = new String(input).getBytes();
						buffer = ByteBuffer.wrap(newInput);
						client.write(buffer);
						buffer.clear();
						client.close();
						System.exit(1);
					}
				}
				sendMessage();
			}
			INPUT.close();
		} catch (Exception e) {
			System.err.println("Something went wrong");
		}
	}

	private static void logger(String str) {
		System.out.println(str);
	}

	private void sendLog(String msg) {
		System.out.println("[" + "\u001B[32m" + "Send_Message" + ANSI_RESET + "] " + msg);
	}

	private void receivedLog(String msg) {
		System.out.println("[" + "\u001B[32m" + "Received_Message" + ANSI_RESET + "] " + msg);
	}

	public boolean validateBuy(SocketChannel channel) throws IOException {
		BufferedReader read = new BufferedReader(new FileReader("marketDB.txt"));
		BufferedWriter newWrite = new BufferedWriter(new FileWriter("temp.txt"));

		File temp = new File("temp.txt");
		File db = new File("marketDB.txt");
		FileWriter writer = new FileWriter(db, true);
		List<String> list = new ArrayList<>();
		String line;

		while ((line = read.readLine()) != null) {
			list.add(line);
		}
		boolean lst = removeItem(list);
			for (String remainList : list) {
				newWrite.write(remainList + "\n");
				newWrite.flush();
			}
			if (db.exists()) {
				db.delete();
				writer.close();
			}
			if (temp.exists()) {
				temp.renameTo(db);
				temp.delete();
			}
			read.close();
			newWrite.close();
		return lst;
	}

	public boolean validateSell(SocketChannel channel) throws IOException {
		BufferedReader read = new BufferedReader(new FileReader("marketDB.txt"));
		BufferedWriter newWrite = new BufferedWriter(new FileWriter("temp.txt"));

		File temp = new File("temp.txt");
		File db = new File("marketDB.txt");
		FileWriter writer = new FileWriter(db, true);
		List<String> list = new ArrayList<>();
		String line;

		while ((line = read.readLine()) != null) {
			list.add(line);
		}
		boolean lst = addItem(list);
			for (String remainList : list) {
				newWrite.write(remainList + "\n");
				newWrite.flush();
			}
			if (db.exists()) {
				db.delete();
				writer.close();
			}
			if (temp.exists()) {
				temp.renameTo(db);
				temp.delete();
			}
			read.close();
			newWrite.close();
		return lst;
	}

	public boolean removeItem(List<String> list) {
		Iterator<String> iter = list.iterator();
		while (iter.hasNext()) {
			String item = iter.next();
			if (item.split(",")[0].contains(tag.getInstrumentName().split("=")[1])) {
				if(Integer.parseInt(tag.getQuantity().split("=")[1]) < Integer.parseInt(item.split(",")[2]) ) {
					int remaing = Integer.parseInt(item.split(",")[2]) -  Integer.parseInt(tag.getQuantity().split("=")[1]);
					String intRemaing = Integer.toString(remaing);
					iter.remove();
					list.add(tag.getInstrumentName().split("=")[1] + ","+ item.split(",")[1] + ","  + intRemaing);
					return true;
				}
				if(Integer.parseInt(tag.getQuantity().split("=")[1]) > Integer.parseInt(item.split(",")[2]) ) {
					return false;
				}
				if(Integer.parseInt(tag.getQuantity().split("=")[1]) == Integer.parseInt(item.split(",")[2]) )  {
					iter.remove();
					return true;
				}
			}
		}
		return false;
	}

	public boolean addItem(List<String> list) {
		Iterator<String> iter = list.iterator();
		while (iter.hasNext()) {
			String item = iter.next();
			if(!item.split(",")[0].contains(tag.getInstrumentName().split("=")[1])) {
				list.add(tag.getInstrumentName().split("=")[1] + "," + tag.getPrice().split("=")[1] + "," + tag.getQuantity().split("=")[1]);
				return true;
			}
			if(item.split(",")[0].contains(tag.getInstrumentName().split("=")[1])) {
				iter.remove();
				list.add(tag.getInstrumentName().split("=")[1] + "," + tag.getPrice().split("=")[1] + "," + tag.getQuantity().split("=")[1]);
				return true;
			}
		}
		return false;
	}
}
