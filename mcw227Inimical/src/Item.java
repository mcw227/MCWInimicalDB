import java.sql.*;
import java.util.ArrayList;

/** Public class items to model an entry into the item db */

/** Note that type can either be "creation", "signature" or  */
public class Item {
    public int id;
    public String name;
    public double price;
    public String type;

    public Item(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    /** Standard toString method */
    public String toString() {
        return String.format("%-3d\t| %-50%s\t| %.2f", id, name, price);
    }

    /**
     * Adds an item to the database. Note that this DOES NOT add signature items or customer creations
     * @param conn The database connection to use
     * @param i The item to add
     */
    public static boolean addItem(Connection conn, Item i) throws SQLException {
        PreparedStatement addItem = conn.prepareStatement("INSERT INTO items (name, price) VALUES (?, ?)");
        addItem.setString(1, i.name);
        addItem.setDouble(2,i.price);

        System.out.print("Adding item to Database...");
        addItem.executeUpdate();
        System.out.println("Done!");
        return true;
    }

    /**
     * Removes an item from the database. Note that this does not handle signature items or customer creations
     * @param conn The databsae connection to use
     * @param id The id of the item we want to delete.
     */
    public static boolean delItem(Connection conn, int id) throws SQLException {
        PreparedStatement delItem = conn.prepareStatement("DELETE FROM items WHERE id=?");
        delItem.setInt(1,id);
        System.out.print("Removing item from Database...");
        delItem.executeUpdate();
        System.out.println("Done!");
        return true;
    }

    /**
     * Fetches all items from the database (including "ingredient", "signature" and "customer creations")
     * @param conn The database connection to use
     */
    public static ArrayList<Item> fetchItems(Connection conn) {
        ArrayList<Item> items = new ArrayList<>();
        try {
            PreparedStatement fetchItems = conn.prepareStatement("SELECT * FROM items");
            ResultSet rs = fetchItems.executeQuery();

            if (!rs.next()) //no items in db for some reason..
                return items;
            do {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                Double price = rs.getDouble("price");
                items.add(new Item(id,name,price));
            } while(rs.next())

        } catch (Exception e) {
            System.out.println("Unable to fetch items. Try again later");
            return null;
        }
        return null;
    }

    /**
     * Fetches all items which should be on the menu. Customer creations and signature items. Not ingredients
     * @param conn The database connection to use
     */
    public static ArrayList<Item> fetchMenuItems(Connection conn) {
        ArrayList<Item> menu_items = new ArrayList<>();
        menu_items.addAll(SignatureItem.fetchSignatures(conn));
        menu_items.addAll(CustomerCreation.fetchCustomerCreations(conn));
        return menu_items;
    }
}