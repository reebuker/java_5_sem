import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

abstract class Device {
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
    public void turnOn() { on = true; }
    public void turnOff() { on = false; }
    public boolean isOn() { return on; }

    public abstract String getStatus();

    public int getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return name + " #" + id + ": " + (on ? "вкл" : "выкл") + ", " + getStatus();
    }
}

class Tv extends Device {
    // Data
    private static final Tv INSTANCE = new Tv();
    private static final int CHANNELS = 99;
    private static final int MAX_VOLUME = 100;

    private int channel = 1;
    private int volume = 20;

    // Constructors
    private Tv() { super("Телевизор"); }

    // Methods
    public static Tv getInstance() { return INSTANCE; }

    // по кругу: после 99 идет 1, перед 1 идет 99
    public void changeChannel(int step) {
        channel = Math.floorMod(channel - 1 + step, CHANNELS) + 1;
    }

    public void changeVolume(int delta) {
        volume = Math.max(0, Math.min(MAX_VOLUME, volume + delta));
    }

    @Override
    public String getStatus() { return "канал " + channel + ", громкость " + volume; }
}

class Stereo extends Device {
    // Data
    private static final Stereo INSTANCE = new Stereo();
    private static final int MAX_VOLUME = 100;

    private final List<String> tracks = List.of("Track 1", "Track 2", "Track 3");
    private int track = 0;
    private int volume = 20;

    // Constructors
    private Stereo() { super("Стереосистема"); }

    // Methods
    public static Stereo getInstance() { return INSTANCE; }

    public void changeTrack(int step) {
        track = Math.floorMod(track + step, tracks.size());
    }

    public void changeVolume(int delta) {
        volume = Math.max(0, Math.min(MAX_VOLUME, volume + delta));
    }

    @Override
    public String getStatus() { return tracks.get(track) + ", громкость " + volume; }
}

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
