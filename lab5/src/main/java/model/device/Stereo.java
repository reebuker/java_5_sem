package model.device;

import java.util.List;

public class Stereo extends Device {
    // Data
    private List<String> tracks = List.of("Утренний джаз", "Дорожный рок", "Вечерний эмбиент",
            "Лёгкий поп", "Классика");
    private int track = 1;
    private int volume = 20;

    // Constructors
    public Stereo() {
        super("Стереосистема");
    }

    // Methods
    public List<String> getTracks() {
        return tracks;
    }

    public int getTrack() {
        return track;
    }

    // пустой список не принимаем: без треков играть нечему
    public void setTracks(List<String> tracks) {
        if (tracks.isEmpty())
            return;
        this.tracks = List.copyOf(tracks);
        this.track = 1;
    }

    public void setTrack(int number) {
        track = Math.max(1, Math.min(tracks.size(), number));
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
