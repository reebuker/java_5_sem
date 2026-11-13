package model.device;

abstract public class Device {
    // Data
    public static final int MAX_VOLUME = 100;

    private final String name;
    private boolean on = false;

    // Constructors
    protected Device(String name) {
        this.name = name;
    }

    // Methods
    public void turnOn() {
        on = true;
    }

    public void turnOff() {
        on = false;
    }

    public boolean isOn() {
        return on;
    }

    public abstract String getStatus();

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + ": " + (on ? "вкл" : "выкл") + ", " + getStatus();
    }
}
