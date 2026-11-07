package dataloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import remote.*;

public class DataLoader {
    // Data
    private static final Pattern CLASS_LINE = Pattern.compile("^class:\\s*(.+)$");
    private static final Pattern FIELD_LINE = Pattern.compile("^(\\w+):\\s*(.*)$");
    private static final Pattern LIST_VALUE = Pattern.compile("^\"[^\"]+\"(\\s*,\\s*\"[^\"]+\")*$");
    private static final Pattern LIST_ITEM = Pattern.compile("\"([^\"]+)\"");

    // у каждого типа пульта свой ключ для списка
    private static final Map<String, String> LIST_KEYS = Map.of(
            "TvRemote", "channels",
            "StereoRemote", "music",
            "SmartHomeRemote", "appliances");

    private final BufferedReader stream;

    // Constructors
    public DataLoader(BufferedReader stream) {
        this.stream = stream;
    }

    // Methods
    // читает поток до конца, на каждую строку "класс: ..." заводит новый объект,
    // все поля до следующей такой строки относятся к нему
    public List<Remote> load() throws IOException {
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

    // создаёт пульт по типу и добавляет в список, неизвестный тип пропускает
    private void addRemote(List<Remote> result, String type, Map<String, String> fields) {
        if (type == null)
            return;

        String manufacturer = fields.get("manufacturer");
        String model = fields.get("model");
        String batteryType = parseBattery(fields.get("batteryType"));

        Remote remote = switch (type) {
            case "TvRemote" -> new TvRemote(manufacturer, model, batteryType);
            case "StereoRemote" -> new StereoRemote(manufacturer, model, batteryType);
            case "SmartHomeRemote" -> new SmartHomeRemote(manufacturer, model, batteryType);
            default -> null;
        };

        if (remote == null)
            return;

        List<String> items = parseList(fields.get(LIST_KEYS.get(type)));
        if (items != null)
            remote.setChannels(items);

        result.add(remote);
    }

    // AA или AAA в любом регистре (и с кириллической А), иначе null
    private static String parseBattery(String value) {
        if (value == null)
            return null;
        String s = value.toUpperCase().replace('\u0410', 'A'); // \u0410 - кириллическая А
        return (s.equals("AA") || s.equals("AAA")) ? s : null;
    }

    // "a", "b", "c" -> [a, b, c]; если хоть одно значение без кавычек, весь список
    // null
    private static List<String> parseList(String value) {
        if (value == null || !LIST_VALUE.matcher(value).matches())
            return null;
        List<String> items = new ArrayList<>();
        Matcher m = LIST_ITEM.matcher(value);
        while (m.find())
            items.add(m.group(1));
        return items;
    }
}
