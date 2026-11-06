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
    private static final Pattern CLASS_LINE = Pattern.compile("^класс:\\s*(.+)$");
    private static final Pattern FIELD_LINE = Pattern.compile("^(\\w+):\\s*(.*)$");

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
        String batteryType = fields.get("batteryType");

        Remote remote = switch (type) {
            case "TvRemote" -> new TvRemote(manufacturer, model, batteryType);
            case "StereoRemote" -> new StereoRemote(manufacturer, model, batteryType);
            case "SmartHomeRemote" -> new SmartHomeRemote(manufacturer, model, batteryType);
            default -> null;
        };
        if (remote != null)
            result.add(remote);
    }
}
