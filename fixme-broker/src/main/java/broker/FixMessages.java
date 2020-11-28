package broker;

import java.util.HashMap;

public class FixMessages {

   public HashMap<String, String> mapTag = new HashMap<>();
   public String fixHeader;
   public String bodyLength;
   public String msgType;
   public String senderID;
   public String recieverID;
   public String Instrument;
   public String buyTag;
   public String sellTag;
   public String msgfromID;
   public String receivedMsg;
   public int quantity;
   public int price;
   


   public String getFixHeader() {
       mapTag.put("8", "FIX.4.2|");
       return "8=FIX.4.2|";
   }
   public String getBodyLength() {
       mapTag.put("9", totalBodyLength() + "|" );
       return "9=" + totalBodyLength() + "|";
   }
   public void setSenderID(String senderID) {
       this.senderID = senderID;
   }
   public String getSenderID() {
       return "49="+senderID + "|";
   }

     public void setMsgfromID(String msgfromID) {
        this.msgfromID = msgfromID;
    }

    public void setReceivedMsg(String receivedMsg) {
        this.receivedMsg = receivedMsg;
    }

    public String getReceivedMsg() {
        return receivedMsg;
    }

    public String getMsgfromID() {
        return msgfromID;
    }
   public String getRecieverID() {
       return "56=MARKET|";
   }
   public void setQuantity(int quantity) {
       this.quantity = quantity;
   }
   public String getQuantity() {
       return "687=" + quantity + "|";
   }
   public void setPrice(int price) {
       this.price = price;
    }
    public String getPrice() {
        return "44=" + price + "|";
    }
    public void setInstrument(String instrument) {
        Instrument = instrument;
    }
   public String getInstrument() {
       return "460=" + Instrument + "|";
   }
   public String getBuyTag() {
       return "54=1|";
   }
   public String getSellTag() {
       return "54=2|";
   }

   public String getMsgType() {
       return "35=D|";
   }

   private int totalBodyLength() {
       String bodyMsg = getInstrument() + getPrice() + getQuantity() + getRecieverID() + getSenderID() + getBuyTag() + getMsgType();
       String extString = bodyMsg.replace("|", "\u0001");
       return extString.length();
   }

   public String fixMsgBuy()  {

        StringBuilder message = new StringBuilder();

        message.append(getFixHeader());
        message.append(getBodyLength());
        message.append(getSenderID());
        message.append(getMsgType());
        message.append(getInstrument());
        message.append(getQuantity());
        message.append(getRecieverID());
        message.append(getPrice());
        message.append(getBuyTag());

        return message.toString();

   }

   public String fixMsgSell(){

        StringBuilder message = new StringBuilder();

        message.append(getFixHeader());
        message.append(getBodyLength());
        message.append(getSenderID());
        message.append(getMsgType());
        message.append(getInstrument());
        message.append(getQuantity());
        message.append(getRecieverID());
        message.append(getPrice());
        message.append(getSellTag());
        
        return message.toString();
    }



}
