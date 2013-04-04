package Message;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class AuthenticationResponse extends Message{
	
	private static final long serialVersionUID = 1L;
		
	public AuthenticationResponse(SecretKey key,String data, int seq)
	{	
		super(seq);
		this.messageType = MessageType.Auth_Rsp;
		MessageEncrypt(key, data);
		hashedresult = hashAllInfo(key);
	}

	/*public int getDataLength() {
		return dataLength;
	}*/

}
