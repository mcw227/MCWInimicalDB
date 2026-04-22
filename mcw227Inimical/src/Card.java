/**
 * This card class models a row in the Cards table
 */
public class Card {

    public int id;
    public int customer_id;
    public String brand;
    public String name;
    public String card_number;
    public String expr_date;
    public int cvv;

    public Card(int id, int customer_id, String brand, String name, String card_number, String expr_date, String cvv) {
        this.id = id;
        this.customer_id = customer_id;
        this.brand = brand;
        this.name = name;

        this.card_number = card_number;

        this.expr_date = expr_date;

        try {
            this.cvv = Integer.parseInt(cvv);
        } catch (Exception e) {
            this.cvv = -1;
        }
    }

    /**
     * Standard toString function
     * @return formatted ID, Brand, Holder name, Card number, expiration date and cvv.
     */
    public String toString() {
        return String.format("ID: %-3d\t| BRAND: %-10s\t| HOLDER NAME: %-30s\t| CARD NUMBER: %s\t| EXPR DATE: %-7s\t| CVV: %-3d", id, brand, name, card_number, expr_date, cvv);
    }
}