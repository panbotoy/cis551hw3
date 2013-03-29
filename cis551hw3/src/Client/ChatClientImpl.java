package Client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClientImpl {
	private String hostName;
	private int portNumber;
	private Socket connection;
	private String authenticationReq = new String("please input username and password");
	
	public ChatClientImpl(){}
	
	public void Start(String hostName, int portNumber)
	{
		this.hostName = hostName;
		this.portNumber = portNumber;
		
		try {
			this.connection = new Socket(this.hostName, this.portNumber);
			
			/****
			 * When connection started, server will send request for username and password
			 * Send user name and password
			 * Server will return authentication successful/failed 
			 * 
			 * Once failed, server will close the socket
			 * once successful, client can start chat, and can close connection by typing "EXIT"
			 * ***/
			
			/********
			 * Possible Extensions:
			 * 1. Create different types of packets: server authentication messages, client authentication messages, data messages, closing messages etc.
			 * 		instead of using just socket stream
			 * 
			 * *********/
			PrintWriter pw = new PrintWriter(this.connection.getOutputStream(), true);
			DataInputStream socketInput = new DataInputStream(this.connection.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(socketInput));
			BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
			
			String currentLine = br.readLine();
			String responseLine = new String();
			while(true)
			{
				if(currentLine.equals(this.authenticationReq))
				{
					System.out.println("Please input your username and password:");
					currentLine = new String();
					responseLine = userInput.readLine();
					pw.println(responseLine);
				}
				else{
					if(socketInput.available()>0)
					{
						currentLine = br.readLine();
						System.out.println("Server said: " + currentLine);
					}
					if(System.in.available()>0)
					{
						responseLine = userInput.readLine();
						pw.println(responseLine);
						if(responseLine.equals("exit"))
						{
							return;
						}		
					}
				}
			}
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println("Unknown host, check host name and port number");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Problems with the socket IO");
			e.printStackTrace();
		}
		
	}
}
