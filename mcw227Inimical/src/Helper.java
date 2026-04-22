import java.sql.*;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.util.ArrayList;
import java.util.regex.*;

public final class Helper {

    /** These variables dictate the behavior of certain functionality. The database also enforces some of these rules but perhaps we would want to control them here as well? */
    private final int MAX_BRAND_LEN = 40; //max is 40 according to db
    private final int MAX_NAME_LEN = 30; //max is 30 according to db
    private final int MAX_EXPR_DATE_LEN = 7; //max is 7 according to db
    private final int MAX_CARD_NUM_LEN = 20; //max is 20 according to db
    private final String[] allowedCreditCardBrands = ["visa","mastercard","american-express","discover"];

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
     * @param scn The scanner to grab input from
     * @return A string that has been regex-suggested to be a valid email
     */
    public static String nextEmail(Scanner scn) {
        String email_regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"; //Plagiarized from Gemini. :)

        while(true) {
            String resp = nextSafeString(scn, 40);
            if (matchRegex(resp, email_regex))
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
     * This function handles previous/next/quit/add/delete input from user.
     * @param scn The scanner to grab input from
     * @return 1 if user types previous, 2 if user types next, 3 if user types add, 4 if user types delete -2 if user quits. Retries until a valid input is reached.
     */
    public static int nextPNQAD(Scanner scn) {
        int r = 0;
        while (r == 0) {
            String resp = scn.nextLine();
            if (resp.equalsIgnoreCase("q") || resp.equalsIgnoreCase("quit"))
                return -2;
            else if (resp.equalsIgnoreCase("p") || resp.equalsIgnoreCase("previous"))
                return 1;
            else if (resp.equalsIgnoreCase("n") || resp.equalsIgnoreCase("next"))
                return 2;
            else if (resp.equalsIgnoreCase("a") || resp.equalsIgnoreCase("add"))
                return 3;
            else if (resp.equalsIgnoreCase("d") || resp.equalsIgnoreCase("delete"))
                return 4;
            System.out.println("Please type either (n)ext, (p)revious, (a)dd, (d)elete, or (q)uit");
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

    /**
     * Checks whether user is trying to quit by typing !q or !quit
     * @return either the input string or null if user is trying to quit.
     */
    static String safeCheckQuit(Scanner scn, int len) {
        String resp = scn.nextSafeString(len);
        if (resp.equalsIgnoreCase("!q") || resp.equalsIgnoreCase("!quit"))
            return null;
        return resp;
    }

    /**
     * Opens an add card screen. Uses the provided customer number, or queries user for one if it is null.
     * @param c Customer adding card, or null (if admin)
     * @param conn Database connection to use
     * @param scn Scanner to grab input from
     * @return true if card was added, false if not.
     */
    static boolean addCardScreen(Customer c, Connection conn, Scanner scn) {
        String resp = null;
        String brand; String holder_name; String card_number; String expr_date; String cvv;
        System.out.println("Type (!q)uit at any time to quit.\n");

        if (!c) {
            System.out.println("What is the customer id of the card holder?");
            int c_id = nextId(scn);
            
        }

        (while !resp) {
            System.out.println("What is the brand?");
            String resp = safeCheckQuit(scn, MAX_BRAND_LEN);
            if (!resp)
                return false;
            // else check to make sure the brand is allowed
            if (Arrays.asList(allowedCreditCardBrands).contains(resp)) {
                brand = resp;
            }
            resp = null;
        }

        System.out.println("What is the name of the card holder?");
        String resp = safeCheckQuit(scn, MAX_NAME_LEN);
        if (!resp)
            return false;
        holder_name = resp;

        System.out.println("What is the number on the card? Enter with no spaces.");
        while (!resp) {
            String resp = safeCheckQuit(scn, MAX_CARD_NUM_LEN);
            if (!resp)
                return false;

            if (resp.contains(' ') || !matchRegex(resp,String.format("\\d{%d}",MAX_CARD_NUM_LEN))) {
                resp = null;
                System.out.println("Do not enter spaces or non-numerics.");
            }
        }
        card_number = resp;

        System.out.println("What is the expiration date. Enter in the format (YEAR-MO. example: 2026-04)");
        while (!resp) {
            String resp = safeCheckQuit(scn, MAX_EXPR_DATE_LEN);
            if (!resp)
                return false;
            
            if (!matchRegex(resp, "\\d{4}-\\d{2}")) {
                System.out.println("Please provide expiration date in the format: XXXX-XX");
                resp = null;
            }
        }
        expr_date = resp;

        System.out.println("What is the cvv?");
        while(!resp) {
            String resp = safeCheckQuit(scn, 3);
            if (!resp)
                return false;

            if(!matchRegex(resp, "\\d{3}")) {
                System.out.println("Please provide a valid, three digit cvv");
                resp = null;
            }
        }
        cvv = resp;

        try {
            PreparedStatement addCard = conn.prepareStatement("INSERT INTO cards (brand, name, card_number, expr_date, cvv) VALUES (?, ?, ?, ?, ?)")
            addCard.setString(1, brand);
            addCard.setString(2, holder_name);
            addCard.setString(3, card_number);
            addCard.setString(4, expr_date);
            addCard.setString(5, cvv);

            System.out.print("Adding card to Database...");
            addCard.executeUpdate();
            System.out.println("Done!");
            return true;
        } catch (Exception e) {
            System.err.println("Could not update database. Try again later.");
            e.printStackTrace(); //debug
        }
        return false;
    }

    /**
     * Sees whether the base string contains the target pattern.
     * @param base The string to compare to the pattern
     * @param target The regex pattern to search for
     * @return true if the pattern is contained, false if not.
     */
    static boolean matchRegex(String base, String target) {
        Pattern re_pattern = Pattern.compile(target);
        Matcher pattern_matcher = re_pattern.matcher(base);
        if (pattern_matcher.matches())
            return true;
        return false;
    }
}