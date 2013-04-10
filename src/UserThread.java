import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UserThread extends Thread {
    
    BufferedReader input;
    
    public UserThread(){
        
    }

   

    public void startHere() {
        System.out
                .print("To use this P2P system please enter a valid SQL query, invalid SQL queries will not be handled and the program will exit.\n");
        System.err.print("Example of valid SQL queries: \"insert\", \"delete\".\n");
    }
    
    public void receiveQuery(){
        input = new BufferedReader(new InputStreamReader(System.in));
        boolean receivingQuery = true;
        
        while(receivingQuery == true){
            
            try {
                String SQLCommand = input.readLine();
                
                runQuery(SQLCommand);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public void runQuery(String command){
        
    }

    public static void main(String[] args) {
        UserThread test = new UserThread();
        
        test.startHere();
    }

}
