public class Remote {
    // Data
    private static  int nextId  = 0;
    private final   int id;

    private String  manufacturer    = "Unknown";
    private int     numberOfButtons = 1;
    private int     price           = 0;

    // Constructors
    Remote() {
        this.id = nextId++;
    }

    Remote(String manufacturer, int numberOfButtons, int price) {
        this();
        this.manufacturer = manufacturer;
        this.numberOfButtons = numberOfButtons;
        this.price = price;
    }

    // Functions
    public int getId() {
        return this.id;
    }

    public String getManufacturer() {
        return this.manufacturer;
    }

    public int getNumberOfButtons() {
        return this.numberOfButtons;
    }

    public int getPrice() {
        return this.price;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setNumberOfButtons(int number) {
        this.numberOfButtons = number;
    }

    public void setPrice(int price) {
        this.price = price;
    }

} 

