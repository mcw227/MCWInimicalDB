import java.util.Scanner;
import java.util.ArrayList;
import java.sql.*;
/**
 * Public class customer to model an entry in the customer table
 */

public class Customer {
    public int id;
    public String name;
    public String email;
    public boolean membership;
    public int points;

    /** Standard account creator. */
    public Customer(int id, String name, String email, int member, int points) {
        this.id = id;
        this.name = name;
        this.email = email;
        if (member == 0) {
            this.membership = false;
        } else {
            this.membership = true;
        }
        this.points = points;
    }

     /** Standard unofficial (not in database yet) account creator. */
    public Customer(String name, String email, int member, int points) {
        this.id = -1;
        this.name = name;
        this.email = email;
        if (member == 0) {
            this.membership = false;
        } else {
            this.membership = true;
        }
        this.points = points;
    }

    public static Customer InactiveCustomer() {
        return new Customer(-2, "INACTIVE CUSTOMER", "n/a", 0, 0);
    }

    /**
     * Standard toString function
     */
    public String toString() {
        return String.format("ID: %-8d| NAME: %-30s| EMAIL: %-40s| MEMBERSHIP:%b\t| POINTS: %-8d",id,name,email, membership, points);
    }

    /**
     * Adds a customer to the database
     * @param conn The database connection to use
     * @param c The customer to add
     * @return True if customer was added, false if not
     */
    public static boolean addCustomer(Connection conn, Customer c) {
        try {
            PreparedStatement addCustomer = conn.prepareStatement("INSERT INTO customers (name, email, membership, points, active) VALUES (?,?,?,?,1)", new String[] {"ID"});
            addCustomer.setString(1, c.name);
            addCustomer.setString(2, c.email);
            int membership = c.membership ? 1 : 0;
            addCustomer.setInt(3, membership);
            addCustomer.setInt(4, c.points);

            if (addCustomer.executeUpdate() != 0) {
                ResultSet rs = addCustomer.getGeneratedKeys();
                rs.next();
                int newId = (int)rs.getLong(1);
                c.id = newId;
            }
 

            return true;
        } catch (Exception e) {
            System.out.println("Could not add customer to database. Try again later");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addCustomerScreen(Connection conn, Scanner scn) {
        Helper.clearConsole();
        System.out.println("Type (!q) to quit at any time!");

        System.out.println("What is the customer's name?");
        String n = Helper.safeCheckQuit(scn, 30);
        if (n == null)
            return false;
        
        System.out.println("What is the customer's email?");
        String e = Helper.nextEmail(scn);
        if (e == null)
            return false;
        
        System.out.println("Is the customer a member? (yes/no)");
        int r = Helper.nextYNQ(scn);
        
        int m;

        if (r == -2) {return false;}
        else if (r == 1) {m = 1;}
        else {m = 0;}

        int pts = 0;
        if (m == 1) {
            System.out.println("How many points does the customer have to start with?");
            pts = Helper.nextId(scn);
            if (pts == -2)
                return false;

            while (pts > 9999999) {
                System.out.println("Points must be less than 9,999,999");
                pts = Helper.nextId(scn);
            }
        }

        boolean add = Customer.addCustomer(conn, new Customer(n, e, m, pts));
        if (add) {
            System.out.println("Added customer! Type anything to continue.");
            Helper.nextOK(scn);
            return true;
        }
        return false;
    }

    /**
     * Deactivates the customer's membership
     * @return true if membership was deactivated, false if not
     */
    public boolean deactivateMembership(Connection conn) {
        if (this.id < 0)
            return false;
        try {
                PreparedStatement cancelMembership = conn.prepareStatement("UPDATE customers SET membership=0 WHERE id=?");
                cancelMembership.setInt(1,this.id);

                PreparedStatement setPointsZero = conn.prepareStatement("UPDATE customers SET points=0 WHERE id=?");
                setPointsZero.setInt(1,this.id);

                conn.setAutoCommit(false); //start transaction
                System.out.print("Cancelling membership... ");
                cancelMembership.executeUpdate();
                setPointsZero.executeUpdate();
                conn.commit();
                System.out.println("Done!");
                return true;
            } catch (Exception e) {
                try {
                    System.out.println("Could not update membership status. Try again later");
                    conn.rollback();
                    return false;
                } catch (Exception f) { //Critical db error
                    System.err.println("Database connection terminated. Please restart software");
                    System.exit(-1);
                }

            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (Exception e) { //Critical db error
                    System.err.println("Database connection terminated. Please restart software.");
                    System.exit(-1);
                }
            }
        return false;
    }

    /**
     * Deactivates a customer's membership based on the provided ID
     * @param id The id of the customer whose membership you want to deactivate
     */
    public static boolean deactivateMembership(Connection conn, int id) {
        if (id < 0)
            return false;
        try { 
                PreparedStatement cancelMembership = conn.prepareStatement("UPDATE customers SET membership=0 WHERE id=?");
                cancelMembership.setInt(1,id);

                PreparedStatement setPointsZero = conn.prepareStatement("UPDATE customers SET points=0 WHERE id=?");
                setPointsZero.setInt(1,id);

                conn.setAutoCommit(false); //start transaction
                System.out.print("Cancelling membership... ");
                cancelMembership.executeUpdate();
                setPointsZero.executeUpdate();
                conn.commit();
                System.out.println("Done!");
                return true;
            } catch (Exception e) {
                try {
                    System.out.println("Could not update membership status. Try again later");
                    conn.rollback();
                    return false;
                } catch (Exception f) { //Critical db error
                    System.err.println("Database connection terminated. Please restart software");
                    System.exit(-1);
                }

            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (Exception e) { //Critical db error
                    System.err.println("Database connection terminated. Please restart software.");
                    System.exit(-1);
                }
            }
        return false;
    }

    /**
     * Activates the class' membership
     * @return true if the membership was updated, false if not
     */
    public boolean activateMembership(Connection conn) {
        if (this.id < 0)
            return false;
        try {
            PreparedStatement enrollMembership = conn.prepareStatement("UPDATE customers SET membership=1 WHERE id=?");
            enrollMembership.setInt(1, this.id);

            System.out.print("Enrolling in membership... ");
            enrollMembership.executeUpdate();
            System.out.println("Done!");
            return true;
        } catch (Exception e) {
            System.out.println("Could not update membership status. Please try again later.");
            return false;
        }
    }

    /**
     * Activate the membership of the customer based on the given id
     * @param id The id of the customer to activate their membership
     * @return True if membership was updated, false if not
     */
    public static boolean activateMembership(Connection conn, int id) {
        if (id < 0)
            return false;
        try {
            PreparedStatement enrollMembership = conn.prepareStatement("UPDATE customers SET membership=1 WHERE id=?");
            enrollMembership.setInt(1, id);

            System.out.print("Enrolling in membership... ");
            enrollMembership.executeUpdate();
            System.out.println("Done!");
            return true;
        } catch (Exception e) {
                System.out.println("Could not update membership status. Please try again later.");
                return false;
        }
    }

    /**
     * This changes the email of the customer to the given string. Note that this function expects a valid string as input. It does not check it
     * @param newEmail email to change to
     * @return True if email was updated, false if not
     */
    public boolean emailChange(Connection conn, String newEmail) {
        if (this.id < 0)
            return false;
        try {
            PreparedStatement emailChange = conn.prepareStatement("UPDATE customers SET email=? WHERE id=?");
            System.out.printf("\nChanging email to %s... ", newEmail);
            emailChange.setString(1, newEmail);
            emailChange.setInt(2, this.id);
            emailChange.executeUpdate();
            System.out.println("email updated.");
            return true;
        } catch (Exception e) {
            System.out.println("Could not update customer email. Please try again later.");
            return false;
            //e.printStackTrace(); //debug
        }
    }

    /**
     * This changes the email of the customer with the given id to the given string. Note that this function expects a valid string as input. It does not check it
     * @param id The id of the customer to change
     * @param newEmail email to change to
     * @return True if email was updated, false if not
     */
    public static boolean emailChange(Connection conn, int id, String newEmail) {
        if (id < 0)
            return false;
        try {
            PreparedStatement emailChange = conn.prepareStatement("UPDATE customers SET email=? WHERE id=?");
            System.out.printf("\nChanging email to %s... ", newEmail);
            emailChange.setString(1, newEmail);
            emailChange.setInt(2, id);
            emailChange.executeUpdate();
            System.out.println("email updated.");
            return true;
        } catch (Exception e) {
            System.out.println("Could not update customer email. Please try again later.");
            return false;
            //e.printStackTrace(); //debug
        }
    } 

    /**
     * Changes the name of the customer based on the given string
     * @param conn The database connection to use
     * @param newName the name to set to
     */
    public boolean nameChange(Connection conn, String newName) {
        if (this.id < 0)
            return false;
        try {
            PreparedStatement nameChange = conn.prepareStatement("UPDATE customers SET name=? WHERE id=?");
            System.out.printf("\nChanging name to %s... ", newName);
            nameChange.setString(1, newName);
            nameChange.setInt(2, this.id);
            nameChange.executeUpdate();
            System.out.println("Name updated.");
            return true;
        } catch (Exception e) {
            System.out.println("Could not update customer name. Try again later.");
            return false;
        }
    }

    /** 
     * Changes the name of a customer based on the given ID and string
     * @param conn The database connection to be used
     * @param id The id of the customer whose name is being changed
     * @param newName the new name to apply to the customer
     */
    public static boolean nameChange(Connection conn, int id, String newName) {
        if (id < 0)
            return false;
        try {
            PreparedStatement nameChange = conn.prepareStatement("UPDATE customers SET name=? WHERE id=?");
            System.out.printf("\nChanging name to %s... ", newName);
            nameChange.setString(1, newName);
            nameChange.setInt(2, id);
            nameChange.executeUpdate();
            System.out.println("Name updated.");
            return true;
        } catch (Exception e) {
            System.out.println("Could not update customer name. Try again later.");
            return false;
        }
    }

    /**
     * Adds card to the current customer
     * @param card Card to add
     * @return True if added, false if failed
     */
    public boolean addCard(Connection conn, Card card) {
        if (id < 0) 
            return false;
        try {
            card.customer_id = this.id;
            return Card.addCard(conn, card);
        } catch (Exception e) {
            System.out.println("Could not add card. Please try again later.");
            return false;
        }
    }

    /**
     * Adds card to the current customer
     * @param id the ID of the customer to add the card to
     * @param card the Card to add
     * @return true if the card was added, false if not
     */
    public static boolean addCard(Connection conn, int id, Card card) {
        if (id < 0)
            return false;
        try {
            card.customer_id = id;
            return Card.addCard(conn, card);
        } catch (Exception e) {
            System.out.println("Could not add card. Please try again later.");
            return false;
        }
    }

     /**
     * Adds card to the current customer
     * @param card Card to add
     * @return True if added, false if failed
     */
    public boolean addPhone(Connection conn, PhoneNumber pn) {
        if (id < 0)
            return false;
        try {
            pn.customer_id = this.id;
            return PhoneNumber.addPhone(conn, pn);
        } catch (Exception e) {
            System.out.println("Could not add phone number. Please try again later.");
            return false;
        }
    }

    /**
     * Adds card to the current customer
     * @param pn Phone number to add
     * @return True if added, false if failed
     */
    public static boolean addPhone(Connection conn, PhoneNumber pn, int id) {
        if (id < 0)
            return false;
            
        try {
            pn.customer_id = id;
            return PhoneNumber.addPhone(conn, pn);
        } catch (Exception e) {
            System.out.println("Could not add phone number. Please try again later.");
            return false;
        }
    }

    /**
     * Removes the card with the given id from the customer
     * @param id The id of the card to remove
     */
    public boolean removeCard(Connection conn, int id) {
        if (id < 0)
            return false;
        try {
            return Card.removeCard(this, conn, id);
        } catch (Exception e) {
            System.out.println("Could not remove card. Try again later.");
            return false;
        }
    }
    /**
     * Removes a card from a customer
     * @param c_id the customer's id
     * @param id the ID of the card to remove
     * @param card the Card to add
     * @return true if the card was added, false if not
     */
    public static boolean removeCard(Connection conn, int c_id, int id) {
        if (id < 0)
            return false;
        try {
            return Card.removeCard(new Customer(c_id, null, null, 0, 0), conn, id);
        } catch (Exception e) {
            System.out.println("Could not remove card. Please try again later.");
            return false;
        }
    }

    /**
     * Removes the phone number with the given id from the customer
     * @param id The id of the phone number to remove
     */
    public boolean removePhone(Connection conn, int id) {
        if (id < 0)
            return false;
        try {
            return PhoneNumber.removePhone(this, conn, id);
        } catch (Exception e) {
            System.out.println("Could not remove phone number. Try again later.");
            return false;
        }
    }
    /**
     * Removes a phoen number from a customer
     * @param c_id the customer's id
     * @param id the ID of the phone number to remove
     * @return true if the phone number was removed, false if not
     */
    public static boolean removePhone(Connection conn, int c_id, int id) {
        if (id < 0)
            return false;
        try {
            return PhoneNumber.removePhone(new Customer(c_id, null, null, 0, 0), conn, id);
        } catch (Exception e) {
            System.out.println("Could not remove phone number. Please try again later.");
            return false;
        }
    }

    /**
     * Fetches new customer data.
     * @param c The customer object to update. Uses its id to find the customer in the db.
     * @param conn The database connection to use
     */
    static void updateCustomerInfo(Customer c, Connection conn) {
        // admin account short circuit
        if (c.id < 0) {
            return;
        }
        try {
            PreparedStatement findCustomer = conn.prepareStatement("SELECT * FROM customers WHERE id = ?");
            findCustomer.setInt(1, c.id);
            ResultSet rs = findCustomer.executeQuery();
            if (rs == null) {//critical error
                System.err.println("Customer not found in database. Please restart software.\n");
                System.exit(-1);
            }
            else {
                rs.next();
                if (rs.getInt("active") == 0) //someone cancelled the account while the person was logged in
                    c = Customer.InactiveCustomer();
                c.id = rs.getInt("id");
                c.name = rs.getString("name");
                c.email = rs.getString("email");
                c.membership = (rs.getInt("membership") == 1) ? true : false;
                c.points = rs.getInt("points");
            }
        } catch (Exception e) {
            System.out.println("Could not update customer info.\n");
        }
    }

    /**
     * Fetches cards for a given customer id
     * @param c_id The customer id to query
     * @param conn The database connection to use
     * @return An arrayList of cards
     */
    public static ArrayList<Card> fetchCards(int c_id, Connection conn) {
        return Card.fetchCards(c_id, conn);
    }

    /**
     * Fetches cards for the customer
     * @param conn the Database connection to use
     * @return An arraylist of cards
     */
    public ArrayList<Card> fetchCards(Connection conn) {
        return Card.fetchCards(this.id, conn);
    }

    public void cardScreen(Connection conn, Scanner scn) {
        Helper.clearConsole();
        Pager<Card> cards = new Pager(Card.fetchCards(this.id, conn), 5);
        boolean update = false;
        while (true) {
            if (update) //update, restart list
                cards = new Pager(Card.fetchCards(this.id, conn), 5);
            cards.printCurrentPage();
            if (!cards.list.isEmpty()) {
                System.out.println("Press n to go to next page, p to go to previous, q to quit.");
                System.out.println("You may type a to add or d to delete");
                int resp = Helper.nextPNQAD(scn);
                switch (resp) {
                    case -2:
                        return;
                    case 1:
                        Helper.clearConsole();
                        cards.previousPage();
                        break;
                    case 2:
                        Helper.clearConsole();
                        cards.nextPage();
                        break;
                    case 3:
                        update = Card.addCardScreen(this, conn, scn);
                        break;
                    case 4:
                        update = Card.removeCardScreen(this, conn, scn);
                        break;
                }
            }
            else {
                System.out.println("You have no cards saved to your account. Would you like to add one? (y)es/(n)o");
                int resp = Helper.nextYN(scn);
                if (resp == -2)
                    return;
                else {
                    update = Card.addCardScreen(this, conn, scn);
                }
            }
        }
    }

    public int selectCardScreen(Connection conn, Scanner scn) {
        Helper.clearConsole();
        Pager<Card> cards = new Pager(Card.fetchCards(this.id, conn), 5);
        boolean update = false;
        while (true) {
            if (update) //update, restart list
                cards = new Pager(Card.fetchCards(this.id, conn), 5);
            cards.printCurrentPage();
            if (!cards.list.isEmpty()) {
                System.out.println("Press n to go to next page, p to go to previous, q to quit");
                System.out.println("You may type a to add or d to delete");
                System.out.println("Press s to select a card");
                int resp = Helper.nextPNQADS(scn);
                switch (resp) {
                    case -2:
                        return -2;
                    case 1:
                        Helper.clearConsole();
                        cards.previousPage();
                        break;
                    case 2:
                        Helper.clearConsole();
                        cards.nextPage();
                        break;
                    case 3:
                        update = Card.addCardScreen(this, conn, scn);
                        break;
                    case 4:
                        update = Card.removeCardScreen(this, conn, scn);
                        break;
                    case 5:
                        System.out.println("What card would you like to select? (!q)uit to return");
                        while (true) {
                            final int card_id = Helper.nextId(scn);
                            if (card_id == -2)
                                break;
                            if (cards.list.stream().anyMatch(card -> card.id == card_id))
                                return card_id;
                            else {
                                System.out.printf("Card with id %d not found!\n", id);
                            }
                        }
                }
            }
            else {
                System.out.println("You have no cards saved to your account. Would you like to add one? (y)es/(n)o");
                int resp = Helper.nextYN(scn);
                if (resp == -2)
                    return -2;
                else {
                    update = Card.addCardScreen(this, conn, scn);
                }
            }
        }
    }

    /**
     * Allows a customer to check the orders listed under their accounts
     */
    public void checkOrderScreen(Connection conn, Scanner scn) {
        ArrayList<Order> orders = Order.fetchOrdersByCustomer(conn, this.id);
        if (orders == null) {
            System.out.println("No orders under your account. (Type anything to continue)");
            Helper.nextYN(scn);
            return;
        }
        Pager<Order> order_pager = new Pager(orders, 10);
        while (true) {
            order_pager.printCurrentPage();
            System.out.println("Press n to go to next page, p to go to previous, q to quit.");
            int resp = Helper.nextPNQ(scn);
            switch (resp) {
                case -2:
                    return;
                case 1:
                    Helper.clearConsole();
                    order_pager.previousPage();
                    break;
                case 2:
                    Helper.clearConsole();
                    order_pager.nextPage();
            }
        }
    }
}
