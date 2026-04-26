import java.sql.*;
import java.util.ArrayList;

/** Public class OrderItem to model an entry into the OrderItem db */
public class OrderItem extends Item {

    public int order_id;
    public int quantity;

    public OrderItem(int order_id, int item_id, String name, double price, int quantity) {
        super(item_id, name, price);
        this.quantity = quantity;
        this.order_id = order_id;
    }

    /** Standard toString method */
    public String toString() {
        return String.format("ID: %-3d\t| ORDER_ID: %-3d\t| NAME:%-50%s\t| QUANTITY:%3d\t| PRICE:%.2f\t | CUMULATIVE TOTAL:%.2f", id, name, quantity, price, price*quantity);
    }

    /**
     * Adds an order item to the database.
     * @param conn The database connection to use
     * @param i The order item to add
     */
    public static boolean addOrderItem(Connection conn, OrderItem i) throws SQLException {
        PreparedStatement addItem = conn.prepareStatement("INSERT INTO order_items (order_id, name, price, quantity) VALUES (?,?,?,?)");
        addItem.setString(2, i.name);
        addItem.setDouble(3,i.price);
        addItem.setInt(1, i.order_id);
        addItem.setInt(4, i.quantity);

        System.out.print("Adding order item to Database...");
        addItem.executeUpdate();
        System.out.println("Done!");
        return true;
    }

    /**
     * Adds the orderitem to the database
     * @param conn The database connection to use
     */
    public boolean addOrderItem(Connection conn) throws SQLException {
        PreparedStatement addItem = conn.prepareStatement("INSERT INTO order_items (order_id, name, price, quantity) VALUES (?,?,?,?)");
        addItem.setString(2, this.name);
        addItem.setDouble(3,this.price);
        addItem.setInt(1, this.order_id);
        addItem.setInt(4, this.quantity);

        System.out.print("Adding order item to Database...");
        addItem.executeUpdate();
        System.out.println("Done!");
        return true;
    }

    /**
     * Removes an item from the database. Note that this does not handle signature items or customer creations
     * @param conn The databsae connection to use
     * @param id The id of the item we want to delete.
     */
    public static boolean delOrderItem(Connection conn, int order_id, int item_id) throws SQLException {
        PreparedStatement delItem = conn.prepareStatement("DELETE FROM order_items WHERE order_id=? AND item_id = ?");
        delItem.setInt(1,order_id);
        delItem.setInt(2, item_id);
        System.out.print("Removing order_item from Database...");
        delItem.executeUpdate();
        System.out.println("Done!");
        return true;
    }

    /**
     * Fetches all order_items from the database
     * @param conn The database connection to use
     */
    public static ArrayList<OrderItem> fetchOrderItems(Connection conn) {
        ArrayList<OrderItem> items = new ArrayList<>();
        try {
            PreparedStatement fetchItems = conn.prepareStatement("SELECT * FROM order_items");
            ResultSet rs = fetchItems.executeQuery();

            if (!rs.next()) //no items in db for some reason..
                return items;
            do {
                int o_id = rs.getInt("order_id");
                int item_id = rs.getInt("item_id");
                String name = rs.getString("name");
                Double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                items.add(new OrderItem(o_id,item_id,name,price,quantity));
            } while(rs.next());

        } catch (Exception e) {
            System.out.println("Unable to fetch order items. Try again later");
            return null;
        }
        return null;
    }

    /**
     * Obtains all order items from the order with the given id
     * @param conn The database connection to use
     * @param order_id The order to query
     * @return An array list of OrderItems
     */
    public static ArrayList<OrderItem> fetchOrderItems(Connection conn, int order_id) {
        ArrayList<OrderItem> items = new ArrayList<>();
        try {
            PreparedStatement fetchItems = conn.prepareStatement("SELECT * FROM order_items WHERE order_id = ?");
            fetchItems.setInt(1, order_id);
            ResultSet rs = fetchItems.executeQuery();

            if (!rs.next()) //no items in db for some reason..
                return items;
            do {
                int item_id = rs.getInt("item_id");
                String name = rs.getString("name");
                Double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                items.add(new OrderItem(order_id,item_id,name,price,quantity));
            } while(rs.next());

        } catch (Exception e) {
            System.out.println("Unable to fetch order items. Try again later");
            return null;
        }
        return null;
    }
}