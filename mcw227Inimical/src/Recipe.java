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
     * A helpful constructor that automatically populates the ingredients' details when possible
     */
    public Recipe(int r_id, int i_id, int quantity, Connection conn) {
        this.recipe_id = r_id;
        this.ingredient_id = i_id;
        this.quantity = quantity;
        getIngredientDetails(conn);
    }

    /**
     * A helpful constructor that automatically populates the ingredients' details when possible
     */
    public Recipe(int r_id, int i_id, int quantity, Connection conn, Location l) {
        this.recipe_id = r_id;
        this.ingredient_id = i_id;
        this.quantity = quantity;
        getIngredientDetails(conn, l);
    }

    public Recipe(int r_id, int i_id, int quantity, Item item) {
        this.recipe_id = r_id;
        this.ingredient_id = i_id;
        this.quantity = quantity;
        this.ingredient = item;
    }

    /**
     * A helpful constructor that automatically populates the ingredients' details when possible
     */
    public Recipe(int r_id, int i_id, int quantity, Connection conn, Location l) {
        this.recipe_id = r_id;
        this.ingredient_id = i_id;
        this.quantity = quantity;
        getIngredientDetails(conn, l);
    }

    /** Standard to string */
    public String toString() {
        return String.format("RECIPE ID:%-3d\t| INGREDIENT ID: %-3d\t| QUANTITY %-3d", this.recipe_id, this.ingredient_id, this.quantity);
    }

    /** Summarizes the recipe better for the user */
    public String recipeItemSummary() {
        return String.format("INGREDIENT_ID: %-3d\t| NAME: %-40s\t| INGREDIENT_PRICE: %5.2f\t| QUANTITY: %-3d", this.ingredient_id, this.ingredient.name, this.ingredient.price, this.quantity);
    }

    /**
     * Parses a recipe from a result set, fills its item information
     * @param rs The result set to parse
     * @param conn The database connection to fill item information from
     * @return A recipe if the RS is valid, or null if not
     */
    public static Recipe parseRecipeFromRS(ResultSet rs, Connection conn) {
        try {
            int r_id = rs.getInt("recipe_id");
            int i_id = rs.getInt("ingredient_id");
            int quantity = rs.getInt("quantity");
            return new Recipe(r_id, i_id, quantity, conn);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses a recipe from a result set, fills its item information
     * @param rs The result set to parse
     * @param conn The database connection to fill item information from
     * @param l The location to grab prices from, if necessary
     * @return A recipe if the RS is valid, or null if not
     */
    public static Recipe parseRecipeFromRS(ResultSet rs, Connection conn, Location l) {
        try {
            int r_id = rs.getInt("recipe_id");
            int i_id = rs.getInt("ingredient_id");
            int quantity = rs.getInt("quantity");
            return new Recipe(r_id, i_id, quantity, conn, l);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Populates the ingredient field using the ingredient id
     * @param conn The database connection to use.
     */
    public void getIngredientDetails(Connection conn) {
        this.ingredient = Item.createItemFromID(conn, this.ingredient_id);
    }

    /**
     * Populates the ingredient field using the ingredient id
     * @param conn The database connection to use.
     */
    public void getIngredientDetails(Connection conn, Location l) {
        Item i = Item.createItemFromID(conn, this.ingredient_id, l.id);
        this.ingredient = i;
    }

    /**
     * Attempts to add the recipe to the database.
     * @param conn The database connection to use
     */
    public boolean addRecipe(Connection conn) throws SQLException {
        PreparedStatement addRecipe = conn.prepareStatement("INSERT INTO recipes (recipe_id, ingredient_id, quantity) VALUES (?,?,?)");
        addRecipe.setInt(1, this.recipe_id); addRecipe.setInt(2, this.ingredient_id);
        addRecipe.setInt(3, this.quantity);
        addRecipe.executeUpdate();
        addRecipe.close();
        return true;
    }
}