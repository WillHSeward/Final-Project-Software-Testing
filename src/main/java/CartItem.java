public class CartItem {
    private String name;
    private double pricePerUnit;
    private int quantity;

    public CartItem(String name, double pricePerUnit, int quantity) {
        this.name = name;
        this.pricePerUnit = pricePerUnit;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return pricePerUnit * quantity;
    }

    @Override
    public String toString() {
        return String.format("%-20s | Qty: %-4d | Unit Price: $%-8.2f | Subtotal: $%.2f",
                name, quantity, pricePerUnit, getSubtotal());
    }
}
