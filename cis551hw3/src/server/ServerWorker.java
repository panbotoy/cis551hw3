package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerWorker {
	private Socket connection;
	private String authenticationReq = new String("please input username and password");
	private String username = new String("user");
	private String password = new String("123");
	private String exitMsg = new String("exit");
	private boolean authenticated = false;
	
	public ServerWorker(){
		
	}
	
	/****
	 * When connection started, server will send request for username and password
	 * Send user name and password
	 * Server will return authentication successful/failed 
	 * 
	 * Once failed, server will close the socket
	 * once successful, client can start chat, and can close connection by typing "EXIT"
	 * ***/
	
	public boolean Work(Socket connection) throws IOException
	{
		this.connection = connection;
		PrintWriter pw = new PrintWriter(this.connection.getOutputStream(), true); //write to socket
		DataInputStream socketInput = new DataInputStream(this.connection.getInputStream());
		BufferedReader br = new BufferedReader(new InputStreamReader(socketInput));		 // read from socket
		BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
		this.authenticated = AuthenticationReq(pw, br); // authenticates user
		if(this.authenticated)
		{
			pw.println("Successfully logged in! You can start to chat!");
			String currentLine = br.readLine();
			String responseLine = new String();
			System.out.println("Client Said : " + currentLine);
			while(!currentLine.equals(this.exitMsg))
			{
				if(socketInput.available()>0)
				{
					currentLine = br.readLine();
					System.out.println("Client Said : " + currentLine);
				}
				if(System.in.available()>0)
				{
					responseLine = userInput.readLine();
					pw.println(responseLine);
				}
			}
			System.out.println("Server Received exit command, closing connection!");
			this.connection.close();
			return true;
		}
		else
		{
			System.out.println("Authentication Failed");
			pw.println("Authentication failed! Close Connection!");
			this.connection.close();
			return true;
		}

	}

	private boolean AuthenticationReq(PrintWriter pw, BufferedReader br){
		// TODO Auto-generated method stub
		pw.println(this.authenticationReq);
		try {
			String currentLine = br.readLine();
			System.out.println("Server received : " + currentLine);
			return (currentLine.equals(this.username + " " + this.password));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Client does not send authentication data!  ---  Server AuthenticationReq()");
			e.printStackTrace();
		}
		return false;
	}
}
