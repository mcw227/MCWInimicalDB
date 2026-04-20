/**
 * Skeleton + Procedure shamelessly stolen from HW4 assignment
 */
import java.sql.*;
import java.util.Scanner;
import java.util.InputMismatchException;

import java.util.regex.*;

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
     * @param conn Database connection to use
     */
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
        System.out.println("Goodbye! :)");
        return;
    }

    /**
     * Customer Interface Functions
     */
    static void cInterface(Connection conn, Scanner scn) {
        Customer c = cLogin(conn, scn);
        if (c == null) return; //user is quitting.
        else {
            //System.out.println(c.toString()); // debug
            cMenu(c, conn, scn);
        }
    }

    /**
     * @param conn the Database connection to use
     * @param scn the scanner to use to get input.
     * @return a valid customer or null
     */
    static Customer cLogin(Connection conn, Scanner scn) {
        ResultSet rs = null;
        int id = -1;
        Customer c = null;
        while (rs == null) {
            System.out.print("Enter a valid id, or press q to quit: ");
            id = nextId(scn);

            if (id == -2) return null;

            while (id == -1) {
                id = nextId(scn);
                System.out.print("Enter a valid id, or press q to quit: ");

                if (id == -2) return null; //quit casz
            }

            try {
                PreparedStatement findCustomer = conn.prepareStatement("SELECT * FROM customers WHERE id = ?");
                findCustomer.setInt(1, id);
                rs = findCustomer.executeQuery();
                if (rs == null)
                    System.out.println("User id not found, try again.");
                else {
                    rs.next();
                    c = new Customer(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getInt("membership"), rs.getInt("points"));
                }
            } catch (Exception e) {
                System.out.println("Could not query database, or found invalid customer, please try again.");
                e.printStackTrace();
            }
            
        }     
        return c;
    }

    /**
     * @param Customer customer to take data from
     * @param conn DB Connection to query
     * @param scn Scanner to use for input
     */
    static void cMenu(Customer c, Connection conn, Scanner scn) {
        int resp = 0;
        while (resp != -2) {
            printCMenu(c);
            resp = nextId(scn);
            if (resp == 0 || resp > 5 || resp == -1) {
                System.out.println("Please pick a valid option!");
            } else if (resp != -2) {
                switch(resp) {
                    case 1:
                        cNameChange(c, conn, scn);
                        break;
                    case 2:
                        cEmailChange(c, conn, scn);
                        break;
                    case 3:
                        cMemberChange(c, conn, scn);
                        break;
                    case 4:
                        cMakeOrder(c, conn, scn);
                        break;
                    case 5:
                        if (cDeleteAccount(c, conn, scn) == true) {
                            return;
                        }
                        break;
                }
            }
        }
        return;
    }

    /**
     * Allows the signed-in user to change their name if they desire.
     * @param c The customer who is currently signed in
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     */
    static void cNameChange(Customer c, Connection conn, Scanner scn) {
        System.out.printf("Your current name is: %s, would you like to change it? ([y]es/[n]o)\n", c.name);
        int choice = nextYN(scn);
        if (choice == -2) { return; }
        try {
            PreparedStatement nameChange = conn.prepareStatement("UPDATE customers SET name=? WHERE id=?");
            System.out.println("What would you like your new name to be?");
            String newName = nextSafeString(scn, 30); //Names can be up to 30 characters long
            System.out.printf("\nChanging name to %s... ", newName);
            nameChange.setString(1, newName);
            nameChange.setInt(2, c.id);
            nameChange.executeUpdate();
            System.out.println("Name updated.");
        } catch (Exception e) {
            System.out.println("Could not update customer name. Please try again later.");
            e.printStackTrace();
        } finally {
            return;
        }
    }

    /**
     * Allows the user to change their email, if they desire.
     * @param c The customer who is logged in
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     */
    static void cEmailChange(Customer c, Connection conn, Scanner scn) {
        System.out.printf("Your current email is: %s, would you like to change it? ([y]es/[n]o)\n", c.email);
        int choice = nextYN(scn);
        if (choice == -2) { return; }
        try {
            PreparedStatement emailChange = conn.prepareStatement("UPDATE customers SET email=? WHERE id=?");
            System.out.println("What would you like your new email to be?");
            String newEmail = nextEmail(scn);
            System.out.printf("\nChanging email to %s... ", newEmail);
            emailChange.setString(1, newEmail);
            emailChange.setInt(2, c.id);
            emailChange.executeUpdate();
            System.out.println("email updated.");
        } catch (Exception e) {
            System.out.println("Could not update customer email. Please try again later.");
            //e.printStackTrace(); //debug
        } finally {
            return;
        }
    }
    static void cMemberChange(Customer c, Connection conn, Scanner scn) {return;}
    static void cMakeOrder(Customer c, Connection conn, Scanner scn) {return;}
    static boolean cDeleteAccount(Customer c, Connection conn, Scanner scn) {return false;}

    static void printCMenu(Customer c) {
        System.out.printf("\n\nHello, %s!\nWhat would you like to do today?\n\t1. Change Name\n\t2. Change Email\n\t3. View Membership Details or Enroll \n\t4. Make An Order\n\t5. Delete Account\nEnter a 1-5 to select an option or enter quit (q) to quit!\n", c.name);
    }


    /**
     * Location Manager Interface Functions
     */
    static void lmInterface(Connection conn, Scanner scn) {
        System.out.println("Enter your location manager id:");
        return;
    }

    /**
     * General Manager Interface Functions
     */
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

    /**
     * Helper Functions
     */

    /**
     * This is also used to get non-negative integer input while handling quit case
     * @param scn Scanner to grab input from
     * @return A positive integer or -1
     */
    static int nextId(Scanner scn) {
        try {
            String resp = scn.nextLine();
            if (resp.equalsIgnoreCase("q") || resp.equalsIgnoreCase("quit")) return -2;
            System.out.println(resp);
            return Integer.parseInt(resp);
        } catch (Exception e) {
            return -1;
        }
    }


    /**
     * Gets the next valid email address input from the user.
     */
    static String nextEmail(Scanner scn) {
        String email_regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"; //Plagiarized from Gemini. :)
        Pattern email_pattern = Pattern.compile(email_regex);

        while(true) {
            String resp = nextSafeString(scn, 40);
            Matcher email_matcher = email_pattern.matcher(resp);
            if (email_matcher.matches())
                return resp;
            System.out.println("Please provide a valid email.");
        }
    }

    /**
     * This function handles yes/no input from user. Also handles quit for interface cohesiveness. Note that no and quit have the same return value
     * @param scn The scanner to grab input from
     * @return 1 if user types yes, -2 if user types no or wants to quit. Retries until a valid input is reached.
     */
    static int nextYN(Scanner scn) {
        int r = 0;
        while (r == 0) {
            String resp = scn.nextLine();
            if (resp.equalsIgnoreCase("q") || resp.equalsIgnoreCase("quit") || resp.equalsIgnoreCase("n") || resp.equalsIgnoreCase("no"))
                return -2;
            else if (resp.equalsIgnoreCase("y") || resp.equalsIgnoreCase("yes"))
                return 1;
            System.out.println("Please type either (y)es or (n)o");
        }
        return -2;
    }

    /**
     * This function prompts the user for a string that is within a particular length and does not contain the character "'"
     * @param scn Scanner to grab input from
     * @param len Maximum length of string to accept
     * @return a sane user string.
     */
    static String nextSafeString(Scanner scn, int len) {
        while (true) {
            String resp = scn.nextLine();
            if (resp.length() > len) {
                System.out.printf("Provided string is longer than %d characters. Please shorten it.\n", len);
            } else if (resp.contains("'")) {
                System.out.printf("Provided string contains an invalid character ('). Please remove it.\n");
            }
            else { return resp; }
        }
    }

    /**
     * Core DB Functions
     */

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
        } finally {
            System.out.println("You are no longer connected to the database.");
        }
    }
}
