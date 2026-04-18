/**
 * Skeleton + Procedure shamelessly stolen from HW4 assignment
 */
import java.sql.*;
import java.util.Scanner;
import java.util.InputMismatchException;

/** Provided to CSE241 Spring 2026
 * This class uses System.console() to protect the user's password from displaying (System.in would show it).
 * It also shows an example of creating a statement, executing a query, and using the result set.
 * 
 * To compile: `javac ConnectionConsole.java`
 * To run class file: `java -cp "ojdbc11.jar:." ConnecstionConsole`
 *     on windows, use a semicolon rather than a colon (i.e. "ojdbc11.jar;." rather than "ojdbc11.jar:.")
 *     Assumes you have ojdbc*.jar alongside the file (e.g. ojdbc11.jar) 
 *       get requisite jar from: https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html
 * To build jar: `jar cfmv SimpleTest.jar Manifest.txt ConnectionConsole.class`
 *     Assumes you have plain text file `Manifest.txt` with contents (may need an empty 3rd line on some hosts)
 *         Main-class: ConnectionConsole
 *         Class-Path: ojdbc11.jar
 * To run your jar: `java -jar SimpleTest.jar`
 */

public class DatabaseCLI {
    private static final String DB_URL = "jdbc:oracle:thin:@//rocordb01.cse.lehigh.edu:1522/cse241pdb";

    public static void main(String[] args){
        System.out.println("Connecting to Oracle database...");
        dbLogin(args);
    }

    /**
     * Login to database and then run the 
     * @param args
     */
    public static void dbLogin(String[] args) {
        System.out.println("---Attempting To Connect to DB---");
        // Try-with-resources to manage connection --- auto closes conn
        try (Connection conn = attemptLogin()) {
            System.out.println("Logged into Database successfully!");
            userLogin(conn);
        } catch (SQLException e) {
            System.err.println("[Error]: Connect error, re-enter login data.");
            //e.printStackTrace(); //debug
        }
    }

    /** get user name and password using Console (so password is not seen)
     * @return uname in [0], pword in [1]
     * */
    static String[] getUserAndPass(){
        java.io.Console in = System.console();
        if (in == null) {
            System.err.println("[Error]: Could not get console instance.");
            System.exit(1);
        }
        System.out.print("Enter Oracle user id: ");
        String user = in.readLine();
        System.out.print("Enter Oracle user password: ");
        String pass = new String(in.readPassword());
        System.out.flush();
        return new String[]{user, pass};
    }

    /**
     * This function allows the user to login to the database and creates a connection. Allows the user to retry if they fail to enter their info correctly.
     * @return JDBC Connection object
     */
    static Connection attemptLogin() {
        while (true) {
            String[] usrPass = getUserAndPass();
            try {
                return DriverManager.getConnection(DB_URL, usrPass[0], usrPass[1]);
            } catch (SQLException e) {
                System.err.println("[Error]: Connect error, re-enter login data.");
                //e.printStackTrace(); //Probably unnecessary
            }
        }
    }

    /**
     * Allows use to query database to obtain students with a name like the input. Sanitized
     * @param conn Database connection to query
     * @param scn Scanner to use
     */
    static void findIDsByName(Connection conn, Scanner scn) throws SQLException {
        ResultSet rs = null;
        PreparedStatement likeSearch = conn.prepareStatement("SELECT id, name FROM student WHERE name LIKE ?");

        while (rs == null || !rs.next()) { //result set is empty.
            System.out.println("Input name for string subsearch:");
            String in = scn.nextLine();
            if (in.contains("'")) {
                System.out.println("A single quote is not allowed in the substring query!");
                continue;
            }
            
            likeSearch.setString(1,"%" + in + "%");
            rs = likeSearch.executeQuery();
        }
        
        System.out.println("Here is a list of all students that match the pattern provided:");
        do {
            Integer id = rs.getInt("id");
            String name = rs.getString("name");
            System.out.println(id.toString() + "\t" + name);
        } while (rs.next()); //since we checked rs.next() above we have to use a do while loop instead, otherwise we skip the first one :(
    }
    
    static void userLogin(Connection conn) {
        String resp = "";
        System.out.println("Welcome!");
        try (Scanner scn = new Scanner(System.in)) {
            while (!resp.equals("q") && !resp.equalsIgnoreCase("quit")) {
                System.out.println("Would you like to login to the customer (c), general management (gm) or location management (lm) interface? You may also quit (q)");
                resp = scn.nextLine();
                if (resp.equalsIgnoreCase("C") || resp.equalsIgnoreCase("customer")) {
                    cInterface(conn, scn);
                }

                else if (resp.equalsIgnoreCase("LM") || resp.equalsIgnoreCase(("Location Management"))) {
                    lmInterface(conn,scn);
                }

                else if (resp.equalsIgnoreCase("GM") || resp.equalsIgnoreCase("General Management")) {
                    gmInterface(conn, scn);
                }

                else if (!resp.equals("q") && !resp.equalsIgnoreCase("quit")) {
                    System.out.println("Please pick a valid interface!");
                }
            }
        } catch (Exception e) {
            System.err.println("An unexpected error occured.");
            e.printStackTrace();
        }
    }

    static void cInterface(Connection conn, Scanner scn) {
        System.out.println("Enter your customer id:");
        return;
    }

    static void lmInterface(Connection conn, Scanner scn) {
        System.out.println("Enter your location manager id:");
        return;
    }

    static void gmInterface(Connection conn, Scanner scn) {
        System.out.println("Enter your general manager id:");
        return;
    }

    /** static init block ensures driver is available to fail-fast; can safely be removed */
    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch(ClassNotFoundException e) {
            System.err.println("[Error]: Could not load required jdbc driver.");
            System.exit(1);
        }
    }
}
