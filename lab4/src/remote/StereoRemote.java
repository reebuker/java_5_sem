import java.util.List;

class StereoRemote extends Remote {
    private final Stereo stereo = Stereo.getInstance();
    private int playedCount = 0;
    private int lastShuffle = 0;

    StereoRemote(String manufacturer, String model, String batteryType) {
        super(manufacturer, model, batteryType);
        devices.add(stereo);
    }

    @Override
    protected void onTurnOn() {
        stereo.turnOn();
    }

    @Override
    protected void onTurnOff() {
        stereo.turnOff();
    }

    @Override
    protected void onChangeVolume(int delta) {
        stereo.changeVolume(delta);
    }

    @Override
    protected void onSetChannel(int number) {
        play(number);
    }

    @Override
    public List<String> getChannels() {
        return stereo.getTracks();
    }

    @Override
    public List<String> getExtraActions() {
        return List.of("следующий трек", "случайный трек");
    }

    @Override
    protected void onExtra(int index) {
        switch (index) {
            case 0 -> nextTrack();
            case 1 -> shuffle();
        }
    }

    private void shuffle() {
        lastShuffle = randomBetween(1, stereo.getTracks().size());
        play(lastShuffle);
    }

    private void play(int number) {
        stereo.setTrack(number);
        playedCount++;
    }

    private void nextTrack() {
        play(stereo.getTrack() % stereo.getTracks().size() + 1);
    }

    @Override
    public String toString() {
        return super.toString() + ", треков включено: " + playedCount
                + ", последний случайный: " + (lastShuffle == 0 ? "нет" : lastShuffle);
    }
}
