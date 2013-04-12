

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Calendar;

import ASN1Encoder.ASN1DecoderFail;
import ASN1Encoder.Decoder;
import ASN1Encoder.Encoder;


public class ServerThread implements Runnable {

	
	SocketChannel clientSocket;
	ServerManager manager;
	
	
	public boolean LOG=true; //  Turn it off if you don't to see log
	
	/**
	 * Constructor for the class.
	 * @param clientSocket socket object to handle connections...
	 * @param manager manager to recall
	 */
	public ServerThread(SocketChannel _clientSocket, ServerManager _manager){
	
		clientSocket=_clientSocket;
		manager=_manager;
		
	}


	@Override
	public void run() {
		
		// For now, it will just decode request and print it
		
		// Decode first two bytes to identify length
		
		
		
		ByteBuffer initBuffer=ByteBuffer.allocate(2);
		
		try {
			
			// Read first to bytes. According to structure, second byte should define whole message
			clientSocket.read(initBuffer);
			int bufferSize=initBuffer.get(1);
			
			ByteBuffer wholeRequest=ByteBuffer.allocate(bufferSize+2);
			wholeRequest.put(initBuffer.array()); // Add intial request
			
			clientSocket.read(wholeRequest);
			
			
			// Try to decode requested request
			Decoder requestDecoder=new Decoder(wholeRequest.array());
		
			requestDecoder=requestDecoder.getContent();
		
		
			if (LOG){
		
			System.out.println("Request Proceeded, request is following:");
			System.err.println("--------------------------------------------------");
			
			System.out.printf("%20s | %25s%n", "Field Name", "Field Value");
			System.out.println("_____________________|___________________________");
			
			
			}
			/*
			 
			  This is required request. Recover it
			 
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
			if (LOG){
			System.out.printf("%20s | %25s%n", "Version", requestDecoder.getFirstObject(true).getString());
			String lastSnapshot = requestDecoder.getFirstObject(true).getString();
			System.out.printf("%20s | %25s%n", "Snapshot", lastSnapshot);
			
			} else {
				
				// Ignore those inputs
				requestDecoder.getFirstObject(true);
				requestDecoder.getFirstObject(true);
			}
			
			
			// Get list of table names.
			ArrayList<String> tableNames=requestDecoder.getFirstObject(true).getSequenceOfAL(Encoder.TAG_UTF8String);

			if (LOG){
			
			for (int i=0;i<tableNames.size();i++){
				
				System.out.printf("%20s | %25s%n", "Table Name", tableNames.get(i));
				
			}
			}
			
			if (LOG){
			
			System.out.printf("%20s | %25s%n", "Signature", requestDecoder.getFirstObject(true).getString());
			
			System.err.println("--------------------------------------------------");
			
			} else {
				
				requestDecoder.getFirstObject(true);
				
			}
			
			clientSocket.close();
			
			manager.removeThread(this); 
			
			
			// Construct DER output to the client according to database.asn
			
			
			
			
			
			
			
		} catch (IOException e) {
			System.err.println("Thread connection error");
			e.printStackTrace();
		} catch (ASN1DecoderFail e) {
			System.err.println("Decoding request error");
			e.printStackTrace();
		}
		
		
		
		
		
	}
	
	
	
	
	
	
	
}
