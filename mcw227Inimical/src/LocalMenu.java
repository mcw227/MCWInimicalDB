import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/** Public class LocalMenu models an entry in the local menu table in the database */
public class LocalMenu extends Menu {
    private static final int MASTER_LUNCH_MENU_ID = 1;
    private static final int MASTER_DINNER_MENU_ID = 2;
    private static final int MASTER_DESSERT_MENU_ID = 3;

    public int location_id;

    public LocalMenu(int id, String name, ArrayList<Item> items, int location_id) {
        super(id, name, items);
        this.location_id = location_id;
    }

    public LocalMenu(Menu menu, int location_id) {
        super(menu.id, menu.name, menu.items);
        this.location_id = location_id;
    }

    public LocalMenu(int id, String name, int location_id) {
        super(id, name, new ArrayList<Item>());
        this.location_id = location_id;
    }

    public String toString() {
        return String.format("ID: %-3d\t| NAME: %-20s\t| LOCATION_ID: %-3d", this.id, this.name, this.location_id);
    }

    /**
     * Fills a local menu with its corresponding item objects with prices based on the location id of the local menu
     * @param conn The database connection to use
     */
    public void fillItems(Connection conn) {
        try {
            PreparedStatement getMenuItems = conn.prepareStatement("SELECT * FROM menu_items WHERE menu_id = ?");
            getMenuItems.setInt(1, this.id);

            ResultSet rs = getMenuItems.executeQuery();
            if (!rs.next()) { //no items in menu
                this.items = new ArrayList<>(); 
            }
            else {
                do {
                    int item_id = rs.getInt("item_id");
                    Item newItem = Item.createItemFromID(conn, item_id, this.location_id);
                    this.items.add(newItem);
                } while (rs.next());
            }
        } catch (Exception e) {
            System.out.println("Unable to populate menu. Try again later.");
            e.printStackTrace(); //debug
        }
    }

    /**
     * Fetches the menu with id from the database and populates it with items that are priced at the location's set price.
     * @param conn The database connection to use
     * @param id The id to query
     * @return The filled menu, or null if it was not found
     */
    public static LocalMenu getPopulatedMenu(Connection conn, int id, int location_id) {
        LocalMenu rm;
        try {
            PreparedStatement getMenu = conn.prepareStatement("SELECT * FROM menus WHERE id=?");
            getMenu.setInt(1, id);

            ResultSet rs_gm = getMenu.executeQuery();
            if (!rs_gm.next()) //no menu found!
                return null;
            String n = rs_gm.getString("name");
            rm = new LocalMenu(id, n, new ArrayList<Item>(), location_id);
            rm.fillItems(conn);
            return rm;
        } catch (Exception e) {
            System.out.printf("Could not get a populated menu with ID: %d\n", id);
            e.printStackTrace(); //debug
        }
        return null;
    }

    /**
     * Returns the master menus 
     * @param conn The database connection to use
     * @param location_id The location whose prices must be used
     * @return An arraylist of local menus derived from master menus
     */
    public static ArrayList<LocalMenu> getMasterMenus(Connection conn, int location_id) {
        ArrayList<LocalMenu> r = new ArrayList<>();
        r.add(LocalMenu.getPopulatedMenu(conn, MASTER_LUNCH_MENU_ID, location_id));
        r.add(LocalMenu.getPopulatedMenu(conn, MASTER_DINNER_MENU_ID, location_id));
        r.add(LocalMenu.getPopulatedMenu(conn, MASTER_DESSERT_MENU_ID, location_id));
        return r;
    }
}