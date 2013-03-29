package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerWorker {
	Socket connection;
	
	public ServerWorker(){
		
	}
	
	public void Work(Socket connection) throws IOException
	{
		this.connection = connection;
		PrintWriter pw = new PrintWriter(this.connection.getOutputStream());
		BufferedReader br = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
		String currentLine = br.readLine();
		System.out.print(currentLine);
	}
}
