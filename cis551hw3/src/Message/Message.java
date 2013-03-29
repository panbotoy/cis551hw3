package Message;
import java.io.*;


/************
 * Different messages all inherits from this message
 * 
 * Different message types may have different encoding schemes as well as decoding schemes
 * 
 * Different messages may have different encryptions. i.e. Auth_Resp may have strongest encryption, data may have less strong
 * 
 * After serializing messages, we can also encode them before sending them over the socket, maybe.
 * 
 * Auth_Rsp has data(user name, password)
 * 
 * Auth_Conf has data(boolean indicating the result of the authentication)
 * 
 * Data has data(chatting contents)
 * 
 * 
 * Work needed to do:
 * 1. Think of new message types, if any
 * 2. Think of different encoding/encryption mechanisms for different msgs And implements the abstract methods MessageEncode, MessageDecode, MessageEncrypt, MessageDecrypt
 * 3. We also need to encrypt the user info on the server
 * *************/
public class Message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	MessageType messageType;
	int encodingType;
	int encryptionMethod;

	public MessageType getMessageType() {
		return messageType;
	}
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}
	public int getEncodingType() {
		return encodingType;
	}
	public void setEncodingType(int encodingType) {
		this.encodingType = encodingType;
	}
	public int getEncriptionMethod() {
		return encryptionMethod;
	}
	public void setEncriptionMethod(int encriptionMethod) {
		this.encryptionMethod = encriptionMethod;
	}
	
	public void MessageEncode(){}
	public void MessageDecode(){}
	public void MessageEncrypt(String key){}
	public void MessageDecrypt(String key){}
}
