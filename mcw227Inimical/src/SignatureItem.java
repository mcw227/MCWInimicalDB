import java.sql.*;

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

    // There's no real difference since the dependency between signature items table and items table is cascading on deletion
    /**
     * Deletes a signature item
     * @param conn The database connection to use
     * @param id The id of the signature item to delete
     */
    public static boolean delSig(Connection conn, int id) {
       return super.delItem(conn, id);
    }

    /**
     * Obtains all signature items currently in the database.
     * @param conn The database connection to use
     */
    public static ArrayList<SignatureItem> fetchSigs(Connection conn) {
        ArrayList<SignatureItem> signature_items = new ArrayList<>();
        try {
            PreparedStatement fetchCustomerCreations = conn.prepareStatement("SELECT * FROM customer_creations_view");
                ResultSet rs = fetchSignatures.executeQuery();

                if (!rs.next())
                    continue;
                else {
                    do {
                        int id = rs.getInt("id");
                        String name = rs.getString("name");
                        double price = rs.getDouble("price");
                        signature_items.add(new SignatureItem(id, name, price));
                    } (while rs.next());
                }

            return signature_items;
        } catch (Exception e) {
            System.out.println("Unable to fetch signature items. Try again later.");
            return null;
        }
    }


}