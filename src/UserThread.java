/**
 * Author: Alexander Troshchenko, Damas Mlabwa
 * Course: CSE 4232, Spring 2013
 * Assignment: p3
 * Date: April 10,2013
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import DatabaseHelper.Database;

public class UserThread extends Thread {

    private BufferedReader input;
    private final String fileName;
    private Database db;

    /**
     * Create UserThread object
     * @param filename
     *            database file name to execute SQL queries on
     */
    public UserThread(final String filename) {
        fileName = filename;
        try {
            db = new Database(fileName);
        } catch (final FileNotFoundException e) {

            e.printStackTrace();
        }
    }

    /**
     * Introduce user to how to perform SQL queries on database.
     */
    public final void startHere() {
        System.out
                .print("To use this P2P system please enter a valid SQL query, invalid SQL queries will not be handled and the program will exit.\n");
        System.err
                .print("Example of valid SQL queries: \"insert\", \"delete\".\n");
    }

    /**
     * Receive SQL Queries from user, push query to runQuery() for processing.
     */
    public final void receiveQuery() {
        input = new BufferedReader(new InputStreamReader(System.in));
        boolean receivingQuery = true;
        System.out.println("Type \"exit\" to leave query input");
        while (receivingQuery) {

            try {
                final String sqlCommand = input.readLine();

                if (sqlCommand.equals("exit")) {
                    receivingQuery = false;
                    input.close();
                    System.exit(0);
                }

                runQuery(sqlCommand);
            } catch (final IOException e) {

                e.printStackTrace();
            }
        }
    }

    /**
     * Receive SQL query from user, execute the query to the database.
     * @param command
     *            SQL query from the user
     */
    public final void runQuery(final String command) {

        db.executeSQL(command);
    }

}
