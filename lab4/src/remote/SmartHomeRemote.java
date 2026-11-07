package remote;

import device.SmartHome;

import java.util.List;

public class SmartHomeRemote extends Remote {
    private final SmartHome home = SmartHome.getInstance();
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
    protected void onChangeVolume(int delta) {
        System.out.println("В умном доме нет громкости");
    }

    @Override
    protected void onSetChannel(int number) {
        home.toggleByNumber(number);
    }

    @Override
    public List<String> getChannels() {
        return home.getAppliances();
    }

    @Override
    public String getChannelLabel() {
        return "прибор";
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
        allOffCount++;
    }

    @Override
    public String toString() {
        return super.toString() + ", «выключить всё» нажато раз: " + allOffCount
                + ", после сценария включено: " + (lastSceneOn < 0 ? "нет" : lastSceneOn);
    }
}
