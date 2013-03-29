package Client;

public class ChatClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String hostName = "localhost";
		int portNumber = 30130;
		ChatClientImpl client = new ChatClientImpl();
		client.Start(hostName, portNumber);
	}

}
