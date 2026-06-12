public class TaxCalculator {
    private static final double TAX_RATE = 0.06;

    public static double calculateTax(String state, double subtotal) {
        String s = state.trim().toUpperCase();
        if (s.equals("IL") || s.equals("CA") || s.equals("NY")) {
            return subtotal * TAX_RATE;
        }
        return 0.0;
    }

    public static boolean isTaxableState(String state) {
        String s = state.trim().toUpperCase();
        return s.equals("IL") || s.equals("CA") || s.equals("NY");
    }
}
