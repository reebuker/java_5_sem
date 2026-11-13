package model.device;

abstract public class Device {
    // Data
    private static int nextId = 0;
    private final int id;
    private final String name;
    private boolean on = false;

    // Constructors
    protected Device(String name) {
        this.id = nextId++;
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

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " #" + id + ": " + (on ? "вкл" : "выкл") + ", " + getStatus();
    }
}
