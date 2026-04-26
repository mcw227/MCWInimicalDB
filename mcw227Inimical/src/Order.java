import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/** Public class order models  */
public class Order {

    private static final String[] order_status_strings = {"CANCELLED", "RECIEVED", "IN-PROGRESS", "READY FOR PICKUP", "COMPLETED"};
    private static final int ITEM_MENU_PAGE_SIZE = 10;
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
        this.total += item.price * item.quantity * location.sales_tax;
    }

    /**
     * Removes an item from the bag with the given item id
     * @param item_id The item to remove from the bag
     */
    public void removeItemFromBag(int item_id) {
        for (int i = 0; i < this.bag.size(); i++) {
            if (this.bag.get(i).id == item_id) {
                this.total -= this.bag.get(i).price * this.bag.get(i).quantity * location.sales_tax;
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
            addOrder.setInt(1, this.location.id); addOrder.setInt(2, this.customer_id);
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

    public static void newOrderScreen(Customer c, Connection conn, Scanner scn) {
        Order customer_order = new Order(c);
        Location loc = Location.locationSelectScreen(conn, scn); //User must first select a location
        loc.getLocalMenus(conn);
        if (loc == null)
            return;
        customer_order.location = loc;
        LocalMenu lm = loc.pickMenu(scn);
        if (lm == null)
            return;
        ArrayList<Item> items = lm.items;
        Pager<Item> menu = new Pager(items, 5); //turn menu into pager
        Helper.clearConsole();

        while (true) {
            menu.printCurrentPage();
            System.out.println("Type (n)ext to go to next page, or (p)revious to go to previous page. Type (m)enus to change menus.");
            System.out.println("Type (a)dd to add an item to your bag, (c)heck to check out an item's details, (b)ag to check bag");
            System.out.println("Type (ch)eckout to checkout or (q)uit to quit [Deletes order progress!]");
            int resp = Helper.nextACQNPB(scn);
            if (resp == -2)
                return;

            if (resp == 8) {
                LocalMenu m = loc.pickMenu(scn);
                if (m == null)
                    return;
                menu = new Pager<Item>(m.items, ITEM_MENU_PAGE_SIZE);
                Helper.clearConsole();
            } else {
                switch (resp) {
                    case (1):
                        customer_order.addItemScreen(scn, items);
                        Helper.clearConsole();
                        break;
                    case (2):
                        Helper.clearConsole();
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
                }
            }

            
            //Helper.clearConsole();
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
                System.out.printf("How many servings of %s would you like?\n", item.name);
                int quantity = Helper.nextId(scn);
                if (quantity == -2 || quantity == 0)
                    return;
                OrderItem existingItem = getBagItem(item_id);
                if (existingItem != null) { //if the user is trying to add more of the same item, just add it to the existing quantity
                    existingItem.quantity += quantity;
                    return;
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
                System.out.println("What would you like to set the new quantity to?");
                int quantity = Helper.nextId(scn);
                if (quantity == -2)
                    return;
                if (quantity == 0) {
                    this.removeItemFromBag(id);
                }
                getBagItem(id).quantity = quantity;
                System.out.printf("Changed quantity to %d\n", quantity);
                return;
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

    public static void checkOrderScreen(Connection conn, Scanner scn) {
        return;
    }

    /**
     * Allows a user to checkout
     */
    public boolean checkout(Customer c, Connection conn, Scanner scn) {
        System.out.println("What card would you like to pay with?");
        int card_id = c.selectCardScreen(conn, scn);
        if (card_id == -2)
            return false;
        else {
            this.payment_id = card_id;
        }
        return false;
    }
}
