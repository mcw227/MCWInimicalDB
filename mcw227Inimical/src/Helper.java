import java.sql.*;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.*;

public final class Helper {

    public Helper() {
        throw new UnsupportedOperationException("This is a utility class. Do not instantiate it.");
    }
    
    /** Attempts to clear console */
    /** Shamelessly sourced from Copilot, but I understand how it works. */
    public static void clearConsole() {
        return;
        // try {
        //     String os = System.getProperty("os.name");
        //     if (os.contains("Windows")) { //windwos needs special handling bc its so special...
        //         new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        //     } else { //unix-like systems
        //         new ProcessBuilder("clear").inheritIO().start().waitFor();
        //     }
        //     return;
        // } catch (Exception e) {
        //     System.out.println("Cannot clear console."); //debug
        //     return;
        // }
        
    }
    /**
     * This is also used to get non-negative integer input while handling quit case
     * @param scn Scanner to grab input from
     * @return A positive integer or -1
     */
    public static int nextId(Scanner scn) {
        try {
            String resp = scn.nextLine();
            if (resp.equalsIgnoreCase("q") || resp.equalsIgnoreCase("quit")) return -2;
            return Integer.parseInt(resp);
        } catch (Exception e) {
            return -1;
        }
    }


    /**
     * Gets the next valid email address input from the user.
     * @param scn The scanner to grab input from
     * @return A string that has been regex-suggested to be a valid email
     */
    public static String nextEmail(Scanner scn) {
        String email_regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"; //Plagiarized from Gemini. :)

        while(true) {
            String resp = nextSafeString(scn, 40);
            if (matchRegex(resp, email_regex))
                return resp;
            System.out.println("Please provide a valid email.");
        }
    }

    /**
     * Gets the next valid phone number from the user
     * @param scn Scanner to grab input from
     * @return A string that has been regex-suggested to be a valid phone number
     */
    public static String nextPhoneNumber(Scanner scn) {
        String phone_regex = "\\+\\d{1,3}-\\d{3}-\\d{3}-\\d{4}";

        while(true) {
            String resp = safeCheckQuit(scn, 40);
            if (resp == null)
                return null;
            if (matchRegex(resp, phone_regex))
                return resp;
            System.out.println("Please provide a valid phone number in the format +X-XXX-XXX-XXXX. (Country code up to three digits)");
        }
    }
    /**
     * This function handles yes/no input from user. Also handles quit for interface cohesiveness. Note that no and quit have the same return value
     * @param scn The scanner to grab input from
     * @return 1 if user types yes, -2 if user types no or wants to quit. Retries until a valid input is reached.
     */
    public static int nextYN(Scanner scn) {
        int r = 0;
        while (r == 0) {
            String resp = scn.nextLine();
            if (resp.equalsIgnoreCase("q") || resp.equalsIgnoreCase("quit") || resp.equalsIgnoreCase("n") || resp.equalsIgnoreCase("no"))
                return -2;
            else if (resp.equalsIgnoreCase("y") || resp.equalsIgnoreCase("yes"))
                return 1;
            System.out.println("Please type either (y)es or (n)o");
        }
        return -2;
    }

    /**
     * This function handles previous/next/quit input from user.
     * @param scn The scanner to grab input from
     * @return 1 if user types previous, 2 if user types next, -2 if user quits. Retries until a valid input is reached.
     */
    public static int nextPNQ(Scanner scn) {
        while (true) {
            String resp = scn.nextLine();
            if (resp.equalsIgnoreCase("q") || resp.equalsIgnoreCase("quit"))
                return -2;
            else if (resp.equalsIgnoreCase("p") || resp.equalsIgnoreCase("previous"))
                return 1;
            else if (resp.equalsIgnoreCase("n") || resp.equalsIgnoreCase("next"))
                return 2;
            System.out.println("Please type either (n)ext, (p)revious, or (q)uit");
        }
    }

    /**
     * This function handles previous/next/select/quit input from user.
     * @param scn The scanner to grab input from
     * @return 1 if user types previous, 2 if user types next, 3 if user types select, -2 if user quits. Retries until a valid input is reached.
     */
    public static int nextPNQS(Scanner scn) {
        while (true) {
            String resp = scn.nextLine();
            if (resp.equalsIgnoreCase("q") || resp.equalsIgnoreCase("quit"))
                return -2;
            else if (resp.equalsIgnoreCase("p") || resp.equalsIgnoreCase("previous"))
                return 1;
            else if (resp.equalsIgnoreCase("n") || resp.equalsIgnoreCase("next"))
                return 2;
            else if (resp.equalsIgnoreCase("s") || resp.equalsIgnoreCase("select"))
                return 3;
            System.out.println("Please type either (n)ext, (p)revious, (s)elect or (q)uit");
        }
    }

