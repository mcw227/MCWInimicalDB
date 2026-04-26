import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/** Public class location models an entry in the database under the location table. */
public class Location {

    public int id;
    public String address;
    public String phone;
    public double sales_tax; 
    public ArrayList<Item> local_menu; //holds all price adjustments for the restaurant

    public Location(int id, String address, String phone, double sales_tax) {
        this.id = id;
        this.address = address;
        this.phone = phone;
        this.sales_tax = sales_tax;
        local_menu = new ArrayList<Item>();
    }

    /**
     * Allows the selection of locations
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     * @return The id of a location or -2 if the user decided to quit
     * NEEDS TO BE IMPLEMENTED!
     */
    public static int locationSelectScreen(Connection conn, Scanner scn) {
        return 1;
    }

    public static ArrayList<Item> fetchLocalMenu(Connection conn, int location_id) {
        if (location_id == -1) //Non-local menu
            return Item.fetchMenuItems(conn);
        else //local menu
            return Item.fetchMenuItems(conn, location_id);

    }
}