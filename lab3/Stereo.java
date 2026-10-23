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
