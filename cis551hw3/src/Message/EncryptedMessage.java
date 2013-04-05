package Message;

public class EncryptedMessage {
	
	private static final long serialVersionUID = 1L;
	protected byte[] sequencenumber;
	protected byte[] nonce;
	protected byte[] messageType;
	protected byte[] timestamp;
	protected byte[] data = null;
	protected byte[] hashedresult;
}
