/**
 * This card class models a row in the Cards table
 */
public class Card {

    public int id;
    public int customer_id;
    public String brand;
    public String name;
    public int card_number;
    public String expr_date;
    public int cvv;

    public Card(int id, int customer_id, String brand, String name, String card_number, String expr_date, String cvv) {
        this.id = id;
        this.customer_id = customer_id;
        this.brand = brand;
        this.name = name;

        try {
            this.card_number = Integer.parseInt(card_number);
        } catch (Exception e) {
            this.card_number = -1;
        }

        this.expr_date = expr_date;

        try {
            this.cvv = Integer.parseInt(cvv);
        } catch (Exception e) {
            this.cvv = -1;
        }
    }

    public String toString() {
        return String.format("BRAND: %-40s\t| HOLDER NAME: %-30s\t| CARD NUMBER: %d\t| EXPR DATE: %d\t| CVV: %-3d", brand, name, card_number, expr_date, cvv);
    }
}