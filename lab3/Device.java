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
    private static final int CHANNELS = 10;
    private static final int MAX_VOLUME = 100;

    private final List<String> channels = List.of("Новости", "Кино", "Спорт", "Музыка", "Мультфильмы",
            "Документальный", "Сериалы", "Наука", "Юмор", "Погода");

    private int channel = 1;
    private int volume = 20;

    // Constructors
    private Tv() { super("Телевизор"); }

    // Methods
    public static Tv getInstance() { return INSTANCE; }
    public List<String> getChannels() { return channels; }

    public void setChannel(int number) {
        channel = Math.max(1, Math.min(channels.size(), number));
    }

    public void changeVolume(int delta) {
        volume = Math.max(0, Math.min(MAX_VOLUME, volume + delta));
    }

    @Override
    public String getStatus() {
        return "канал " + channel + " (" + channels.get(channel - 1) + "), громкость " + volume;
    }
}

class Stereo extends Device {
    // Data
    private static final Stereo INSTANCE = new Stereo();
    private static final int MAX_VOLUME = 100;
    private static final int TRACKS = 10;

    private final List<String> tracks = List.of("Утренний джаз", "Дорожный рок", "Вечерний эмбиент",
            "Лёгкий поп", "Классика");
    private int track = 1;
    private int volume = 20;

    // Constructors
    private Stereo() { super("Стереосистема"); }

    // Methods
    public static Stereo getInstance() { return INSTANCE; }
    public List<String> getTracks() { return tracks; }

    public void setTrack(int number) {
        track = Math.max(1, Math.min(tracks.size(), number));
    }

    public void changeVolume(int delta) {
        volume = Math.max(0, Math.min(MAX_VOLUME, volume + delta));
    }

    @Override
    public String getStatus() {
        return "трек " + track + " (" + tracks.get(track - 1) + "), громкость " + volume;
    }
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
