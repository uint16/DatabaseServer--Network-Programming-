import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Calendar;

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
			
			// Wait for 5 seconds before actually refreshing
			
			
			
			
			// Get all peers from the database
			Table peer_address=db.getTableWithColumns("peer_address", new String[]{"peer_id", "address"});
			
			@SuppressWarnings("unchecked")
			Column<String> peer_address_address_column=peer_address.getColumn("address");
			
			// For each peer, try to connect and update list of peers here 
			for (int i=0;i<peer_address_address_column.size();i++){
				
				// Get address of this peer
				InetAddress peerAddress=new InetAddress(peer_address_address_column.getObjectAtRow(i));
				
				
				SocketChannel test;
				
				try {
					
					System.out.printf("Trying to connect to %s:%d%n", peerAddress.adress, peerAddress.port);
					test=SocketChannel.open();
					
					// I didn't figure out how to setup timeout
					test.socket().connect((new InetSocketAddress(peerAddress.adress, peerAddress.port))); // 2000 is a timeout specified in assignment instructions
				
					// At this point client is connected.
					
					
					// Get last sync date 
					String peer_id=(String) peer_address.getColumn("peer_ID").getObjectAtRow(i); // Gets a peer_id. Since it should be the same, we can find this peer_id in peer table 
					
					String lastSyncDate=db.getSingleField("SELECT last_sync_date FROM peer WHERE peer_id='"+peer_id+"'");
					
					
					
					
					// Construct request
					
					// message example { 20130410202659.999Z, tables{ ... , ... }}
					Encoder ASNSyncRequest=new Encoder();
					ASNSyncRequest.initSequence();
					/*
					 
					  This is required request. Build it
					 
					ASNSyncRequest ::= [APPLICATION 7] SEQUENCE {
						version UTF8String, -- currently 2
						lastSnapshot GeneralizedTime OPTIONAL,
						tableNames [APPLICATION 0] SEQUENCE OF TableName OPTIONAL,
						-- orgFilter [APPLICATION 1] SEQUENCE OF OrgFilter OPTIONAL,
						-- address [APPLICATION 2] D_PeerAddress OPTIONAL,
						-- request [APPLICATION 3] SpecificRequest OPTIONAL,
						-- plugin_msg [APPLICATION 4] D_PluginData OPTIONAL,
						-- plugin_info [APPLICATION 6] SEQUENCE OF ASNPluginInfo OPTIONAL,
						-- pushChanges ASNSyncPayload OPTIONAL,
						signature NULLOCTETSTRING -- prior to version 2 it was [APPLICATION 5]
						}
					
					*/
					
					ASNSyncRequest.addToSequence(new Encoder("2")); // Version
					
					// We need a timestamp in Generalized Time
					ASNSyncRequest.addToSequence(new Encoder(lastSyncDate));
					
					Encoder TableNames=new Encoder();
					
					// Construct sequence of tables
					TableNames.initSequence();
					TableNames.addToSequence(new Encoder("peer"));
					TableNames.addToSequence(new Encoder("peer_date"));
					
					ASNSyncRequest.addToSequence(TableNames);
					
					// Some lines are commented out, so I add signature
					
					ASNSyncRequest.addToSequence(new Encoder("NULL"));
					
					
					test.write(ByteBuffer.wrap(ASNSyncRequest.getBytes())); // Send Bytes over the network
					
					// For the purposes of one-connection test, close the loop after first conneciton
					runLoop=false;
					
					
				
				// Will throw an error when timeout is reached. Prints error.
				} catch (IOException e) {
					
					System.err.printf("[%s:%d  Error]: Peer has timed out.", peerAddress.adress, peerAddress.port);
					
				}
				
				
				
				
			}
			
			
		} // End of runLoop
		
	}



}
