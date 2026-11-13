package model.remote;

import model.device.Stereo;

import java.util.List;

public class StereoRemote extends Remote {
    private final Stereo stereo = new Stereo();
    private int playedCount = 0;
    private int lastShuffle = 0;

    public StereoRemote(String manufacturer, String model, String batteryType) {
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
    protected boolean onChangeVolume(int delta) {
        stereo.changeVolume(delta);
        return true;
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

    @Override
    public void setChannels(List<String> items) {
        stereo.setTracks(items);
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
    public RemoteState state() {
        return new RemoteState(getBatteryLevel(), isOn(), stereo.getVolume(), stereo.getTrack(), null,
                playedCount, lastShuffle);
    }

    @Override
    public void restore(RemoteState state) {
        restoreCommon(state);
        if (state.volume() != null)
            stereo.setVolume(state.volume());
        if (state.position() != null)
            stereo.setTrack(state.position());
        if (state.extra1() != null)
            playedCount = state.extra1();
        if (state.extra2() != null)
            lastShuffle = state.extra2();
    }
}
