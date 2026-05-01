import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/** Public class order models  */
public class Order {

    private static final String[] order_status_strings = {"CANCELLED", "RECIEVED", "IN-PROGRESS", "READY FOR PICKUP", "COMPLETED"};
    private static final int ITEM_MENU_PAGE_SIZE = 10;
    public static final double MAX_ORDER_COST = 99999999.99;
    public int id;
    public int payment_id;
    public int customer_id;
    public String created_at;
    public Location location;
    public ArrayList<OrderItem> bag;
    public int status;
    public double total;


    /** Standard constructor */
    public Order(int id, int payment_id, int customer_id, String created_at, Location Location, int status, ArrayList<OrderItem> bag, double total) {
        this.id = id;
        this.payment_id = payment_id;
        this.customer_id = customer_id;
        this.created_at = created_at;
        this.location = location;
        this.status = status;
        this.bag = bag;
        this.total = total;
    }

    public Order(int id, int payment_id, int customer_id, String created_at, int location_id, int status, double total, Connection conn) {
        this.id = id;
        this.payment_id = payment_id;
        this.customer_id = customer_id;
        this.created_at = created_at;
        this.status = status;
        this.total = total;

        this.bag = OrderItem.fetchOrderItems(conn, this.id);
        this.location = Location.fetchLocation(conn, location_id);
    }

    /** Creates a new, unplaced order for the customer */
    public Order(Customer c) {
        this.id = -1;
        this.payment_id = -1;
        this.customer_id = c.id;
        this.created_at = null;
        this.location = null;
        this.status = 1;
        this.bag = new ArrayList<OrderItem>();
        this.total = 0;
    }

