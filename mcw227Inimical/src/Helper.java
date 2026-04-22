import java.sql.*;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.util.ArrayList;
import java.util.regex.*;

public final class Helper {

    public Helper() {
        throw new UnsupportedOperationException("This is a utility class. Do not instantiate it.");
    }
    
    /** Attempts to clear console */
    /** Shamelessly sourced from Copilot, but I understand how it works. */
    public static void clearConsole() {
        try {
            String os = System.getProperty("os.name");
            if (os.contains("Windows")) { //windwos needs special handling bc its so special...
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else { //unix-like systems
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
            return;
        } catch (Exception e) {
            System.out.println("Cannot clear console."); //debug
            return;
        }
        
    }
    /**
     * This is also used to get non-negative integer input while handling quit case
     * @param scn Scanner to grab input from
     * @return A positive integer or -1
     */
    public static int nextId(Scanner scn) {
        try {
            String resp = scn.nextLine();
            if (resp.equalsIgnoreCase("q") || resp.equalsIgnoreCase("quit")) return -2;
            return Integer.parseInt(resp);
        } catch (Exception e) {
            return -1;
        }
    }


    /**
     * Gets the next valid email address input from the user.
     */
    public static String nextEmail(Scanner scn) {
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
    public static int nextYN(Scanner scn) {
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
     * This function handles previous/next/quit input from user.
     * @param scn The scanner to grab input from
     * @return 1 if user types previous, 2 if user types next, -2 if user quits. Retries until a valid input is reached.
     */
    public static int nextPNQ(Scanner scn) {
        int r = 0;
        while (r == 0) {
            String resp = scn.nextLine();
            if (resp.equalsIgnoreCase("q") || resp.equalsIgnoreCase("quit"))
                return -2;
            else if (resp.equalsIgnoreCase("p") || resp.equalsIgnoreCase("previous"))
                return 1;
            else if (resp.equalsIgnoreCase("n") || resp.equalsIgnoreCase("next"))
                return 2;
            System.out.println("Please type either (n)ext, (p)revious, or (q)uit");
        }
        return -2;
    }

    /**
     * This function prompts the user for a string that is within a particular length and does not contain the character "'"
     * @param scn Scanner to grab input from
     * @param len Maximum length of string to accept
     * @return a sane user string.
     */
    public static String nextSafeString(Scanner scn, int len) {
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
    public static ArrayList<Card> fetchCards(int c_id, Connection conn) {
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
        return cards;
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
}