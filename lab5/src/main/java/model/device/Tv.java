package model.device;

import java.util.List;

public class Tv extends Device {
    // Data
    private List<String> channels = List.of("Новости", "Кино", "Спорт", "Музыка", "Мультфильмы",
            "Документальный", "Сериалы", "Наука", "Юмор", "Погода");

    private int channel = 1;
    private int volume = 20;

    // Constructors
    public Tv() {
        super("Телевизор");
    }

    // Methods
    public List<String> getChannels() {
        return channels;
    }

    public int getChannel() {
        return channel;
    }

    // пустой список не принимаем: без каналов переключать нечего
    public void setChannels(List<String> channels) {
        if (channels.isEmpty())
            return;
        this.channels = List.copyOf(channels);
        this.channel = 1;
    }

    public void setChannel(int number) {
        channel = Math.max(1, Math.min(channels.size(), number));
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(MAX_VOLUME, volume));
    }

    public void changeVolume(int delta) {
        setVolume((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, (long) volume + delta)));
    }
}