    /**
     * Basic to-string method
     */
    public String toString() {
        return String.format("ID:%-3d\n\tPAYMENT_ID: %-3d\t| CUSTOMER_ID: %-3d\t| CREATED AT: %s\n\tADDRESS: %-50s\n\tSTATUS: %s\n\n\t\t---TOTAL: %.2f---\n\n", this.id, this.payment_id, this.customer_id, this.created_at, this.location.address, order_status_strings[this.status], this.total);
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
     * Fetches all orders in the database
     * @param conn The database connection to use
     * @return An array list of orders
     */
    public static ArrayList<Order> fetchOrders(Connection conn) {
        ArrayList<Order> orders = new ArrayList<>();
        try {
            PreparedStatement getOrders = conn.prepareStatement("SELECT * FROM orders");
            ResultSet rs = getOrders.executeQuery();
            if (!rs.next())
                return orders;

            do {
                orders.add(parseOrderFromRS(rs, conn));
            } while (rs.next());
            return orders;
        } catch (Exception e) {
            System.out.println("Unable to fetch orders. Please try again later.");
            return null;
        }
    }

    /**
     * Fetches all orders in the database
     * @param conn The database connection to use
     * @param loc_id The location to query orders on
     * @return An array list of orders
     */
    public static ArrayList<Order> fetchOrders(Connection conn, int loc_id) {
        ArrayList<Order> orders = new ArrayList<>();
        try {
            PreparedStatement getOrders = conn.prepareStatement("SELECT * FROM orders WHERE location_id = ?");
            getOrders.setInt(1,loc_id);
            ResultSet rs = getOrders.executeQuery();
            if (!rs.next())
                return orders;

            do {
                orders.add(parseOrderFromRS(rs, conn));
            } while (rs.next());
            return orders;
        } catch (Exception e) {
            System.out.println("Unable to fetch orders. Please try again later.");
            return null;
        }
    }


    /**
     * Gets all orders based on customer id
     * @param conn The database connection to use
     * @param c_id The customer id to use
     * @return An array list of orders
     */
    public static ArrayList<Order> fetchOrdersByCustomer(Connection conn, int c_id) {
        ArrayList<Order> orders = new ArrayList<>();
        try {
            PreparedStatement getOrders = conn.prepareStatement("SELECT * FROM orders WHERE customer_id = ?");
            getOrders.setInt(1, c_id);
            ResultSet rs = getOrders.executeQuery();
            if (!rs.next())
                return orders;

            do {
                orders.add(parseOrderFromRS(rs, conn));
            } while (rs.next());
            return orders;
        } catch (Exception e) {
            System.out.println("Unable to fetch orders. Please try again later.");
            return null;
        }
    }

    /**
     * Gets all orders based on customer id
     * @param conn The database connection to use
     * @param c_id The customer id to use
     * @return An array list of orders
     */
    public static ArrayList<Order> fetchOrdersByLocation(Connection conn, int l_id) {
        ArrayList<Order> orders = new ArrayList<>();
        try {
            PreparedStatement getOrders = conn.prepareStatement("SELECT * FROM orders WHERE location_id = ?");
            getOrders.setInt(1, l_id);
            ResultSet rs = getOrders.executeQuery();
            if (!rs.next())
                return orders;

            do {
                orders.add(parseOrderFromRS(rs, conn));
            } while (rs.next());
            return orders;
        } catch (Exception e) {
            System.out.println("Unable to fetch orders. Please try again later.");
            return null;
        }
    }

    /**
     * Parses an order from a resultSet
     * @param rs The resultset to parse
     */
    public static Order parseOrderFromRS(ResultSet rs, Connection conn) {
        try {
            int id = rs.getInt("id");
            String created_at = rs.getString("created_at");
            int location_id = rs.getInt("location_id");
            int customer_id = rs.getInt("customer_id");
            int payment_id = rs.getInt("payment_id");
            int status = rs.getInt("status");
            double total = rs.getDouble("total");
            return new Order(id, payment_id, customer_id, created_at, location_id, status, total, conn);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Adds an order to the database
     * @param conn The database connection to use
     */
    public boolean addOrder(Connection conn) {
        try {
            conn.setAutoCommit(false);
            PreparedStatement addOrder = conn.prepareStatement("INSERT INTO orders (location_id, customer_id, payment_id, status, total) VALUES (?, ?, ?, ?, ?)", new String[] {"ID"});
            addOrder.setInt(1, this.location.id); addOrder.setInt(2, this.customer_id);
            addOrder.setInt(3, this.payment_id); addOrder.setInt(4, this.status);
            addOrder.setDouble(5, this.total);

            int upd_rows = addOrder.executeUpdate();
            if (upd_rows > 0) {
                try (ResultSet rs = addOrder.getGeneratedKeys()) { //This obtains the identity key that was generated when the item was inserted
                    if (rs.next()) {
                        int newId = (int)rs.getLong(1);
                        for (OrderItem item : this.bag) {
                            System.out.println("ADDING ITEM TO DB");
                            item.order_id = newId;
                            item.addOrderItem(conn);
                        }
                        conn.commit();
                    }
                } catch (Exception f) {
                    f.printStackTrace();
                    throw new Exception("Failed to acquire generated id!");
                }
            } else {
                throw new Exception("Failed to execute update!");
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
     * Updates the status of an order
     * @param conn The database connection to use
     * @param status The status to set the order to
     * @return True if the status was updated, false if not
     */
    public boolean editStatus(Connection conn, int status) {
        if (status >= 5 || status < 0)
            return false;
        try {
            PreparedStatement updStatus = conn.prepareStatement("UPDATE orders SET status = ? WHERE id = ?");
            updStatus.setInt(1, status);
            updStatus.setInt(2, this.id);
            updStatus.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Unable to update order status.");
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

    /**
     * Allows the user to create a new order
     * @param c The customer placing the order
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     */
    public static void newOrderScreen(Customer c, Connection conn, Scanner scn) {
        Order customer_order = new Order(c);
        Location loc = Location.locationSelectScreen(conn, scn); //User must first select a location
        if (loc == null)
            return;
        loc.getLocalMenus(conn);
        Helper.clearConsole();

        customer_order.location = loc;
        LocalMenu lm = loc.pickMenu(scn);

        Helper.clearConsole();
        if (lm == null)
            return;
        ArrayList<Item> items = lm.items;
        Pager<Item> menu = new Pager<>(items, ITEM_MENU_PAGE_SIZE); //turn menu into pager

        boolean upd = false;

        while (true) {
            Helper.clearConsole();
            if (upd) {
                lm.fillItems(conn);
                items = lm.items;
                menu = new Pager<>(items, ITEM_MENU_PAGE_SIZE);
                upd=false;
            }
            menu.printCurrentPage();
            System.out.println("Type (n)ext to go to next page, or (p)revious to go to previous page. Type (m)enus to change menus.");
            System.out.println("Type (a)dd to add an item to your bag\n(c)heck to view an item's details\n(cr)eate to create an item\n(b)ag to check bag");
            System.out.println("Type (ch)eckout to checkout\n(q)uit to quit [Deletes order progress!]");
            int resp = Helper.nextACQNPB(scn);
            if (resp == -2)
                return;

            if (resp == 8) {
                Helper.clearConsole();
                lm = loc.pickMenu(scn);
                if (lm == null)
                    return;
                menu = new Pager<Item>(items, ITEM_MENU_PAGE_SIZE);
                items = lm.items;
                Helper.clearConsole();
            } else {
                switch (resp) {
                    case (1):
                        customer_order.addItemScreen(scn, items);
                        Helper.clearConsole();
                        break;
                    case (2):
                        Item.checkItemScreen(conn, scn, items);
                        break;
                    case (3):
                        Helper.clearConsole();
                        menu.nextPage();
                        break;
                    case(4):
                        Helper.clearConsole();
                        menu.previousPage();
                        break;
                    case(5):
                        Helper.clearConsole();
                        customer_order.checkBagScreen(scn);
                        break;
                    case(6):
                        Helper.clearConsole();
                        if (customer_order.checkout(c, conn, scn))
                            return;
                        break;
                    case(7):
                        Helper.clearConsole();
                        CustomerCreation cr = CustomerCreation.createItemScreen(conn, scn, c, lm);
                        if (cr == null) {
                            upd = false;
                            break;
                        }
                        upd = true;
                        while (true) {
                            System.out.printf("How many servings of '%s' would you like to add? (Must be less than 99)\n", cr.name);
                            int quantity = Helper.nextId(scn);
                            if (quantity > 99)
                                continue;
                            if (quantity == 0)
                                break;
                            customer_order.addItemToBag(new OrderItem(cr, -1, quantity));
                            break;
                        }
                        
                    default:
                        break;
                }
            }

            
            Helper.clearConsole();
        }
    }

    /**
     * Finds item in bag with the given item id
     * @param id The id of the item to search for
     * @return True if it exists in the bag, false if not
     */
    public boolean bagHasItem(int id) {
        return this.bag.stream().anyMatch(item -> item.id == id);
    }

    /**
     * @param id The item id of the order_item to search for
     * @return the item in the bag if it exists, or null if not
     */
    public OrderItem getBagItem(int id) {
        OrderItem item = this.bag.stream().filter(i -> i.id == id).findFirst().orElse(null); //grabs the first item from the list, feels like javascript style :)
        return item;
    }
    /**
     * Allows someone to add an item to the order
     */
    public void addItemScreen(Scanner scn, ArrayList<Item> items) {
        System.out.println("What is the ID of the item you would like to add? Can also type (!q)uit to quit");
        while (true) {
            int item_id = Helper.nextId(scn);
            if (item_id == -2)
                return;
            Item item = items.stream().filter(i -> i.id == item_id).findFirst().orElse(null); //grabs the first item from the list, feels like javascript style :)

            if (item != null) {
                int quantity;
                while (true) {
                    System.out.printf("How many servings of %s would you like? (Must be less than 99)\n", item.name);
                    quantity = Helper.nextId(scn);
                    if (quantity <= 0)
                        return;
                    OrderItem existingItem = getBagItem(item_id);
                    if (existingItem != null) { //if the user is trying to add more of the same item, just add it to the existing quantity
                        existingItem.quantity += quantity;
                        return;
                    }
                    if (quantity <= 99)
                        break;
                    System.out.println("Must be less than 99!");
                }
                
                this.addItemToBag(new OrderItem(item, this.id, quantity)); //otherwise they are adding new item
                return;
            }
            System.out.printf("No item with ID: %d found.\n", item_id);
        } 
    }

    /**
     * Allows a user to remove an item from their bag
     * @param scn The scanner to grab input from
     */
    public void removeItemScreen(Scanner scn) {
        System.out.println("What is the ID of the item you'd like to remove? (Type (!q)uit to quit)");
        while (true) {
            int id = Helper.nextId(scn);
            if (id == -2)
                return;
            if (!this.bagHasItem(id)) {
                System.out.printf("Item with id: %d not found in bag!", id);
                return;
            } else {
                this.removeItemFromBag(id);
                System.out.println("Removed.");
                return;
            }
        }
    }

    /**
     * Allows the user to edit an item's quantity in their bag
     * @param scn The scanner to grab input from
     */
    public void editItemScreen(Scanner scn) {
        System.out.println("What is the ID of the item you'd like to edit? (Type (!q)uit to quit)");
        while (true) {
            int id = Helper.nextId(scn);
            if (id == -2)
                return;
            if (!this.bagHasItem(id)) {
                System.out.printf("Item with id: %d not found in bag!", id);
                return;
            } else {
                System.out.println("What would you like to set the new quantity to? (Must be less than 99)");
                int quantity = Helper.nextId(scn);
                if (quantity == -2)
                    return;
                if (quantity == 0) {
                    this.removeItemFromBag(id);
                }
                if (quantity <= 99) {
                    OrderItem oi = getBagItem(id);
                    this.removeItemFromBag(id);
                    oi.quantity = quantity;
                    this.addItemToBag(oi);
                    System.out.printf("Changed quantity to %d\n", quantity);
                    return;
                }
                System.out.println("Must be less than 99!");
            }
        }
    }

    /**
     * Allows a user to make changes to the order's bag
     * @param scn The scanner to grab input from
     */
    public void checkBagScreen(Scanner scn) {
        if (this.bag.size() == 0) {
            System.out.println("Bag is empty!");
        }
        System.out.println(this.getOrderSummary());
        System.out.println("Press (d)elete to delete an item, (e) to edit an item's quantity or (q)uit to return to menu screen.");
        while (true) {
            int resp = Helper.nextDEQ(scn);
            if (resp == -2)
                return;
            else if (resp == 1)
                this.removeItemScreen(scn);
            else if (resp == 2)
                this.editItemScreen(scn);
            Helper.clearConsole();
            System.out.println(this.getOrderSummary());
            System.out.println("Press (d)elete to delete an item, (e) to edit an item's quantity or (q)uit to return to menu screen.");
        }
    }

    /**
     * Creates an order summary
     * @return A string with all order items and a cumulative total
     */
    public String getOrderSummary() {
        String ret = "";
        ret += "ITEMS:\n";
        if (this.bag.size() == 0) {
            return "Bag is empty!";
        }
        for (Item item : this.bag) {
            ret += "\t" + item.toString() + "\n";
        }
        ret += String.format("TOTAL AFTER TAX: %.2f\nAT LOCATION: %d WITH SALES TAX: %.2f%%\n", this.total, this.location.id, this.location.sales_tax*100);
        return ret;
    }

    /**
     * Allows a user to checkout
     * @param c 
     */
    public boolean checkout(Customer c, Connection conn, Scanner scn) {
        if (this.total > MAX_ORDER_COST) {
            System.out.println("Order is too expensive! Please remove some items. (Type anything to continue.)");
            Helper.nextOK(scn);
            return false;
        }
        System.out.println(getOrderSummary());
        System.out.println("What card would you like to pay with?");
        int card_id = c.selectCardScreen(conn, scn);
        if (card_id == -2)
            return false;
        else {
            this.payment_id = card_id;
            System.out.println("Would you like to place your order? ((y)es/(n)o)");
            int r = Helper.nextYN(scn);
            if (r == -2)
                return false;
            boolean orderSuccess = this.addOrder(conn);
            if (orderSuccess) {
                System.out.println("Done! See you soon! Your order should be done in about 20 minutes. (Enter anything to continue)");
                Helper.nextOK(scn);
                return true;
            } else {
                System.out.println("Could not send order. Please try again later.");
                return false;
            }
        }
    }

    /**
     * Allows someone to edit the status of an order
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     * @return True if the order was updated, false if not
     */
    public boolean editStatusScreen(Connection conn, Scanner scn) {
        Helper.clearConsole();
        int resp = -1;
        while (resp >= 5 || resp < 0) {
            System.out.println("What would you like to set the order status to?\n\t0 -> Cancelled\n\t1 -> Recieved\n\t2 -> In Progress\n\t3 -> Waiting for pickup\n\t4 -> Complete");
            resp = Helper.nextId(scn);
            if (resp == -2)
                return false;
        }
        return this.editStatus(conn, resp);
    }
}
