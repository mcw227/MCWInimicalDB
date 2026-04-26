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

    /** Inactive account object */
    public static Customer InactiveCustomer() {
        return new Customer(-2, "Inactive Customer", null, 0, 0);
    }

    /**
     * Standard toString function
     */
    public String toString() {
        return String.format("ID: %-8d| NAME: %-30s| EMAIL: %-40s| MEMBERSHIP:%b\t| POINTS: %-8d",id,name,email, membership, points);
    }

    /**
     * Deactivates the customer's membership
     * @return true if membership was deactivated, false if not
     */
    public boolean deactivateMembership(Connection conn) {
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
     * Adds card to the current customer
     * @param card Card to add
     * @return True if added, false if failed
     */
    public boolean addCard(Connection conn, Card card) {
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
        if (c.id == -1) {
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
}
