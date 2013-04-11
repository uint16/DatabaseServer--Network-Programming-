

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public class ServerThread implements Runnable {

	
	SocketChannel clientSocket;
	ServerManager manager;
	
	
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
			clientSocket.read(initBuffer);
			
			System.out.println(initBuffer.get(2));
			
			
			
			clientSocket.close();
			
			manager.removeThread(this); 
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
	}
	
	
	
	
	
	
	
}
