import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/** Public class location models an entry in the database under the location table. */
public class Location {

    private static final int LOCATION_PAGE_SIZE = 10;
    public int id;
    public String address;
    public String phone;
    public double sales_tax; 
    public ArrayList<LocalMenu> local_menus;

    public Location(int id, String address, String phone, double sales_tax) {
        this.id = id;
        this.address = address;
        this.phone = phone;
        this.sales_tax = sales_tax;
        local_menus = new ArrayList<LocalMenu>();
    }

    public String toString() {
        return String.format("ID: %-3d\t| ADDRESS: %-50s\t| PHONE: %-20S \t| SALES TAX: %.2f", this.id, this.address, this.phone, this.sales_tax);
    }

    /**
     * Allows the selection of locations
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     * @return The id of a location or -2 if the user decided to quit
     * NEEDS TO BE IMPLEMENTED!
     */
    public static Location locationSelectScreen(Connection conn, Scanner scn) {
        Helper.clearConsole();
        Pager<Location> locations = new Pager(Location.fetchLocations(conn), LOCATION_PAGE_SIZE);
        while (true) {
            locations.printCurrentPage();
            if (!locations.list.isEmpty()) {
                System.out.println("Press n to go to next page, p to go to previous, q to quit.");
                System.out.println("Please select a location using an id.");
                int resp = Helper.nextPNQID(scn);
                switch (resp) {
                    case -2:
                        return null;
                    case -3:
                        Helper.clearConsole();
                        locations.previousPage();
                        break;
                    case -4:
                        Helper.clearConsole();
                        locations.nextPage();
                        break;
                    default:
                        return locations.list.stream().filter(location -> location.id == resp).findFirst().orElse(null); //returns the location object
                }
            }
            else {
                return null;
            }
        }
    }

    /**
     * Populates the menus for a location
     * @return An array list of populated local menus
     */
    public void getLocalMenus(Connection conn) {
        try {
            PreparedStatement getLocalMenus = conn.prepareStatement("SELECT * FROM local_menu_view WHERE location_id = ?");
            getLocalMenus.setInt(1, this.id);

            ResultSet rs = getLocalMenus.executeQuery();
            if (!rs.next()) {
                local_menus.addAll(LocalMenu.getMasterMenus(conn, this.id));
                return;
            }
                
            
            do {
                int menu_id = rs.getInt("id");
                String name = rs.getString("name");
                LocalMenu lm = new LocalMenu(menu_id, name, this.id);
                lm.fillItems(conn);
                local_menus.add(lm);
                
            } while (rs.next());
        } catch (Exception e) {
            System.out.println("Could not get local menus. Try again later.");
            e.printStackTrace(); //debug
        }
    }

    /**
     * Fetches all available locations and then returns them in an arrayList
     * @param conn The connection to use
     * @return An array list of location objects
     */
    public static ArrayList<Location> fetchLocations(Connection conn) {
        ArrayList<Location> locations = new ArrayList<>();

        try {
            PreparedStatement getLocations = conn.prepareStatement("SELECT * FROM locations");
            ResultSet rs = getLocations.executeQuery();
            
            if (!rs.next()) { //No cards, return empty arraylist
                return locations;
            }

            do {
                locations.add(parseLocationFromRS(rs));
            } while (rs.next()); //since we checked rs.next() above we have to use a do while loop instead, otherwise we skip the first one :(

        } catch (Exception e) {
            System.out.println("Could not query Database for locations. Please try again later.");
            e.printStackTrace(); //debug
            return null;
        }
        return locations;
    }

    /**
     * Attempts to fetch a location by its id, or returns null if it is not found
     * @param conn The database connection to use
     * @param id The id to search for
     */
    public static Location fetchLocation(Connection conn, int id) {
        Location location = null;

        try {
            PreparedStatement getLocations = conn.prepareStatement("SELECT * FROM locations WHERE id = ?");
            getLocations.setInt(1, id);
            ResultSet rs = getLocations.executeQuery();
            
            if (!rs.next()) { //No cards, return empty arraylist
                return location;
            }

            return parseLocationFromRS(rs);

        } catch (Exception e) {
            System.out.println("Could not query Database for locations. Please try again later.");
            e.printStackTrace(); //debug
            return null;
        }
    }

    /**
     * Parses a location from a result set
     * @param rs the Result set to parse
     * @return The location corresponding to the row given by the result set or null if it is invalid
     */
    public static Location parseLocationFromRS(ResultSet rs) {
        try {
            int id = rs.getInt("id");
            String address = rs.getString("address");
            String phone_number = rs.getString("phone");
            double sales_tax = rs.getDouble("sales_tax");

            return new Location(id, address, phone_number, sales_tax);
        } catch (Exception e) {
            return null;
        }
       
    }

    /**
     * This method consolodates all menus into one list of items.
     * @return A list of items that exist in all the menus. There may be repeats.
     */
    public ArrayList<Item> fetchConsolodatedMenu() {
        ArrayList<Item> items = new ArrayList<>();
        for (Menu m : local_menus) {
            items.addAll(m.items);
        }
        return items;
    }
    
    /**
     * Lets users pick from menus available at the given location.
     * @param scn Scanner to grab input from
     * @return A localMenu or null if user wnats to quit
     */
    public LocalMenu pickMenu(Scanner scn) {
        System.out.println(this.getMenuString());
        System.out.println("Which menu would you like to view? (or type (q)uit to quit.)");
        while(true) {
            int m_id = Helper.nextId(scn);
            if (m_id == -2)
                return null;
            LocalMenu m = local_menus.stream().filter(menu -> menu.id == m_id).findFirst().orElse(null);
            if (m == null)
                System.out.printf("Menu with ID: %d not found!\n", m_id);
            else
                return m;
        } 
    }

    public String getMenuString() {
        String ret = "";
        if (local_menus.size() == 0) {
            return "NO LOCAL MENUS";
        }
        for (LocalMenu m : local_menus) {
            System.out.println(m);
        }
        return ret;
    }
}