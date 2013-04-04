package Message;

import javax.crypto.SecretKey;

public class DataMessage extends Message{
	
	private static final long serialVersionUID = 1L;
	
	public DataMessage(SecretKey key, String data, int seq)
	{
		super(seq);
		this.messageType = MessageType.Data;
		MessageEncrypt(key, data);
		hashedresult = hashAllInfo(key);
	}
}
