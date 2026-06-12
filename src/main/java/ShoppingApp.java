import java.util.List;
import java.util.Scanner;

public class ShoppingApp {

    static final double MIN_PURCHASE = 1.00;
    static final double MAX_PURCHASE = 99999.99;

    public static void main(String[] args) {
        run(new Scanner(System.in));
    }

    public static void run(Scanner scanner) {
        System.out.println("=== Welcome to the Shopping Application ===");
        System.out.println();

        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();

        String state = promptState(scanner);
        String shippingOption = promptShipping(scanner);

        ShoppingCart cart = new ShoppingCart(state, shippingOption);

        System.out.println("\nHello, " + name + "! You can now manage your shopping cart.");
        System.out.println("Shipping: " + shippingOption + " | State: " + state.toUpperCase());

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addItem(scanner, cart);
                    break;
                case "2":
                    showTotal(cart);
                    break;
                case "3":
                    showCart(cart);
                    break;
                case "4":
                    editQuantity(scanner, cart);
                    break;
                case "5":
                    removeItem(scanner, cart);
                    break;
                case "6":
                    checkout(cart);
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number from 1 to 6.");
            }
        }

    }

    static String promptState(Scanner scanner) {
        while (true) {
            System.out.print("Enter your state abbreviation (e.g. IL, CA, NY, TX): ");
            String state = scanner.nextLine().trim();
            if (!state.isEmpty()) {
                return state.toUpperCase();
            }
            System.out.println("State cannot be empty.");
        }
    }

    static String promptShipping(Scanner scanner) {
        while (true) {
            System.out.println("Select a shipping option:");
            System.out.println("  1. Standard ($10.00, free if subtotal > $50)");
            System.out.println("  2. Next Day ($25.00)");
            System.out.print("Enter choice (1 or 2): ");
            String input = scanner.nextLine().trim();
            if (input.equals("1")) {
                return ShippingCalculator.STANDARD;
            } else if (input.equals("2")) {
                return ShippingCalculator.NEXT_DAY;
            }
            System.out.println("Invalid choice. Please enter 1 or 2.");
        }
    }

    static void printMenu() {
        System.out.println();
        System.out.println("--- Cart Menu ---");
        System.out.println("1. Add item to cart");
        System.out.println("2. Get current total");
        System.out.println("3. View cart contents");
        System.out.println("4. Edit item quantity");
        System.out.println("5. Remove item from cart");
        System.out.println("6. Checkout");
        System.out.print("Select an option: ");
    }

    static void addItem(Scanner scanner, ShoppingCart cart) {
        System.out.print("Enter item name: ");
        String itemName = scanner.nextLine().trim();
        if (itemName.isEmpty()) {
            System.out.println("Item name cannot be empty.");
            return;
        }

        double price = promptPrice(scanner);
        int quantity = promptQuantity(scanner);
        double itemTotal = price * quantity;

        if (itemTotal < MIN_PURCHASE) {
            System.out.printf("Error: Item total $%.2f is below the minimum purchase amount of $%.2f.%n",
                    itemTotal, MIN_PURCHASE);
            return;
        }

        double projectedSubtotal = cart.getRawSubtotal() + itemTotal;
        if (projectedSubtotal > MAX_PURCHASE) {
            System.out.printf("Error: Adding this item would bring the subtotal to $%.2f, " +
                    "exceeding the maximum of $%.2f.%n", projectedSubtotal, MAX_PURCHASE);
            return;
        }

        cart.addItem(new CartItem(itemName, price, quantity));
        System.out.printf("'%s' added to cart. You now have %d item(s) in your cart.%n",
                itemName, cart.getTotalItemCount());
    }

    static double promptPrice(Scanner scanner) {
        while (true) {
            System.out.print("Enter unit price ($): ");
            String input = scanner.nextLine().trim();
            try {
                double price = Double.parseDouble(input);
                if (price <= 0) {
                    System.out.println("Error: Price must be greater than zero.");
                    continue;
                }
                return price;
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid price. Please enter a numeric value.");
            }
        }
    }

    static int promptQuantity(Scanner scanner) {
        while (true) {
            System.out.print("Enter quantity: ");
            String input = scanner.nextLine().trim();
            try {
                int qty = Integer.parseInt(input);
                if (qty < 1) {
                    System.out.println("Error: Quantity must be at least 1.");
                    continue;
                }
                return qty;
            } catch (NumberFormatException e) {
                System.out.println("Error: Quantity must be a whole number (non-decimal integer).");
            }
        }
    }

    static void showTotal(ShoppingCart cart) {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        double subtotal = cart.getRawSubtotal();
        double tax = cart.getTax();
        double shipping = cart.getShipping();
        double total = cart.getTotal();

        System.out.println();
        System.out.printf("  Subtotal : $%.2f%n", subtotal);
        System.out.printf("  Tax (6%%) : $%.2f%s%n", tax,
                TaxCalculator.isTaxableState(cart.getState()) ? "" : " (no tax for " + cart.getState() + ")");
        System.out.printf("  Shipping : $%.2f (%s)%n", shipping, cart.getShippingOption());
        System.out.printf("  ---------------------------%n");
        System.out.printf("  Total    : $%.2f%n", total);
    }

    static void showCart(ShoppingCart cart) {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        List<CartItem> items = cart.getItems();
        System.out.println();
        System.out.println("=== Shopping Cart ===");
        for (int i = 0; i < items.size(); i++) {
            System.out.printf("[%d] %s%n", i + 1, items.get(i));
        }
        System.out.printf("Total items in cart: %d%n", cart.getTotalItemCount());
    }

    static void editQuantity(Scanner scanner, ShoppingCart cart) {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        showCart(cart);
        System.out.print("Enter the item number to edit (or 0 to cancel): ");
        String input = scanner.nextLine().trim();
        int index;
        try {
            index = Integer.parseInt(input) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }
        if (index == -1) return;
        if (index < 0 || index >= cart.getItems().size()) {
            System.out.println("Invalid item number.");
            return;
        }

        int newQty = promptQuantity(scanner);
        CartItem item = cart.getItems().get(index);
        double projectedSubtotal = (cart.getRawSubtotal() - item.getSubtotal()) + (item.getPricePerUnit() * newQty);

        if (projectedSubtotal > MAX_PURCHASE) {
            System.out.printf("Error: New quantity would bring subtotal to $%.2f, exceeding the maximum of $%.2f.%n",
                    projectedSubtotal, MAX_PURCHASE);
            return;
        }

        cart.editQuantity(index, newQty);
        System.out.printf("Quantity for '%s' updated to %d.%n", item.getName(), newQty);
    }

    static void removeItem(Scanner scanner, ShoppingCart cart) {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        showCart(cart);
        System.out.print("Enter the item number to remove (or 0 to cancel): ");
        String input = scanner.nextLine().trim();
        int index;
        try {
            index = Integer.parseInt(input) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }
        if (index == -1) return;
        if (index < 0 || index >= cart.getItems().size()) {
            System.out.println("Invalid item number.");
            return;
        }
        String removedName = cart.getItems().get(index).getName();
        cart.removeItem(index);
        System.out.printf("'%s' has been removed from your cart.%n", removedName);
    }

    static void checkout(ShoppingCart cart) {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty. Nothing to checkout.");
            return;
        }
        showCart(cart);
        showTotal(cart);
        System.out.println();
        System.out.println("transaction completed");
    }
}
