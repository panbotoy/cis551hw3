package Message;

public class Nonce {
	private int value;
	public Nonce (){
		this.value = (int) (Math.random() * Math.pow(10, 10));
	}
	
	/***A static nonce generator**********/ 
	static public Nonce getNonce()
	{
		Nonce nonce = new Nonce();
		return nonce;
	}
	
	public int getValue()
	{
		return this.value;
	}
	
}
