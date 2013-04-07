package Message;

import java.util.Random;

public class Nonce {
	private int value;
	public Nonce (){
		//this.value = (int) (Math.random() * Math.pow(10, 10));
		this.value = new Random(System.currentTimeMillis()).nextInt();
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
