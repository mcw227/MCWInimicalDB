import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/** Public class signature item models a signature item entity in the db */
public class SignatureItem extends Item {

    /** Standard constructor */
    public SignatureItem(int id, String name, double price) {
        super(id,name,price);
    }

    /**
     * Adds a signature item to the database
     * @param conn The database connection to use
     * @param si The signature item to add
     */
    public static void addSig(Connection conn, SignatureItem si) {
        
        try {
            PreparedStatement addSig = conn.prepareStatement("INSERT INTO items (name, price) VALUES (?, ?)");
            PreparedStatement addSigToSigList = conn.prepareStatement("INSERT INTO signature_items (id) VALUES (?)");
            conn.setAutoCommit(false);
            addSig.setString(1, si.name);
            addSig.setDouble(2, si.price);
            
            System.out.print("Adding signature item to Database...");
            int upd_rows = addSig.executeUpdate();
            if (upd_rows > 0) {
                try (ResultSet rs = addSig.getGeneratedKeys()) { //This obtains the identity key that was generated when the item was inserted
                    if (rs.next()) {
                        int newId = (int)rs.getLong(1);

                        addSigToSigList.setInt(1, newId);
                        addSigToSigList.executeUpdate();
                        System.out.println("Done!");
                    }
                }
            }
            else {
                System.out.println("Could not add item to database! Try again later.");
            }
        } catch (Exception e) {
            System.out.println("Could not add signature item to database.");
            try {
                conn.rollback();
            } catch (Exception f) {
                System.err.println("Critical database error. Please restart application.");
                System.exit(-1);
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (Exception e) {
                System.err.println("Critical database error. Please restart application.");
                System.exit(-1);
            }
        }
    }

    /**
     * Allows a user to select a signature item from the list. Returns the id if it is a valid item in the list, or -2 if user quits
     * @param scn The scanner to grab input from
     * @param items The list of items to take from
     */
    public static SignatureItem chooseSignatureItem(Scanner scn, ArrayList<SignatureItem> items) {
        while (true) {
            System.out.println("Which item do you want to choose? (or type (q)uit to quit)");
            int choice = Helper.nextId(scn);
            if (choice == -2)
                return null;
            SignatureItem s = items.stream().filter(i -> i.id == choice).findFirst().orElse(null);
            if (s != null)
                return s;
            System.out.println("Please pick a valid item!");
        }
    }

    // There's no real difference since the dependency between signature items table and items table is cascading on deletion
    /**
     * Deletes a signature item
     * @param conn The database connection to use
     * @param id The id of the signature item to delete
     */
    public static boolean delSig(Connection conn, int id) {
        try {
            return Item.delItem(conn, id);
        } catch (Exception e) {
            System.out.printf("Could not delete signature item with id %d. Try again later.\n", id);
            return false;
        }
    }

    /**
     * Obtains all signature items currently in the database.
     * @param conn The database connection to use
     */
    public static ArrayList<SignatureItem> fetchSignatures(Connection conn) {
        ArrayList<SignatureItem> signature_items = new ArrayList<>();
        try {
            PreparedStatement fetchSignatures = conn.prepareStatement("SELECT * FROM signature_item_view");
                ResultSet rs = fetchSignatures.executeQuery();

                if (!rs.next())
                    return signature_items;
                else {
                    do {
                        int id = rs.getInt("id");
                        String name = rs.getString("name");
                        double price = rs.getDouble("price");
                        signature_items.add(new SignatureItem(id, name, price));
                    } while (rs.next());
                }

            return signature_items;
        } catch (Exception e) {
            System.out.println("Unable to fetch signature items. Try again later.");
            return null;
        }
    }

    /**
     * Obtains all signature items currently in the database.
     * @param conn The database connection to use
     */
    public static ArrayList<SignatureItem> fetchSignatures(Connection conn, Menu m) {
        ArrayList<SignatureItem> signature_items = new ArrayList<>();
        try {
            PreparedStatement fetchSignatures = conn.prepareStatement("SELECT * FROM menu_items m JOIN signature_item_view sig ON m.item_id = sig.id WHERE m.menu_id = ?");
                ResultSet rs = fetchSignatures.executeQuery();

                if (!rs.next())
                    return signature_items;
                else {
                    do {
                        int id = rs.getInt("id");
                        String name = rs.getString("name");
                        double price = rs.getDouble("price");
                        signature_items.add(new SignatureItem(id, name, price));
                    } while (rs.next());
                }

            return signature_items;
        } catch (Exception e) {
            System.out.println("Unable to fetch signature items. Try again later.");
            return null;
        }
    }

    /**
     * Grabs signature items from a list
     * @param items The item list to add
     */
    public static ArrayList<SignatureItem> fetchSigsFromList(ArrayList<Item> items) {
        ArrayList<SignatureItem> sigs = new ArrayList<>();
        for (Item i : items) {
            if (SignatureItem.class.isInstance(i))
                sigs.add((SignatureItem)i);
        }
        return sigs;
    }


}