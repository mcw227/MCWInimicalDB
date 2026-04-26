import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/** Public class order models  */
public class Order {

    private static String[] order_status_strings = {"CANCELLED", "RECIEVED", "IN-PROGRESS", "READY FOR PICKUP", "COMPLETED"};
    public int id;
    public int payment_id;
    public int customer_id;
    public String created_at;
    public int location_id;
    public ArrayList<OrderItem> bag;
    public int status;
    public double total;


    /** Standard constructor */
    public Order(int id, int payment_id, int customer_id, String created_at, int location_id, int status, ArrayList<OrderItem> bag, double total) {
        this.id = id;
        this.payment_id = payment_id;
        this.customer_id = customer_id;
        this.created_at = created_at;
        this.location_id = location_id;
        this.status = status;
        this.bag = bag;
        this.total = total;
    }

    /** 
     * Populates the order's bag
     * @param conn The database connection to use
     */
    public void fetchBag(Connection conn) {
        this.bag = OrderItem.fetchOrderItems(conn, this.id);
    }

    /**
     * Adds an item to the bag
     * @param item An OrderItem to add to the bag
     */
    public void addItemToBag(OrderItem item) {
        this.bag.add(item);
        this.total += item.price * item.quantity;
    }

    /**
     * Removes an item from the bag with the given item id
     * @param item_id The item to remove from the bag
     */
    public void removeItemFromBag(int item_id) {
        for (int i = 0; i < this.bag.size(); i++) {
            if (this.bag.get(i).id == item_id) {
                this.total -= this.bag.get(i).price * this.bag.get(i).quantity;
                this.bag.remove(i);
            }
        }
    }

    /**
     * Adds an order to the database
     * @param conn The database connection to use
     */
    public boolean addOrder(Connection conn) {
        try {
            conn.setAutoCommit(false);
            PreparedStatement addOrder = conn.prepareStatement("INSERT INTO orders (location_id, customer_id, payment_id, status, total) VALUES (?, ?, ?, ?, ?)");
            addOrder.setInt(1, this.location_id); addOrder.setInt(2, this.customer_id);
            addOrder.setInt(3, this.payment_id); addOrder.setInt(4, this.status);
            addOrder.setDouble(5, this.total);
            addOrder.executeUpdate();

            int upd_rows = addOrder.executeUpdate();
            if (upd_rows > 0) {
                try (ResultSet rs = addOrder.getGeneratedKeys()) { //This obtains the identity key that was generated when the item was inserted
                    if (rs.next()) {
                        int newId = (int)rs.getLong(1);
                        for (OrderItem item : this.bag) {
                            item.order_id = newId;
                            item.addOrderItem(conn);
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            try {
                conn.rollback();
                System.out.println("Could not place order. Try again later.");
                e.printStackTrace(); //debug
                return false;
            } catch (Exception f) {
                System.err.println("Critical database error. Please restart software.");
                System.exit(-1);
            }
        } finally {
            try {
                conn.setAutoCommit(false);
            } catch (Exception e) {
               System.err.println("Critical database error. Please restart software.");
               System.exit(-1); 
            }
        }
        return false;
    }

    /**
     * Adds an order to the database
     * @param conn The database connection to use
     * @param order The order to add
     */
    public static boolean addOrder(Connection conn, Order order) {
        return order.addOrder(conn);
    }

    /**
     * Deletes an order from the database
     * @param conn The database connection to use
     */
    public boolean delOrder(Connection conn) {
        try {
            PreparedStatement delOrder = conn.prepareStatement("DELETE FROM orders WHERE id = ?");
            delOrder.setInt(1, this.id);
            int upd = delOrder.executeUpdate();
            if (upd == 0) {
                System.out.printf("No order with id %d found\n", this.id);
                return false;
            }
            return true;
        } catch (Exception e) {
            System.out.println("Could not delete order from database. Try again later.");
            return false;
        }
    }

    /**
     * Deletes an order with the given id from the database
     * @param conn The database connection to use
     * @param id The id of the order to delete
     */
    public static boolean delOrder(Connection conn, int id) {
        try {
            PreparedStatement delOrder = conn.prepareStatement("DELETE FROM orders WHERE id = ?");
            delOrder.setInt(1, id);
            int upd = delOrder.executeUpdate();
            if (upd == 0) {
                System.out.printf("No order with id %d found\n", id);
                return false;
            }
            return true;
        } catch (Exception e) {
            System.out.println("Could not delete order from database. Try again later.");
            return false;
        }
    }
}
