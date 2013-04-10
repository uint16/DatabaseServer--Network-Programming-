package DatabaseHelper;

public class Table {

	// Will hold columns
	private String[] columnsNames;
	private Column[] columns;
	
	// Table properties
	private String tableName;
	
	// -------------------- GETTERS -----------------------
	public String getTableName() {
		return tableName;
	}

	public int getTableWidth() {
		return tableWidth;
	}

	public int getTableLength() {
		return tableLength;
	}
	
	// -----------------------------------------

	private int tableWidth;
	private int tableLength;
	
	/**
	 * Constructor for the Table class
	 * @param _columns Array of all columns with filled data
	 * @param _columnsNames Array of column names
	 */
	public Table(final String _tableName,final Column[] _columns,final String[] _columnsNames){
		columns=_columns;
		columnsNames = _columnsNames;
		
		
		tableName=_tableName;
		
		
	}

	/**
	 * Gets column at index
	 * @param index index of the column as integer
	 * @return Column object or null if error occures, prints error as well
	 */
	public Column getColumn(final int index){
		
		try {
			
			return columns[index];
		}catch (Exception  e){
			
			System.err.println("Requested non-existing index, null is returned");
			return null;
		}
		 
	}
	
	/**
	 * Gets a columns specified by name
	 * @param name Name f the column, case-sensitive.
	 * @return null if column with this name is not found, Column object if it is found
	 */
	public Column getColumn(final String name){
		
		// Scan through all table names
		for (int i=0;i<columns.length;i++){
			
			if (name.equals(columnsNames[i])) return columns[i];
			
		}
		
		
		
		return null; // If column not found
	}
	
	
}
