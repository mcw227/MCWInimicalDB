import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
/** Public class Recipe represents an entry in the recipe table of the database */
public class Recipe {
    public int recipe_id;
    public int ingredient_id;
    public int quantity;
    public Item ingredient;

    /**
     * Standard constructor
     */
    public Recipe(int r_id, int i_id, int quantity) {
        this.recipe_id = r_id;
        this.ingredient_id = i_id;
        this.quantity = quantity;
        this.ingredient = null;
    }

    /**
     * Populates the ingredient field using the ingredient id
     * @param conn The database connection to use.
     */
    public void getIngredientDetails(Connection conn) {
        this.ingredient = Item.createItemFromID(conn, this.ingredient_id);
    }

    /**
     * Attempts to add the recipe to the database.
     * @param conn The database connection to use
     */
    public boolean addRecipe(Connection conn) {
        try {
            PreparedStatement addRecipe = conn.prepareStatement("INSERT INTO recipes (recipe_id, ingredient_id, quantity) VALUES (?,?,?)");
            addRecipe.setInt(1, this.recipe_id); addRecipe.setInt(2, this.ingredient_id);
            addRecipe.setInt(3, this.quantity);
            addRecipe.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Could not add recipe to database. Try again later");
            return false;
        }
    }
}