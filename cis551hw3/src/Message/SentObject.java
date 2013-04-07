package Message;

import java.io.Serializable;

public class SentObject implements Serializable{
	
	private static final long serialVersionUID = 1L;
	byte[] content;
	public SentObject(byte[] content){
		this.content = content;
	}
	public byte[] getContent() {
		return content;
	}
	
}
