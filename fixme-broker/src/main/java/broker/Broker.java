package broker;

public class Broker {
   
    public static void main(String[] args) throws Exception {

      BrokerConnHandler client = new BrokerConnHandler();

      client.start();
      client.sendMessage();
    
    }
}
