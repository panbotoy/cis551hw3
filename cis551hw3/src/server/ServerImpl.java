package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerImpl {
	private ServerSocket serverSocket;
	private int portNumber;
	private boolean isInterrupted;

	public ServerImpl(int portNumber){
		this.portNumber = portNumber;
		this.isInterrupted = false;
	}
	
	public void Start()
	{
		try {
			this.serverSocket = new ServerSocket(this.portNumber);
			System.out.println("Server socket set up! Waiting for Client!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Cannot Set up Server Socket!");
			e.printStackTrace();
		}
		while(!this.isInterrupted)
		{
			try {
				Socket connection = this.serverSocket.accept();
				System.out.println("Accepted Client Connection!");
				ServerWorker worker = new ServerWorker();
				this.isInterrupted = worker.Work(connection);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Cannot Accept Client Socket! Or Server IO problem");
				e.printStackTrace();
			} 
		}
	}
	
	
	
}
