/**
 * Public class customer to model an entry in the customer table
 */

public class Customer {
    public int id;
    public String name;
    public String email;
    public boolean membership;
    public int points;

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

    public String toString() {
        return String.format("ID: %-8d| NAME: %-30s| EMAIL: %-40s| MEMBERSHIP:%b\t| POINTS: %-8d",id,name,email, membership, points);
    }
}