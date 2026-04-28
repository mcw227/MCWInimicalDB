import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;

/** Public class items to model an entry into the item db */

/** Note that type can either be "creation", "signature" or  */
public class Item {
    public int id;
    public String name;
    public double price;

    public Item(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    /** Standard toString method */
    public String toString() {
        return String.format("ID: %-3d\t| NAME: %-50s\t| PRICE: $%.2f", id, name, price);
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
            } while(rs.next());

        } catch (Exception e) {
            System.out.println("Unable to fetch items. Try again later");
            return null;
        }
        return null;
    }

    /**
     * Fetches all items which could be on the menu. Customer creations and signature items. Not ingredients
     * @param conn The database connection to use
     */
    public static ArrayList<Item> fetchMenuItems(Connection conn) {
        ArrayList<Item> menu_items = new ArrayList<>();
        try {
            PreparedStatement fetchMenuItems = conn.prepareStatement("SELECT * FROM menu_item_view");
            ResultSet rs = fetchMenuItems.executeQuery();

            if (!rs.next()) //no items in db for some reason..
                return menu_items;
            do {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                Double price = rs.getDouble("price");
                menu_items.add(new Item(id,name,price));
            } while(rs.next());

        } catch (Exception e) {
            System.out.println("Unable to fetch items. Try again later");
            return null;
        }
        return menu_items;
    }

    /**
     * Returns price adjusted items for the given location
     * @param conn the Database connection to use
     * @param location_id The id of the location to query
     */
    public static ArrayList<Item> fetchMenuItems(Connection conn, int location_id) {
        return fetchMenuItems(conn);
    }

    /**
     * Creates an item object by querying the databse for an item with the given id
     * @param conn The database connection to use
     * @param query_id The item id to query
     * @return The item found in the database, or null if it did not exist
     */
    public static Item createItemFromID(Connection conn, int query_id) {
        try {
            PreparedStatement getItem = conn.prepareStatement("SELECT * FROM items WHERE id = ?");
            getItem.setInt(1, query_id);
            ResultSet rs = getItem.executeQuery();
            if (!rs.next()) { //item not found
                System.out.printf("Unable to create item from ID: %d, not found in database!\n", query_id);
                return null;
            }
            String n = rs.getString("name");
            double p = rs.getDouble("price");
            return new Item(query_id, n, p);
        } catch (Exception e) {
            System.out.printf("Unable to create item from ID: %d\n", query_id);
            return null;
        }
    }

    /**
     * Creates an item object by querying the databse for an item with the given id and ensure that it is the correct price based on the location
     * @param conn The database connection to use
     * @param query_id The item id to query
     * @param loc_id The location the item is sold at (in case of location specific price updates)
     * @return The item found in the database, or null if it did not exist
     * @override
     */
    public static Item createItemFromID(Connection conn, int query_id, int loc_id) {
        try {
            PreparedStatement getItem = conn.prepareStatement("SELECT * FROM items WHERE id = ?");
            getItem.setInt(1, query_id);
            ResultSet rs_gi = getItem.executeQuery();
            if (!rs_gi.next()) { //item not found
                System.out.printf("Unable to create item from ID: %d, not found in database!\n", query_id);
                return null;
            }
            String n = rs_gi.getString("name");
            double p;
            
            //See whether there is a price change for this item
            PreparedStatement getItemPriceUpdate = conn.prepareStatement("SELECT * FROM price_change WHERE item_id = ? AND location_id = ?");
            getItemPriceUpdate.setInt(1, query_id);
            getItemPriceUpdate.setInt(2, loc_id);

            PreparedStatement getLocationSalesTax = conn.prepareStatement("SELECT sales_tax FROM locations WHERE id = ?");
            getLocationSalesTax.setInt(1,loc_id);

            ResultSet rs_gipu = getItemPriceUpdate.executeQuery();
            ResultSet rs_glst = getLocationSalesTax.executeQuery();

            double sales_tax = 1;

            /** This allows us to include tax in the order */
            if (rs_glst.next())
                sales_tax = rs_glst.getDouble("sales_tax");

            if (!rs_gipu.next())
                p = rs_gi.getDouble("price") * sales_tax;
            else
                p = rs_gipu.getDouble("price") * sales_tax;

            return new Item(query_id, n, p);
        } catch (Exception e) {
            System.out.printf("Unable to create item from ID: %d\n", query_id);
            return null;
        }
    }

    public static void checkItemScreen(Connection conn, Scanner scn, ArrayList<Item> items) {
        return;
    }
}