import ASN1Encoder.*;
public class Main {

	
	public static void main(String[] args) throws ASN1DecoderFail{
		
		EncodingAndDecodingFun();
		
		
	}
	
	
	/**
	 * Test function to play with encode and decoder
	 * @param args
	 * @throws ASN1DecoderFail 
	 */
	public static void EncodingAndDecodingFun() throws ASN1DecoderFail {
	
	
		
	// Sequence
	Encoder enc=new Encoder();
	enc.initSequence();
	enc.addToSequence(new Encoder());
	enc.addToSequence(new Encoder("Anybody there?"));
	   
	
	
	System.out.println(enc.toString());
	
	// Decoding
	Decoder dec=new Decoder(enc.getBytes(), 0);
	dec=dec.getContent(); // DONT FORGET THIS LINE, WITHOUT IT, NOTHING WORKS!!!!
	System.out.println(dec.getFirstObject(true).getInteger());
	System.out.println(dec.getFirstObject(true).getString());
	
	
	
	
	}
}
