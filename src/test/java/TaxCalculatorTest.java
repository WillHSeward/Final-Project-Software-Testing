import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaxCalculatorTest {

    @Test
    void constructor_coversDefaultConstructor() {
        assertNotNull(new TaxCalculator());
    }

    @Test
    void calculateTax_IL_sixPercent() {
        assertEquals(6.00, TaxCalculator.calculateTax("IL", 100.00), 0.001);
    }

    @Test
    void calculateTax_CA_sixPercent() {
        assertEquals(6.00, TaxCalculator.calculateTax("CA", 100.00), 0.001);
    }

    @Test
    void calculateTax_NY_sixPercent() {
        assertEquals(6.00, TaxCalculator.calculateTax("NY", 100.00), 0.001);
    }

    @Test
    void calculateTax_TX_noTax() {
        assertEquals(0.00, TaxCalculator.calculateTax("TX", 100.00));
    }

    @Test
    void calculateTax_otherState_noTax() {
        assertEquals(0.00, TaxCalculator.calculateTax("FL", 100.00));
    }

    @Test
    void calculateTax_lowercase_isTreatedAsTaxable() {
        assertEquals(6.00, TaxCalculator.calculateTax("il", 100.00), 0.001);
        assertEquals(6.00, TaxCalculator.calculateTax("ca", 100.00), 0.001);
        assertEquals(6.00, TaxCalculator.calculateTax("ny", 100.00), 0.001);
    }

    @Test
    void calculateTax_withSpaces_trimmedCorrectly() {
        assertEquals(6.00, TaxCalculator.calculateTax(" IL ", 100.00), 0.001);
    }

    @Test
    void calculateTax_rateIsExactlySixPercent() {
        assertEquals(3.00, TaxCalculator.calculateTax("IL", 50.00), 0.001);
    }

    @Test
    void isTaxableState_true_forIL() {
        assertTrue(TaxCalculator.isTaxableState("IL"));
    }

    @Test
    void isTaxableState_true_forCA() {
        assertTrue(TaxCalculator.isTaxableState("CA"));
    }

    @Test
    void isTaxableState_true_forNY() {
        assertTrue(TaxCalculator.isTaxableState("NY"));
    }

    @Test
    void isTaxableState_false_forTX() {
        assertFalse(TaxCalculator.isTaxableState("TX"));
    }

    @Test
    void isTaxableState_false_forOtherState() {
        assertFalse(TaxCalculator.isTaxableState("WA"));
    }

    @Test
    void isTaxableState_caseInsensitive_lowercase() {
        assertTrue(TaxCalculator.isTaxableState("il"));
        assertFalse(TaxCalculator.isTaxableState("tx"));
    }
}
