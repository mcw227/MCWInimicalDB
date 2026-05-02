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

            try (PreparedStatement findEmployee = conn.prepareStatement("SELECT * FROM employees WHERE id = ?")) {
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
                //e.printStackTrace();
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
        int resp = 0;
        while (resp != -2) {
            Helper.clearConsole();
            printMMenu(e);
            resp = Helper.nextId(scn);
            switch (resp) {
                case (1):
                    mViewLocations(conn, scn);
                    break;
                case (2):
                    mViewItems(conn, scn);
                    break;
            }
        }
    }

    /**
     * Prints the Manager options menu
     * @param e Employee who is currently logged in
     */
    public static void printMMenu(Employee e) {
        System.out.flush();
        System.out.printf("\n\nHello, %s! Welcome to the statistics portal!", e.name);
        System.out.printf("\nWhat would you like to do today?\n\t1. View Location Sales Summaries\n\t2. View Item Summaries\n");
    }

    /**
     * Prints basic location summary metrics
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     */
    public static void mViewLocations(Connection conn, Scanner scn) {
        while (true) {
            Location l = Location.locationSelectScreen(conn, scn);
            if (l == null)
                return;
            l.printLocSummary(conn, scn);
        }
    }

    /**
     * Prints basic item summary metrics
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     */
    public static void mViewItems(Connection conn, Scanner scn) {
        while (true) {
            Item i = Item.itemSelectScreen(conn, scn);
            if (i == null)
                return;
            i.printItemSummary(conn, scn);
        }
    }
}
