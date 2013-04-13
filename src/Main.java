import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ASN1Encoder.ASN1DecoderFail;
import DatabaseHelper.Database;
public class Main {

	private static int maxClients=10;
	/**
	 * 
	 * @param args
	 * @throws ASN1DecoderFail
	 */
	public static void main(String[] args) throws ASN1DecoderFail{
		Logger.getLogger("com.almworks.sqlite4java").setLevel(Level.OFF);
		// EncodingAndDecodingFun(); Fun with encoding and decoding
		
		// Important objects to begin work
		Database db;
		String databaseFilepath="databases/deliberation.db";
		int launchPort=32901;
		
		
		// Handle Options
		OptionScanner options=new OptionScanner(args);
		
		if (options.optionExists("-d")){
			
			databaseFilepath=options.getOption("-d");
			
		} else {
			
			System.err.println("[WARNING!] Database path is not configured (-d option is not used), default path is used");
			
		}
		
		
	if (options.optionExists("-p")){
			
			launchPort=Integer.parseInt(options.getOption("-p"));
			
		} else {
			
			System.err.println("[WARNING!] Port is not configured (-p option is not used), default port is used, 32901");
			
		}
		
	
		
		// Try to create a database
		try {
			db=new Database(databaseFilepath);
			
			ServerManager serverThread=new ServerManager(db, maxClients, launchPort);
			
			new Thread(serverThread).start();
			
			
			ClientThread clientThread=new ClientThread(db);
			new Thread(clientThread).start();
			
			new UserThread(db).startHere();
			
			
			
			
			
			
			
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
