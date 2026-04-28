import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/** Public class menu models an entry in the Menu tbale in the database */
public class Menu {
    public int id;
    public String name;
    public ArrayList<Item> items;

    public Menu(int id, String name, ArrayList<Item> items) {
        this.id = id;
        this.name = name;
        this.items = items;
    }

    public Menu(int id, String name) {
        this.id = id;
        this.name = name;
        this.items = new ArrayList<Item>();
    }

    public Menu(int id, Connection conn) {
        this.getPopulatedMenu(conn, id);
    }

    public String toString() {
        return String.format("ID: %-3d\t| NAME: %-20s", this.id, this.name);
    }
    
    /**
     * Fills a menu with its corresponding item objects
     * @param conn The database connection to use
     */
    public void fillItems(Connection conn) {
        ArrayList<Item> filled_items = new ArrayList<>();
        try {
            PreparedStatement getMenuItems = conn.prepareStatement("SELECT * FROM menu_items WHERE menu_id = ?");
            getMenuItems.setInt(1, this.id);

            ResultSet rs = getMenuItems.executeQuery();
            if (!rs.next()) { //no items in menu
                return; 
            }
            else {
                do {
                    int item_id = rs.getInt("item_id");
                    filled_items.add(Item.createItemFromID(conn, item_id));
                } while (rs.next());
            }
        } catch (Exception e) {
            System.out.println("Unable to populate menu. Try again later.");
            e.printStackTrace(); //debug
        }
    }

    /**
     * Add an item to the current menu
     * @param conn The database connection to use
     * @param i The item to add to the menu
     */
    public boolean addItem(Connection conn, Item i) {
        try {
            PreparedStatement addItem = conn.prepareStatement("INSERT INTO menu_items (menu_id, item_id) VALUES (?,?)");
            addItem.setInt(1, this.id);
            addItem.setInt(2, i.id);

            addItem.executeQuery();
            return true;
        } catch (Exception e) {
            System.out.printf("Unable to add item with ID: %d to menu with ID: %d. Try again later.\n", i.id, this.id);
            return false;
        }
    }

    /**
     * Add an item to the current menu
     * @param conn The database connection to use
     * @param i The item to add to the menu
     */
    public boolean delItem(Connection conn, Item i) {
        try {
            PreparedStatement delItem = conn.prepareStatement("DELETE FROM menu_items WHERE menu_id = ? AND item_id = ?");
            delItem.setInt(1, this.id);
            delItem.setInt(2, i.id);

            delItem.executeQuery();
            return true;
        } catch (Exception e) {
            System.out.printf("Unable to delete item with ID: %d from menu with ID: %d. Try again later.\n", i.id, this.id);
            return false;
        }
    }

    /**
     * Add an item to the current menu
     * @param conn The database connection to use
     * @param i The item to add to the menu
     * @param m The menu to add the item to
     */
    public static boolean addItem(Connection conn, Item i, Menu m) {
        try {
            PreparedStatement addItem = conn.prepareStatement("INSERT INTO menu_items (menu_id, item_id) VALUES (?,?)");
            addItem.setInt(1, m.id);
            addItem.setInt(2, i.id);

            addItem.executeQuery();
            return true;
        } catch (Exception e) {
            System.out.printf("Unable to add item with ID: %d to menu with ID: %d. Try again later.\n", i.id, m.id);
            return false;
        }
    }

    /**
     * Add an item to the current menu
     * @param conn The database connection to use
     * @param i The item to add to the menu
     * @param m The menu to add the item to
     */
    public boolean delItem(Connection conn, Item i, Menu m) {
        try {
            PreparedStatement delItem = conn.prepareStatement("DELETE FROM menu_items WHERE menu_id = ? AND item_id = ?");
            delItem.setInt(1, this.id);
            delItem.setInt(2, i.id);

            delItem.executeQuery();
            return true;
        } catch (Exception e) {
            System.out.printf("Unable to delete item with ID: %d from menu with ID: %d. Try again later.\n", i.id, m.id);
            return false;
        }
    }
    

    /**
     * Fetches the menu with id from the database and populates it with items
     * @param conn The database connection to use
     * @param id The id to query
     * @return The filled menu, or null if it was not found
     */
    public static Menu getPopulatedMenu(Connection conn, int id) {
        Menu rm;
        try {
            PreparedStatement getMenu = conn.prepareStatement("SELECT * FROM menus WHERE id=?");
            getMenu.setInt(1, id);

            ResultSet rs_gm = getMenu.executeQuery();
            if (!rs_gm.next()) //no menu found!
                return null;
            String n = rs_gm.getString("name");
            rm = new Menu(id, n, new ArrayList<Item>());
            rm.fillItems(conn);
            return rm;
        } catch (Exception e) {
            System.out.printf("Could not get a populated menu with ID: %d\n", id);
            e.printStackTrace(); //debug
        }
        return null;
    }

    /**
     * Populates current menu with items and names
     * @param conn The database connection to use
     * @param
     */
    private void getPopulatedMenu(Connection conn) {
       this.items = getPopulatedMenu(conn, this.id).items;
    }
}