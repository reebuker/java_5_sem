package model.remote;

import model.device.SmartHome;

import java.util.List;

public class SmartHomeRemote extends Remote {
    private final SmartHome home = new SmartHome();
    private int allOffCount = 0;
    private int lastSceneOn = -1; // -1 - сценарий ещё не запускали

    public SmartHomeRemote(String manufacturer, String model, String batteryType) {
        super(manufacturer, model, batteryType);
        devices.add(home);
    }

    @Override
    protected void onTurnOn() {
        home.turnOn();
    }

    @Override
    protected void onTurnOff() {
        home.turnOff();
    }

    @Override
    protected boolean onChangeVolume(int delta) {
        return false; // в умном доме нет громкости
    }

    @Override
    protected void onSetChannel(int number) {
        home.toggleByNumber(number);
    }

    // названия приборов, их состояние отдаёт state()
    @Override
    public List<String> getChannels() {
        return List.copyOf(home.getAppliances().keySet());
    }

    @Override
    public List<String> getExtraActions() {
        return List.of("выключить все приборы", "случайный сценарий");
    }

    @Override
    protected void onExtra(int index) {
        switch (index) {
            case 0 -> allOff();
            case 1 -> lastSceneOn = home.randomScene();
        }
    }

    @Override
    public void setChannels(List<String> items) {
        home.setAppliances(items);
    }

    private void allOff() {
        home.allOff();
        allOffCount = plusOne(allOffCount);
    }

    @Override
    public RemoteState state() {
        return new RemoteState(getBatteryLevel(), isOn(), null, null, home.getAppliancesState(),
                allOffCount, lastSceneOn);
    }

    @Override
    public void restore(RemoteState state) {
        restoreCommon(state);
        if (state.appliances() != null)
            home.setAppliancesState(state.appliances());
        if (state.extra1() != null)
            allOffCount = clamp(state.extra1(), 0, MAX_COUNTER);
        if (state.extra2() != null)
            lastSceneOn = clamp(state.extra2(), -1, MAX_COUNTER);
    }
}
