package device;

import java.util.List;

public class Tv extends Device {
    // Data
    private static final Tv INSTANCE = new Tv();
    private static final int MAX_VOLUME = 100;

    private List<String> channels = List.of("Новости", "Кино", "Спорт", "Музыка", "Мультфильмы",
            "Документальный", "Сериалы", "Наука", "Юмор", "Погода");

    private int channel = 1;
    private int volume = 20;

    // Constructors
    private Tv() {
        super("Телевизор");
    }

    // Methods
    public static Tv getInstance() {
        return INSTANCE;
    }

    public List<String> getChannels() {
        return channels;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannels(List<String> channels) {
        this.channels = List.copyOf(channels);
        this.channel = 1;
    }

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
