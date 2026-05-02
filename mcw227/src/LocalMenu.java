import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/** Public class LocalMenu models an entry in the local menu table in the database */
public class LocalMenu extends Menu {
    private static final int MASTER_MENU_ID = 1;
    private static final int MASTER_LUNCH_MENU_ID = 2;
    private static final int MASTER_DINNER_MENU_ID = 3;
    private static final int MASTER_DESSERT_MENU_ID = 4;

    private static final int MAX_PAGE_SIZE = 10;

    public int location_id;
    public Location location;

    public LocalMenu(int id, String name, ArrayList<Item> items, int location_id) {
        super(id, name, items);
        this.location_id = location_id;
        this.location = null;
    }

    public LocalMenu(Menu menu, int location_id) {
        super(menu.id, menu.name, menu.items);
        this.location_id = location_id;
        this.location = null;
    }

    public LocalMenu(int id, String name, int location_id) {
        super(id, name, new ArrayList<Item>());
        this.location_id = location_id;
        this.location = null;
    }

    public String toString() {
        return String.format("ID: %-3d\t| NAME: %-20s\t| LOCATION_ID: %-3d", this.id, this.name, this.location_id);
    }

    /**
     * Fills a local menu with its corresponding item objects with prices based on the location id of the local menu
     * @param conn The database connection to use
     */
    public void fillItems(Connection conn) {
        this.items = new ArrayList<Item>();

        if (this.location == null)
            location = Location.fetchLocation(conn, this.location_id);
        if (this.location == null || this.location.price_changes == null) {
            System.out.println("Could not fetch location to fill items for local menu.");
            return;
        }
        try (PreparedStatement getMenuItems = conn.prepareStatement("SELECT * FROM menu_items m JOIN all_items_class_view i ON m.item_id = i.id WHERE m.menu_id = ? ORDER BY i.id DESC")) {
            getMenuItems.setInt(1, this.id);

            ResultSet rs = getMenuItems.executeQuery();
            if (!rs.next()) { //no items in menu
                this.items = new ArrayList<>(); 
            }
            else {
                do {
                    Item r_item = Item.parseItemFromRS(rs);
                    if (CustomerCreation.class.isInstance(r_item)) {
                        ((CustomerCreation)r_item).populateRecipe(conn, this.location);
                    } else {
                        PriceChange pr = this.location.fetchPriceChange(r_item.id);
                        if (pr == null) {
                            r_item.price *= this.location.sales_tax;
                        } else {
                            r_item.price = pr.price * this.location.sales_tax;
                        }
                        
                    }
                    this.items.add(r_item);
                } while (rs.next());
            }
        } catch (Exception e) {
            System.out.println("Unable to populate menu. Try again later.");
            this.items = null;
            //e.printStackTrace(); //debug
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
        try (PreparedStatement getMenu = conn.prepareStatement("SELECT * FROM menus WHERE id=?")) {
            getMenu.setInt(1, id);

            ResultSet rs_gm = getMenu.executeQuery();
            if (!rs_gm.next()) //no menu found!
                return null;
            String n = rs_gm.getString("name");
            rm = new LocalMenu(id, n, new ArrayList<Item>(), location_id);
            rm.location = Location.fetchLocation(conn, location_id);
            rm.fillItems(conn);
            return rm;
        } catch (Exception e) {
            System.out.printf("Could not get a populated menu with ID: %d\n", id);
            //e.printStackTrace(); //debug
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
        r.add(LocalMenu.getPopulatedMenu(conn, MASTER_MENU_ID, location_id));
        r.add(LocalMenu.getPopulatedMenu(conn, MASTER_LUNCH_MENU_ID, location_id));
        r.add(LocalMenu.getPopulatedMenu(conn, MASTER_DINNER_MENU_ID, location_id));
        r.add(LocalMenu.getPopulatedMenu(conn, MASTER_DESSERT_MENU_ID, location_id));
        return r;
    }

    public static boolean buildLocalMenuScreen(Connection conn, Scanner scn, Menu m, Location l) {
        if (l == null) {
            System.out.println("Location given was null.");
            return false;
        }

        LocalMenu lm = new LocalMenu(m, l.id);

        if (lm.id != -1)
            lm.fillItems(conn);
        
        LocalMenu master_list = LocalMenu.getPopulatedMenu(conn, MASTER_MENU_ID, l.id);

        ArrayList<Item> items = master_list.items;
        Pager<Item> i = new Pager<>(items, MAX_PAGE_SIZE);
        while(true) {
            Helper.clearConsole();
            i.printCurrentPage();
            System.out.println("Type (n)ext, (p)revious, (m)enu to view and edit current items, (d)one to add the menu, or (q)uit to quit (You will lose your progress!).");
            int r = Helper.nextPNQMDID(scn);
            
            if (r == -2)
                return false;

            switch (r) {
                case (-3):
                    i.previousPage();
                    break;
                case (-4):
                    i.nextPage();
                    break;
                case (-5):
                    lm.editMenu(scn);
                    break;
                case (-6):
                    if (lm.addMenu(conn)) {
                        System.out.println("Menu added successfully. Type anything to continue.");
                        Helper.nextOK(scn);
                        return true;
                    }

                    break;
                default:
                    Item n_i = items.stream().filter(it -> it.id == r).findFirst().orElse(null);
                    if (n_i == null) {
                        System.out.printf("Item with ID: %d not found.");
                        break;
                    }
                    lm.addItem(n_i);
            }

        }
            
    }
}