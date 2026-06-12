public class ShippingCalculator {
    public static final String STANDARD = "STANDARD";
    public static final String NEXT_DAY = "NEXT_DAY";

    private static final double STANDARD_COST = 10.00;
    private static final double NEXT_DAY_COST = 25.00;
    private static final double FREE_STANDARD_THRESHOLD = 50.00;

    public static double calculateShipping(String shippingOption, double rawSubtotal) {
        if (shippingOption.equalsIgnoreCase(NEXT_DAY)) {
            return NEXT_DAY_COST;
        }
        // STANDARD
        if (rawSubtotal > FREE_STANDARD_THRESHOLD) {
            return 0.00;
        }
        return STANDARD_COST;
    }
}
