
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Calendar;

import com.almworks.sqlite4java.SQLiteException;

import ASN1Encoder.ASN1DecoderFail;
import ASN1Encoder.Decoder;
import ASN1Encoder.Encoder;
import DatabaseHelper.Database;
import DatabaseHelper.Table;

public class ServerThread implements Runnable {

	SocketChannel clientSocket;
	ServerManager manager;

	public boolean LOG = false; // Turn it off if you don't to see log

	/**
	 * Constructor for the class.
	 * 
	 * @param clientSocket
	 *            socket object to handle connections...
	 * @param manager
	 *            manager to recall
	 */
	public ServerThread(SocketChannel _clientSocket, ServerManager _manager) {

		clientSocket = _clientSocket;
		manager = _manager;

	}

	@Override
	public void run() {

		// For now, it will just decode request and print it

		// Decode first two bytes to identify length

		ByteBuffer initBuffer = ByteBuffer.allocate(2);

		try {

			// Read first to bytes. According to structure, second byte should
			// define whole message
			clientSocket.read(initBuffer);
			int bufferSize = initBuffer.get(1);

			ByteBuffer wholeRequest = ByteBuffer.allocate(bufferSize + 2);
			wholeRequest.put(initBuffer.array()); // Add intial request

			clientSocket.read(wholeRequest);

			// Try to decode requested request
			Decoder requestDecoder = new Decoder(wholeRequest.array());

			requestDecoder = requestDecoder.getContent();

			if (LOG) {

				System.out.println("Request Proceeded, request is following:");
				System.err
						.println("--------------------------------------------------");

				System.out.printf("%20s | %25s%n", "Field Name", "Field Value");
				System.out
						.println("_____________________|___________________________");

			}
			/*
			 * 
			 * This is required request. Recover it
			 * 
			 * ASNSyncRequest ::= [APPLICATION 7] SEQUENCE { version UTF8String,
			 * -- currently 2 lastSnapshot GeneralizedTime OPTIONAL, tableNames
			 * [APPLICATION 0] SEQUENCE OF TableName OPTIONAL, -- orgFilter
			 * [APPLICATION 1] SEQUENCE OF OrgFilter OPTIONAL, -- address
			 * [APPLICATION 2] D_PeerAddress OPTIONAL, -- request [APPLICATION
			 * 3] SpecificRequest OPTIONAL, -- plugin_msg [APPLICATION 4]
			 * D_PluginData OPTIONAL, -- plugin_info [APPLICATION 6] SEQUENCE OF
			 * ASNPluginInfo OPTIONAL, -- pushChanges ASNSyncPayload OPTIONAL,
			 * signature NULLOCTETSTRING -- prior to version 2 it was
			 * [APPLICATION 5] }
			 */

			String version = requestDecoder.getFirstObject(true).getString();
			// Calendar
			// lastSnapshot=(requestDecoder.getFirstObject(true).getGeneralizedTimeCalender(Encoder.TAG_GeneralizedTime));
			String lastSnapshot = requestDecoder.getFirstObject(true)
					.getGeneralizedTime(Encoder.TAG_GeneralizedTime);

			if (LOG) {
				System.out.printf("%20s | %25s%n", "Version", version);

				System.out.printf("%20s | %25s%n", "Snapshot", lastSnapshot);

			}

			// Get list of table names.
			ArrayList<String> tableNames = requestDecoder.getFirstObject(true)
					.getSequenceOfAL(Encoder.TAG_UTF8String);

			if (LOG) {

				for (int i = 0; i < tableNames.size(); i++) {

					System.out.printf("%20s | %25s%n", "Table Name",
							tableNames.get(i));

				}
			}

			String signature = requestDecoder.getFirstObject(true).getString();

			if (LOG) {

				System.out.printf("%20s | %25s%n", "Signature", signature);

				System.err
						.println("--------------------------------------------------");

			}

			// Prepare Response.

			/*
			 * ASNDatabase ::= SEQUENCE { tables SEQUENCE OF Table, snapshot
			 * GeneralizedTime OPTIONAL }
			 */

			// Verification
			if (!version.equals("2")) {

				System.err.println("Version is supposed to be 2");

			} else if (!tableNames.contains("peer")) {

				System.err
						.println("Client supposed to request for peer table, but he doesn't");
			} else if (!tableNames.contains("peer_address")) {
				System.err
						.println("Client supposed to request peer_address table");

			} else if (tableNames.size() > 2) {
				System.err
						.println("Client is supposed to request only two tables, peer and peer_address");

				// All error-checks are passed. Let's move
			} else {

				Encoder response=new Encoder();
				response.initSequence();
				
				
				Encoder tablesEncoder = new Encoder();
				tablesEncoder.initSequence();

				tablesEncoder
						.addToSequence(tableEncoder("SELECT global_peer_ID, name, broadcastable, last_sync_date FROM peer WHERE broadcastable != 0 AND arrival_date > \""+lastSnapshot+"\"", "peer"));
				
				
				
				tablesEncoder.addToSequence(tableEncoder("SELECT peer_address.address AS address, peer_address.type AS type, peer.global_peer_ID AS peer_ID FROM peer JOIN peer_address ON peer.peer_ID=peer_address.peer_ID WHERE broadcastable != 0 AND peer_address.arrival_date > \""+lastSnapshot+"\"", "peer_address"));
			
				
				response.addToSequence(tablesEncoder);
				response.addToSequence(new Encoder(Calendar.getInstance()));
				
				
				
				// Data has been packed, send it
				clientSocket.write(ByteBuffer.wrap(response.getBytes()));
				System.out.println("Sent data length: "+response.getBytes().length);
				System.out.println("Sent Data: "+response.toString());
			}

			// Close the connection
			clientSocket.close();
		} catch (IOException e) {
			System.err.println("Thread connection error");
			e.printStackTrace();
		} catch (ASN1DecoderFail e) {
			System.err.println("Decoding request error");
			e.printStackTrace();
		} catch (SQLiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			manager.removeThread(this);

		}

	}

