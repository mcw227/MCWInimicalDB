import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Public class price change models an entry in the price change table in the database
 */
public class PriceChange {

    private static final int MAX_PAGE_SIZE = 10;
    private static final double MAX_PRICE = 99999999.99;
    private static final int MASTER_MENU_ID = 1;

    public int location_id;
    public int item_id;
    public double price;

    public PriceChange(int l_id, int i_id, double price) {
        this.location_id = l_id;
        this.item_id = i_id;
        this.price = price;
    }

    /**
     * Standard to string
     */
    public String toString() {
        return String.format("LOCATION ID: %-5d\t| ITEM ID: %-5d\t| NEW PRICE: %10.2f", this.location_id, this.item_id, this.price);
    }

    /**
     * Parses a price change from a result set
     * @return a price change if valid, null if invalid
     */
    public static PriceChange parsePriceChangeFromRS(ResultSet rs) {
        try {
            int l_id = rs.getInt("location_id");
            int i_id = rs.getInt("item_id");
            double p = rs.getDouble("price");
            return new PriceChange(l_id, i_id, p);
        } catch (Exception e) {
            //e.printStackTrace(); //debug
            return null;
        }
    }

    /**
     * Allows users to add a price change to database
     * @param conn The connection to the database.
     * @return True if price change was added, false if not
     */
    public boolean addPriceChange(Connection conn) {
        if (this.price > MAX_PRICE) {
            System.out.println("Price too high! Must be less than 99,999,999.99");
            return false;
        }
        try (PreparedStatement addPC = conn.prepareStatement("INSERT INTO price_change (item_id, location_id, price) VALUES (?, ?, ?)")) {
            addPC.setInt(1, this.item_id);
            addPC.setInt(2, this.location_id);
            addPC.setDouble(3, this.price);

            addPC.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Could not add price change to database. Try again later.");
            return false;
        }
    }

    /**
     * Allows the modification of a price change in the database
     * @param conn The database connection to use
     */
    public boolean editPriceChange(Connection conn) {
        if (this.price > 99999999.99) {
            System.out.println("Price too high! Must be less than 99,999,999.99");
            return false;
        }
        try (PreparedStatement addPC = conn.prepareStatement("UPDATE price_change SET price = ? WHERE item_id = ? AND location_id = ?")) {
            addPC.setDouble(1, this.price);
            addPC.setInt(2, this.item_id);
            addPC.setInt(3, this.location_id);

            addPC.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Could not edit the price change in the database. Try again later.");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Allows the deletion of a price change from the database
     * @param conn The database connection to use
     * @param l The location to delete hte price change from
     * @param id The id of the item whose price change we want to remove
     */
    public static boolean delPriceChange(Connection conn, Location l, int id) {
        try (PreparedStatement delPC = conn.prepareStatement("DELETE FROM price_change WHERE item_id = ? AND location_id = ?")) {
            delPC.setInt(1, id);
            delPC.setInt(2, l.id);

            delPC.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Could not delete the price change from the database. Try again later.");
            return false;
        }
    }

    /**
     * Allows the alteration of price changes for a particular location
     * @param conn The connection to the database
     * @param scn The scanner to grab input from
     * @param l The location to set price changes at
     */
    public static void priceChangeScreen(Connection conn, Scanner scn, Location l) {
        if (l == null) {
            System.out.println("Location given is null. Type anything to continue.");
            Helper.nextOK(scn);
            return;
        }

        l.fetchPriceChanges(conn);
        if (l.price_changes == null) {
            System.out.println("Unable to fetch price changes. Type anything to continue.");
            Helper.nextOK(scn);
            return;
        }

        boolean upd = false;

        Pager<PriceChange> prs = new Pager<>(l.price_changes, MAX_PAGE_SIZE);
        while (true) {
            if (upd) {
                l.fetchPriceChanges(conn);
                prs = new Pager<>(l.price_changes, MAX_PAGE_SIZE);
                upd = false;
            }
            prs.printCurrentPage();
            System.out.println("Type (n)ext, (p)revious, (a)dd (you may overwrite price changes this way), (d)elete or (q)uit.");
            int r = Helper.nextPNQAD(scn);

            if (r == -2)
                return;
            
            switch (r) {
                case (1):
                    prs.previousPage();
                    break;
                case (2):
                    prs.nextPage();
                    break;
                case (3):
                    upd = PriceChange.addPCScreen(conn, scn, l.price_changes, l);
                    break;
                case (4):
                    upd = PriceChange.delPCScreen(conn, scn, l.price_changes, l);
                    break;
                default:
                    break;
            }

        }

    }

    /**
     * Creates a screen that allows the user to add a price change
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     * @param price_changes the list of existing price_changes
     * @param l The location to add the price change to.
     */
    public static boolean addPCScreen(Connection conn, Scanner scn, ArrayList<PriceChange> price_changes, Location l) {
        ArrayList<Item> allowed_items = SignatureItem.fetchSignatures(conn);
        Helper.clearConsole();
        System.out.println("What is the id of the item whose price you'd like to change? Note that while we do not stop you from creating price changes on customer creations, they will not go into effect unless those items are turned into staple items or ingredients!.");
        int id = 0;
        boolean edit = false;
        while (id == 0) {
            final int r = Helper.nextId(scn);
            if (r == -2)
                return false;
            if (!allowed_items.stream().anyMatch(i -> i.id == r))
                System.out.println("Please input the id of a valid item.");
            else if (price_changes.stream().anyMatch(p -> p.item_id == r)) {
                edit = true;
                id = r;
            }
            else
                id = r;
        }

        System.out.println("What would you like to set the new price to?");
        double p = Helper.nextSafeDouble(scn, MAX_PRICE);
        if (p == -2.0)
            return false;

        PriceChange pc = new PriceChange(l.id, id, p);
        if (edit)
            return pc.editPriceChange(conn);
        return pc.addPriceChange(conn);
    }

    /**
     * Creates a screen that allows the user to delete a price change
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     * @param price_changes the list of existing price_changes
     * @param l The location to add the price change to.
     */
    public static boolean delPCScreen(Connection conn, Scanner scn, ArrayList<PriceChange> price_changes, Location l) {
        Helper.clearConsole();
        System.out.println("What is the id of the item whose price change you'd like to remove");
        int id = 0;
        while (id == 0) {
            final int r = Helper.nextId(scn);
            if (r == -2)
                return false;
            if (!price_changes.stream().anyMatch(p -> p.item_id == r))
                System.out.printf("No price change found for ID: %d\n", r);
            else
                id = r;
        }

        return PriceChange.delPriceChange(conn, l, id);
    }



}