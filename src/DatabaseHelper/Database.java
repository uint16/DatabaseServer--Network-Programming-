package DatabaseHelper;

import java.io.File;
import java.io.FileNotFoundException;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

/**
 * Simple Package that makes work with database easier. Has simple methods
 * 
 * @author Alexander Viktorovich Troshchenko, main.snivik@gmail.com
 * 
 */
public class Database {

	private File dbFile;
	private SQLiteConnection db;

	/**
	 * Constructor for the database. Loads a file to the object under specified
	 * filename
	 * 
	 * @param filename
	 * @throws FileNotFoundException
	 *             if database file doesn't exist or it's a directory
	 */
	public Database(String filename) throws FileNotFoundException {

		// Check whether file exist and assign it if it does.
		dbFile = new File(filename);

		if (dbFile.exists() == false) {

			throw new FileNotFoundException();

		}

		// If there is directory instead of file, throw error as well.
		if (dbFile.isDirectory()) {

			throw new FileNotFoundException();
		}

		db = new SQLiteConnection(dbFile);
	}

	/**
	 * Function builds a Table object based on passed parameters. Table will be
	 * ready to use.
	 * 
	 * @param tableName
	 *            name of table to search in
	 * @param columnsString
	 *            String that represents columns to be selected from table
	 * @return Table under specified format
	 * @retuen conditions. Should be in format "where ... ", or " " if no conditions.
	 * @throws SQLiteException
	 *             If exception occurred during SQL execution
	 */
	private Table getTable(final String tableName,final String columnsString,
			final String conditions) throws SQLiteException {

		// Open database to fill the whole table
		db.openReadonly();
		Table returnTable=null; // Table to return that will be updated in the end.
		// Get whole table data
		SQLiteStatement tableRows = db.prepare(String.format(
				"SELECT %s FROM %s %s", columnsString, tableName, conditions));

		try {
			// Create Columns
			Column[] tableColumns = new Column[tableRows.columnCount()];
			String[] tableColumnsNames = new String[tableRows.columnCount()];

			// Initialize each column and column name
			for (int i = 0; i < tableRows.columnCount(); i++) {

				// Assign name
				tableColumnsNames[i] = tableRows.getColumnName(i);
				tableColumns[i] = new Column<String>(); // TODO Fix it so column
														// will check a type of
														// field and define
														// object on this basis.

			}

			// Fill the content
			while (tableRows.step()) {

				// Add A field to each column
				for (int i=0;i<tableRows.columnCount();i++){
					tableColumns[i].addObject(tableRows.columnString(i)); // TODO Fix it so column will check a type of field and define object on this basis.	
				}
				

			}

			returnTable=new Table(tableName, tableColumns, tableColumnsNames); // Update table

		// In case if something crashes, cursor still needs to be closed
		} finally {

			tableRows.dispose();

		}

		
		
		
		
		// Close Database in the end of usage
		db.dispose();
		return returnTable;
	}

	/**
	 * A class that gets a full table. No arguments, no columns
	 * @param tableName
	 * @return
	 */
	public Table getFullTable (String tableName){
		
		try {
			return getTable (tableName, "*", "");
		} catch (SQLiteException e) {
			
			System.out.println("Error During getting the Table, null will be returned");
			
			return null;
		} 
		
	}
	
	/**
	 * Function gets specific columns from the table without conditions
	 * @param tableName Name of the table
	 * @param columnNames Array of columns
	 * @return Table in case of success, null in case of failure + Error is printed
	 */
	public Table getTableWithColumnds (String tableName, String[] columnNames){
		try {
			
			String columnString="";
			
			// construct string in format "a,b,c,d,e"
			for (int i=0;i<columnNames.length;i++){
				
				columnString+=" "+columnNames[i]+",";
				
			}
			
			columnString.substring(0, columnString.length()-1); // Chop off last comma
			
			return getTable (tableName, columnString, "");
		} catch (SQLiteException e) {
			
			System.out.println("Error During getting the Table, null will be returned");
			
			return null;
		} 
	}
	
	/**
	 * Get a table object where conditions are specified (e.g. where clause)
	 * @param tableName name of the table
	 * @param String - conditions. e.g. "fieldname="value" AND fieldname2="value" "
	 * @return null if error, correct Table if no error. Error is printed if it occures.
	 */
	public Table getTableWithConditions (String tableName, String conditions){
		
		try {
			return getTable (tableName, "*", " where "+conditions);
		} catch (SQLiteException e) {
			
			System.err.println("Error During getting the Table, null will be returned");
			
			return null;
		} 
		
	}
	
	/**
	 * Gets Table object where columns and conditions are specified
	 * @param tableName Name of the table
	 * @param columnNames array of column names to extract
	 * @param conditions conditions without where
	 * @return Table object if no error, null if error is catched + warning is printed
	 */
	public Table getTableWithFieldsAndColumns(String tableName,  String[] columnNames, String conditions){
	try {
			
			String columnString="";
			
			// construct string in format "a,b,c,d,e"
			for (int i=0;i<columnNames.length;i++){
				
				columnString+=" "+columnNames[i]+",";
				
			}
			
			columnString.substring(0, columnString.length()-1); // Chop off last comma
			
			return getTable (tableName, columnString, "where "+conditions);
		} catch (SQLiteException e) {
			
			System.err.println("Error During getting the Table, null will be returned");
			
			return null;
		} 
		
	
	}
	
	/**
	 * Executes sql without any return
	 * @param sql SQL query
	 */
	public void executeSQL (final String sql){
	
		try{
		db.open();
		
		db.exec(sql);
		
		db.dispose();
		} catch (Exception e){
			
			db.dispose();
			System.err.println("DB sql execution failed. Check your SQL query");
			
		}
		
	}
	
}
