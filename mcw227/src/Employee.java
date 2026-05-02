import java.sql.*;

/**
 * Public class employee models an entry into the database under the employee table
 */
public class Employee {
    public int id;
    public String name;
    public int location_id;
    public Location location;
    public int role; //1 = location manager, 2 = manager

    /** Standard constructor */
    public Employee(int id, String name, int location_id, int role) {
        this.id = id;
        this.name = name;
        this.location_id = location_id;
        this.role = role;
    }

    /** Standard constructor w/ immediate fetch location */
    public Employee(int id, String name, int location_id, int role, Connection conn) {
        this.id = id;
        this.name = name;
        this.location_id = location_id;
        this.role = role;
        populateLocation(conn);
    }

    /** populates the location object with an actual location */
    public void populateLocation(Connection conn) {
        this.location = Location.fetchLocation(conn, this.location_id);
        if (this.location == null)
            System.out.println("Unable to fetch location for employee.");
    }

    /**
     * Parses an employee from a result set
     * @param rs The result set to parse
     * @return A valid employee or null if the result set was invalid
     */
    public static Employee parseEmployeeFromRS(ResultSet rs) {
        try (rs) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            int location_id = rs.getInt("location_id");
            int role = rs.getInt("role");
            return new Employee(id, name, location_id, role);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses an employee from a result set
     * @param rs The result set to parse
     * @return A valid employee with a filled location field, or null if the result set was invalid
     */
    public static Employee parseEmployeeFromRS(ResultSet rs, Connection conn) {
        try (rs) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            int location_id = rs.getInt("location_id");
            int role = rs.getInt("role");
            return new Employee(id, name, location_id, role, conn);
        } catch (Exception e) {
            return null;
        }
    }
}
