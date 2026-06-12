import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartTest {

    private ShoppingCart txStandard() {
        return new ShoppingCart("TX", ShippingCalculator.STANDARD);
    }

    // --- constructor / getters ---

    @Test
    void constructor_setsState() {
        assertEquals("IL", new ShoppingCart("IL", ShippingCalculator.NEXT_DAY).getState());
    }

    @Test
    void constructor_setsShippingOption() {
        assertEquals(ShippingCalculator.NEXT_DAY,
                new ShoppingCart("IL", ShippingCalculator.NEXT_DAY).getShippingOption());
    }

    // --- isEmpty ---

    @Test
    void isEmpty_trueWhenNew() {
        assertTrue(txStandard().isEmpty());
    }

    @Test
    void isEmpty_falseAfterAddItem() {
        ShoppingCart cart = txStandard();
        cart.addItem(new CartItem("Widget", 10.00, 1));
        assertFalse(cart.isEmpty());
    }

    // --- addItem / getItems ---

    @Test
    void getItems_returnsAllAddedItems() {
        ShoppingCart cart = txStandard();
        cart.addItem(new CartItem("A", 5.00, 1));
        cart.addItem(new CartItem("B", 10.00, 2));
        assertEquals(2, cart.getItems().size());
    }

    // --- getTotalItemCount ---

    @Test
    void getTotalItemCount_sumsQuantities() {
        ShoppingCart cart = txStandard();
        cart.addItem(new CartItem("A", 5.00, 3));
        cart.addItem(new CartItem("B", 10.00, 2));
        assertEquals(5, cart.getTotalItemCount());
    }

    @Test
    void getTotalItemCount_emptyCart_zero() {
        assertEquals(0, txStandard().getTotalItemCount());
    }

    // --- getRawSubtotal ---

    @Test
    void getRawSubtotal_sumOfItemSubtotals() {
        ShoppingCart cart = txStandard();
        cart.addItem(new CartItem("A", 10.00, 2)); // 20
        cart.addItem(new CartItem("B", 5.00, 3));  // 15
        assertEquals(35.00, cart.getRawSubtotal());
    }

    @Test
    void getRawSubtotal_emptyCart_zero() {
        assertEquals(0.00, txStandard().getRawSubtotal());
    }

    // --- getTax ---

    @Test
    void getTax_IL_sixPercent() {
        ShoppingCart cart = new ShoppingCart("IL", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Item", 100.00, 1));
        assertEquals(6.00, cart.getTax(), 0.001);
    }

    @Test
    void getTax_CA_sixPercent() {
        ShoppingCart cart = new ShoppingCart("CA", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Item", 100.00, 1));
        assertEquals(6.00, cart.getTax(), 0.001);
    }

    @Test
    void getTax_NY_sixPercent() {
        ShoppingCart cart = new ShoppingCart("NY", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Item", 100.00, 1));
        assertEquals(6.00, cart.getTax(), 0.001);
    }

    @Test
    void getTax_nonTaxableState_zero() {
        ShoppingCart cart = txStandard();
        cart.addItem(new CartItem("Item", 100.00, 1));
        assertEquals(0.00, cart.getTax());
    }

    // --- getShipping ---

    @Test
    void getShipping_standard_under50_costs10() {
        ShoppingCart cart = txStandard();
        cart.addItem(new CartItem("Item", 30.00, 1));
        assertEquals(10.00, cart.getShipping());
    }

    @Test
    void getShipping_standard_exactly50_costs10() {
        ShoppingCart cart = txStandard();
        cart.addItem(new CartItem("Item", 50.00, 1));
        assertEquals(10.00, cart.getShipping());
    }

    @Test
    void getShipping_standard_over50_free() {
        ShoppingCart cart = txStandard();
        cart.addItem(new CartItem("Item", 60.00, 1));
        assertEquals(0.00, cart.getShipping());
    }

    @Test
    void getShipping_nextDay_alwaysCosts25() {
        ShoppingCart cart = new ShoppingCart("TX", ShippingCalculator.NEXT_DAY);
        cart.addItem(new CartItem("Item", 100.00, 1));
        assertEquals(25.00, cart.getShipping());
    }

    // --- getTotal ---

    @Test
    void getTotal_subtotalPlusTaxPlusShipping() {
        ShoppingCart cart = new ShoppingCart("IL", ShippingCalculator.STANDARD);
        cart.addItem(new CartItem("Item", 20.00, 1));
        // subtotal=20, tax=1.20 (6%), shipping=10 (under 50)
        assertEquals(31.20, cart.getTotal(), 0.001);
    }

    @Test
    void getTotal_noTax_freeShipping() {
        ShoppingCart cart = txStandard();
        cart.addItem(new CartItem("Item", 60.00, 1));
        // subtotal=60, tax=0, shipping=0 (over 50)
        assertEquals(60.00, cart.getTotal(), 0.001);
    }

    // --- removeItem ---

    @Test
    void removeItem_validIndex_removesAndReturnsTrue() {
        ShoppingCart cart = txStandard();
        cart.addItem(new CartItem("Widget", 10.00, 1));
        assertTrue(cart.removeItem(0));
        assertTrue(cart.isEmpty());
    }

    @Test
    void removeItem_negativeIndex_returnsFalse() {
        ShoppingCart cart = txStandard();
        cart.addItem(new CartItem("Widget", 10.00, 1));
        assertFalse(cart.removeItem(-1));
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void removeItem_indexTooHigh_returnsFalse() {
        ShoppingCart cart = txStandard();
        cart.addItem(new CartItem("Widget", 10.00, 1));
        assertFalse(cart.removeItem(5));
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void removeItem_indexExactlyAtSize_returnsFalse() {
        // index == size is the exact boundary: kills >= mutated to >
        ShoppingCart cart = txStandard();
        cart.addItem(new CartItem("Widget", 10.00, 1));
        assertFalse(cart.removeItem(1)); // size=1, index=1: 1>=1 true
        assertEquals(1, cart.getItems().size());
    }

    // --- editQuantity ---

    @Test
    void editQuantity_validIndex_updatesAndReturnsTrue() {
        ShoppingCart cart = txStandard();
        cart.addItem(new CartItem("Widget", 10.00, 1));
        assertTrue(cart.editQuantity(0, 5));
        assertEquals(5, cart.getItems().get(0).getQuantity());
    }

    @Test
    void editQuantity_negativeIndex_returnsFalse() {
        ShoppingCart cart = txStandard();
        cart.addItem(new CartItem("Widget", 10.00, 1));
        assertFalse(cart.editQuantity(-1, 5));
        assertEquals(1, cart.getItems().get(0).getQuantity());
    }

    @Test
    void editQuantity_indexTooHigh_returnsFalse() {
        ShoppingCart cart = txStandard();
        cart.addItem(new CartItem("Widget", 10.00, 1));
        assertFalse(cart.editQuantity(99, 5));
        assertEquals(1, cart.getItems().get(0).getQuantity());
    }

    @Test
    void editQuantity_indexExactlyAtSize_returnsFalse() {
        // index == size is the exact boundary: kills >= mutated to >
        ShoppingCart cart = txStandard();
        cart.addItem(new CartItem("Widget", 10.00, 1));
        assertFalse(cart.editQuantity(1, 5)); // size=1, index=1: 1>=1 true
        assertEquals(1, cart.getItems().get(0).getQuantity());
    }
}
