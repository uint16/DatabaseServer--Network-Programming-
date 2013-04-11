import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import ASN1Encoder.Encoder;
import DatabaseHelper.Column;
import DatabaseHelper.Database;
import DatabaseHelper.Table;

public class ClientThread implements Runnable {

	/**
	 * Simple inet-address class that breaks up string of format
	 * "aaa.aaa.aaa.aaa:pppp" into string adress and int port
	 * 
	 * @author Alexander Viktorovich Troshchenko
	 * 
	 */
	private class InetAddress {

		public int port;
		public String adress;

		public InetAddress(String unf_adress) {

			// Get a colon
			for (int i=0;i<unf_adress.length();i++){
				
				// When colon is found, update port and adress
				if (unf_adress.charAt(i)==':'){
				  adress=unf_adress.substring(0, i);
				  port=Integer.parseInt(unf_adress.substring(i+1));
					
				}
			}
			
		}

	}

	private Database db;

	boolean runLoop = true; // Identifies whether user loop should be runned or
							// not. Can be stopped by internal error or other
							// thread.

	/**
	 * Constructor for the client class
	 * 
	 * @param database
	 *            Database file for the work
	 */
	public ClientThread(Database database) {

		db = database;

	}

	public void run() {
	
		// While it's not stopped by internal error or external command
		while (runLoop){
			
			
			// Get all peers from the database
			Table peer_address=db.getTableWithColumns("peer_address", new String[]{"peer_id", "address"});
			
			@SuppressWarnings("unchecked")
			Column<String> peer_address_address_column=peer_address.getColumn("address");
			
			// For each peer, try to connect and update list of peers here 
			for (int i=0;i<peer_address_address_column.size();i++){
				
				// Get address of this peer
				InetAddress peerAddress=new InetAddress(peer_address_address_column.getObjectAtRow(i));
				
				
				Socket test=new Socket();
				
				try {
					
					
					test.connect(new InetSocketAddress(peerAddress.adress, peerAddress.port), 2000); // 2000 is a timeout specified in assignment instructions
				
					// At this point client is connected.
					
					
					// Get last sync date 
					String peer_id=(String) peer_address.getColumn("peer_id").getObjectAtRow(i); // Gets a peer_id. Since it should be the same, we can find this peer_id in peer table 
					
					
					
					
					// Generate a message to the client
					
					// message example { 20130410202659.999Z, tables{ ... , ... }}
					Encoder requestEncoder=new Encoder();
					requestEncoder.initSequence();
					
					
					
					
				
				// Will throw an error when timeout is reached. Prints error.
				} catch (IOException e) {
					
					System.err.printf("[%s:%d  Error]: Peer has timed out.", peerAddress.adress, peerAddress.port);
				}
				
				
				
				
			}
			
			
		} // End of runLoop
		
	}

}
