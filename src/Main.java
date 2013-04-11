import java.io.FileNotFoundException;
import java.io.IOException;

import ASN1Encoder.ASN1DecoderFail;
import DatabaseHelper.Database;
public class Main {

	
	/**
	 * 
	 * @param args
	 * @throws ASN1DecoderFail
	 */
	public static void main(String[] args) throws ASN1DecoderFail{
		
		// EncodingAndDecodingFun(); Fun with encoding and decoding
		
		// Important objects to begin work
		Database db;
		String databaseFilepath="databases/deliberation.db";
		int launchPort=32901;
		
		/*
		 * Assume here is handling input and shit
		 */
		

		
		
		
		
		// Try to create a database
		try {
			db=new Database(databaseFilepath);
			
			ServerManager serverThread=new ServerManager(db, 10);
			
			
			ClientThread clientThread=new ClientThread(db);
			new Thread(clientThread).start();
			
			
			serverThread.startListening(launchPort);
			
			
			
			
			
			
		} catch (FileNotFoundException e) {
		
			System.err.println("Error Occured During Initialization of database, quitting...");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			System.err.println("Error Occured During Initialization of Listening Server, quitting...");
			e.printStackTrace();
			return;
		} 
		
		
		
		ClientThread clientThread = new ClientThread(db);
		
		
		
	}
	
	
	/**
	 * Test function to play with encode and decoder
	 * @param args
	 * @throws ASN1DecoderFail 
	 */
	public static void EncodingAndDecodingFun() throws ASN1DecoderFail {
	
	
		
	try {
		Database test=new Database("databases/test.db");
		System.out.println(test.getSQLQueryAsString("SELECT * FROM tabel1"));
		System.out.println(test.getSingleField("SELECT column1 FROM tabel1 WHERE column2='row22'"));
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	

	
	
	}
}
