class StereoRemote extends Remote {
    StereoRemote(String manufacturer, String model, String batteryType) {
        super(manufacturer, model, batteryType);
    }

    @Override public void turnOn() { System.out.println("Стереосистема включена"); }
    @Override public void turnOff() { System.out.println("Стереосистема выключена"); }
    @Override public void changeVolume(int delta) { System.out.println("Громкость стереосистемы: " + delta); }
    @Override public void changeChannel(int step) { System.out.println("Трек: сдвиг на " + step); }
}
