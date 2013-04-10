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
	private class InetAdress {

		public int port;
		public String adress;

		public InetAdress(String unf_adress) {

			// Get a colon
			for (int i=0;i<unf_adress.length();i++){
				
				if (unf_adress.charAt(i)==':'){
					String 
					
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
			Table peers=db.getTableWithColumnds("peer_adress", new String[]{"global_peer_id", ""})
			
			
			
			
		} // End of runLoop
		
	}

}
