/**
 * Skeleton + Procedure shamelessly stolen from HW4 assignment
 */
import java.sql.*;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.util.ArrayList;

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
                    if (rs.getInt("active") != 0) { //Account is inactive. They cannot login.
                        c = new Customer(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getInt("membership"), rs.getInt("points"));
                    } else {
                        System.out.println("User is inactive. Contact management to reinstate account or type a valid ID.");
                        rs = null;
                    }
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
                        cCheckCreditCards(c, conn, scn);
                        break;
                    case 6:
                        if (cDeactivateAccount(c, conn, scn) == true) {
                            return;
                        }
                        break;
                }
            }
            updateCustomerInfo(c, conn);
            if (c.id == -2) { //acount was marked as inactive while another user was logged in!
                System.out.println("Customer is now marked inactive. Please contact management if you think this is an error.");
                return;
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

    /**
     * Allows the user to check their membership status and cancel if they want to.
     * @param c Customer logged in
     * @param conn the Database connection to use
     * @param scn Scanner to grab input from
     */
    static void cMemberChange(Customer c, Connection conn, Scanner scn) {
        if (c.membership) {
            System.out.println("You are currently a member! Yay!");
            System.out.printf("You have %d points. That equates to about %.2f dollars!\nWould you like to cancel your membership? (You will lose your points...) [y]es/[n]o/[q]uit\n", c.points, (float)(c.points)/100);
            
            if (nextYN(scn) == -2)
                return;
            System.out.println("Are you really sure?");
            if (nextYN(scn) == -2)
                return;

            try {
                PreparedStatement cancelMembership = conn.prepareStatement("UPDATE customers SET membership=0 WHERE id=?");
                cancelMembership.setInt(1,c.id);

                PreparedStatement setPointsZero = conn.prepareStatement("UPDATE customers SET points=0 WHERE id=?");
                setPointsZero.setInt(1,c.id);

                conn.setAutoCommit(false); //start transaction
                System.out.print("Cancelling membership... ");
                cancelMembership.executeUpdate();
                setPointsZero.executeUpdate();
                conn.commit();
                System.out.println("Done!");
            } catch (Exception e) {
                try {
                    System.out.println("Could not update membership status. Try again later");
                    conn.rollback();
                } catch (Exception f) { //Critical db error
                    System.err.println("Database connection terminated. Please restart software");
                    System.exit(-1);
                }

            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (Exception e) { //Critical db error
                    System.err.println("Database connection terminated. Please restart software.");
                    System.exit(-1);
                }
            }
        } else {
            System.out.println("You are not a member yet, would you like to enroll? [y]es/[n]o/[q]uit");
            if (nextYN(scn) == -2)
                return;
            try {
                PreparedStatement enrollMembership = conn.prepareStatement("UPDATE customers SET membership=1 WHERE id=?");
                enrollMembership.setInt(1, c.id);

                System.out.print("Enrolling in membership... ");
                enrollMembership.executeUpdate();
                System.out.println("Done!");
            } catch (Exception e) {
                System.out.println("Could not update membership status. Please try again later.");
                return;
            }
        }
    }

    /**
     * @param c Customer that is logged in
     * @param conn Database connection to use
     * @param scn Scanner to grab input from
     */
    static void cCheckCreditCards(Customer c, Connection conn, Scanner scn) {return;}

    static void cMakeOrder(Customer c, Connection conn, Scanner scn) {return;}

    /**
     * Allows the user to "delete" their account. Note that this just sets it as inactive in the system rather than deleting it for... record keeping purposes.
     * @param c Customer that is logged in
     * @param conn Database connection to use
     * @param scn Scanner to grab input from
     */
    static boolean cDeactivateAccount(Customer c, Connection conn, Scanner scn) {
        System.out.println("Are you sure you want to deactivate your account? You can contact support to reinstate it... [y]es/[n]o/[q]uit");
        if (nextYN(scn) == -2)
            return false;
        System.out.println("Are you really sure?");
        if (nextYN(scn) == -2)
            return false;

        System.out.println("Okay...");
        try {
            PreparedStatement deactivateAccount = conn.prepareStatement("UPDATE customers SET active=0 WHERE id=?");
            deactivateAccount.setInt(1,c.id);
            System.out.print("Deactivating account... ");
            deactivateAccount.executeUpdate();
            System.out.println("Done! Goodbye!");
            c = Customer.InactiveCustomer(); //set c to inactive customer
            return true;
        } catch (Exception e) {
            System.out.println("Could not delete account. Try again later.");
            return false;
        }
    }

    /**
     * Fetches new customer data.
     * @param c The customer object to update. Uses its id to find the customer in the db.
     * @param conn The database connection to use
     */
    static void updateCustomerInfo(Customer c, Connection conn) {
        try {
            PreparedStatement findCustomer = conn.prepareStatement("SELECT * FROM customers WHERE id = ?");
            findCustomer.setInt(1, c.id);
            ResultSet rs = findCustomer.executeQuery();
            if (rs == null) {//critical error
                System.err.println("Customer not found in database. Please restart software.\n");
                System.exit(-1);
            }
            else {
                rs.next();
                if (rs.getInt("active") == 0) //someone cancelled the account while the person was logged in
                    c = Customer.InactiveCustomer();
                c.id = rs.getInt("id");
                c.name = rs.getString("name");
                c.email = rs.getString("email");
                c.membership = (rs.getInt("membership") == 1) ? true : false;
                c.points = rs.getInt("points");
            }
        } catch (Exception e) {
            System.out.println("Could not update customer info.\n");
        }
    }

    /** Prints the customer control menu */
    static void printCMenu(Customer c) {
        if (c.membership)
            System.out.printf("\n\nHello, esteemed %s! You have %d points!", c.name, c.points);
        else
            System.out.printf("\n\nHello, %s!", c.name);
        System.out.printf("\nWhat would you like to do today?\n\t1. Change Name\n\t2. Change Email\n\t3. View Membership Details or Enroll \n\t4. Make An Order\n\t5. Check And Adjust Credit Cards\n\t6. Deactivate Account\nEnter a 1-6 to select an option or enter quit (q) to quit!\n", c.name);
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
     * Gets all cards for a particular customer id
     * @param c_id Customer id to query. If set to -1, gets all credit cards and puts them in a list.
     * @param conn Database connection to use.
     * @return an ArrayList populated with the cards obtained
     */
    static ArrayList<Card> fetchCards(int c_id, Connection conn) {
        ArrayList<Card> cards = new ArrayList<>();

        try {
            PreparedStatement getCards;
            if (c_id == -1)
                getCards = conn.prepareStatement("SELECT * FROM cards");
            else {
                getCards = conn.prepareStatement("SELECT * FROM cards WHERE c_id = ?");
                getCards.setInt(1, c_id);
            }
            ResultSet rs = getCards.executeQuery();
            
            if (!rs.next()) { //No cards, return empty arraylist
                return cards;
            }

            do {
                int id = rs.getInt("id");
                int cid = rs.getInt("customer_id");
                String brand = rs.getString("brand");
                String name = rs.getString("name");
                String card_number = rs.getString("card_number");
                String expr_date = rs.getString("expr_date");
                String cvv = rs.getString("cvv");

                cards.add(new Card(id, cid, brand, name, card_number, expr_date, cvv));
            } while (rs.next()); //since we checked rs.next() above we have to use a do while loop instead, otherwise we skip the first one :(

        } catch (Exception e) {
            System.out.println("Could not query Database for cards. Please try again later.");
            e.printStackTrace(); //debug
            return null;
        }
        return null;
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
