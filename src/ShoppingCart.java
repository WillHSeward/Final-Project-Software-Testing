import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private List<CartItem> items = new ArrayList<>();
    private String state;
    private String shippingOption;

    public ShoppingCart(String state, String shippingOption) {
        this.state = state;
        this.shippingOption = shippingOption;
    }

    public void addItem(CartItem item) {
        items.add(item);
    }

    public List<CartItem> getItems() {
        return items;
    }

    public int getTotalItemCount() {
        int count = 0;
        for (CartItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    public double getRawSubtotal() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    public double getTotal() {
        double subtotal = getRawSubtotal();
        double tax = TaxCalculator.calculateTax(state, subtotal);
        double shipping = ShippingCalculator.calculateShipping(shippingOption, subtotal);
        return subtotal + tax + shipping;
    }

    public double getTax() {
        return TaxCalculator.calculateTax(state, getRawSubtotal());
    }

    public double getShipping() {
        return ShippingCalculator.calculateShipping(shippingOption, getRawSubtotal());
    }

    public boolean removeItem(int index) {
        if (index < 0 || index >= items.size()) {
            return false;
        }
        items.remove(index);
        return true;
    }

    public boolean editQuantity(int index, int newQuantity) {
        if (index < 0 || index >= items.size()) {
            return false;
        }
        items.get(index).setQuantity(newQuantity);
        return true;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public String getState() {
        return state;
    }

    public String getShippingOption() {
        return shippingOption;
    }
}
