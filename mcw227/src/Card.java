import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Arrays;

/**
 * This card class models a row in the Cards table
 * Note that a negative id corresponds to a card that has not been added in the database yet
 */
public class Card {

    public int id;
    public int customer_id;
    public String brand;
    public String name;
    public String card_number;
    public String expr_date;
    public String cvv;

    /** These variables dictate the behavior of certain functionality. The database also enforces some of these rules but perhaps we would want to control them here as well? */
    public final static int MAX_BRAND_LEN = 40; //max is 40 according to db
    public final static int MAX_NAME_LEN = 30; //max is 30 according to db
    public final static int MAX_EXPR_DATE_LEN = 7; //max is 7 according to db
    public final static int MAX_CARD_NUM_LEN = 20; //max is 20 according to db
    public final static ArrayList<String> allowedCreditCardBrands = new ArrayList<>(Arrays.asList("visa","mastercard","american-express","discover"));

    /** Standard constructor that matches what is held in the database */
    public Card(int id, int customer_id, String brand, String name, String card_number, String expr_date, String cvv) {
        this.id = id;
        this.customer_id = customer_id;
        this.brand = brand;
        this.name = name;
        this.card_number = card_number;
        this.expr_date = expr_date;
        this.cvv = cvv;
    }

    /**
     * Standard toString function
     * @return formatted ID, customer_id, Brand, Holder name, Card number, expiration date and cvv.
     */
    public String toString() {
        return String.format("ID: %-3d\t| CUSTOMER_ID: %-3d\t| BRAND: %-20s\t| HOLDER NAME: %-30s\t| CARD NUMBER: %s\t| EXPR DATE: %-7s\t| CVV: %-3s", id, customer_id, brand, name, card_number, expr_date, cvv);
    }

    /**
     * Adds a card to the database
     * @param conn The database connection to use
     * @param customer_id the customer id to add the card under
     * @param c the card to add
     */
    public static boolean addCard(Connection conn, Card c) throws SQLException {
        PreparedStatement addCard = conn.prepareStatement("INSERT INTO cards (customer_id, brand, name, card_number, expr_date, cvv) VALUES (?, ?, ?, ?, ?, ?)");
        addCard.setInt(1, c.customer_id);
        addCard.setString(2, c.brand);
        addCard.setString(3, c.name);
        addCard.setString(4, c.card_number);
        addCard.setString(5, c.expr_date);
        addCard.setString(6, c.cvv);

        System.out.print("Adding card to Database...");
        addCard.executeUpdate();
        addCard.close();
        System.out.println("Done!");
        return true;
    }

    /**
     * Removes a card from a given customer (or any customer if the provided customer's id is -1)
     * @param c The customer to remove the card from
     * @param conn The database connection to use
     * @param id The id of the card to remove
     */
    public static boolean removeCard(Customer c, Connection conn, int id) throws SQLException {
        PreparedStatement delCard;
        if (c.id == -1) {
            delCard = conn.prepareStatement("DELETE FROM cards WHERE id=?");
            delCard.setInt(1,id);
        }
        else {
            delCard = conn.prepareStatement("DELETE FROM cards WHERE id=? AND customer_id=?");
            delCard.setInt(1, id); //card entry id
            delCard.setInt(2, c.id); //customer id
        }

        System.out.print("Removing card from database...");
        int row_update = delCard.executeUpdate();
        if (row_update == 0) {
            System.out.printf("Card with id %d not found!\n", id);
            delCard.close();
            return false;
        }
        else {
            System.out.println("Done!");
            delCard.close();
            return true;
        }
    }

    /**
     * Allows the customer to delete a card if their id matches the card or if the admin "account" is provided
     * @param c The customer currently logged in. Could be an admin user with c.id == -1
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     * @return True if cards were updated, false if not
     */
    public static boolean removeCardScreen(Customer c, Connection conn, Scanner scn) {
        System.out.println("What is the id of the card you would like to delete?");
        int id = Helper.nextId(scn);
        try {
            return Card.removeCard(c, conn, id);
        } catch (Exception e) {
            System.out.println("Unable to remove card from database, please try again later.");
            return false;
        }
    }

    /**
     * Opens an add card screen. Uses the provided customer number, or queries user for one if it is null.
     * @param c Customer adding card. Should have id of -1 if admin
     * @param conn Database connection to use
     * @param scn Scanner to grab input from
     * @return true if card was added, false if not.
     */
    public static boolean addCardScreen(Customer c, Connection conn, Scanner scn) {
        String resp = null;
        String brand = ""; String holder_name=""; String card_number=""; String expr_date=""; String cvv="";
        System.out.println("Type (!q)uit at any time to quit.\n");

        if (c.id == -1) {
            System.out.println("What is the customer id of the card holder?");
            int c_id = Helper.nextId(scn);
            c = new Customer(c_id, c.name, c.email, 0, c.points);
        }

        while (resp == null) {
            System.out.println("What is the brand? Must be one of " + allowedCreditCardBrands.toString());
            resp = Helper.safeCheckQuit(scn, MAX_BRAND_LEN);
            if (resp == null)
                return false;
            // else check to make sure the brand is allowed
            if (allowedCreditCardBrands.contains(resp)) {
                brand = resp;
            } else {
                resp = null;
            }
        }

        brand = resp;

        System.out.println("What is the name of the card holder?");
        resp = Helper.safeCheckQuit(scn, MAX_NAME_LEN);
        if (resp == null)
            return false;
        holder_name = resp;

        resp = null;
        System.out.println("What is the number on the card? Enter with no spaces.");
        while (resp == null) {
            resp = Helper.safeCheckQuit(scn, MAX_CARD_NUM_LEN);
            if (resp == null)
                return false;
            if (!Helper.matchRegex(resp, "\\d+\\s*")) {
                resp = null;
                System.out.println("Do not enter spaces or non-numerics.");
            }
        }
        card_number = resp;

        resp = null;
        System.out.println("What is the expiration date. Enter in the format (YEAR-MO. example: 2026-04)");
        while (resp == null) {
            resp = Helper.safeCheckQuit(scn, MAX_EXPR_DATE_LEN);
            if (resp == null)
                return false;
            
            if (!Helper.matchRegex(resp, "\\d{4}-\\d{2}")) {
                System.out.println("Please provide expiration date in the format: XXXX-XX");
                resp = null;
            }
        }
        expr_date = resp;

        resp = null;
        System.out.println("What is the cvv?");
        while(resp == null) {
            resp = Helper.safeCheckQuit(scn, 3);
            if (resp == null)
                return false;

            if(!Helper.matchRegex(resp, "\\d{3}")) {
                System.out.println("Please provide a valid, three digit cvv");
                resp = null;
            }
        }
        cvv = resp;

        try {
            addCard(conn, new Card(-1, c.id, brand, holder_name, card_number, expr_date, cvv));
            return true;
        } catch (Exception e) {
            System.err.println("Could not update database. Try again later.");
            //e.printStackTrace(); //debug
        }
        return false;
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
                getCards = conn.prepareStatement("SELECT * FROM cards WHERE customer_id = ?");
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
            getCards.close();
        } catch (Exception e) {
            System.out.println("Could not query Database for cards. Please try again later.");
            //e.printStackTrace(); //debug
            return null;
        }
        return cards;
    }
}