    /**
     * This function handles previous/next/select/check/quit input from user.
     * @param scn The scanner to grab input from
     * @return 1 if user types previous, 2 if user types next, 3 if user types select, 4 if user types check, 5 if user types bag, 6 if user types done -2 if user quits. Retries until a valid input is reached.
     */
    public static int nextPNQSC(Scanner scn) {
        while (true) {
            String resp = scn.nextLine();
            if (resp.equalsIgnoreCase("q") || resp.equalsIgnoreCase("quit"))
                return -2;
            else if (resp.equalsIgnoreCase("p") || resp.equalsIgnoreCase("previous"))
                return 1;
            else if (resp.equalsIgnoreCase("n") || resp.equalsIgnoreCase("next"))
                return 2;
            else if (resp.equalsIgnoreCase("s") || resp.equalsIgnoreCase("select"))
                return 3;
            else if (resp.equalsIgnoreCase("c") || resp.equalsIgnoreCase("check"))
                return 4;
            else if (resp.equalsIgnoreCase("b") || resp.equalsIgnoreCase("bag"))
                return 5;
            else if (resp.equalsIgnoreCase("d") || resp.equalsIgnoreCase("done"))
                return 6;
            System.out.println("Please type either (n)ext, (p)revious, (s)elect, (c)heck, (b)ag, (d)one or (q)uit");
        }
    }

    /**
     * This function handles previous/next/quit input from user while also allowing them to .
     * @param scn The scanner to grab input from
     * @return -3 if user types previous, -4 if user types next, -2 if user quits. Retries until a valid input is reached.
     */
    public static int nextPNQID(Scanner scn) {
        while (true) {
            String resp = scn.nextLine();
            try {
                int id = Integer.parseInt(resp);
                if (id <= 0) {
                    System.out.println("ID must be greater than 0.");
                } else {
                    return id;
                }
            } catch (NumberFormatException e) {
                if (resp.equalsIgnoreCase("q") || resp.equalsIgnoreCase("quit"))
                    return -2;
                else if (resp.equalsIgnoreCase("p") || resp.equalsIgnoreCase("previous"))
                    return -3;
                else if (resp.equalsIgnoreCase("n") || resp.equalsIgnoreCase("next"))
                    return -4;
                System.out.println("Please type either (n)ext, (p)revious, or (q)uit");
            }
           
        }
    }

    /**
     * Handles user new/check/quit input
     * @param scn The scanner to grab input from
     * @return 1 if user wants new, 2 if user checks, -2 if user quits
     */
    public static int nextNCQ(Scanner scn) {
        while (true) {
            String resp = scn.nextLine();
            if (resp.equalsIgnoreCase("q") || resp.equalsIgnoreCase("quit"))
                return -2;
            else if (resp.equalsIgnoreCase("c") || resp.equalsIgnoreCase("check"))
                return 2;
            else if (resp.equalsIgnoreCase("n") || resp.equalsIgnoreCase("new"))
                return 1;
            System.out.println("Please type either (n)ew, (c)heck, or (q)uit");
        }
    }

    /**
     * This function handles delete/edit/quit input from user
     * @param scn The scanner to grab input from
     * @return -2 if user wnats to quit, 1 if user wants to delete, 2 if user wants to edit
     */
    public static int nextDEQ(Scanner scn) {
        int r = 0;
        while (r == 0) {
            String resp = scn.nextLine();
            if (resp.equalsIgnoreCase("q") || resp.equalsIgnoreCase("quit"))
                return -2;
            else if (resp.equalsIgnoreCase("d") || resp.equalsIgnoreCase("delete"))
                return 1;
            else if (resp.equalsIgnoreCase("e") || resp.equalsIgnoreCase("edit"))
                return 2;
            System.out.println("Please type either (d)elete, (e)dit, or (q)uit");
        }
        return -2;
    }

    public static void nextOK(Scanner scn) {
        scn.nextLine();
        return;
    }

    /**
     * This function handles previous/next/quit/add/delete input from user.
     * @param scn The scanner to grab input from
     * @return 1 if user types previous, 2 if user types next, 3 if user types add, 4 if user types delete -2 if user quits. Retries until a valid input is reached.
     */
    public static int nextPNQAD(Scanner scn) {
        int r = 0;
        while (r == 0) {
            String resp = scn.nextLine();
            if (resp.equalsIgnoreCase("q") || resp.equalsIgnoreCase("quit"))
                return -2;
            else if (resp.equalsIgnoreCase("p") || resp.equalsIgnoreCase("previous"))
                return 1;
            else if (resp.equalsIgnoreCase("n") || resp.equalsIgnoreCase("next"))
                return 2;
            else if (resp.equalsIgnoreCase("a") || resp.equalsIgnoreCase("add"))
                return 3;
            else if (resp.equalsIgnoreCase("d") || resp.equalsIgnoreCase("delete"))
                return 4;
            System.out.println("Please type either (n)ext, (p)revious, (a)dd, (d)elete, or (q)uit");
        }
        return -2;
    }

