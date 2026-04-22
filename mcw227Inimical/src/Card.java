import java.sql.*;

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
        System.out.println("Done!");
        return true;
    }
}