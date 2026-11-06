package device;

import java.util.List;

public class Stereo extends Device {
    // Data
    private static final Stereo INSTANCE = new Stereo();
    private static final int MAX_VOLUME = 100;

    private final List<String> tracks = List.of("Утренний джаз", "Дорожный рок", "Вечерний эмбиент",
            "Лёгкий поп", "Классика");
    private int track = 1;
    private int volume = 20;

    // Constructors
    private Stereo() {
        super("Стереосистема");
    }

    // Methods
    public static Stereo getInstance() {
        return INSTANCE;
    }

    public List<String> getTracks() {
        return tracks;
    }

    public int getTrack() {
        return track;
    }

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
