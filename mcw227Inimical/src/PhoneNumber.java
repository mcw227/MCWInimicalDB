import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This card class models a row in the Cards table
 * Note that a negative id corresponds to a card that has not been added in the database yet
 */
public class PhoneNumber {

    public int id;
    public int customer_id;
    public String phone;

    public PhoneNumber(int id, int customer_id, String phone_number) {
        this.id = id;
        this.customer_id = customer_id;
        this.phone = phone_number;
    }

    /**
     * Standard toString function
     * @return formatted ID, customer_id, phone number.
     */
    public String toString() {
        return String.format("ID: %-3d\t| CUSTOMER_ID: %-3d\t| NUMBER: %-20s", id, customer_id, phone);
    }

    /**
     * Adds a phone number to the database
     * @param conn The database connection to use
     * @param customer_id the customer id to add the phone number under
     * @param c the phone number to add
     */
    public static boolean addPhone(Connection conn, PhoneNumber pn) throws SQLException {
        PreparedStatement addPhone = conn.prepareStatement("INSERT INTO phone_numbers (customer_id, phone) VALUES (?, ?)");
        addPhone.setInt(1, pn.customer_id);
        addPhone.setString(2, pn.phone);

        System.out.print("Adding phone number to Database...");
        addPhone.executeUpdate();
        System.out.println("Done!");
        return true;
    }

    /**
     * Removes a phone number from a customer
     * @param c The customer to remove the phone number from. If id is -1 that means it is an admin account
     * @param conn The database connection to use
     * @param id The id of the phone number to delete
     */
    public static boolean removePhone(Customer c, Connection conn, int id) throws SQLException {
        PreparedStatement delPhone;
        if (c.id == -1) {
            delPhone = conn.prepareStatement("DELETE FROM phone_numbers WHERE id=?");
            delPhone.setInt(1,id);
        }
        else {
            delPhone = conn.prepareStatement("DELETE FROM phone_numbers WHERE id=? AND customer_id=?");
            delPhone.setInt(1, id); //card entry id
            delPhone.setInt(2, c.id); //customer id
        }

        System.out.print("Removing phone number from database...");
        int row_update = delPhone.executeUpdate();
        if (row_update == 0) {
            System.out.printf("Phone number with id %d not found!\n", id);
            return false;
        }
        else {
            System.out.println("Done!");
            return true;
        }
    }

    
    /**
     * Allows for easy adding of phone numbers
     * @param c The customer to add the phone number under. If id is -1 (admin account) then prompts for valid id
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     * @return True if phone was added, false if not.
     */
    public static boolean addPhoneScreen(Customer c, Connection conn, Scanner scn) {
        System.out.println("Please enter a phone number in the format: +XXX-XXX-XXX-XXXX (with support for 3 digit country code) or type (!q)uit to quit.");
        String phone = Helper.nextPhoneNumber(scn);

        if (phone == null)
            return false;

        try {
            if (c.id != -1)
                return addPhone(conn, new PhoneNumber(-1, c.id, phone));
            else {
                System.out.println("Which user would you like to add the phone number under?");
                int id = Helper.nextId(scn);
                return addPhone(conn, new PhoneNumber(-1, id, phone));
            }
        } catch (Exception e) {
            System.out.println("Could not add phone to database. Please try again later.");
            //e.printStackTrace(); //debug
            return false;
        }
    }

    /**
     * Allows for easy adding of phone numbers
     * @param c The customer to add the phone number under. If id is -1 (admin account) then prompts for valid id
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     * @return True if phone was added, false if not.
     */
    public static boolean removePhoneScreen(Customer c, Connection conn, Scanner scn) {
        System.out.println("What is the id of the phone number you'd like to remove?");
        int id = Helper.nextId(scn);

        try {
                return PhoneNumber.removePhone(c,conn, id);
        } catch (Exception e) {
            System.out.println("Could not remove phone from database. Please try again later.");
            //e.printStackTrace(); //debug
            return false;
        }
    }

    /**
     * Gets all cards for a particular customer id
     * @param c_id Customer id to query. If set to -1, gets all credit cards and puts them in a list.
     * @param conn Database connection to use.
     * @return an ArrayList populated with the cards obtained
     */
    public static ArrayList<PhoneNumber> fetchPhones(int c_id, Connection conn) {
        ArrayList<PhoneNumber> phones = new ArrayList<>();
        String sql = (c_id == -1) ? "SELECT * FROM phone_numbers" : "SELECT * FROM phone_numbers WHERE customer_id = ?"; // I am so smard
        try (PreparedStatement getPhones = conn.prepareStatement(sql)) {
            if (c_id != -1)
                getPhones.setInt(1, c_id);
            ResultSet rs = getPhones.executeQuery();
            
            if (!rs.next()) { //No cards, return empty arraylist
                return phones;
            }

            do {
                int id = rs.getInt("id");
                int cid = rs.getInt("customer_id");
                String number = rs.getString("phone");

                phones.add(new PhoneNumber(id, cid, number));
            } while (rs.next()); //since we checked rs.next() above we have to use a do while loop instead, otherwise we skip the first one :(

        } catch (Exception e) {
            System.out.println("Could not query Database for phone numbers. Please try again later.");
            e.printStackTrace(); //debug
            return null;
        } finally {

        }
        return phones;
    }
}