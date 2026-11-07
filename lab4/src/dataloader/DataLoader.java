package dataloader;

import remote.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataLoader {
    // Data
    private static final Pattern CLASS_LINE = Pattern.compile("^class:\\s*(.+)$");
    private static final Pattern FIELD_LINE = Pattern.compile("^(\\w+):\\s*(.*)$");

    // список: только значения в кавычках через запятую, например "News", "Movies"
    private static final Pattern LIST_VALUE = Pattern.compile("^\"[^\"]+\"(\\s*,\\s*\"[^\"]+\")*$");
    private static final Pattern LIST_ITEM = Pattern.compile("\"([^\"]+)\"");

    // у каждого типа пульта свой ключ для списка
    private static final Map<String, String> LIST_KEYS = Map.of(
            "TvRemote", "channels",
            "StereoRemote", "music",
            "SmartHomeRemote", "appliances");

    private final BufferedReader stream;

    // итоги последнего чтения
    private int found; // объектов известных типов
    private int skipped; // из них отвергнуто
    private int unknown; // объектов неизвестных типов
    private int propertiesRead; // свойства прочитаны и корректны
    private int propertiesMissing; // свойства нет или оно пустое, взято умолчание
    private int propertiesInvalid; // свойство есть, но некорректное

    // Constructors
    public DataLoader(BufferedReader stream) {
        this.stream = stream;
    }

    // Getters
    public int getFound() {
        return found;
    }

    public int getSkipped() {
        return skipped;
    }

    public int getUnknown() {
        return unknown;
    }

    public int getPropertiesRead() {
        return propertiesRead;
    }

    public int getPropertiesMissing() {
        return propertiesMissing;
    }

    public int getPropertiesInvalid() {
        return propertiesInvalid;
    }

    // Methods
    // читает поток до конца, на каждую строку "class: ..." заводит новый объект,
    // все поля до следующей такой строки относятся к нему
    public List<Remote> load() throws IOException {
        found = skipped = unknown = 0;
        propertiesRead = propertiesMissing = propertiesInvalid = 0;

        List<Remote> result = new ArrayList<>();
        String type = null;
        Map<String, String> fields = new HashMap<>();

        String line;
        while ((line = stream.readLine()) != null) {
            line = line.trim();

            Matcher classMatch = CLASS_LINE.matcher(line);
            if (classMatch.matches()) {
                addRemote(result, type, fields); // прошлый объект закончился
                type = classMatch.group(1).trim();
                fields = new HashMap<>();
                continue;
            }

            Matcher fieldMatch = FIELD_LINE.matcher(line);
            if (type != null && fieldMatch.matches()) {
                fields.put(fieldMatch.group(1), fieldMatch.group(2).trim());
            }
        }
        addRemote(result, type, fields); // последний объект
        return result;
    }

    // сначала проверяет все свойства, и только если всё в порядке, создаёт пульт:
    // так id не тратятся на отвергнутые объекты, а устройства не портятся
    private void addRemote(List<Remote> result, String type, Map<String, String> fields) {
        if (type == null)
            return;
        if (!LIST_KEYS.containsKey(type)) {
            unknown++;
            return;
        }
        found++;
        int readBefore = propertiesRead;

        String manufacturer = checkName(fields.get("manufacturer"), Remote.DEFAULT_MANUFACTURER);
        String model = checkName(fields.get("model"), Remote.DEFAULT_MODEL);
        String batteryType = checkBattery(fields.get("batteryType"));
        List<String> items = checkList(fields.get(LIST_KEYS.get(type)));

        // любое некорректное свойство или ни одного прочитанного: объект не берём
        boolean broken = manufacturer == null || model == null || batteryType == null || items == null;
        if (broken || propertiesRead == readBefore) {
            skipped++;
            return;
        }

        Remote remote = switch (type) {
            case "TvRemote" -> new TvRemote(manufacturer, model, batteryType);
            case "StereoRemote" -> new StereoRemote(manufacturer, model, batteryType);
            case "SmartHomeRemote" -> new SmartHomeRemote(manufacturer, model, batteryType);
            default -> throw new IllegalStateException("нет конструктора для " + type);
        };
        if (!items.isEmpty())
            remote.setChannels(items);
        result.add(remote);
    }

    // нет или пусто -> умолчание, слишком длинное -> null, иначе само значение
    private String checkName(String raw, String def) {
        if (raw == null || raw.isEmpty()) {
            propertiesMissing++;
            return def;
        }
        if (raw.length() > Remote.MAX_NAME_LENGTH) {
            propertiesInvalid++;
            return null;
        }
        propertiesRead++;
        return raw;
    }

    // нет или пусто -> умолчание, не AA и не AAA -> null
    private String checkBattery(String raw) {
        if (raw == null || raw.isEmpty()) {
            propertiesMissing++;
            return Remote.DEFAULT_BATTERY;
        }
        String battery = Remote.normalizeBattery(raw);
        if (battery == null) {
            propertiesInvalid++;
            return null;
        }
        propertiesRead++;
        return battery;
    }

    // нет или пусто -> пустой список (оставить прежний), без кавычек -> null
    private List<String> checkList(String raw) {
        if (raw == null || raw.isEmpty()) {
            propertiesMissing++;
            return List.of();
        }
        if (!LIST_VALUE.matcher(raw).matches()) {
            propertiesInvalid++;
            return null;
        }
        List<String> items = new ArrayList<>();
        Matcher m = LIST_ITEM.matcher(raw);
        while (m.find())
            items.add(m.group(1));
        propertiesRead++;
        return items;
    }
}
