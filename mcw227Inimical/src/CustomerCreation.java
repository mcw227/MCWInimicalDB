import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/** Customer creation object  */
public class CustomerCreation extends Item {
    private final int ITEM_PAGE_SIZE = 10;
    private final double MAX_PRICE = 99999999.99;
    public ArrayList<Recipe> ingredients;
    public String creator;

    /** Standard constructor */
    public CustomerCreation(int id, String name, double price, String creator, ArrayList<Recipe> ingredients) {
        super(id, name, price);
        if (creator == null) { this.creator = "Anonymous"; }
        else { this.creator = creator; }
        this.ingredients = ingredients;
    }

    public CustomerCreation(int id, String name, String creator, Connection conn) {
        super(id, name, price);
        if (creator == null) { this.creator = "Anonymous"; }
        else { this.creator = creator; }
        this.ingredients = new ArrayList<Recipe>();
        this.populateRecipe(conn);
    }

    /** Standard toString function */
    public String toString() {
        return String.format("%-40s\t| CREATOR:%s",super.toString(), this.creator);
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

            fetchCustomerCreations.close();
            return menu_items;
        } catch (Exception e) {
            System.out.println("Unable to fetch customer creations. Try again later.");
            return null;
        }
    }

    /**
     * Fetches customer creations
     * @param conn The database connection to use
     */
    public static ArrayList<CustomerCreation> fetchCustomerCreations(Connection conn, Customer c) {
        ArrayList<CustomerCreation> menu_items = new ArrayList<>();
        try {
            PreparedStatement getCustomerCreations = conn.prepareStatement("SELECT * FROM customer_creations_view WHERE creator=?");
            getCustomerCreations.setString(1, c.name);
            ResultSet rs = getCustomerCreations.executeQuery();

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
            getCustomerCreations.close();
            return menu_items;
        } catch (Exception e) {
            System.out.println("Unable to fetch customer creations. Try again later.");
            //e.printStackTrace();
            return null;
        }
    }

    /**
     * Attempts to add a customer creation to the database
     * @param conn The database connection to use
     * @param cc The customer creation to add
     * @param c The customer who made the creation
     * @param lm The menu to add it to
     */
    public static boolean addItem(Connection conn, CustomerCreation cc, Menu lm) {
        if (cc.price > 99999999.99) {
            System.out.println("PRICE TOO HIGH!");
            return false;
        }
        try {
            PreparedStatement addCC = conn.prepareStatement("INSERT INTO items (name, price) VALUES (?, ?)", new String[]{"ID"});
            PreparedStatement addCCtoCCList = conn.prepareStatement("INSERT INTO customer_creations (id, creator) VALUES (?,?)");
            PreparedStatement addToMenu = conn.prepareStatement("INSERT INTO menu_items (menu_id, item_id) VALUES (?,?)");
            conn.setAutoCommit(false);
            addCC.setString(1, cc.name);
            addCC.setDouble(2, 0);
            addCCtoCCList.setString(2, cc.creator);
            addToMenu.setInt(1, lm.id);
            
            int upd_rows = addCC.executeUpdate();
            if (upd_rows > 0) {
                try (ResultSet rs = addCC.getGeneratedKeys()) { //This obtains the identity key that was generated when the item was inserted
                    if (rs.next()) {
                        int newId = (int)rs.getLong(1);
                        cc.id = newId;
                        addCCtoCCList.setInt(1, newId);
                        addCCtoCCList.executeUpdate();

                        if (lm.id != 1) {
                            addToMenu.setInt(2, newId);
                            addToMenu.executeUpdate();
                        }
                        for (Recipe r : cc.ingredients) {
                            r.recipe_id = newId;
                            r.addRecipe(conn);
                        }
                        addCC.close(); addCCtoCCList.close(); addToMenu.close();
                        return true;
                    }
                }
            } else {
                    System.out.println("Could not add item to database! Try again later.");
                    addCC.close(); addCCtoCCList.close(); addToMenu.close();
                    throw new Exception("Row not added!");
                }
            } catch (Exception e) {
                System.out.println("Could not add customer creation to database.");
                //e.printStackTrace();
                try {
                    conn.rollback();
                    return false;
                } catch (Exception f) {
                    System.err.println("Critical database error. Please restart application.");
                    System.exit(-1);
                }
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (Exception e) {
                    System.err.println("Critical database error. Please restart application.");
                    System.exit(-1);
                }
        }
        return false;
    }

    /**
     * Adds a customer creation to the database
     * @param conn The connection to the database
     * @param lm The menu to add the item to
     */
    public boolean addItem(Connection conn, Menu lm) {
        return CustomerCreation.addItem(conn, this, lm);
    }

    /**
     * Makes a "create item" screen that the customer can use to create a new creation
     * @param conn The database connection to use
     * @param scn The scanner to grab input from
     * @param c The customer 
     * @param lm The menu to generate prices from, and to add the item to in the end.
     */
    public static CustomerCreation createItemScreen(Connection conn, Scanner scn, Customer c, LocalMenu lm) {
        ArrayList<Item> ingredients = Item.fetchIngredientsFromList(lm.items);
        ArrayList<SignatureItem> signatures = SignatureItem.fetchSigsFromList(lm.items);

        while(true) {
            CustomerCreation newCC = new CustomerCreation(-1, "Unnamed Creation", 0, c.name, new ArrayList<Recipe>());
            boolean base = newCC.selectSignatureBase(scn, signatures);
            if (base == false)
                return null;
            
            boolean addons = newCC.selectAddons(conn, scn, ingredients);
            if (addons == false)
                continue;
            
            System.out.println("What would you like to name your item?");
            String cc_name = Helper.safeCheckQuit(scn, 50);
            if (cc_name == null)
                continue;
            
            newCC.name = cc_name;
            if (newCC.addItem(conn, lm)) {
                return newCC;
            }
            return null;
        }
    }

    /**
     * Allows a user to pick a single signature item from a list
     * @param scn The scanner to grab input from
     * @param sigs The signature items the user can choose from
     */
    public boolean selectSignatureBase(Scanner scn, ArrayList<SignatureItem> sigs) {
        Pager<SignatureItem> sig_pager = new Pager<>(sigs, ITEM_PAGE_SIZE);

        while (true) {
            sig_pager.printCurrentPage();
            System.out.println("Type (n)ext, (p)revious, (s)elect an item or (q)uit.");
            System.out.println("Pick a signature item to be the base of your custom creation.");
            int r = Helper.nextPNQS(scn);
            if (r == -2)
                return false;

            switch (r) {
                case 1:
                    sig_pager.previousPage();
                    Helper.clearConsole();
                    break;
                case 2:
                    sig_pager.nextPage();
                    Helper.clearConsole();
                    break;
                case 3:
                    SignatureItem sig = SignatureItem.chooseSignatureItem(scn, sigs);
                    Helper.clearConsole();
                    if (sig == null)
                        break;
                    this.addIngredient(sig, 1);
                    return true;
            }
        }
    }

    /**
     * Allows a user to pick some addons from a list
     * @param scn The scanner to grab input from
     * @param items The ingredients the user can choose from
     */
    public boolean selectAddons(Connection conn, Scanner scn, ArrayList<Item> items) {
        Pager<Item> ingredient_pager = new Pager<>(items, ITEM_PAGE_SIZE);

        while (true) {
            ingredient_pager.printCurrentPage();
            System.out.println("Type (n)ext, (p)revious, (s)elect an item, (c)heck an item, (b)ag to see what you've add, (d)one to finish or (q)uit.");
            System.out.println("Pick some addons you'd like to include on your creation.");
            int r = Helper.nextPNQSC(scn);
            if (r == -2)
                return false;

            switch (r) {
                case 1:
                    ingredient_pager.previousPage();
                    Helper.clearConsole();
                    break;
                case 2:
                    ingredient_pager.nextPage();
                    Helper.clearConsole();
                    break;
                case 3:
                    Item item = Item.chooseItem(scn,items);
                    if (item == null)
                        break;
                    System.out.println("How many would you like to add? (must be less than 99)");
                    while (true) {
                        int quantity = Helper.nextId(scn);
                        if (quantity == 0 || quantity == -2)
                            break;
                        if (quantity <= 99) {
                            this.addIngredient(item, quantity);
                            Helper.clearConsole();
                            break;
                        }
                        System.out.println("Must be less than 99!");
                    }
                    break;
                    
                case 4:
                    Item.checkItemScreen(conn, scn, items);
                    break;
                case 5:
                    System.out.println(this.getSummary(conn));
                    System.out.println("Type anything to return to main screen.");
                    Helper.nextOK(scn);
                    Helper.clearConsole();
                    break;
                case 6:
                    if (this.price > MAX_PRICE) {
                        System.out.println("Price too high! Please remove some items. Type anything to continue.");
                        Helper.nextOK(scn);
                        break;
                    }
                    return true;
                default:
                    break;
            }
        }
    }
    

    /**
     * Adds an ingredient to the customer creation
     * @param i The item to add
     * @param q the quantity of the item to add
     */
    public void addIngredient(Item i, int quantity) {
        this.ingredients.add(new Recipe(-1, i.id, quantity, i));
        this.price += i.price * quantity;
    }

    public void addIngredient(Recipe r) {
        this.ingredients.add(r);
        this.price += r.ingredient.price * r.quantity;
    }

    /**
     * Prints a recipe summary of the customer creation object
     * @param conn The database connection to use in the case that a recipe is not populated yet
     */
    public String getPrintableRecipeSummary(Connection conn) {
        String r = "";
        r += String.format("ID:%-3d\tNAME:%-50s\n", this.id, this.name);
        if (ingredients == null || ingredients.size() == 0) {
            this.populateRecipe(conn);
        }
        for (Recipe rec : ingredients) {
            r += "\t" + rec.recipeItemSummary() + "\n";
        }
        r+= String.format("PRICE: %.2f\n", this.price);
        return r;
    }

     /**
     * Prints a recipe summary of the customer creation object
     * @param conn The database connection to use in the case that a recipe is not populated yet
     * @param l The location to obtain prices from
     */
    public String getPrintableRecipeSummary(Connection conn, Location l) {
        String r = "";
        r += String.format("ID:%-3d\tNAME:%-50s\n", this.id, this.name);
        if (ingredients == null || ingredients.size() == 0) {
            this.populateRecipe(conn,l);
        }
        for (Recipe rec : ingredients) {
            r += "\t" + rec.recipeItemSummary() + "\n";
        }
        r+= String.format("PRICE: %.2f\n", this.price);
        return r;
    }

    /**
     * Prints a recipe summary of the customer creation object
     * @param conn The database connection to use in the case that a recipe is not populated yet
     * @param l The location to obtain prices from
     */
    public String getPrintableRecipeSummary(Connection conn, Location l) {
        String r = "";
        r += String.format("ID:%-3d\tNAME:%-50s\n", this.id, this.name);
        if (ingredients == null || ingredients.size() == 0) {
            this.populateRecipe(conn,l);
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
            this.price = 0;
            PreparedStatement getRecipes = conn.prepareStatement("SELECT * FROM recipes WHERE recipe_id=?");
            getRecipes.setInt(1,this.id);
            ResultSet rs = getRecipes.executeQuery();
            if (!rs.next())
                return;
            do {
                this.addIngredient(Recipe.parseRecipeFromRS(rs, conn));
            } while (rs.next());
            getRecipes.close();
            return;

        } catch (Exception e) {
            System.out.println("Unable to update populate recipe. Try again later.");
            //e.printStackTrace();
            return;
        }
    }

    /**
     * Populates a customer creations' item list
     */
    public void populateRecipe(Connection conn, Location l) {
        try {
            this.price = 0;
            PreparedStatement getRecipes = conn.prepareStatement("SELECT * FROM recipes WHERE recipe_id=?");
            getRecipes.setInt(1,this.id);
            ResultSet rs = getRecipes.executeQuery();
            if (!rs.next())
                return;
            do {
                this.ingredients.add(Recipe.parseRecipeFromRS(rs, conn, l));
            } while (rs.next());
            return;

        } catch (Exception e) {
            System.out.println("Unable to update populate recipe. Try again later.");
            //e.printStackTrace();
            return;
        }
    }

    /**
     * Populates a customer creations' item list
     */
    public void populateRecipe(Connection conn, Location l) {
        try {
            this.price = 0;
            PreparedStatement getRecipes = conn.prepareStatement("SELECT * FROM recipes WHERE recipe_id=?");
            getRecipes.setInt(1, this.id);
            ResultSet rs = getRecipes.executeQuery();
            if (!rs.next())
                return;
            do {
                Recipe rec = Recipe.parseRecipeFromRS(rs, conn, l);
                this.addIngredient(rec);
                
            } while (rs.next());
            getRecipes.close();
            return;

        } catch (Exception e) {
            System.out.println("Unable to update populate recipe. Try again later.");
            //e.printStackTrace();
            return;
        }
    }

    @Override
    public String getSummary(Connection conn) {
        return getPrintableRecipeSummary(conn);
    }
}