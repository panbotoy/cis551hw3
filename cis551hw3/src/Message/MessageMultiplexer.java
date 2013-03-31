package Message;

public class MessageMultiplexer {
	static public Object determineMsgType(Message msg)
	{
		MessageType type = msg.getMessageType();
		switch(type){
			case Auth_Req:
				return (AuthenticationRequest)msg;
			case Auth_Rsp:
				return (AuthenticationResponse)msg;
			case Auth_Conf:
				return (AuthenticationConfirmation)msg;
			case Data:
				return (DataMessage)msg;
			case Exit:
				return (ExitMessage)msg;
			default:
				return null;
		}
	}
}
