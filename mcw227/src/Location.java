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
    public ArrayList<PriceChange> price_changes;

    public Location(int id, String address, String phone, double sales_tax) {
        this.id = id;
        this.address = address;
        this.phone = phone;
        this.sales_tax = sales_tax;
        local_menus = new ArrayList<LocalMenu>();
        price_changes = null;
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
        Pager<Location> locations = new Pager<>(Location.fetchLocations(conn), LOCATION_PAGE_SIZE);
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
        try (PreparedStatement getLocalMenus = conn.prepareStatement("SELECT * FROM local_menu_view WHERE location_id = ?")) {
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
            //e.printStackTrace(); //debug
        }
    }

    /**
     * Fetches all available locations and then returns them in an arrayList
     * @param conn The connection to use
     * @return An array list of location objects
     */
    public static ArrayList<Location> fetchLocations(Connection conn) {
        ArrayList<Location> locations = new ArrayList<>();

        try (PreparedStatement getLocations = conn.prepareStatement("SELECT * FROM locations")) {

            ResultSet rs = getLocations.executeQuery();
            
            if (!rs.next()) { //No cards, return empty arraylist
                return locations;
            }

            do {
                locations.add(parseLocationFromRS(rs));
            } while (rs.next()); //since we checked rs.next() above we have to use a do while loop instead, otherwise we skip the first one :(

        } catch (Exception e) {
            System.out.println("Could not query Database for locations. Please try again later.");
            //e.printStackTrace(); //debug
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

        try (PreparedStatement getLocations = conn.prepareStatement("SELECT * FROM locations WHERE id = ?")) {
            getLocations.setInt(1, id);
            ResultSet rs = getLocations.executeQuery();
            
            if (!rs.next()) { //No cards, return empty arraylist
                return location;
            }

            Location l = parseLocationFromRS(rs);
            l.fetchPriceChanges(conn);
            return l;

        } catch (Exception e) {
            System.out.println("Could not query Database for locations. Please try again later.");
            //e.printStackTrace(); //debug
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
     * Fetches price changes for the location
     */
    public void fetchPriceChanges(Connection conn) {
        ArrayList<PriceChange> pr = new ArrayList<>();
        try (PreparedStatement getChanges = conn.prepareStatement("SELECT * FROM price_change WHERE location_id = ?")) {
            getChanges.setInt(1, this.id);
            ResultSet rs = getChanges.executeQuery();
            
            if (!rs.next()) { // no price changes
                this.price_changes = pr;
                return;
            }
            do {
                PriceChange c = PriceChange.parsePriceChangeFromRS(rs);
                if (c == null)
                    return;
                pr.add(c);
            } while (rs.next());

            this.price_changes = pr;
            return;
        } catch (Exception e) {
            System.out.println("Could not fetch price changes. Please try again later.");
            return;
        }
    }

    /**
     * Fetches price changes for the location
     * @param conn The database connection to use
     * @param l_id the location id to query
     */
    public ArrayList<PriceChange> fetchPriceChanges(Connection conn, int l_id) {
        ArrayList<PriceChange> pr = new ArrayList<>();
        try (PreparedStatement getChanges = conn.prepareStatement("SELECT * FROM price_change WHERE location_id = ?")) {
            getChanges.setInt(1, l_id);
            ResultSet rs = getChanges.executeQuery();
            
            if (!rs.next()) // no price changes
                return pr;
            do {
                PriceChange c = PriceChange.parsePriceChangeFromRS(rs);
                if (c == null)
                    return null;
                pr.add(c);
                System.out.println(pr);
            } while (rs.next());

            return pr;

        } catch (Exception e) {
            System.out.println("Could not fetch price changes. Please try again later.");
            return null;
        }
    }

    /**
     * Checks whether there is a price change at the location
     * @param i_id the item you want to query.
     * @return null if there is no price change, a price change object if there is
     */
    public PriceChange fetchPriceChange(int i_id) {
        return this.price_changes.stream().filter(p -> p.item_id == i_id).findFirst().orElse(null);
    }

    /**
     * Checks whether there is a price change at the given location
     * @param conn The database connection to use
     * @param l_id the id of the location to query
     * @param i_id the id of the item whose price you want to investigate
     */
    public static PriceChange fetchPriceChange(Connection conn, int l_id, int i_id) {
        return fetchLocation(conn, l_id).fetchPriceChange(i_id);
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
            if (m_id < 0)
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

    /**
     * Prints the location's data summary
     * @param conn The connection to the database
     * @param scn The scanner to grab input from
     */
    public void printLocSummary(Connection conn, Scanner scn) {
        try {
            PreparedStatement totalSales = conn.prepareStatement("select sum(order_items.quantity) as total_items from order_items join orders on orders.id = order_items.order_id where orders.location_id = ?");
            Helper.clearConsole();
            totalSales.setInt(1, this.id);
            ResultSet rs = totalSales.executeQuery();

            int totalItems = 0;
            double totalGross = 0.0;
            String topFive = "";

            if (!rs.next())
                throw new Exception("No data found for location.");
            
            totalItems = rs.getInt("total_items");
            totalSales.close();

            PreparedStatement getTotalGross = conn.prepareStatement("select sum(order_items.price) as gross_total from order_items join orders on orders.id = order_items.order_id where orders.location_id = ?");

            getTotalGross.setInt(1, this.id);
            rs = getTotalGross.executeQuery();

            if (!rs.next())
                throw new Exception("No data found for location.");
            
            totalGross = rs.getDouble("gross_total");
            getTotalGross.close();


            PreparedStatement rankedItems = conn.prepareStatement("select items.name, items.id, items.price, sum(order_items.price) as total_gross, sum(order_items.quantity) as total_sold, dense_rank() over (order by sum(order_items.price) DESC) as item_rank from orders join order_items on order_items.order_id = orders.id join items on items.id = order_items.item_id where orders.location_id = ? group by items.id,items.name, items.price order by item_rank ASC FETCH FIRST 5 ROWS ONLY");
            rankedItems.setInt(1, this.id);

            rs = rankedItems.executeQuery();
            if (!rs.next())
                topFive = "\nNo items sold at location.";
            
            else {
                do {    
                    int item_id = rs.getInt("id");
                    String item_name = rs.getString("name");
                    double item_price = rs.getDouble("price");
                    double gross = rs.getDouble("total_gross");
                    int total_sold = rs.getInt("total_sold");
                    int rank = rs.getInt("item_rank");
                    topFive += String.format("RANK: %-4d\t| ID:%-5d\t| NAME: %-30s\t| INDIVIDUAL PRICE: $%-6.2f\t| TOTAL SOLD: %-7d\t| GROSS EARNINGS: $%.2f\n\n", rank, item_id, item_name, item_price, total_sold, gross);
                } while (rs.next());
                rankedItems.close();
            }

            System.out.printf("SUMMARY FOR LOCATION WITH ID: %d\n\tADDRESS: %s\n\tTOTAL ITEMS SOLD: %d\tGROSS TOTAL: $%.2f\n\t\n--- TOP FIVE ITEMS ---\n", this.id, this.address, totalItems, totalGross);
            System.out.println(topFive);

            System.out.println("\nType anything to continue.");
            Helper.nextOK(scn);
            return;
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Could not generate statistics from the database. Try again later.");
            System.out.println("Type anything to continue.");
            Helper.nextOK(scn);
            return;
        }
    } 
}