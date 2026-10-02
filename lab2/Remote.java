public class Remote {
    // Data
    private static  int     nextId          = 0;
    private final   int     id;
    public  static  String  defaultGroup    = "No group";

    private String  group           = defaultGroup;
    private String  manufacturer    = "Unknown";
    private int     numberOfButtons = 1;
    private double  price           = 0;

    // Constructors
    Remote() {
        this.id = nextId++;
    }

    Remote(String manufacturer, int numberOfButtons, double price) {
        this();
        this.manufacturer = manufacturer;
        this.numberOfButtons = numberOfButtons;
        this.price = price;
    }

    // Getters
    public int getId() {
        return this.id;
    }

    public String getGroup() {
        return this.group;
    }

    public String getManufacturer() {
        return this.manufacturer;
    }

    public int getButtons() {
        return this.numberOfButtons;
    }

    public double getPrice() {
        return this.price;
    }

    // Setters
    public void setGroup(String group) {
        this.group = group;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setButtons(int number) {
        this.numberOfButtons = number;
    }

    public void setPrice(double price) {
        this.price = price;
    }

} 

