import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/** Customer creation object  */
public class CustomerCreation extends Item {
    public ArrayList<Item> ingredients;
    public ArrayList<Integer> quantities;

    public String creator;

    /** Standard constructor */
    public CustomerCreation(int id, String name, double price, String creator, ArrayList<Item> ingredients, ArrayList<Integer> quantities) {
        super(id, name, price);
        this.creator = creator;
        this.ingredients = ingredients;
        this.quantities = quantities;
    }

    public CustomerCreation(int id, String name, double price, String creator) {
        super(id, name, price);
        this.creator = creator;
        this.populateRecipe();
    }

    /** Standard toString function */
    public String toString() {
        return String.format("CUSTOMER CREATION! %s\t| CREATOR:%s",super.toString(), this.creator);
    }

    public static boolean delCustomerCreation(Connection conn, int id) {
        return super.delItem(conn, id);
    }

    /**
     * Fetches customer creations
     * @param conn The database connection to use
     */
    public static fetchCustomerCreations(Connection conn) {
        try {
            PreparedStatement fetchCustomerCreations = conn.prepareStatement("SELECT * FROM customer_creations_view");
            ResultSet rs = fetchSignatures.executeQuery();

            if (!rs.next())
                continue;
            else {
                do {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");
                    String creator = rs.getString("creator");
                    menu_items.add(new CustomerCreation(id, name, price, creator));
                } (while rs.next());
            }

            return menu_items;
        } catch (Exception e) {
            System.out.println("Unable to fetch customer creations. Try again later.");
            return null;
        }
    }

    public String getPrintableRecipe() {
        String ret = "";
        return ret;
    }

    private void populateRecipe() {
        return;
    }
}