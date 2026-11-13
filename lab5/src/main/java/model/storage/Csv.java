package model.storage;

import java.util.ArrayList;
import java.util.List;

// разбор и сборка одной строки csv. Поля идут через запятую, а поле с запятой, кавычкой
// или переводом строки берётся в кавычки, при этом кавычка внутри удваивается.
// Одна запись это одна строка файла
public class Csv {
    private Csv() {
    }

    // незакрытая кавычка не ошибка: всё до конца строки считается частью поля
    public static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c != '"') {
                    current.append(c);
                } else if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"'); // две кавычки подряд это одна кавычка в значении
                    i++;
                } else {
                    quoted = false;
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }

    // значение для поля: null это пустое поле
    public static String text(Object value) {
        return value == null ? "" : value.toString();
    }

    public static String join(List<String> fields) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0)
                line.append(',');
            line.append(escape(fields.get(i)));
        }
        return line.toString();
    }

    private static String escape(String field) {
        boolean special = field.contains(",") || field.contains("\"") || field.contains("\n")
                || field.contains("\r");
        if (!special)
            return field;
        return "\"" + field.replace("\"", "\"\"") + "\"";
    }
}
