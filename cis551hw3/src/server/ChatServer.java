package server;

public class ChatServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ServerImpl server = new ServerImpl(30130);
		server.Start();
	}

}
