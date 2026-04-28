import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/** Customer creation object  */
public class CustomerCreation extends Item {
    public ArrayList<Recipe> ingredients;
    public String creator;

    /** Standard constructor */
    public CustomerCreation(int id, String name, double price, String creator, ArrayList<Recipe> ingredients) {
        super(id, name, price);
        this.creator = creator;
        this.ingredients = ingredients;
    }

    public CustomerCreation(int id, String name, double price, String creator, Connection conn) {
        super(id, name, price);
        this.creator = creator;
        this.ingredients = new ArrayList<Recipe>();
        this.populateRecipe(conn);
    }

    /** Standard toString function */
    public String toString() {
        return String.format("%-30s\t| CREATOR:%s",super.toString(), this.creator);
    }

    /**
     * Deletes a customer creation with the given id
     * @param conn The database connection to use
     * @param id The id of the customer creation to delete
     */
    public static boolean delCustomerCreation(Connection conn, int id) {
        try {
            return Item.delItem(conn, id);
        } catch (Exception e) {
            System.out.printf("Could not delete customer creation with id: %d\n", id);
            return false;
        }
        
    }

    /**
     * Fetches customer creations
     * @param conn The database connection to use
     */
    public static ArrayList<CustomerCreation> fetchCustomerCreations(Connection conn) {
        ArrayList<CustomerCreation> menu_items = new ArrayList<>();
        try {
            PreparedStatement fetchCustomerCreations = conn.prepareStatement("SELECT * FROM customer_creations_view");
            ResultSet rs = fetchCustomerCreations.executeQuery();

            if (!rs.next())
                return menu_items;
            else {
                do {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");
                    String creator = rs.getString("creator");
                    menu_items.add(new CustomerCreation(id, name, price, creator, conn));
                } while (rs.next());
            }

            return menu_items;
        } catch (Exception e) {
            System.out.println("Unable to fetch customer creations. Try again later.");
            return null;
        }
    }

    public static boolean addItem(Connection conn, Scanner scn, Customer c, Menu lm) {
        return false;
    }

    public static boolean createItemScreen(Connection conn, Scanner scn, Customer c, LocalMenu lm) {
        ArrayList<Item> ingredients = Item.fetchIngredientsFromList(lm.items);
        ArrayList<SignatureItem> signatures = SignatureItem.fetchSigsFromList(lm.items);
        return false;
    }

    /**
     * Prints a recipe summary of the customer creation object
     * @param conn The database connection to use in the case that a recipe is not populated yet
     */
    public String getPrintableRecipeSummary(Connection conn) {
        String r = "";
        r += String.format("ID:%-3d\tNAME:%-50s\n", this.id, this.name);
        if (ingredients == null) {
            this.populateRecipe(conn);
        }
        for (Recipe rec : ingredients) {
            r += "\t" + rec.recipeItemSummary() + "\n";
        }
        r+= String.format("PRICE: %.2f\n", this.price);
        return r;
    }

    /**
     * Populates a customer creations' item list
     */
    public void populateRecipe(Connection conn) {
        try {
            PreparedStatement getRecipes = conn.prepareStatement("SELECT * FROM recipes WHERE recipe_id=?");
            getRecipes.setInt(1,this.id);
            ResultSet rs = getRecipes.executeQuery();
            if (!rs.next())
                return;
            do {
                this.ingredients.add(Recipe.parseRecipeFromRS(rs, conn));
            } while (rs.next());
            return;

        } catch (Exception e) {
            System.out.println("Unable to update populate recipe. Try again later.");
            e.printStackTrace();
            return;
        }
    }
}