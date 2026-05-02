import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class LocationManagerInterface {
    
    private static final int MAX_PAGE_SIZE = 10;
    /**
     * Opens the location manager menu
     * @param conn
     * @param scn
     * @param e
     */
    public static void lmMenu(Connection conn, Scanner scn, Employee e) {
        int resp = 0;
        Helper.clearConsole();
        while (resp != -2) {
            printLMMenu(e);
            resp = Helper.nextId(scn);
            switch (resp) {
                case (1):
                    lmViewOrders(conn, scn, e);
                    break;
                case (2):
                    lmViewItems(conn, scn);
                    break;
                case (3):
                    lmViewLocations(conn, scn);
                    break;
                case (4):
                    lmMenus(conn, scn, e);
                    break;
                case (5):
                    lmPriceChange(conn, scn, e);
                    break;
                case (6):
                    Customer.addCustomerScreen(conn, scn);
                    break;
                case (7):
                    Helper.clearConsole();
                    System.out.println("What is the id of the customer whose account you want to reactivate?");
                    int r = Helper.nextId(scn);
                    if (r == -2)
                        return;
                    Customer.activateAccount(r, conn, scn);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Prints the Location Manager options menu
     * @param e Employee who is currently logged in
     */
    public static void printLMMenu(Employee e) {
        System.out.flush();
        System.out.printf("\n\nHello, %s! You logged into location: %d\n", e.name, e.location_id);
        System.out.printf("\tRestaurant Address: %s\n\n", e.location.address);
        System.out.printf("\nWhat would you like to do today?\n\t1. View/Update Order Status\n\t2. View All Items\n\t3. View All Locations\n\t4. View/Edit Menus\n\t5. Create A Local Price Change\n\t6. Create Customer Account\n\t7. Activate/Reinstate Customer Account\nEnter a 1-7 to select an option or enter quit (q) to quit!\n", e.name);
    }

    /**
     * 
     * @param conn
     * @param scn
     * @param e
     */
    public static void lmViewOrders(Connection conn, Scanner scn, Employee e) {
        ArrayList<Order> orders = Order.fetchOrdersByLocation(conn, e.location_id);

        Pager<Order> op = new Pager<>(orders, MAX_PAGE_SIZE);
        Helper.clearConsole();
        boolean upd = false;
        while(true) {
            if (upd) {
                orders = Order.fetchOrdersByLocation(conn, e.location_id);
                op = new Pager<>(orders, MAX_PAGE_SIZE);
                upd = false;
            }
            Helper.clearConsole();
            op.printCurrentPage();
            System.out.println("Press (n)ext, (p)revious, an id to edit an order or (q)uit.");
            int resp = Helper.nextPNQID(scn);
            if (resp == -2)
                return;
            
            switch (resp) {
                case (-3):
                    op.previousPage();
                    break;
                case (-4):
                    op.nextPage();
                    break;
                default:
                    if (resp < 0)
                        System.out.println("Invalid id!");
                    else {
                        Order o = op.list.stream().filter(or -> or.id == resp).findFirst().orElse(null);
                        if (o == null)
                            System.out.printf("Could not find order with id %d.\n",resp);
                        else {
                            upd = o.editStatusScreen(conn, scn);
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Allows a user to view all items that exist in the database and check them as they desire
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     */
    public static void lmViewItems(Connection conn, Scanner scn) {
        ArrayList<Item> items = Item.fetchItems(conn);
        if (items == null) {
            System.out.println("--- UNABLE TO LOAD ITEMS OR NO ITEMS IN DATABASE ---");
            System.out.println("Type anything to continue.");
            Helper.nextOK(scn);
            return;
        }

        Pager<Item> it = new Pager<>(items, MAX_PAGE_SIZE);
        while(true) {
            Helper.clearConsole();
            it.printCurrentPage();
            System.out.println("Press (n)ext, (p)revious, an id to check an item, or (q)uit.");
            int resp = Helper.nextPNQID(scn);
            if (resp == -2)
                return;
            
            switch (resp) {
                case (-3):
                    it.previousPage();
                    break;
                case (-4):
                    it.nextPage();
                    break;
                default:
                    if (resp < 0)
                        System.out.println("Invalid id!");
                    else {
                        Item i = it.list.stream().filter(ir -> ir.id == resp).findFirst().orElse(null);
                        if (i == null)
                            System.out.printf("Could not find order with id %d.\n",resp);
                        else {
                            System.out.println(i.getSummary(conn));
                            System.out.println("Type anything to continue");
                            Helper.nextOK(scn);
                        }
                    }
                    break;
            }
        }

    }

    /**
     * Allows a user to view the details of all locations
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     */
    public static void lmViewLocations(Connection conn, Scanner scn) {
        ArrayList<Location> locs = Location.fetchLocations(conn);
        Pager<Location> l = new Pager<>(locs, MAX_PAGE_SIZE);
        l.show(scn);
        return;
    }

    /**
     * Allows the user to view menus based on their working location, create local menus and edit local menus
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     * @param e The employee making the changes
     */
    public static void lmMenus(Connection conn, Scanner scn, Employee e) {
        while (true) {
            Helper.clearConsole();
            System.out.println("What would you like to do?\n\t1. View Menus\n\t2. View/Edit Menus For Your Restaurant\n\t3. Create A New Menu\n\t");
            int r = Helper.nextId(scn);
            switch (r) {
                case (-2):
                    return;
                case (1):
                    lmViewMenus(conn, scn);
                    break;
                case (2):
                    lmViewMenus(conn, scn, e.location);
                    break;
                case (3):
                    Menu.createMenu(conn, scn, e.location);
                    break;
                default:
                    System.out.println("Please choose a number 1-3.");
                    break;
            }
        }
    }

    /**
     * Allows the user to create a price change for the location they work at
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     * @param e The employee making the change
     */
    public static void lmPriceChange(Connection conn, Scanner scn, Employee e) {
        PriceChange.priceChangeScreen(conn, scn, e.location);
        return;
    }

    /**
     * Allows users to look at menus and view their items
     * @param conn
     * @param scn
     */
    public static void lmViewMenus(Connection conn, Scanner scn) {
        Menu.showMenus(conn, scn);
        return;
    }

    public static void lmViewMenus(Connection conn, Scanner scn, Location l) {
        Menu.showMenus(conn, scn, l);
        return;
    }
}

