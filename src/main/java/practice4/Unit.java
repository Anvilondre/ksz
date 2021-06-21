package practice4;

/** Class to represent goods */
public class Unit {
    private Integer sku; // Unique product id
    private String name;
    private double price; // Well decimal would work better here but ok
    private double amount;


    /* Stuff generated by IDEA */

    public Unit(int sku, String name, double price, double amount) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.amount = amount;
    }


    public Integer getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public double getAmount() {
        return amount;
    }

    public void setSku(Integer sku) {
        this.sku = sku;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Unit{" +
                "sku=" + sku +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", isWeighted=" + amount +
                '}';
    }
}
