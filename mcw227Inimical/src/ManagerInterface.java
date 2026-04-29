import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class ManagerInterface {

    public static void start(Connection conn, Scanner scn) {
        Employee e = eLogin(conn, scn);
        if (e == null)
            return;
        if (e.role == 1)
            LocationManagerInterface.lmMenu(conn, scn, e);
        else if (e.role == 2)
            mMenu(conn, scn, e);
        return;
    }

    /**
     * @param conn the Database connection to use
     * @param scn the scanner to use to get input.
     * @return a valid customer or null
     */
    static Employee eLogin(Connection conn, Scanner scn) {
        ResultSet rs = null;
        int id = -1;
        Employee e = null;
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
                PreparedStatement findEmployee = conn.prepareStatement("SELECT * FROM employees WHERE id = ?");
                findEmployee.setInt(1, id);
                rs = findEmployee.executeQuery();
                if (rs == null)
                    System.out.println("User id not found, try again.");
                else {
                    rs.next();
                    e = Employee.parseEmployeeFromRS(rs, conn);
                    return e;
                }
            } catch (Exception ex) {
                System.out.println("Could not query database, or found invalid employee, please try again.");
                ex.printStackTrace();
            }
            
        }     
        return e;
    }

    /**
     * Opens the manager menu interface
     * @param conn
     * @param scn
     * @param e
     */
    static void mMenu(Connection conn, Scanner scn, Employee e) {
        return;
    }
}
