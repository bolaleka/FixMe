package router;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Hashtable;
import java.util.Iterator;

public class Server {

    
    private static ServerSocketChannel serverSocket = null;
    private static Hashtable<SocketAddress, SocketChannel> clientlist = new Hashtable<SocketAddress, SocketChannel>();
    private static Hashtable<Integer, Hashtable<SocketAddress, SocketChannel>> routingTable = new Hashtable<>();
    
    
    public static void main(String[] args) throws IOException {
        //Get selector
        Selector selector = Selector.open();

        int ports[] = new int[] { 5000, 5001};
        //ServerSocketChannel serverSocket = null;
        SocketChannel socketChannel = null;
        SelectionKey key = null;
       
        // loop through each port in our list and bind it to a ServerSocketChannel
        for (int port : ports) {
             // Get server socket channel and register with selector
                serverSocket = ServerSocketChannel.open();
                serverSocket.bind(new InetSocketAddress("localhost", port));
                serverSocket.configureBlocking(false);
                serverSocket.register(selector, SelectionKey.OP_ACCEPT);              
        }
        logger(InetAddress.getLocalHost().getHostAddress() + " [Main]  INFO Router - Waiting for conection from Broker/Market side"); 
    
        for(;;) {
            
          selector.select();          
           Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey selectedKey = selectedKeys.next();
                selectedKeys.remove();
               
                if (selectedKey.isAcceptable()) {

                    // Accept the new client connection
                     socketChannel = ((ServerSocketChannel) selectedKey.channel()).accept();
                    socketChannel.configureBlocking(false);

                    // Add the new connection to the selector
                  key =  socketChannel.register(selector, SelectionKey.OP_READ);
                  
                  int randomID = (int)(Math.random() * (999999 - 100001 + 1) + 100001);
                    logger("Accepted new connection from port: " + socketChannel.getLocalAddress());
                    String msg = "[Main]  INFO RouterBroker - Router ID: " + randomID;

                    clientlist.put(socketChannel.getLocalAddress(), socketChannel);
                    routingTable.put(randomID, clientlist);

                    sendID(msg, key);
                    
                } else if (selectedKey.isReadable()) {
                    readAndDispatchMessage(selectedKey);
                } 
            }
            
        }
      
    }

    private static void sendID(String msg, SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(msg.getBytes());
        buffer.flip();
        channel.write(buffer);
        buffer.clear();

    }


    private static void readAndDispatchMessage(SelectionKey key) throws IOException {
        
        SocketChannel channel = (SocketChannel) key.channel();

        // read the data
        CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
        
        ByteBuffer buffer = ByteBuffer.allocate(1024);
		channel.read(buffer);
		buffer.flip();
        String msg = decoder.decode(buffer).toString();
        if (msg.equalsIgnoreCase("exit")) {
            logger("Port " + channel.getLocalAddress() + " disconnected succesfully");
            channel.close();
        }else if(msg.contains("|")) {
            //Identify the destination in the routing table
            for(SocketAddress soc : routingTable.values().iterator().next().keySet()) {
                if(!soc.equals(channel.getLocalAddress())) {
                    CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
                    try {
                        //• Forward the message
                        routingTable.values().iterator().next().get(soc).write(encoder.encode(CharBuffer.wrap(msg + validateCheckSum(msg))));
                    } catch (ClosedChannelException e) {
                        System.out.println("\nMarket is closed");
                        
                    }
                }
            }
        }

    }

    private static void logger(String str) {
        System.out.println(str);
    }

    //Validate the message based on the checkshum
    private static String validateCheckSum(String msg) {
        //Always remember to replace "|" to SOH
        String bodyLength = msg.replace("|", "\u0001");

        byte[] byteLength = bodyLength.getBytes();
        int sum = 0;
        for (byte b : byteLength) {
            sum = sum + b;
        }
        int checkSum = sum % 256;
        //Convert checksum to string and check the string length 
        if(Integer.toString(checkSum).length() == 2 ) {
            return "10=0" + checkSum + "|";
        }else {
            return "10=" + checkSum + "|";
        }
    }

}


