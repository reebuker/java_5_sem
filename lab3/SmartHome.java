class SmartHome extends Device {
    // Data
    private static final SmartHome INSTANCE = new SmartHome();

    private final Map<String, Boolean> appliances = new LinkedHashMap<>();

    // Constructors
    private SmartHome() {
        super("Умный дом");
        appliances.put("Свет в гостиной", false);
        appliances.put("Чайник", false);
        appliances.put("Замок", true);
    }

    // Methods
    public static SmartHome getInstance() { return INSTANCE; }

    // false, если такого прибора нет
    public boolean toggle(String appliance) {
        if (!appliances.containsKey(appliance)) return false;
        appliances.put(appliance, !appliances.get(appliance));
        return true;
    }

    @Override
    public String getStatus() { return appliances.toString(); }
}
