package market;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class FixMessages {

    public List<String> marketList = new ArrayList<>();

    public String fixHeader;
    public String bodyLength;
    public String msgType;
    public String serverID;
    public String recieverID;
    public String receivedBuyTag;
    public String receivedSellTag;
    public String receievedMsg;
    public String instrumentName;
    public String price;
    public String quantity;

    public String getFixHeader() {

        return "8=FIX.4.2|";
    }

    public String getBodyLength() {
        return "9=" + totalBodyLength() + "|";
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public String getServerID() {
        return "49=" + serverID + "|";
    }

    public void setRecieverID(String recieverID) {
        this.recieverID = recieverID;
    }

    public String getRecieverID() {
        String newRec = recieverID.split("=")[1];
        return "56=" + newRec + "|";
    }

    public String getAcceptMsgType() {
        return "35=8|";
    }

    public String getRejectMsgType() {
        return "35=3|";
    }

    public String getRejectReasonTag() {
        return "103=5|";
    }
    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }
    public String getInstrumentName() {
        return instrumentName;
    }

    public void setPrice(String price) {
        this.price = price;
    }
    public String getPrice() {
        return price;
    }
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
    public String getQuantity() {
        return quantity;
    }
    
   
    private int totalBodyLength() {
        String bodyMsg = getRecieverID() + getServerID() + getRejectMsgType();
        String extString = bodyMsg.replace("|", "\u0001");
        return extString.length();
    }

    public void setReceivedMsg(String receievdMsg) {
        this.receievedMsg = receievdMsg;
    }
    public String getReceivedMsg() {
        return receievedMsg;
    }

    public String fixExecutedMsg() {
        StringBuilder message = new StringBuilder();

        message.append(getFixHeader());
        message.append(getBodyLength());
        message.append(getServerID());
        message.append(getAcceptMsgType());
        message.append(getRecieverID());

        return message.toString();
    }

    public String fixRejectMsg() {
        StringBuilder message = new StringBuilder();

        message.append(getFixHeader());
        message.append(getBodyLength());
        message.append(getServerID());
        message.append(getRejectMsgType());
        message.append(getRejectReasonTag());
        message.append(getRecieverID());
        return message.toString();
    }

    public List<String> getMarketList() {
        return marketList;
    }

    public void marketList() throws Exception {
        try {
            marketList.add("dog,10,4");
            marketList.add("cat,5,2");
            marketList.add("pen,5,3");
            marketList.add("currency,5,7");
            marketList.add("phone,4,2");
            marketList.add("jean,5,3");
            marketList.add("tie,6,2");
            marketList.add("security,5,2");
            marketList.add("shares,5,3");
            marketList.add("gold,10,2");
            BufferedWriter writer = new BufferedWriter(new FileWriter("marketDB.txt"));

            for (String lst : marketList) {
                writer.write(lst);
                writer.newLine();
            }
            writer.flush();
            writer.close();

          } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
    }
}
