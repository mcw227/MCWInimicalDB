/**
 * Public class customer to model an entry in the customer table
 */

public class Customer {
    public int id;
    public String name;
    public String email;
    public boolean membership;
    public int points;

    public Customer(int id, String name, String email, boolean member, int points) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.membership = member;
        this.points = points;
    }

    public String toString() {
        return String.format("ID: %-8d| NAME: %-30s| EMAIL: %-40s",id,name,email);
    }
}