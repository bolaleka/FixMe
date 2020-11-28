package market;

public class Market {
    public static void main(String[] args) throws Exception {

      MarketConnHandler client = new MarketConnHandler();
      client.start();
      client.sendMessage();
      
    }
}
