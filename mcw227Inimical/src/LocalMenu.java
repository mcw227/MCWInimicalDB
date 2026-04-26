import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/** Public class LocalMenu models an entry in the local menu table in the database */
public class LocalMenu extends Menu {
    public int location_id;

    public LocalMenu(int id, String name, ArrayList<Item> items, int location_id) {
        super(id, name, items);
        this.location_id = location_id;
    }

    public LocalMenu(int id, String name, int location_id) {
        super(id, name, new ArrayList<Item>());
        this.location_id = location_id;
    }

    /**
     * Fills a local menu with its corresponding item objects with prices based on the location id of the local menu
     * @param conn The database connection to use
     */
    public void fillItems(Connection conn) {
        ArrayList<Item> filled_items = new ArrayList<>();
        try {
            PreparedStatement getMenuItems = conn.prepareStatement("SELECT * FROM menu_items WHERE menu_id = ?");
            getMenuItems.setInt(1, this.id);

            ResultSet rs = getMenuItems.executeQuery();
            if (!rs.next()) { //no items in menu
                this.items = filled_items; 
            }
            else {
                do {
                    int item_id = rs.getInt("item_id");
                    filled_items.add(Item.createItemFromID(conn, item_id, this.location_id));
                } while (rs.next());
            }
        } catch (Exception e) {
            System.out.println("Unable to populate menu. Try again later.");
            e.printStackTrace(); //debug
        }
        this.items = filled_items;
    }


}