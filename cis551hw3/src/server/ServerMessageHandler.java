package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import Message.AuthenticationConfirmation;
import Message.AuthenticationResponse;
import Message.DataMessage;
import Message.Message;

public class ServerMessageHandler {
	private static String username = "user";
	private static String password = "123";
	public static boolean handleMsg(Message msg, ObjectOutputStream oos, ObjectInputStream ois, BufferedReader userInput) throws IOException
	{
		switch(msg.getMessageType())
		{
			case Auth_Rsp:
				System.out.println("Received client username and password");
				AuthenticationResponse authRsp = (AuthenticationResponse) msg;
				String userinfo = authRsp.getData();
				boolean isAuthentication = (userinfo.equals(username + " " + password));
				AuthenticationConfirmation authConf = new AuthenticationConfirmation(isAuthentication);
				oos.writeObject(authConf);
				oos.flush();
				return isAuthentication;
			case Data:
				DataMessage dataMsg = (DataMessage) msg;
				String data = dataMsg.getData();
				System.out.println("Client Said: " + data);
				return true;
			case Exit:
				System.out.println("Client requires close connection.");
				return false;
			default:
				System.out.println("Received unknown message type");
				return true;
		}
	}
}
