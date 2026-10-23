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
