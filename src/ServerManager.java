import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import DatabaseHelper.Database;


public class ServerManager {


	
	public boolean runServer=true;
	private Database db;
	private int maxClients;
	List<ServerThread> clients;
	SocketChannel clientSocket;
	
	
	
	public ServerManager(Database _db, int _maxClients){
		
		db=_db;
		maxClients=_maxClients;
		
	}
	
	
	public void startListening (int port) throws IOException{
	
		// Start Server on specific port
		clients=new LinkedList();
		ServerSocketChannel server= ServerSocketChannel.open();
		server.socket().bind(new InetSocketAddress(port));
		
	
		// While nobody stopped server, keep accepting clients
		while (runServer){
			
			clientSocket=server.accept();
			
			// Check if we exceed max number of clients
			if (clients.size()<=maxClients){
			
				
			// Department
			System.out.printf("Client connected %n Client Data: %n --------------------------- %n Address: %s %n" +
					"Client Count: %d %n" +
					" --------------------------- %n", clientSocket.socket().getInetAddress().getHostAddress(), maxClients);
				

			
			ServerThread st=new ServerThread(clientSocket, this);
			new Thread(st).start();
			
			clients.add(st); 
			
			
			
			} else {
				
				
				System.err.println("Client is disconnected due to exceeded amount of clients. Max Clients is set to: "+maxClients);
				clientSocket.close();
				
			}
			
			
			
			
		}
		
		
		
	}
	
	
	public void removeThread(ServerThread thread){
		
		
		
		
		clients.remove(thread);
		System.out.printf("Client with IP %s Disconnected, clients count: %d. %n", this.clientSocket.socket().getInetAddress().getHostAddress(), clients.size());
		
	}
		
	
	
	

}
