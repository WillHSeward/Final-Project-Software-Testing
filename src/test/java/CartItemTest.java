import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CartItemTest {

    @Test
    void constructor_setsAllFields() {
        CartItem item = new CartItem("Widget", 9.99, 3);
        assertEquals("Widget", item.getName());
        assertEquals(9.99, item.getPricePerUnit());
        assertEquals(3, item.getQuantity());
    }

    @Test
    void getSubtotal_returnsPriceTimesQuantity() {
        CartItem item = new CartItem("Test", 5.00, 4);
        assertEquals(20.00, item.getSubtotal());
    }

    @Test
    void setQuantity_updatesQuantity() {
        CartItem item = new CartItem("Test", 10.00, 1);
        item.setQuantity(5);
        assertEquals(5, item.getQuantity());
    }

    @Test
    void setQuantity_updatesSubtotal() {
        CartItem item = new CartItem("Test", 10.00, 1);
        item.setQuantity(5);
        assertEquals(50.00, item.getSubtotal());
    }

    @Test
    void toString_containsName() {
        CartItem item = new CartItem("Widget", 10.00, 3);
        assertTrue(item.toString().contains("Widget"));
    }

    @Test
    void toString_containsQuantity() {
        CartItem item = new CartItem("Widget", 10.00, 3);
        assertTrue(item.toString().contains("3"));
    }

    @Test
    void toString_containsUnitPrice() {
        CartItem item = new CartItem("Widget", 10.00, 3);
        assertTrue(item.toString().contains("10.00"));
    }

    @Test
    void toString_containsSubtotal() {
        CartItem item = new CartItem("Widget", 10.00, 3);
        assertTrue(item.toString().contains("30.00"));
    }
}
