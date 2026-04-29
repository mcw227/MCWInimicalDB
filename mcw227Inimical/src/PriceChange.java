import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Public class price change models an entry in the price change table in the database
 */
public class PriceChange {
    public int location_id;
    public int item_id;
    public double price;

    public PriceChange(int l_id, int i_id, double price) {
        this.location_id = l_id;
        this.item_id = i_id;
        this.price = price;
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
}