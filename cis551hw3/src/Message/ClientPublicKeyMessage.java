package Message;

import javax.crypto.SecretKey;

public class ClientPublicKeyMessage extends Message{
private static final long serialVersionUID = 1L;
	
	private byte[] publickey;
	
	public ClientPublicKeyMessage(byte[] publickeyinfo, int seq){
		super(seq);
		this.publickey = publickeyinfo;
		this.messageType = MessageType.Client_pub;
	}
		
	public byte[] getPublickey() {
		return publickey;
	}

	public void setPublickey(byte[] publickey) {
		this.publickey = publickey;
	}

}
