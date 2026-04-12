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
        mainTWR(args);
    }

    /** uses try-with-resources to ensure conn correctly closed */
    public static void mainTWR(String[] args) {
        System.out.println("---Using try-with-resources to get connection.---");
        // Try-with-resources to manage connection --- auto closes conn
        try (Connection conn = attemptLogin()) {
            System.out.println("Wecome! :)");
            findTranscripts(conn);
        } catch (SQLException e) {
            System.err.println("[Error]: Connect error, re-enter login data.");
            e.printStackTrace();
        }
    }

    /** example of using jdbc to create a statement, execute query, and use ResultSet*/
    private static void findTranscripts( Connection conn ){
        try (Scanner scn = new Scanner(System.in)) {
            findIDsByName(conn, scn);
            findTakesByID(conn, scn);
            
        } catch (SQLException e) { //Handle SQL errors
            System.out.println("[Error]: SQL error: " + e.getMessage());
            e.printStackTrace();

        } catch (Exception e) { //Handle other errors :(
            System.out.println("Unexpected error occured: " + e.getMessage());
            e.printStackTrace();
        }


        // try( Statement stmt = conn.createStatement();
        //         ResultSet rs = stmt.executeQuery( "SELECT * FROM student FETCH FIRST 10 ROWS ONLY" ); ) {
        //     System.out.println("------------------------------------------");
        //     // --- Process the ResultSet --- Iterate through the data and print it
        //     while (rs.next()) {
        //         int id = rs.getInt("id");
        //         String firstName = rs.getString("name");
        //         String lastName = rs.getString("dept_name");
        //         double gpa = rs.getDouble("tot_cred");
        //         System.out.println("ID: " + id + ", Name: " + firstName + " " + lastName + ", GPA: " + gpa);
        //     }
        //     System.out.println("------------------------------------------");
        //     System.out.println("Finished fetching data.");
        // } catch (SQLException e){
        //     System.err.println("[Error]: Problem executing query" );
        //     e.printStackTrace();
        // }        
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

    /**
     *  Allows use to query database to obtain the classes a student has taken given their id
     * @param conn Database connection to use
     * @param scn Scanner to use
     */
    static void findTakesByID(Connection conn, Scanner scn) throws SQLException {
        ResultSet rs = null;
        String takesQuery;
        PreparedStatement nameSearch = conn.prepareStatement("SELECT name FROM student WHERE id=?");
        PreparedStatement takesSearch = conn.prepareStatement("SELECT year, semester, dept_name, course_id, title, grade FROM takes NATURAL JOIN course WHERE id = ? ORDER BY year ASC, semester DESC");

        int query_id = -1; String name = "null";
        while (rs == null) {
            System.out.println("Enter the Student ID whose transcript you seek.");
  

            // Sanitized integer input management
            try {
                System.out.print("Enter an integer between 0 and 99999: ");
                query_id = scn.nextInt();
                if (query_id < 0 || query_id > 99999)
                    throw new InputMismatchException();
            } catch (InputMismatchException e) {
                scn.nextLine(); //clear bad input
                continue;
            }

            nameSearch.setInt(1, query_id);
            rs = nameSearch.executeQuery();

            if (!rs.next()) {
                System.out.println("No user with id of " + query_id);
                return;
            }

            name = rs.getString("name");

            takesSearch.setInt(1, query_id);
            rs = takesSearch.executeQuery();

            if (!rs.next()) {
                System.out.println("Student named '" + name + "' hasn't taken any classes yet.");
                return;
            }
        }
            
        System.out.println("Transcript for student " + query_id + " " + name);
        do {
            Integer year = rs.getInt("year");
            String semester = rs.getString("semester");
            String dept_name = rs.getString("dept_name");
            Integer course_id = rs.getInt("course_id");
            String title = rs.getString("title");
            String grade = rs.getString("grade");
            System.out.printf("%-4d %-8s %-15s\t %-3d %-40s\t%-2s\n", year, semester, dept_name, course_id, title, grade);
        } while (rs.next());
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
