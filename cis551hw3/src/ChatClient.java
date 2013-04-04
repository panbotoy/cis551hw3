

import Client.ChatClientImpl;

public class ChatClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*if(args.length!=1){
			System.out.println("Invalid arguments");
			System.exit(0);
		}*/
		//String hostName = args[0];
		String hostName = "localhost";
		int portNumber = 30130;
		ChatClientImpl client = new ChatClientImpl();
		client.Start(hostName, portNumber);
	}

}
