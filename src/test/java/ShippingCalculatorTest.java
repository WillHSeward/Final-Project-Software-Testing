import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShippingCalculatorTest {

    @Test
    void standard_subtotalBelowThreshold_costs10() {
        assertEquals(10.00, ShippingCalculator.calculateShipping(ShippingCalculator.STANDARD, 30.00));
    }

    @Test
    void standard_subtotalExactlyAtThreshold_costs10() {
        // Must be OVER $50 for free; $50.00 exactly is NOT over the threshold
        assertEquals(10.00, ShippingCalculator.calculateShipping(ShippingCalculator.STANDARD, 50.00));
    }

    @Test
    void standard_subtotalJustAboveThreshold_free() {
        assertEquals(0.00, ShippingCalculator.calculateShipping(ShippingCalculator.STANDARD, 50.01));
    }

    @Test
    void standard_subtotalWellAboveThreshold_free() {
        assertEquals(0.00, ShippingCalculator.calculateShipping(ShippingCalculator.STANDARD, 500.00));
    }

    @Test
    void nextDay_lowSubtotal_costs25() {
        assertEquals(25.00, ShippingCalculator.calculateShipping(ShippingCalculator.NEXT_DAY, 10.00));
    }

    @Test
    void nextDay_highSubtotal_stillCosts25() {
        assertEquals(25.00, ShippingCalculator.calculateShipping(ShippingCalculator.NEXT_DAY, 1000.00));
    }

    @Test
    void nextDay_caseInsensitive() {
        assertEquals(25.00, ShippingCalculator.calculateShipping("next_day", 10.00));
    }

    @Test
    void constants_haveCorrectValues() {
        assertEquals("STANDARD", ShippingCalculator.STANDARD);
        assertEquals("NEXT_DAY", ShippingCalculator.NEXT_DAY);
    }
}
