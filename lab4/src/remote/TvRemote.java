import java.util.List;

class TvRemote extends Remote {
    private final Tv tv = Tv.getInstance();
    private int previousChannel = 0; // 0 - ещё не переключали
    private int lastRandomChannel = 0; // 0 - случайный ещё не выбирали

    TvRemote(String manufacturer, String model, String batteryType) {
        super(manufacturer, model, batteryType);
        devices.add(tv);
    }

    @Override
    protected void onTurnOn() {
        tv.turnOn();
    }

    @Override
    protected void onTurnOff() {
        tv.turnOff();
    }

    @Override
    protected void onChangeVolume(int delta) {
        tv.changeVolume(delta);
    }

    @Override
    protected void onSetChannel(int number) {
        switchTo(number);
    }

    @Override
    public List<String> getChannels() {
        return tv.getChannels();
    }

    @Override
    public List<String> getExtraActions() {
        return List.of("вернуться на прошлый канал", "случайный канал");
    }

    @Override
    protected void onExtra(int index) {
        switch (index) {
            case 0 -> backToPrevious();
            case 1 -> randomChannel();
        }
    }

    private void switchTo(int number) {
        previousChannel = tv.getChannel();
        tv.setChannel(number);
    }

    private void backToPrevious() {
        if (previousChannel == 0)
            return;
        switchTo(previousChannel);
    }

    private void randomChannel() {
        lastRandomChannel = randomBetween(1, tv.getChannels().size());
        switchTo(lastRandomChannel);
    }

    @Override
    public String toString() {
        return super.toString() + ", прошлый канал: " + (previousChannel == 0 ? "нет" : previousChannel)
                + ", последний случайный: " + (lastRandomChannel == 0 ? "нет" : lastRandomChannel);
    }
}
