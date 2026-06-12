import org.junit.jupiter.api.*;
import java.io.*;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class ShoppingAppTest {

    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    private Scanner sc(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes()));
    }

    private String out() {
        return outContent.toString();
    }

    // ── promptState ──────────────────────────────────────────────────

    @Test
    void promptState_emptyThenValid_returnsUppercase() {
        assertEquals("TX", ShoppingApp.promptState(sc("\nTX\n")));
        assertTrue(out().contains("State cannot be empty."));
        assertTrue(out().contains("Enter your state abbreviation"));
    }

    @Test
    void promptState_validImmediately() {
        assertEquals("IL", ShoppingApp.promptState(sc("IL\n")));
        assertTrue(out().contains("Enter your state abbreviation"));
    }

    // ── promptShipping ───────────────────────────────────────────────

    @Test
    void promptShipping_invalidThenStandard() {
        String result = ShoppingApp.promptShipping(sc("3\n1\n"));
        assertEquals(ShippingCalculator.STANDARD, result);
        assertTrue(out().contains("Invalid choice."));
        assertTrue(out().contains("Select a shipping option:"));
        assertTrue(out().contains("1. Standard"));
        assertTrue(out().contains("2. Next Day"));
        assertTrue(out().contains("Enter choice (1 or 2):"));
    }

    @Test
    void promptShipping_nextDay() {
        assertEquals(ShippingCalculator.NEXT_DAY, ShoppingApp.promptShipping(sc("2\n")));
        assertTrue(out().contains("Select a shipping option:"));
        assertTrue(out().contains("1. Standard"));
        assertTrue(out().contains("2. Next Day"));
    }

    // ── promptPrice ──────────────────────────────────────────────────

    @Test
    void promptPrice_nonNumericThenValid() {
        assertEquals(10.00, ShoppingApp.promptPrice(sc("abc\n10.00\n")));
        assertTrue(out().contains("Invalid price."));
        assertTrue(out().contains("Enter unit price ($):"));
    }

    @Test
    void promptPrice_zeroThenValid() {
        assertEquals(5.00, ShoppingApp.promptPrice(sc("0\n5.00\n")));
        assertTrue(out().contains("Price must be greater than zero."));
    }

    @Test
    void promptPrice_negativeThenValid() {
        assertEquals(5.00, ShoppingApp.promptPrice(sc("-1\n5.00\n")));
        assertTrue(out().contains("Price must be greater than zero."));
    }

    @Test
    void promptPrice_validImmediately() {
        assertEquals(25.00, ShoppingApp.promptPrice(sc("25.00\n")));
        assertTrue(out().contains("Enter unit price ($):"));
    }

    // ── promptQuantity ───────────────────────────────────────────────

    @Test
    void promptQuantity_nonIntegerThenValid() {
        assertEquals(2, ShoppingApp.promptQuantity(sc("1.5\n2\n")));
        assertTrue(out().contains("whole number"));
        assertTrue(out().contains("Enter quantity:"));
    }

    @Test
    void promptQuantity_zeroThenValid() {
        assertEquals(1, ShoppingApp.promptQuantity(sc("0\n1\n")));
        assertTrue(out().contains("at least 1"));
    }

    @Test
    void promptQuantity_negativeThenValid() {
        assertEquals(3, ShoppingApp.promptQuantity(sc("-5\n3\n")));
        assertTrue(out().contains("at least 1"));
    }

    @Test
    void promptQuantity_validImmediately() {
        assertEquals(5, ShoppingApp.promptQuantity(sc("5\n")));
        assertTrue(out().contains("Enter quantity:"));
    }

    // ── addItem ──────────────────────────────────────────────────────

    @Test
    void addItem_emptyName_rejectsAndLeavesCartEmpty() {
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        ShoppingApp.addItem(sc("\n"), cart);
        assertTrue(out().contains("Item name cannot be empty."));
        assertTrue(out().contains("Enter item name:"));
        assertTrue(cart.isEmpty());
    }

    @Test
    void addItem_belowMinPurchase_rejects() {
        // price 0.50 * qty 1 = 0.50, which is < 1.00 minimum
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        ShoppingApp.addItem(sc("Pen\n0.50\n1\n"), cart);
        assertTrue(out().contains("below the minimum"));
        assertTrue(out().contains("$0.50"));
        assertTrue(cart.isEmpty());
    }

    @Test
    void addItem_exactlyMinPurchase_accepted() {
        // price 1.00 * qty 1 = 1.00, which equals the minimum — must be accepted
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        ShoppingApp.addItem(sc("Item\n1.00\n1\n"), cart);
        assertFalse(cart.isEmpty());
        assertEquals(1.00, cart.getRawSubtotal(), 0.001);
    }

    @Test
    void addItem_exceedsMaxPurchase_rejects() {
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Existing", 99999.99, 1));
        ShoppingApp.addItem(sc("Extra\n1.00\n1\n"), cart);
        assertTrue(out().contains("exceeding the maximum"));
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void addItem_exactlyAtMax_accepted() {
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        ShoppingApp.addItem(sc("Big\n99999.99\n1\n"), cart);
        assertFalse(cart.isEmpty());
        assertEquals(99999.99, cart.getRawSubtotal(), 0.001);
    }

    @Test
    void addItem_valid_printsCountAndCorrectSubtotal() {
        // Asserts price * quantity (Math mutant) produces the right subtotal
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        ShoppingApp.addItem(sc("Widget\n10.00\n2\n"), cart);
        assertTrue(out().contains("added to cart"));
        assertTrue(out().contains("2 item(s)"));
        assertTrue(out().contains("Enter item name:"));
        assertEquals(20.00, cart.getRawSubtotal(), 0.001);
    }

    // ── showTotal ────────────────────────────────────────────────────

    @Test
    void showTotal_emptyCart_printsEmptyMessage() {
        ShoppingApp.showTotal(new ShoppingCart("TX", ShippingCalculator.STANDARD));
        assertTrue(out().contains("Your cart is empty."));
    }

    @Test
    void showTotal_taxableState_showsCorrectAmounts() {
        ShoppingCart cart = new ShoppingCart("IL", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Widget", 20.00, 1));
        ShoppingApp.showTotal(cart);
        assertTrue(out().contains("$20.00"));
        assertTrue(out().contains("$1.20"));
        assertTrue(out().contains("$10.00"));
        assertTrue(out().contains("$31.20"));
        assertTrue(out().contains("---------------------------"));
        assertFalse(out().contains("(no tax for"));
    }

    @Test
    void showTotal_nonTaxableState_showsNoTaxLabel() {
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Widget", 20.00, 1));
        ShoppingApp.showTotal(cart);
        assertTrue(out().contains("(no tax for TX)"));
        assertTrue(out().contains("---------------------------"));
    }

    @Test
    void showTotal_freeStandardShipping_over50() {
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Item", 60.00, 1));
        ShoppingApp.showTotal(cart);
        assertTrue(out().contains("$60.00"));
        assertTrue(out().contains("$0.00"));
    }

    // ── showCart ─────────────────────────────────────────────────────

    @Test
    void showCart_emptyCart_printsEmptyMessage() {
        ShoppingApp.showCart(new ShoppingCart("TX", ShippingCalculator.STANDARD));
        assertTrue(out().contains("Your cart is empty."));
    }

    @Test
    void showCart_withItems_printsHeaderItemsAndCount() {
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Widget", 10.00, 2));
        cart.addItem(new CartItem("Gadget", 5.00, 1));
        ShoppingApp.showCart(cart);
        assertTrue(out().contains("=== Shopping Cart ==="));
        assertTrue(out().contains("Widget"));
        assertTrue(out().contains("Gadget"));
        assertTrue(out().contains("Total items in cart: 3"));
        assertTrue(out().contains("[1]"));
        assertTrue(out().contains("[2]"));
    }

    // ── editQuantity ─────────────────────────────────────────────────

    @Test
    void editQuantity_emptyCart_printsEmptyMessage() {
        ShoppingApp.editQuantity(sc(""), new ShoppingCart("TX", ShippingCalculator.STANDARD));
        assertTrue(out().contains("Your cart is empty."));
    }

    @Test
    void editQuantity_cancel_leavesCartUnchanged() {
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Widget", 10.00, 1));
        ShoppingApp.editQuantity(sc("0\n"), cart);
        assertEquals(1, cart.getItems().get(0).getQuantity());
        assertFalse(out().contains("Invalid item number."));
        assertTrue(out().contains("Enter the item number to edit"));
    }

    @Test
    void editQuantity_nonNumericInput_printsInvalidSelection() {
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Widget", 10.00, 1));
        ShoppingApp.editQuantity(sc("abc\n"), cart);
        assertTrue(out().contains("Invalid selection."));
    }

    @Test
    void editQuantity_indexTooHigh_printsInvalidNumber() {
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Widget", 10.00, 1));
        ShoppingApp.editQuantity(sc("99\n"), cart);
        assertTrue(out().contains("Invalid item number."));
    }

    @Test
    void editQuantity_indexExactlyAtSize_printsInvalidNumber() {
        // index = parseInt("2") - 1 = 1, size = 1, so 1 >= 1 is the exact boundary
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Widget", 10.00, 1));
        ShoppingApp.editQuantity(sc("2\n"), cart);
        assertTrue(out().contains("Invalid item number."));
    }

    @Test
    void editQuantity_negativeIndex_printsInvalidNumber() {
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Widget", 10.00, 1));
        ShoppingApp.editQuantity(sc("-1\n"), cart);
        assertTrue(out().contains("Invalid item number."));
    }

    @Test
    void editQuantity_exceedsMax_rejects() {
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Widget", 10.00, 1));
        ShoppingApp.editQuantity(sc("1\n10000\n"), cart);
        assertTrue(out().contains("exceeding the maximum"));
        assertEquals(1, cart.getItems().get(0).getQuantity());
    }

    @Test
    void editQuantity_valid_updatesQuantity() {
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Widget", 10.00, 1));
        ShoppingApp.editQuantity(sc("1\n3\n"), cart);
        assertEquals(3, cart.getItems().get(0).getQuantity());
        assertTrue(out().contains("updated to 3"));
        assertTrue(out().contains("=== Shopping Cart ==="));
    }

    @Test
    void editQuantity_multiItemCart_subtractsPreviousItemCorrectly() {
        // Kills the Math subtraction mutant: projected = (rawSubtotal - oldSubtotal) + newSubtotal
        // If wrongly added: (60000 + 30000) + 60000 = 150000 > MAX → rejected (wrong)
        // Correctly subtracted: (60000 - 30000) + 60000 = 90000 < MAX → accepted
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("A", 30000.00, 1));
        cart.addItem(new CartItem("B", 30000.00, 1));
        ShoppingApp.editQuantity(sc("1\n2\n"), cart);
        assertEquals(2, cart.getItems().get(0).getQuantity());
        assertFalse(out().contains("exceeding the maximum"));
    }

    // ── removeItem ───────────────────────────────────────────────────

    @Test
    void removeItem_emptyCart_printsEmptyMessage() {
        ShoppingApp.removeItem(sc(""), new ShoppingCart("TX", ShippingCalculator.STANDARD));
        assertTrue(out().contains("Your cart is empty."));
    }

    @Test
    void removeItem_cancel_leavesCartUnchanged() {
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Widget", 10.00, 1));
        ShoppingApp.removeItem(sc("0\n"), cart);
        assertEquals(1, cart.getItems().size());
        assertTrue(out().contains("Enter the item number to remove"));
    }

    @Test
    void removeItem_nonNumericInput_printsInvalidSelection() {
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Widget", 10.00, 1));
        ShoppingApp.removeItem(sc("abc\n"), cart);
        assertTrue(out().contains("Invalid selection."));
    }

    @Test
    void removeItem_indexTooHigh_printsInvalidNumber() {
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Widget", 10.00, 1));
        ShoppingApp.removeItem(sc("99\n"), cart);
        assertTrue(out().contains("Invalid item number."));
    }

    @Test
    void removeItem_indexExactlyAtSize_printsInvalidNumber() {
        // index = parseInt("2") - 1 = 1, size = 1, so 1 >= 1 is the exact boundary
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Widget", 10.00, 1));
        ShoppingApp.removeItem(sc("2\n"), cart);
        assertTrue(out().contains("Invalid item number."));
    }

    @Test
    void removeItem_negativeIndex_printsInvalidNumber() {
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Widget", 10.00, 1));
        ShoppingApp.removeItem(sc("-1\n"), cart);
        assertTrue(out().contains("Invalid item number."));
    }

    @Test
    void removeItem_valid_removesItem() {
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Widget", 10.00, 1));
        ShoppingApp.removeItem(sc("1\n"), cart);
        assertTrue(cart.isEmpty());
        assertTrue(out().contains("has been removed"));
        assertTrue(out().contains("Enter the item number to remove"));
        assertTrue(out().contains("=== Shopping Cart ==="));
    }

    @Test
    void addItem_halfDollarQty2_exactlyAtMin_accepted() {
        // price=0.50 * qty=2 = 1.00 == MIN_PURCHASE → accepted
        // With division mutant: 0.50/2=0.25 < 1.00 → rejected → kills Math mutant on line 110
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        ShoppingApp.addItem(sc("Pen\n0.50\n2\n"), cart);
        assertFalse(cart.isEmpty());
        assertEquals(1.00, cart.getRawSubtotal(), 0.001);
    }

    @Test
    void editQuantity_projectedSubtotalExactlyAtMax_accepted() {
        // Cart has one item at 99999.98; edit qty to 1 → projected = 99999.98 which is < MAX (accepted)
        // The ConditionalsBoundary mutant changes > to >= so 99999.98 >= 99999.99 is false → still accepted (not killed by this)
        // Better: item price=99999.99, qty=1 in cart; edit qty to 1 → projected=99999.99 which equals MAX
        // With > : 99999.99 > 99999.99 is false → accepted (correct)
        // With >= : 99999.99 >= 99999.99 is true → rejected (wrong) → kills boundary mutant
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Big", 99999.99, 1));
        ShoppingApp.editQuantity(sc("1\n1\n"), cart);
        assertEquals(1, cart.getItems().get(0).getQuantity());
        assertFalse(out().contains("exceeding the maximum"));
    }

    // ── checkout ─────────────────────────────────────────────────────

    @Test
    void checkout_emptyCart_printsNothingToCheckout() {
        ShoppingApp.checkout(new ShoppingCart("TX", ShippingCalculator.STANDARD));
        assertTrue(out().contains("Your cart is empty. Nothing to checkout."));
        assertFalse(out().contains("transaction completed"));
    }

    @Test
    void checkout_withItems_printsTransactionCompleted() {
        ShoppingCart cart = new ShoppingCart("IL", ShippingCalculator.NEXT_DAY);
        cart.addItem(new CartItem("Widget", 30.00, 1));
        ShoppingApp.checkout(cart);
        assertTrue(out().contains("transaction completed"));
        assertTrue(out().contains("=== Shopping Cart ==="));
        assertTrue(out().contains("---------------------------"));
        // blank line before "transaction completed" (kills VoidMethodCall on line 264)
        assertTrue(out().contains(System.lineSeparator() + System.lineSeparator() + "transaction completed"));
    }

    @Test
    void showTotal_outputHasBlankLineBeforeSubtotal() {
        // Kills VoidMethodCall mutant for blank println on line 174
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Item", 20.00, 1));
        ShoppingApp.showTotal(cart);
        assertTrue(out().contains("\n  Subtotal"));
    }

    @Test
    void showCart_outputHasBlankLineBeforeHeader() {
        // Kills VoidMethodCall mutant for blank println on line 189
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Item", 20.00, 1));
        ShoppingApp.showCart(cart);
        assertTrue(out().contains("\n=== Shopping Cart ==="));
    }

    @Test
    void run_outputHasBlankLineAfterWelcome() {
        // Kills VoidMethodCall mutant for blank println on line 15
        // The blank println produces an empty line between welcome and name prompt
        System.setIn(new ByteArrayInputStream("Alice\nTX\n1\n6\n".getBytes()));
        ShoppingApp.main(new String[]{});
        String o = out();
        int welcomeIdx = o.indexOf("=== Welcome to the Shopping Application ===");
        int nameIdx = o.indexOf("Enter your name:");
        // There must be at least one blank line (2 newlines) between welcome and name prompt
        String between = o.substring(welcomeIdx, nameIdx);
        assertTrue(between.contains(System.lineSeparator() + System.lineSeparator())
                || between.chars().filter(c -> c == '\n').count() >= 2);
    }

    // ── run (integration) ────────────────────────────────────────────

    @Test
    void run_invalidMenuOption_thenCheckoutEmpty() {
        ShoppingApp.run(sc("John\nTX\n1\nX\n6\n"));
        assertTrue(out().contains("Invalid option."));
        assertTrue(out().contains("Your cart is empty. Nothing to checkout."));
        assertTrue(out().contains("--- Cart Menu ---"));
        assertTrue(out().contains("1. Add item to cart"));
        assertTrue(out().contains("Select an option:"));
    }

    @Test
    void run_allMenuOptions_endWithTransactionCompleted() {
        // Menu selections: 1=addItem, 2=showTotal, 3=showCart, 4=editQty, 5=removeItem(2nd item), 6=checkout
        String input = "John\nIL\n1\n"
                + "1\nWidget\n10.00\n1\n"
                + "2\n"
                + "3\n"
                + "4\n1\n2\n"
                + "1\nGadget\n5.00\n1\n"
                + "5\n2\n"
                + "6\n";
        ShoppingApp.run(sc(input));
        assertTrue(out().contains("transaction completed"));
        assertTrue(out().contains("Hello, John!"));
        assertTrue(out().contains("Shipping: STANDARD"));
        assertTrue(out().contains("Enter your name:"));
        assertTrue(out().contains("--- Cart Menu ---"));
        // Assert each switch case was actually executed (kills NegateConditionals + VoidMethodCall mutants)
        // "Subtotal :" (with space) only appears in showTotal's printf, NOT in CartItem.toString()
        // Must appear at least twice: once from menu "2" and once from checkout's showTotal call
        assertTrue(out().split("Subtotal :", -1).length - 1 >= 2); // case "2" → showTotal called
        assertTrue(out().contains("=== Shopping Cart ===")); // case "3" → showCart called
        assertTrue(out().contains("updated to 2"));   // case "4" → editQuantity called
        assertTrue(out().contains("has been removed")); // case "5" → removeItem called
        // Assert printMenu output lines are present (kills VoidMethodCall on lines 92-96)
        assertTrue(out().contains("1. Add item to cart"));
        assertTrue(out().contains("2. Get current total"));
        assertTrue(out().contains("3. View cart contents"));
        assertTrue(out().contains("4. Edit item quantity"));
        assertTrue(out().contains("5. Remove item from cart"));
        assertTrue(out().contains("6. Checkout"));
        // No negated case should fall to default (kills NegateConditionals x2 on line 33)
        assertFalse(out().contains("Invalid option."));
        // Blank line exists before "--- Cart Menu ---" (kills VoidMethodCall on line 89)
        assertTrue(out().contains(System.lineSeparator() + System.lineSeparator() + "--- Cart Menu ---"));
    }

    @Test
    void run_menuOption2_showsTotal_withoutCheckoutCovering() {
        // Checkout with empty cart does NOT call showTotal.
        // CartItem.toString() prints "Subtotal: $X" (no space before colon).
        // showTotal prints "  Subtotal : $X" (space before colon) — unique to showTotal.
        // Kills VoidMethodCall (line 38) and NegateConditionals (line 33) for case "2".
        String input = "Ann\nTX\n1\n"
                + "1\nBook\n10.00\n1\n"  // add item
                + "2\n"                   // show total → "Subtotal :"
                + "5\n1\n"               // remove item → cart now empty
                + "6\n";                  // checkout empty cart → no showTotal
        ShoppingApp.run(sc(input));
        assertTrue(out().contains("Subtotal :"));
    }

    @Test
    void run_menuOption3_showsCart_withoutCheckoutCovering() {
        // Checkout with empty cart does NOT call showCart, so we can count showCart calls.
        // editQuantity also calls showCart, so we count: menu "3" adds one more occurrence.
        // Kills VoidMethodCall (line 41) and NegateConditionals (line 33) for case "3".
        String input = "Bob\nTX\n1\n"
                + "1\nPen\n10.00\n1\n"   // add item
                + "3\n"                    // show cart → "=== Shopping Cart ===" (1st)
                + "5\n1\n"                // remove item → showCart inside removeItem (2nd), then cart empty
                + "6\n";                   // checkout empty cart → no showCart
        ShoppingApp.run(sc(input));
        int count = out().split("=== Shopping Cart ===", -1).length - 1;
        assertTrue(count >= 2);
    }

    // ── main ─────────────────────────────────────────────────────────

    @Test
    void main_delegatesToRun() {
        System.setIn(new ByteArrayInputStream("Tester\nTX\n1\n6\n".getBytes()));
        ShoppingApp.main(new String[]{});
        assertTrue(out().contains("Welcome to the Shopping Application"));
        assertTrue(out().contains("Enter your name:"));
        assertTrue(out().contains("--- Cart Menu ---"));
    }
}
