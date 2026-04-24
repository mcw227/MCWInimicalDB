import java.sql.*;

/** Public class signature item models a signature item entity in the db */
public class SignatureItem extends Item {
    
    public SignatureItem(int id, String name, double price) {
        super(id,name,price);
        this.author = author;
    }

    public static addSig(Connection conn, SignatureItem si) {
        PreparedStatement addSig = conn.prepareStatement("INSERT INTO items (name, price) VALUES (?, ?)");
        addItem.setString(1, si.name);
        addItem.setFloat(2, si.price);

        PreparedStatement addSigToSigList = conn.prepareStatement("INSERT INTO signature_items (id) VALUES (?)");
        
        try {
            conn.setAutoCommit(false)
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
            } catch (Exception e) {
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

        System.out.print("Adding item to Database...");
        addItem.executeUpdate();
        System.out.println("Done!");
        return true;
    }


}