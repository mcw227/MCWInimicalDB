import java.sql.*;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.util.ArrayList;

import java.util.regex.*;

/**
 * This is a utility class encapsulating the behavior of the customer interface.
 */
public final class CustomerInterface {
    
    public CustomerInterface() {
        throw new UnsupportedOperationException("This is a utility class. Do not instantiate");
    }

    /**
     * Customer Interface Functions
     */
    public static void start(Connection conn, Scanner scn) {
        Customer c = cLogin(conn, scn);
        if (c == null) return; //user is quitting.
        else {
            //System.out.println(c.toString()); // debug
            cMenu(c, conn, scn);
        }
    }

    /**
     * @param conn the Database connection to use
     * @param scn the scanner to use to get input.
     * @return a valid customer or null
     */
    static Customer cLogin(Connection conn, Scanner scn) {
        ResultSet rs = null;
        int id = -1;
        Customer c = null;
        while (rs == null) {
            System.out.print("Enter a valid id, or press q to quit: ");
            id = Helper.nextId(scn);

            if (id == -2) return null;

            while (id == -1) {
                id = Helper.nextId(scn);
                System.out.print("Enter a valid id, or press q to quit: ");

                if (id == -2) return null; //quit casz
            }

            try {
                PreparedStatement findCustomer = conn.prepareStatement("SELECT * FROM customers WHERE id = ?");
                findCustomer.setInt(1, id);
                rs = findCustomer.executeQuery();
                if (rs == null)
                    System.out.println("User id not found, try again.");
                else {
                    rs.next();
                    if (rs.getInt("active") != 0) { //Account is inactive. They cannot login.
                        c = new Customer(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getInt("membership"), rs.getInt("points"));
                    } else {
                        System.out.println("User is inactive. Contact management to reinstate account or type a valid ID.");
                        rs = null;
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not query database, or found invalid customer, please try again.");
                //e.printStackTrace();
            }
            
        }     
        return c;
    }

    /**
     * @param Customer customer to take data from
     * @param conn DB Connection to query
     * @param scn Scanner to use for input
     */
    static void cMenu(Customer c, Connection conn, Scanner scn) {
        int resp = 0;
        Helper.clearConsole();
        while (resp != -2) {
            Helper.clearConsole();
            printCMenu(c);
            resp = Helper.nextId(scn);
            if (resp == -2)
                return;
            if (resp == 0 || resp > 7 || resp == -1) {
                System.out.println("Please pick a valid option!");
            } else if (resp != -2) {
                switch(resp) {
                    case 1:
                        cNameChange(c, conn, scn);
                        break;
                    case 2:
                        cEmailChange(c, conn, scn);
                        break;
                    case 3:
                        cMemberChange(c, conn, scn);
                        break;
                    case 4:
                        cOrders(c, conn, scn);
                        break;
                    case 5:
                        cCheckCreditCards(c, conn, scn);
                        break;
                    case 6:
                        cCheckPhoneNumbers(c, conn, scn);
                        break;
                    case 7:
                        cCheckCustomerCreations(c, conn, scn);
                        break;
                    case 8:
                        if (cDeactivateAccount(c, conn, scn) == true) {
                            return;
                        }
                        break;
                }
            }
            Customer.updateCustomerInfo(c, conn);
            if (c.id == -2) { //acount was marked as inactive while another user was logged in!
                System.out.println("Customer is now marked inactive. Please contact management if you think this is an error.");
                return;
            }
        }
        return;
    }

    /**
     * Allows the signed-in user to change their name if they desire.
     * @param c The customer who is currently signed in
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     */
    static void cNameChange(Customer c, Connection conn, Scanner scn) {
        Helper.clearConsole();
        System.out.printf("Your current name is: %s, would you like to change it? ([y]es/[n]o)\n", c.name);
        int choice = Helper.nextYN(scn);
        if (choice == -2) { return; }
        System.out.println("What would you like your new name to be?");
        String newName = Helper.nextSafeString(scn, 30); //Names can be up to 30 characters long
        c.nameChange(conn, newName);
    }

    /**
     * Allows the user to change their email, if they desire.
     * @param c The customer who is logged in
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     */
    static void cEmailChange(Customer c, Connection conn, Scanner scn) {
        Helper.clearConsole();
        System.out.printf("Your current email is: %s, would you like to change it? ([y]es/[n]o)\n", c.email);
        int choice = Helper.nextYN(scn);
        if (choice == -2) { return; }

        System.out.println("What would you like your new email to be?");
        String newEmail = Helper.nextEmail(scn);
        c.emailChange(conn, newEmail);
    }

    /**
     * Allows the user to check their membership status and cancel if they want to.
     * @param c Customer logged in
     * @param conn the Database connection to use
     * @param scn Scanner to grab input from
     */
    static void cMemberChange(Customer c, Connection conn, Scanner scn) {
        Helper.clearConsole();
        if (c.membership) {
            System.out.println("You are currently a member! Yay!");
            System.out.printf("You have %d points. That equates to about %.2f dollars!\nWould you like to cancel your membership? (You will lose your points...) [y]es/[n]o/[q]uit\n", c.points, (float)(c.points)/100);
            
            if (Helper.nextYN(scn) == -2)
                return;
            System.out.println("Are you really sure?");
            if (Helper.nextYN(scn) == -2)
                return;
            c.deactivateMembership(conn);

        } else {
            System.out.println("You are not a member yet, would you like to enroll? [y]es/[n]o/[q]uit");
            if (Helper.nextYN(scn) == -2)
                return;
            c.activateMembership(conn);
        }
    }

    /**
     * Allows the user to check their credit cards
     * @param c Customer that is logged in
     * @param conn Database connection to use
     * @param scn Scanner to grab input from
     */
    static void cCheckCreditCards(Customer c, Connection conn, Scanner scn) {
        Helper.clearConsole();
        c.cardScreen(conn, scn);
    }

    /**
     * Allows the user to check their phone numbers
     * @param c Customer that is logged in
     * @param conn Database connection to use
     * @param scn Scanner to grab input from
     */
    static void cCheckPhoneNumbers(Customer c, Connection conn, Scanner scn) {
        Helper.clearConsole();
        Pager<PhoneNumber> phones = new Pager(PhoneNumber.fetchPhones(c.id, conn), 5);
        boolean update = false;
        while (true) {
            if (update) //update, restart list
                phones = new Pager(PhoneNumber.fetchPhones(c.id, conn), 5);
            phones.printCurrentPage();
            if (!phones.list.isEmpty()) {
                System.out.println("Press n to go to next page, p to go to previous, q to quit.");
                System.out.println("You may type a to add or d to delete");
                int resp = Helper.nextPNQAD(scn);
                switch (resp) {
                    case -2:
                        return;
                    case 1:
                        Helper.clearConsole();
                        phones.previousPage();
                        break;
                    case 2:
                        Helper.clearConsole();
                        phones.nextPage();
                        break;
                    case 3:
                        update = PhoneNumber.addPhoneScreen(c, conn, scn);
                        break;
                    case 4:
                        update = PhoneNumber.removePhoneScreen(c, conn, scn);
                        break;
                }
            }
            else {
                System.out.println("You have no phone numbers saved to your account. Would you like to add one? (y)es/(n)o");
                int resp = Helper.nextYN(scn);
                if (resp == -2)
                    return;
                else {
                    update = PhoneNumber.addPhoneScreen(c, conn, scn);
                }
            }
        }
    }

    /**
     * This allows a user to check or create a new order
     * @param c The customer who is placing an order
     * @param conn The connection to the database
     * @param scn The scanenr to grab input from
     */
    static void cOrders(Customer c, Connection conn, Scanner scn) {
        while (true) {
            Helper.clearConsole();
            System.out.println("Would you like to make a new order or check order history/status? (n)ew/(c)heck/(q)uit");
            int r = Helper.nextNCQ(scn);
            if (r == -2)
                return;
            
            switch (r) {
                case 1:
                    Order.newOrderScreen(c, conn, scn);
                    break;
                case 2:
                    c.checkOrderScreen(conn, scn);
                    break;
                default:
                    return;
            }
            Customer.updateCustomerInfo(c, conn);
        }
    }

    /**
     * Allows a customer to check their customer creations
     * @param c The customer whose creations you want to get
     * @param conn The database connection to use
     * @param scn THe scanner to grab input from
     */
    static void cCheckCustomerCreations(Customer c, Connection conn, Scanner scn) {
        ArrayList<CustomerCreation> cc = CustomerCreation.fetchCustomerCreations(conn, c);
        if (cc == null || cc.size() == 0) {
            System.out.println("You haven't made any customer items. (Type anything to continue)");
            Helper.nextOK(scn);
        }
        
        Pager<CustomerCreation> c_page = new Pager<>(cc, 10);
        while (true) {
            Helper.clearConsole();
            c_page.printCurrentPage();
            System.out.println("Type (n)ext for next page, (p)revious for previous page, (q)uit to quit.");
            int r = Helper.nextPNQ(scn);
            if (r == -2)
                return;
            switch (r) {
                case 1:
                    c_page.previousPage();
                    break;
                case 2:
                    c_page.nextPage();
                    break;
                case 3:
                    return;
            }
        }
    }

    /**
     * Allows the user to "delete" their account. Note that this just sets it as inactive in the system rather than deleting it for... record keeping purposes.
     * @param c Customer that is logged in
     * @param conn Database connection to use
     * @param scn Scanner to grab input from
     */
    static boolean cDeactivateAccount(Customer c, Connection conn, Scanner scn) {
        System.out.println("Are you sure you want to deactivate your account? You can contact support to reinstate it... [y]es/[n]o/[q]uit");
        if (Helper.nextYN(scn) == -2)
            return false;
        System.out.println("Are you really sure?");
        if (Helper.nextYN(scn) == -2)
            return false;

        System.out.println("Okay...");
        try {
            PreparedStatement deactivateAccount = conn.prepareStatement("UPDATE customers SET active=0 WHERE id=?");
            deactivateAccount.setInt(1,c.id);
            System.out.print("Deactivating account... ");
            deactivateAccount.executeUpdate();
            System.out.println("Done! Goodbye!");
            c = Customer.InactiveCustomer(); //set c to inactive customer
            return true;
        } catch (Exception e) {
            System.out.println("Could not delete account. Try again later.");
            return false;
        }
    }

    /** Prints the customer control menu */
    static void printCMenu(Customer c) {
        System.out.flush();
        if (c.membership)
            System.out.printf("\n\nHello, esteemed %s! You have %d points!", c.name, c.points);
        else
            System.out.printf("\n\nHello, %s!", c.name);
        System.out.printf("\nWhat would you like to do today?\n\t1. Change Name\n\t2. Change Email\n\t3. View Membership Details or Enroll \n\t4. Make/View Status Of Orders\n\t5. Check And Adjust Credit Cards\n\t6. Check and Adjust Phone Numbers\n\t7. Check Customer Creations\n\t8. Deactivate Account\nEnter a 1-8 to select an option or enter quit (q) to quit!\n", c.name);
    }

}