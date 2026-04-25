import java.sql.*;

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
        addPhone.setString(3, pn.phone);

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
            delPhone = conn.prepareStatement("DELETE FROM phoneNumber WHERE id=?");
            delPhone.setInt(1,id);
        }
        else {
            delPhone = conn.prepareStatement("DELETE FROM phoneNumber WHERE id=? AND customer_id=?");
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
}