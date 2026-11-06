import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

class SmartHome extends Device {
    // Data
    private static final SmartHome INSTANCE = new SmartHome();

    private final Map<String, Boolean> appliances = new LinkedHashMap<>();

    // Constructors
    private SmartHome() {
        super("Умный дом");
        appliances.put("Свет в гостиной", false);
        appliances.put("Чайник", false);
        appliances.put("Обогреватель", false);
    }

    // Methods
    public static SmartHome getInstance() {
        return INSTANCE;
    }

    // false, если такого прибора нет
    public boolean toggle(String appliance) {
        if (!appliances.containsKey(appliance))
            return false;
        appliances.put(appliance, !appliances.get(appliance));
        return true;
    }

    // названия приборов вместе с состоянием, по порядку
    public List<String> getAppliances() {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Boolean> e : appliances.entrySet()) {
            result.add(e.getKey() + " (" + (e.getValue() ? "вкл" : "выкл") + ")");
        }
        return result;
    }

    // number от 1, как в меню
    public void toggleByNumber(int number) {
        if (number < 1 || number > appliances.size())
            return;
        toggle(new ArrayList<>(appliances.keySet()).get(number - 1));
    }

    public void allOff() {
        appliances.replaceAll((name, on) -> false);
    }

    // каждый прибор включается или выключается случайно, возвращает сколько
    // включено
    public int randomScene() {
        int on = 0;
        for (String name : appliances.keySet()) {
            boolean value = ThreadLocalRandom.current().nextBoolean();
            appliances.put(name, value);
            if (value)
                on++;
        }
        return on;
    }

    @Override
    public String getStatus() {
        return appliances.toString();
    }
}