	public Encoder tableEncoder(String sql, String name) throws SQLiteException {

		/*
		 * Pack peers table Table ::= SEQUENCE { name TableName, fields SEQUENCE
		 * OF FieldName, fieldTypes SEQUENCE OF FieldType, rows SEQUENCE OF
		 * SEQUENCE OF NULLOCTETSTRING }
		 */
		Database db = manager.getDB();

		Table peer=db.getTable(sql);
		
		Encoder peerTableEncoder = new Encoder(); // Table Encoder <-------
		
		peerTableEncoder.initSequence();
		peerTableEncoder.addToSequence(new Encoder(name));

		// Add all columns objects to Encoder
		Encoder peerColumnNamesEncoder = new Encoder();
		peerColumnNamesEncoder.initSequence();
		String[] peerColumnNames = peer.getColumnsNames();
		for (int i = 0; i < peerColumnNames.length; i++) {

			peerColumnNamesEncoder
					.addToSequence(new Encoder(peerColumnNames[i]));

		}

		peerTableEncoder.addToSequence(peerColumnNamesEncoder);

		Encoder peerColumnTypesEncoder = new Encoder();
		peerColumnTypesEncoder.initSequence();

		// Set all fields as texts... because they are not used on client side
		for (int i = 0; i < peerColumnNames.length; i++) {

			peerColumnTypesEncoder.addToSequence(new Encoder("TEXT"));

		}

		peerTableEncoder.addToSequence(peerColumnTypesEncoder);
		
		// Now all rowss
		Encoder rowsEncoder = new Encoder();
		rowsEncoder.initSequence();

		// For each row add elements to Sequence, so sequence of sequences will
		// be constructed
		if (LOG)
		System.out.println("Table "+name+" fields results count: "+peer.getColumn(0).size());
		for (int i = 0; i < peer.getColumn(0).size(); i++) {
			Encoder rowEncoder = new Encoder();
			rowEncoder.initSequence();

			for (int j = 0; j < peerColumnNames.length; j++) {
				Object var=peer.getColumn(j)
						.getObjectAtRow(i);
				
				if (var!=null){
				
				rowEncoder.addToSequence(new Encoder(var.toString()));
				} else {
				
				rowEncoder.addToSequence(new Encoder(" "));
				}
			}

			rowsEncoder.addToSequence(rowEncoder);
		}

		peerTableEncoder.addToSequence(rowsEncoder);

		return peerTableEncoder;
	}

}
