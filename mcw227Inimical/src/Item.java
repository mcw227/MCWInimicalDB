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
            PreparedStatement fetchItems = conn.prepareStatement("SELECT * FROM all_items_class_view");
            ResultSet rs = fetchItems.executeQuery();

            if (!rs.next()) //no items in db for some reason..
                return items;
            do {
                int id = rs.getInt("id");
                String n = rs.getString("name");
                double p = rs.getDouble("price");

                 if (rs.getString("item_type").equalsIgnoreCase("SIGNATURE"))
                    items.add(new SignatureItem(id, n, p));
                else if (rs.getString("item_type").equalsIgnoreCase("CUSTOMER_CREATION")) {
                    items.add(new CustomerCreation(id, n, p, rs.getString("specific_attribute"), conn));
                } else {
                    items.add(new Item(id, n, p));
                }
            } while(rs.next());

        } catch (Exception e) {
            System.out.println("Unable to fetch items. Try again later");
            return null;
        }
        return null;
    }

    /**
     * Fetches all possible ingredients
     * @param conn The database connection to use
     * @return An arraylist of ingredients
     */
    public static ArrayList<Item> fetchIngredients(Connection conn) {
        ArrayList<Item> ingredients = new ArrayList<>();
        try {
            PreparedStatement getIngredients = conn.prepareStatement("SELECT * FROM ingredients");
            ResultSet rs = getIngredients.executeQuery();

            if (!rs.next())
                return ingredients;
            do {
                ingredients.add(Item.parseItemFromRS(rs));
            } while (rs.next());
            return ingredients;
        } catch (Exception e) {
            System.out.println("Unable to fetch ingredients. Try again later.");
            return null;
        }
    }

    /**
     * Fetches all possible ingredients from a menu
     * @param conn The database connection to use
     * @param m The menu to grab ingredients from
     * @return An arraylist of ingredients
     */
    public static ArrayList<Item> fetchIngredients(Connection conn, Menu m) {
        ArrayList<Item> ingredients = new ArrayList<>();
        try {
            PreparedStatement getIngredients = conn.prepareStatement("SELECT * FROM menu_items m JOIN ingredients i ON m.item_id = i.id WHERE m.menu_id = ?");
            getIngredients.setInt(1,m.id);
            ResultSet rs = getIngredients.executeQuery();

            if (!rs.next())
                return ingredients;
            do {
                ingredients.add(Item.parseItemFromRS(rs));
            } while (rs.next());
            return ingredients;
        } catch (Exception e) {
            System.out.println("Unable to fetch ingredients. Try again later.");
            return null;
        }
    }

    /**
     * Grabs ingredient items from an arraylist
     * @param items The list of items
     */
    public static ArrayList<Item> fetchIngredientsFromList(ArrayList<Item> items) {
        ArrayList<Item> ingredients = new ArrayList<>();
        for (Item i : items) {
            if (!SignatureItem.class.isInstance(i))
                ingredients.add(i);
        }
        return ingredient;
    }

    /**
     * Parses an item from a result set
     * @param rs The result set to parse
     * @return The item from the result set, or null if it is an invalid result set
     */
    public static Item parseItemFromRS(ResultSet rs) {
        try {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            Double price = rs.getDouble("price");
            return new Item(id,name,price);
        } catch (Exception e) {
            return null;
        }
     
    }

    /**
     * Creates an item object by querying the databse for an item with the given id
     * @param conn The database connection to use
     * @param query_id The item id to query
     * @return The item found in the database, or null if it did not exist. Although cast as an item, it may be subclass Sig or CustomerCreation
     */
    public static Item createItemFromID(Connection conn, int query_id) {
        try {
            Item r_item = null;
            PreparedStatement getItem = conn.prepareStatement("SELECT * FROM all_items_class_view WHERE id = ?");
            getItem.setInt(1, query_id);
            ResultSet rs_gi = getItem.executeQuery();

            if (!rs_gi.next()) { //item not found
                System.out.printf("Unable to create item from ID: %d, not found in database!\n", query_id);
                return null;
            }

            String n = rs_gi.getString("name");
            double p = rs_gi.getDouble("price");

            //Handle the many item types
            if (rs_gi.getString("item_type").equalsIgnoreCase("SIGNATURE"))
                r_item = new SignatureItem(query_id, n, p);
            else if (rs_gi.getString("item_type").equalsIgnoreCase("CUSTOMER_CREATION")) {
                r_item = new CustomerCreation(query_id, n, p, rs_gi.getString("specific_attribute"), conn);
            } else {
                r_item = new Item(query_id, n, p);
            }

            return r_item;
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
     * @return The item found in the database, or null if it did not exist. Can be sig or customer creation
     */
    public static Item createItemFromID(Connection conn, int query_id, int loc_id) {
        try {
            Item r_item = null;
            PreparedStatement getItem = conn.prepareStatement("SELECT * FROM all_items_class_view WHERE id = ?");
            getItem.setInt(1, query_id);
            ResultSet rs_gi = getItem.executeQuery();

            if (!rs_gi.next()) { //item not found
                System.out.printf("Unable to create item from ID: %d, not found in database!\n", query_id);
                return null;
            }
            String n = rs_gi.getString("name");
            double p;

            //Handle the many item types
            if (rs_gi.getString("item_type").equalsIgnoreCase("SIGNATURE"))
                r_item = new SignatureItem(query_id, n, 0);
            else if (rs_gi.getString("item_type").equalsIgnoreCase("CUSTOMER_CREATION")) {
                r_item = new CustomerCreation(query_id, n, 0, rs_gi.getString("specific_attribute"), conn);
            } else {
                r_item = new Item(query_id, n, 0);
            }

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

            r_item.price = p;
            return r_item;
        } catch (Exception e) {
            System.out.printf("Unable to create item from ID: %d\n", query_id);
            return null;
        }
    }

    /**
     * Allows users to check out an item that they know the id of
     * @param conn The connection to the database
     * @param scn The scanner to grab input from
     * @param items The items to query
     */
    public static void checkItemScreen(Connection conn, Scanner scn, ArrayList<Item> items) {
        System.out.println("What is the id of the item you'd like to check?");

        Item i;
        while (true) {
            int r = Helper.nextId(scn);
            if (r == -2)
                return;
            i = items.stream().filter(is -> is.id == r).findFirst().orElse(null);
            if (i != null) {
                Helper.clearConsole();
                System.out.println(Item.getSummary(i, conn));
                System.out.println("Type anything to return to main screen.");
                Helper.nextOK(scn);
                Helper.clearConsole();
                return;
            }
        }
    }

    /**
     * Summarizes an item. Prints the recipe if it is a customer creation
     * @param i The item to summarize
     * @return A string summarizing the item's details
     */
    public static String getSummary(Item i, Connection conn) {
        if (CustomerCreation.class.isInstance(i)) {
            return ((CustomerCreation)i).getPrintableRecipeSummary(conn);
        }
        else {
            return i.toString();
        }
    }
}