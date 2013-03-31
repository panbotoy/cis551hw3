package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import Message.AuthenticationConfirmation;
import Message.AuthenticationRequest;
import Message.AuthenticationResponse;
import Message.DataMessage;
import Message.ExitMessage;
import Message.Message;

public class ClientMessageHandler {
	/******
	 * Returns the state of the client after handling the msg. true is continue working, false is stop working
	 * @throws IOException 
	 * *******/
	public static boolean handleMsg(Message msg, ObjectOutputStream oos, ObjectInputStream ois, BufferedReader userInput) throws IOException
	{
		switch(msg.getMessageType())
		{
			case Auth_Req:
				System.out.println("Please input your username and password:");
				String responseLine = userInput.readLine();
				AuthenticationResponse authRsp = new AuthenticationResponse(responseLine);
				oos.writeObject(authRsp);
				oos.flush();
				return true;
			case Auth_Conf:
				AuthenticationConfirmation authConf = (AuthenticationConfirmation) msg;
				if(authConf.isAuthenticated())
				{
					System.out.println("Log in successful! You can start to chat!");
					return true;
				}
				else
				{
					System.out.println("Log in failed! Close connection!");
					return false;
				}
			case Data:
				DataMessage dataMsg = (DataMessage) msg;
				String data = dataMsg.getData();
				System.out.println("Server Said: " + data);
				return true;
			default:
				System.out.println("Received unknown message type");
				return true;
		}
	}
}
