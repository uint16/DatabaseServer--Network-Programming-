import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Calendar;

import com.almworks.sqlite4java.SQLiteException;

import ASN1Encoder.ASN1DecoderFail;
import ASN1Encoder.Decoder;
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
	private boolean LOG=false;
	boolean runLoop = true; // Identifies whether user loop should be runned or
							// not. Can be stopped by internal error or other
							// thread.

	
	
	// Not very efficient, but will be used
	private ArrayList<String[]> receivedPeerRows;
	private ArrayList<String[]> receivedPeerAddressRows;
	
	
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
					ASNSyncRequest.addToSequence(new Encoder(db.getGeneralizedTime(lastSyncDate)));
					
					Encoder TableNames=new Encoder();
					
					// Construct sequence of tables
					TableNames.initSequence();
					TableNames.addToSequence(new Encoder("peer"));
					TableNames.addToSequence(new Encoder("peer_address"));
					
					ASNSyncRequest.addToSequence(TableNames);
					
					// Some lines are commented out, so I add signature
					
					ASNSyncRequest.addToSequence(new Encoder("NULL"));
					
					
					test.write(ByteBuffer.wrap(ASNSyncRequest.getBytes())); // Send Bytes over the network
					
					
					
					// Data sent, receive data, decode it and update database
					
					
			
					ByteBuffer serverResponse=ByteBuffer.allocate(4000); // Just cheat, read 1024 bytes, I'm tired and nothing works
					test.read(serverResponse);
					
					
					/*
					
					 ASNDatabase ::= SEQUENCE {
					tables SEQUENCE OF Table,
					snapshot GeneralizedTime OPTIONAL
					}
					 
				*/
						Decoder responseDecoder=(new Decoder(serverResponse.array()));
						
						
						if (LOG){
						System.out.println("Recvd Data length: "+responseDecoder.getBytes().length);
						System.out.println("Recvd Data: "+responseDecoder.toString());
						}
						
						
						responseDecoder=responseDecoder.getContent();
						
						
						Decoder tablesDecoder=responseDecoder.getFirstObject(true).getContent();
						
						// Decode peer and peer_address
						processEncodedTable(tablesDecoder.getFirstObject(true));
						processEncodedTable(tablesDecoder.getFirstObject(true));
						//
						
						String responseTime=responseDecoder.getFirstObject(true).getGeneralizedTime_();
						
						if (LOG){
						System.out.println("Response Time: "+responseTime);
						}
						
						// Process Database
						updateDB();
						
						
						
						// For the current peer we should update Sync date
						db.executeSQL("UPDATE peer SET last_sync_date='"+Encoder.getGeneralizedTime(Calendar.getInstance())+"'  WHERE peer_id='"+peer_id+"'");
						//runLoop=false;
					 
					
					// For the purposes of one-connection test, close the loop after first conneciton
					//runLoop=false;
					
					
				
				// Will throw an error when timeout is reached. Prints error.
				} catch (IOException e) {
					
					System.err.printf("[%s:%d  Error]: Peer has timed out.", peerAddress.adress, peerAddress.port);

					//runLoop=false;
				}catch (Exception e) {
					System.err.println("Response Failed to decoded");
					e.printStackTrace();

				
				}
		
				
				
				
				
			}
			
			
		} // End of runLoop
		
	}

	/**
	 * Updates peers id
	 */
	private void updateDB() {
		
		try {
			// THese are all global_ID's
			ArrayList<String> globalPeers=(db.getTable("SELECT global_peer_ID FROM peer")).getColumn(0).getRows();
			
			// Check all new global_id's
			for (int i=0;i<this.receivedPeerRows.size();i++){
				
				if (globalPeers.contains(receivedPeerRows.get(i)[0])){
					
					if (LOG){
						System.out.println("Global Peer Exists, ID: "+receivedPeerRows.get(i)[0]);
					}
				
				// If this global id foesn't appear on the list
				} else {
					db.executeSQL(String.format("INSERT INTO peer (global_peer_ID, name, broadcastable, last_sync_date) VALUES (%s, \"%s\", \"%s\", \"%s\") ", receivedPeerRows.get(i)[0],receivedPeerRows.get(i)[1],receivedPeerRows.get(i)[2],receivedPeerRows.get(i)[3]));
					db.executeSQL(String.format("INSERT INTO peer_address (peer_address, type, peer_ID) VALUES (\"%s\", \"%s\", %s)", receivedPeerAddressRows.get(i)[0],receivedPeerAddressRows.get(i)[1],db.getSingleField("SELECT peer_id FROM peer WHERE global_peer_ID="+receivedPeerRows.get(i)[0])));
					
				}
				
				
			}
			
			
			
			
		} catch (SQLiteException e) {
			System.err.println("Error Occured while updating database...");
			e.printStackTrace();
		}
		
	}

	private void processEncodedTable(Decoder dec) throws ASN1DecoderFail {
		/*
		Table ::= SEQUENCE {
			name TableName,
			fields SEQUENCE OF FieldName,
			fieldTypes SEQUENCE OF FieldType,
			rows SEQUENCE OF SEQUENCE OF NULLOCTETSTRING
			}
			
			*/
		dec=dec.getContent();
		
		// Get all stuff
		String tableName=dec.getFirstObject(true).getString();
		String[] columnNames=dec.getFirstObject(true).getSequenceOf(Encoder.TAG_UTF8String);
	
		// Skip names
		dec.getFirstObject(true);
		ArrayList<String[]> rows=new ArrayList<String[]>();
		
		
		Decoder rowsDec=dec.getFirstObject(true);
		rowsDec=rowsDec.getContent();
		
		// Load all rows
		while (rowsDec.contentLength()>0){
			
			rows.add(rowsDec.getFirstObject(true).getSequenceOf(Encoder.TAG_UTF8String));
			
			
		}
		
		// Print Received table
		if (LOG) {
		System.out.println("Table Name: "+tableName);
		System.out.println("Column Names: ");
		
		// Print out all column names
		for (int i=0;i<columnNames.length;i++){
			System.out.println("	"+columnNames[i]);
		}
		
		System.out.println("Field Values: ");
		
		for (int i=0;i<rows.size();i++){
			
			for (int j=0;j<rows.get(i).length;j++){
			System.out.print(" "+rows.get(i)[j]);	
			}
			
			System.out.println();
			}
			
		}
		
		
		
		// Now update database, this depends on what table we have
		if (tableName.equals("peer")){
			
			this.receivedPeerRows=rows;
			
		} else {
			
			this.receivedPeerAddressRows=rows;
		}
	}

	


}
