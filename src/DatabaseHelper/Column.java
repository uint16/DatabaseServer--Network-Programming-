package DatabaseHelper;

import java.util.ArrayList;

/**
 * Simple column object for Table class that holds rows and methods to access them
 * @author atroshchenko2011
 *
 */
public class Column<Type> {


	private ArrayList<Type> rows;
	
	
	private boolean isEmpty=true;
	
	
	public ArrayList<Type> getRows() {
		return rows;
	}


	/**
	 * Instantiates column that holds undefined amount of objects.
	 * @param columnId
	 */
	public Column(){
		
		rows=new ArrayList<Type>();
		
	}
	

	/**
	 * Adds object to the end of a column
	 * @param obj Inserts specific objject in the end of the column
	 */
	public void addObject(final Type obj){
		rows.add(obj);
		isEmpty=false;
	}
	
	/**
	 * Gets object at the specific row
	 * @param row
	 * @return
	 */
	public Type getObjectAtRow(final int row){
		
		// Check borders
		if (row>rows.size()||row<0) return null;
		
		
		return rows.get(row);
		
	}
	
	
	
	/**
	 * Inserts object after specific index
	 * @param row row number to insert into
	 * @param obj Object to insert
	 * @return false if index out of range, true if object is inserted.
	 */
	public boolean insertObj(final int row, final Type obj){
	
		// Check borders
		if (row>rows.size()||row<0) return false;
		
		
		// Insert object and return true
		rows.add(row, obj);
		return true;
		
	
	}
	
	/**
	 * Gets the size of the row
	 * @return size of the row, int
	 */
	public int size(){
		
		return rows.size();
	}

	
	
	// TODO Implement other methods for columns like delete last, delete at position and etc
	
}
