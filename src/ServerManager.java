import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import DatabaseHelper.Database;


public class ServerManager implements Runnable {


	
	public boolean runServer=true;
	private Database db;
	private int maxClients;
	private List<ServerThread> clients;
	private int port;
	
	
	public ServerManager(Database _db, int _maxClients, int _port){
		
		db=_db;
		maxClients=_maxClients;
		port=_port;
	}
	
	
	private void startListening () throws IOException{
	
		// Start Server on specific port
		clients=new LinkedList();
		ServerSocketChannel server= ServerSocketChannel.open();
		server.socket().bind(new InetSocketAddress(port));
		
		System.out.println("Server started on port "+port);
		
		// While nobody stopped server, keep accepting clients
		while (runServer){
			
			SocketChannel clientSocket;
			
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
		System.out.printf("Client  Disconnected, clients count: %d. %n",  clients.size());
		
	}


	@Override
	public void run() {
		try {
			startListening();
		} catch (IOException e) {
			System.err.println("Error occured while starting up a server");
			e.printStackTrace();
		}
		
	}
		
	
	/**
	 * Get a database
	 * @return
	 */
	public Database getDB(){
		
		return db;
		
	}
	
	

}
