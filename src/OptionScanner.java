import java.util.LinkedList;
import java.util.List;

/**
 * Scans options from the array of arguments. Usage: "-o testoption" will be
 * stored with key o and value testoption. When used getOption(String key)
 * dashes are ignored.
 * 
 * @author Alexadner Troshchenko
 * 
 */
public class OptionScanner {

	// Simple structure to store options
	private class Option {

		public String key;
		public String value;

	}

	private List<Option> options;

	/**
	 * Constructs options that are parsed in command line
	 * 
	 * @param arguments
	 */
	public OptionScanner(String[] arguments) {

		// Don't construct anything, there are no arguments
		if (arguments.length == 0) {
			return;
		}

		// Construct LinkedList of options
		options = new LinkedList<Option>();

		// Run through all arguments
		for (int i = 0; i < arguments.length; i++) {

			String argument = arguments[i];

			// Search for a key identifier
			if (argument.charAt(0) == '-') {

				// If it's the last item in array, store it as a null option
				if (i == arguments.length - 1) {

					Option option = new Option();
					option.key = argument.substring(1);
					options.add(option);

					// If the key is without the value
				} else if (arguments[i + 1].charAt(0) == '-') {

					Option option = new Option();
					option.key = argument.substring(1);
					options.add(option);

					// Option has a value
				} else {

					Option option = new Option();
					option.key = argument.substring(1);
					option.value = arguments[i + 1];
					options.add(option);
				}

			}

		}

	}

	
	/**
	 * Finds a value of the option with specified key, ignores dashes
	 * @param key option key to search for
	 * @return null if option DNE or has no value. Option value otherwise
	 */
	public String getOption(String key){
	
		if (this.options==null){
			return null;
		}
		
		
		
		key=key.replace("-", "");
		
		// Run through all options and find the one that corresponds to key
		for (int i=0;i<options.size();i++){
			
			Option option=options.get(i);
			
			if (option.key.equals(key)){
				
				return option.value;
				
			}
			
			
		}
		
		
		// If option is not found, :C
		return null;
	}
	

	/**
	 * Tries to find an option. Doesn't care about the value
	 * @param key Key of the option, dashes will be ignored
	 * @return false if option DNE, true otherwise
	 */
	public boolean optionExists(String key){
		
		if (this.options==null){
			return false;
		}
		
		
		key=key.replace("-", "");
		
		// Run through all options and find the one that corresponds to key
		for (int i=0;i<options.size();i++){
			
			Option option=options.get(i);
			
			if (option.key.equals(key)){
				
				return true;
				
			}
			
			
		}
		
		
		return false;
	}
	 
	 
}
