import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/** Public class menu models an entry in the Menu tbale in the database */
public class Menu {

    private static final int MAX_PAGE_SIZE = 10;
    private static final int MAX_NAME_SIZE = 20;

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
        try (PreparedStatement getMenuItems = conn.prepareStatement("SELECT * FROM menu_items m JOIN all_items_class_view i ON m.item_id = i.id WHERE m.menu_id = ? ORDER BY i.id DESC")) {
            getMenuItems.setInt(1, this.id);

            ResultSet rs = getMenuItems.executeQuery();
            if (!rs.next()) { //no items in menu
                return; 
            }
            else {
                do {
                    filled_items.add(Item.parseItemFromRS(rs));
                } while (rs.next());
            }
            this.items = filled_items;
        } catch (Exception e) {
            System.out.println("Unable to populate menu. Try again later.");
            //e.printStackTrace(); //debug
        }
    }

    /**
     * Add an item to the current menu
     * @param conn The database connection to use
     * @param i The item to add to the menu
     */
    public boolean addItem(Connection conn, Item i) throws SQLException {
        PreparedStatement addItem = conn.prepareStatement("INSERT INTO menu_items (menu_id, item_id) VALUES (?,?)");
        addItem.setInt(1, this.id);
        addItem.setInt(2, i.id);
        addItem.executeUpdate();
        return true;
    }

    /**
     * Add an item to the current menu
     * @param conn The database connection to use
     * @param i The item to add to the menu
     */
    public boolean delItem(Connection conn, Item i) throws SQLException {
        PreparedStatement delItem = conn.prepareStatement("DELETE FROM menu_items WHERE menu_id = ? AND item_id = ?");
        delItem.setInt(1, this.id);
        delItem.setInt(2, i.id);
        delItem.executeUpdate();
        delItem.close();
        return true;
    }

    /**
     * Add an item to the current menu
     * @param conn The database connection to use
     * @param i The item to add to the menu
     * @param m The menu to add the item to
     */
    public static boolean addItem(Connection conn, Item i, Menu m) {
        try (PreparedStatement addItem = conn.prepareStatement("INSERT INTO menu_items (menu_id, item_id) VALUES (?,?)")){
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
        try (PreparedStatement delItem = conn.prepareStatement("DELETE FROM menu_items WHERE menu_id = ? AND item_id = ?")) {
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
        try (PreparedStatement getMenu = conn.prepareStatement("SELECT * FROM menus WHERE id=?")) {
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
            //e.printStackTrace(); //debug
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

    /**
     * Returns all menus that exist in the database
     * @param conn The database connection to use
     * @return An arraylist of menus
     */
    public static ArrayList<Menu> fetchMenus(Connection conn) {
        ArrayList<Menu> menus = new ArrayList<>();

        try (PreparedStatement getMenus = conn.prepareStatement("SELECT * FROM menus")) {
            ResultSet rs = getMenus.executeQuery();
            
            if (!rs.next()) { //No cards, return empty arraylist
                return menus;
            }

            do {
                menus.add(parseMenuFromRS(rs));
            } while (rs.next()); //since we checked rs.next() above we have to use a do while loop instead, otherwise we skip the first one :(

        } catch (Exception e) {
            System.out.println("Could not query Database for menus. Please try again later.");
            //e.printStackTrace(); //debug
            return null;
        }
        return menus;
    }

    /**
     * Returns all menus that exist in the database under a certain location
     * @param conn The database connection to use
     * @return An arraylist of menus
     */
    public static ArrayList<Menu> fetchMenus(Connection conn, Location l) {
        if (l == null) {
            System.out.println("Location given to fetch menus was null.");
            return null;
        }
        ArrayList<Menu> menus = new ArrayList<>();

        try (PreparedStatement getMenus = conn.prepareStatement("SELECT * FROM local_menu_view WHERE location_id = ?")) {
            getMenus.setInt(1, l.id);
            ResultSet rs = getMenus.executeQuery();
            
            if (!rs.next()) { //No cards, return empty arraylist
                return menus;
            }

            do {
                menus.add(parseMenuFromRS(rs));
            } while (rs.next()); //since we checked rs.next() above we have to use a do while loop instead, otherwise we skip the first one :(

        } catch (Exception e) {
            System.out.println("Could not query Database for menus. Please try again later.");
            //e.printStackTrace(); //debug
            return null;
        }
        return menus;
    }

    /**
     * Parses a result set into a menu object
     * @param rs The result set to parse
     * @return A menu if valid or null if invalid
     */
    public static Menu parseMenuFromRS(ResultSet rs) {
        try {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            return new Menu(id, name);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Attempts to add self to database. Replaces menu if it exists already
     * @param conn The database connection to use
     * @return True if menu was added, false if not
     */
    public boolean addMenu(Connection conn) {
        if (this.id == 1) //cannot alter master menu.
            return false;
        try (PreparedStatement getMenu = conn.prepareStatement("SELECT * FROM menu_items JOIN menus on menus.id = menu_items.menu_id WHERE menu_id = ?")) {
            conn.setAutoCommit(false);
            getMenu.setInt(1, this.id);

            ResultSet rs = getMenu.executeQuery();
            if (rs.next()) { //menu exists, so just wipe all menu items from db
                this.wipeItems(conn);
            } else { //menu does not exist yet, so add it to menu list
                try (PreparedStatement addMenu = conn.prepareStatement("INSERT INTO menus (name) VALUES (?)", new String[] {"ID"})) {
                    addMenu.setString(1, this.name);
                    if (addMenu.executeUpdate() != 0) {
                        ResultSet newIDRS = addMenu.getGeneratedKeys();
                        if (newIDRS.next())
                            this.id = (int)newIDRS.getLong(1);
                        else
                            throw new Exception("ID was not generated!");
                    }
                }
            }

            for (Item i : this.items) {
                this.addItem(conn, i);
            }

            if (LocalMenu.class.isInstance(this)) { //If local menu, add it to local menu table
                try (PreparedStatement addLocalMenu = conn.prepareStatement("INSERT INTO local_menus (menu_id, location_id) VALUES (?,?)")) {
                    LocalMenu temp = (LocalMenu)this;
                    addLocalMenu.setInt(1,this.id);
                    addLocalMenu.setInt(2,temp.location_id);
                    addLocalMenu.executeUpdate();
                }
            }

            return true;
        } catch (Exception e) {
            System.out.println("Could not add menu to database.");
            //e.printStackTrace();
            try {
                conn.rollback();
                return false;
            } catch (Exception f) {
                System.err.println("Critical database error. Please restart application.");
                System.exit(-1);
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (Exception e) {
                System.err.println("Critical database error. Please restart application.");
                System.exit(-1);
            }
        }
        return false;
    }

    public void wipeItems(Connection conn) throws SQLException {
        PreparedStatement wipeItems = conn.prepareStatement("DELETE FROM menu_items WHERE menu_id = ?");
        wipeItems.setInt(1, this.id);
        wipeItems.executeUpdate();
        wipeItems.close();
        return;
    }

    /**
     * Shows the items in the menu
     * @param conn The connection to the database to use
     * @param scn The scanner to grab input from
     */
    public void show(Connection conn, Scanner scn) {
        //Helper.clearConsole();
        if (this.items == null || this.items.size() == 0)
            this.fillItems(conn);
        if (this.items == null) {
            System.out.println("Menu is empty. Type anything to continue");
            Helper.nextOK(scn);
            return;
        }
        Pager<Item> items = new Pager<>(this.items, MAX_PAGE_SIZE);
        items.show(scn);
        return;
    } 

    /**
     * Allows users to pick from a list of all menus and view their items
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     */
    public static void showMenus(Connection conn, Scanner scn) {
        Helper.clearConsole();
        ArrayList<Menu> menus = Menu.fetchMenus(conn);
        if (menus == null || menus.isEmpty()) {
            System.out.println("--- UNABLE TO LOAD MENUS OR NO MENUS IN DATABASE ---");
            System.out.println("Type anything to continue");
            Helper.nextOK(scn);
            return;
        }

        Pager<Menu> m = new Pager<>(menus, MAX_PAGE_SIZE);
        while(true) {
            Helper.clearConsole();
            m.printCurrentPage();
            System.out.println("Press (n)ext, (p)revious, an id to check an item, or (q)uit.");
            int resp = Helper.nextPNQID(scn);
            if (resp == -2)
                return;
            
            switch (resp) {
                case (-3):
                    m.previousPage();
                    break;
                case (-4):
                    m.nextPage();
                    break;
                default:
                    if (resp < 0)
                        System.out.println("Invalid id!");
                    else {
                        Menu me = m.list.stream().filter(men -> men.id == resp).findFirst().orElse(null);
                        if (me == null)
                            System.out.printf("Could not find mwnu with id %d.\n",resp);
                        else {
                            me.show(conn, scn);
                        }
                    }
                    break;
            }
        }
        
    }

    /**
     * Allows users to pick from a list of all menus and view their items
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     * @param l The location to grab "local" menus from. Note that their prices will not be adjusted for that location
     */
    public static void showMenus(Connection conn, Scanner scn, Location l) {
        if (l == null) {
            System.out.println("Location provided is null. Type anything to continue.");
            Helper.nextOK(scn);
            return;
        }
        
        Helper.clearConsole();
        ArrayList<Menu> menus = Menu.fetchMenus(conn, l);
        if (menus == null || menus.isEmpty()) {
            System.out.println("--- UNABLE TO LOAD MENUS OR NO MENUS IN DATABASE ---");
            System.out.println("Type anything to continue");
            Helper.nextOK(scn);
            return;
        }

        Pager<Menu> m = new Pager<>(menus, MAX_PAGE_SIZE);
        while(true) {
            Helper.clearConsole();
            m.printCurrentPage();
            System.out.println("Press (n)ext, (p)revious, an id to edit/check a menu, (d)elete, or (q)uit.");
            int resp = Helper.nextPNQDID(scn);
            if (resp == -2)
                return;
            
            switch (resp) {
                case (-3):
                    m.previousPage();
                    break;
                case (-4):
                    m.nextPage();
                    break;
                case (-5):
                    if (Menu.delMenuScreen(conn, scn, menus)) {
                        menus = Menu.fetchMenus(conn, l);
                        m = new Pager<Menu>(menus, MAX_PAGE_SIZE);
                    }
                    break;
                default:
                    if (resp < 0)
                        System.out.println("Invalid id!");
                    else {
                        Menu me = m.list.stream().filter(men -> men.id == resp).findFirst().orElse(null);
                        if (me == null)
                            System.out.printf("Could not find mwnu with id %d.\n",resp);
                        else {
                            LocalMenu.buildLocalMenuScreen(conn, scn, me, l);
                        }
                    }
                    break;
            }
        } 
    }

    /**
     * Allows a user to delete a menu from the provided list of menus
     * @param conn The database connection to use
     * @param scn the scanner to grab input from
     * @param m The list of menus the user may delete from
     */
    public static boolean delMenuScreen(Connection conn, Scanner scn, ArrayList<Menu> m) {
        if (m == null || m.size() == 0)
            return false;
        System.out.println("Which menu would you like to delete? (Can type (q)uit to quit.)");
        while (true) {
            int r = Helper.nextId(scn);
            if (r == -2)
                return false;
            else if (m.stream().anyMatch(men -> men.id == r))
                return delMenu(conn, r);
            System.out.println("Not a valid id!");
        }
    }

    /**
     * Allows the deletion of a menu from the database
     * @param conn The database connection to use
     * @param id The id of the menu to delete
     */
    public static boolean delMenu(Connection conn, int id) {
        if (id > 0 && id < 4) //cannot delete master menus!
            return false;
        try (PreparedStatement delMenu = conn.prepareStatement("DELETE FROM menus WHERE id = ?")) {
            delMenu.setInt(1, id);
            delMenu.executeQuery();
            return true;
        } catch (Exception e) {
            System.out.printf("Unable to delete menu with ID: %d from database. Try again later.", id);
            return false;
        }
    }

    /**
     * Allows a user to create a new menu
     * @param conn The connection to the database
     * @param scn The scanner to grab input from
     * @param l The location to add the menu to
     */
    public static boolean createMenu(Connection conn, Scanner scn, Location l) {
        if (l == null) {
            System.out.println("Location provided is null. Type anything to continue.");
            Helper.nextOK(scn);
            return false;
        }

        Menu m = null;
        while (m == null) {
            Helper.clearConsole();
            System.out.println("Which would you like to do?\n\t1. Start From Scratch\n\t2. Start From Existing Menu\n Can also type (q)uit.");
            int r = Helper.nextId(scn);
            if (r == -2)
                return false;
            switch (r) {
                case (1):
                    System.out.println("What would you like to name the menu? (Can also type (!q)uit)");
                    String name = Helper.safeCheckQuit(scn, MAX_NAME_SIZE);
                    if (name == null)
                        break;
                    m = new Menu(-1, name);
                    break;
                case (2):
                    while (true) {
                        System.out.println("What is the ID of the menu you'd like to base your menu off of? (Can also type (q)uit)");
                        int id = Helper.nextId(scn);
                        if (id == -2)
                            break;
                        m = getPopulatedMenu(conn, id);
                        if (m == null)
                            System.out.println("Please pick a valid menu id!");
                        else {
                            System.out.printf("What would you like to name your new menu (can also type (!q)uit)? (Parent menu's name is %s)\n", m.name);
                            String n = Helper.safeCheckQuit(scn, MAX_NAME_SIZE);
                            if (n == null)
                                m = null;
                            else {
                                m.id = -1;
                                m.name = n;
                                break;
                            }

                        }
                    }
            }
        }

        return LocalMenu.buildLocalMenuScreen(conn, scn, m, l);

    }

    /**
     * opens a screen that allows the removal of items in the LOCAL (as in not the database) menu
     * @param scn Scanner to grab input from
     */
    public void editMenu(Scanner scn) {
        if (this.items == null || this.items.isEmpty()) {
            System.out.println("--- NO ITEMS IN MENU OR UNABLE TO OBTAIN ITEMS FROM DATABASE ---");
            System.out.println("Type anything to continue.");
            Helper.nextOK(scn);
        }

        Pager<Item> i = new Pager<>(this.items, MAX_PAGE_SIZE);

        while (true) {
            Helper.clearConsole();
            i.printCurrentPage();
            System.out.println("Type (n)ext, (p)revious, an ID to remove from the list, or (q)uit.");
            int r = Helper.nextPNQID(scn);
            
            if (r == -2)
                return;
            switch (r) {
                case (-3):
                    i.previousPage();
                    break;
                case (-4):
                    i.nextPage();
                    break;
                default:
                    this.removeItem(r);
                    break;
            }
        }
    }

    /**
     * Remove an item from the un-made list.
     * @param id the Id of the item to remove
     */
    public void removeItem(int id) {
        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.get(i).id == id) {
                this.items.remove(i);
                return;
            }
        }
        System.out.printf("No item with id %d found in menu.\n", id);
        return;
    }

    /**
     * Add an item to the un-made list
     * @param i The item to add
     */
    public void addItem(Item i) {
        if (i == null)
            return;
        else if (this.items.stream().anyMatch(it -> it.id == i.id)) //item already exists in list.
            return;
        this.items.add(i);
    }
}