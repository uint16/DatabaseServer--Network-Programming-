package DatabaseHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;

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

	}

	/**
	 * Function builds a Table object based on passed parameters. Table will be
	 * ready to use.
	 * 
	 * @param tableName
	 *            name of table to search in
	 * @param columnsString
	 *            String that represents columns to be selected from table
	 * @return Table under specified format conditions. Should be in format
	 *         "where ... ", or " " if no conditions.
	 * @throws SQLiteException
	 *             If exception occurred during SQL execution
	 */
	private Table getTable(final String tableName, final String columnsString,
			final String conditions) throws SQLiteException {

		// Open database to fill the whole table
		SQLiteConnection db;
		db = new SQLiteConnection(dbFile);

		db.open();

		Table returnTable = null; // Table to return that will be updated in the

		String sql = String.format("SELECT %s FROM %s %s", columnsString,
				tableName, conditions);

		System.out.println("SQL Performed :" + sql);
		// end.
		// Get whole table data
		SQLiteStatement tableRows = db.prepare(sql);

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
				for (int i = 0; i < tableRows.columnCount(); i++) {
					tableColumns[i].addObject(tableRows.columnString(i));

					// TODO Instantiate a column of the same type as fields in
					// the database
				}

			}

			returnTable = new Table(tableName, tableColumns, tableColumnsNames); // Update
																					// table

			// In case if something crashes, cursor still needs to be closed
		} finally {

			tableRows.dispose();
			db.dispose();
		}

		// Close Database in the end of usage

		return returnTable;
	}

	/**
	 * A class that gets a full table. No arguments, no columns
	 * 
	 * @param tableName
	 * @return
	 */
	public Table getFullTable(String tableName) {

		try {
			return getTable(tableName, "*", "");
		} catch (SQLiteException e) {

			System.out
					.println("Error During getting the Table, null will be returned");

			return null;
		}

	}

	/**
	 * Function gets specific columns from the table without conditions
	 * 
	 * @param tableName
	 *            Name of the table
	 * @param columnNames
	 *            Array of columns
	 * @return Table in case of success, null in case of failure + Error is
	 *         printed
	 */
	public Table getTableWithColumns(String tableName, String[] columnNames) {
		try {

			String columnString = "";

			// construct string in format "a,b,c,d,e"
			for (int i = 0; i < columnNames.length; i++) {

				columnString += " " + columnNames[i] + ",";

			}

			columnString = columnString.substring(0, columnString.length() - 1); // Chop
																					// off
			// last
			// comma

			return getTable(tableName, columnString, "");
		} catch (SQLiteException e) {

			System.out
					.println("Error During getting the Table, null will be returned");

			e.printStackTrace();

			return null;
		}
	}

	/**
	 * Get a table object where conditions are specified (e.g. where clause)
	 * 
	 * @param tableName
	 *            name of the table
	 * @param String
	 *            - conditions. e.g. "fieldname="value" AND fieldname2="value" "
	 * @return null if error, correct Table if no error. Error is printed if it
	 *         occures.
	 */
	public Table getTableWithConditions(String tableName, String conditions) {

		try {
			return getTable(tableName, "*", " where " + conditions);
		} catch (SQLiteException e) {

			System.err
					.println("Error During getting the Table, null will be returned");

			return null;
		}

	}

	/**
	 * Gets Table object where columns and conditions are specified
	 * 
	 * @param tableName
	 *            Name of the table
	 * @param columnNames
	 *            array of column names to extract
	 * @param conditions
	 *            conditions without where
	 * @return Table object if no error, null if error is catched + warning is
	 *         printed
	 */
	public Table getTableWithFieldsAndColumns(String tableName,
			String[] columnNames, String conditions) {
		try {

			String columnString = "";

			// construct string in format "a,b,c,d,e"
			for (int i = 0; i < columnNames.length; i++) {

				columnString += " " + columnNames[i] + ",";

			}

			columnString = columnString.substring(0, columnString.length() - 1); // Chop
																					// off
			// last
			// comma

			return getTable(tableName, columnString, "where " + conditions);
		} catch (SQLiteException e) {

			System.err
					.println("Error During getting the Table, null will be returned");

			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Executes sql without any return
	 * 
	 * @param sql
	 *            SQL query
	 */
	public void executeSQL(final String sql) {

		SQLiteConnection db;
		db = new SQLiteConnection(dbFile);

		try {

			db.open();

			db.exec(sql);

		} catch (Exception e) {

			System.err.println("DB sql execution failed. Check your SQL query");
			db.dispose();
		}

		db.dispose();
	}

	/**
	 * Executes an SQL and outputs a string with all SQL results
	 * 
	 * @param sql
	 *            SQLite string to query
	 * @return returns "" if error happened + prints an error message.
	 */
	public String getSQLQueryAsString(final String sql) {
		String returnString = "";

		SQLiteConnection db = new SQLiteConnection(dbFile);

		try {
			db.open();
			SQLiteStatement sqlResult = db.prepare(sql);

			// Scroll through all rows
			while (sqlResult.step()) {

				// And through each element in the row.
				for (int i = 0; i < sqlResult.columnCount(); i++) {
					returnString += sqlResult.columnString(i) + " ";
				}

				returnString += "\n";
			}

			sqlResult.dispose();

		} catch (Exception e) {

			System.err.println("Error occured, lol... Actually it's SQL error");
			e.printStackTrace();

		} finally {
			db.dispose();
			return returnString;
		}

	}

	/**
	 * Gets a single field from sql query. The purpose of this method when SQL
	 * query assumes one object
	 * 
	 * @param sql
	 *            SQL query that constructed in such a way that first object
	 *            will be in first column first room
	 * @return first object in SQL response or "" in case of crash
	 */
	@SuppressWarnings("finally")
	public String getSingleField(final String sql) {

		String returnString = "";
		SQLiteConnection db = new SQLiteConnection(dbFile);
		try {

			db.open();
			SQLiteStatement sqlResult = db.prepare(sql);

			// Scroll through all rows
			if (sqlResult.step()) {

				returnString = sqlResult.columnString(0);

			}

			sqlResult.dispose();

		} catch (Exception e) {

			System.err.println("Error occured, lol... Actually it's SQL error");
			e.printStackTrace();

		} finally {
			db.dispose();
			return returnString;
		}

	}

	/**
	 * Gets generalized time from string with format "yyyymmddhhmmss.MMMZ"
	 * 
	 * @param Date to convert
	 * @return Calendar object
	 */
	public Calendar getGeneralizedTime(String lastSyncDate) {
		Calendar calTime = Calendar.getInstance();
		
		int year, month, day, hour, minutes, seconds;
		
		// First four characters will be year
		year=Integer.parseInt(lastSyncDate.substring(0,4));
		month=Integer.parseInt(lastSyncDate.substring(4,6));
		day=Integer.parseInt(lastSyncDate.substring(6,8));
		hour=Integer.parseInt(lastSyncDate.substring(8,10));
		minutes=Integer.parseInt(lastSyncDate.substring(10,12));
		seconds=Integer.parseInt(lastSyncDate.substring(12,14));
		
		
		calTime.set(year, month, day, hour, minutes, seconds);
		return calTime;
	}

}
