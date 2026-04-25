import java.sql.*;

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
}