    /**
     * This function handles previous/next/quit/add/delete/select input from user.
     * @param scn The scanner to grab input from
     * @return 1 if user types previous, 2 if user types next, 3 if user types add, 4 if user types delete 5 if user types select, -2 if user quits. Retries until a valid input is reached.
     */
    public static int nextPNQADS(Scanner scn) {
        int r = 0;
        while (r == 0) {
            String resp = scn.nextLine();
            if (resp.equalsIgnoreCase("q") || resp.equalsIgnoreCase("quit"))
                return -2;
            else if (resp.equalsIgnoreCase("p") || resp.equalsIgnoreCase("previous"))
                return 1;
            else if (resp.equalsIgnoreCase("n") || resp.equalsIgnoreCase("next"))
                return 2;
            else if (resp.equalsIgnoreCase("a") || resp.equalsIgnoreCase("add"))
                return 3;
            else if (resp.equalsIgnoreCase("d") || resp.equalsIgnoreCase("delete"))
                return 4;
            else if (resp.equalsIgnoreCase("s") || resp.equalsIgnoreCase("select"))
                return 5;
            System.out.println("Please type either (n)ext, (p)revious, (a)dd, (d)elete, or (q)uit");

        }
        return -2;
    }

    /**
     * Handles add/check quit input (intended for use with orders/signature items, but may be used elsewhere)
     * @param scn The scanner to grab input from
     * @return 1 if user inputs add, 2 if user inputs check, -2 if quit
     */
    public int nextACQ(Scanner scn) {
        while (true) {
            String resp = scn.nextLine();
            if (resp.equalsIgnoreCase("add") || resp.equalsIgnoreCase("a"))
                return 1;
            else if (resp.equalsIgnoreCase("check") || resp.equalsIgnoreCase("c"))
                return 2;
            else if (resp.equalsIgnoreCase("quit") || resp.equalsIgnoreCase("q"))
                return -2;
            System.out.println("Please type either  (a)dd, (c)heck, or (q)uit");
        }
    }

    /**
     * Handles add/check quit input (intended for use with orders/signature items, but may be used elsewhere)
     * @param scn The scanner to grab input from
     * @return 1 if user inputs add, 2 if user inputs check, 3 if user inputs next, 4 if user inputs previous, 
     * 5 if user inputs bag, 6 if user inputs (ch)eckout, 7 if user inputs (cr)eate, 8 if user inputs (m)enus 
     * or -2 if quit
     */
    public static int nextACQNPB(Scanner scn) {
        while (true) {
            String resp = scn.nextLine();
            if (resp.equalsIgnoreCase("add") || resp.equalsIgnoreCase("a"))
                return 1;
            else if (resp.equalsIgnoreCase("check") || resp.equalsIgnoreCase("c"))
                return 2;
            else if (resp.equalsIgnoreCase("next") || resp.equalsIgnoreCase("n"))
                return 3;
            else if (resp.equalsIgnoreCase("previous") || resp.equalsIgnoreCase("p"))
                return 4;
            else if (resp.equalsIgnoreCase("bag") || resp.equalsIgnoreCase("b"))
                return 5;
            else if (resp.equalsIgnoreCase("checkout") || resp.equalsIgnoreCase("ch"))
                return 6;
            else if (resp.equalsIgnoreCase("create") || resp.equalsIgnoreCase("cr"))
                return 7;
            else if (resp.equalsIgnoreCase("menus") || resp.equalsIgnoreCase("m"))
                return 8;
            else if (resp.equalsIgnoreCase("quit") || resp.equalsIgnoreCase("q"))
                return -2;
            System.out.println("Please type either (a)dd, (c)heck, (n)ext, (p)revious, (b)ag, (ch)eckout, (cr)eate or (q)uit");
        }
    }

    /**
     * This function prompts the user for a string that is within a particular length and does not contain the character "'"
     * @param scn Scanner to grab input from
     * @param len Maximum length of string to accept
     * @return a sane user string.
     */
    public static String nextSafeString(Scanner scn, int len) {
        while (true) {
            String resp = scn.nextLine();
            if (resp.length() > len) {
                System.out.printf("Provided string is longer than %d characters. Please shorten it.\n", len);
            } else if (resp.contains("'")) {
                System.out.printf("Provided string contains an invalid character ('). Please remove it.\n");
            }
            else { return resp; }
        }
    }

    public static ArrayList<Item> getMenu(Connection conn, Scanner scn)
    {
        return null;
    }

    /**
     * Checks whether user is trying to quit by typing !q or !quit
     * @return either the input string or null if user is trying to quit.
     */
    static String safeCheckQuit(Scanner scn, int len) {
        String resp = nextSafeString(scn, len);
        if (resp.equalsIgnoreCase("!q") || resp.equalsIgnoreCase("!quit"))
            return null;
        return resp;
    }

    /**
     * Sees whether the base string contains the target pattern.
     * @param base The string to compare to the pattern
     * @param target The regex pattern to search for
     * @return true if the pattern is contained, false if not.
     */
    static boolean matchRegex(String base, String target) {
        Pattern re_pattern = Pattern.compile(target);
        Matcher pattern_matcher = re_pattern.matcher(base);
        if (pattern_matcher.matches())
            return true;
        return false;
    }
}