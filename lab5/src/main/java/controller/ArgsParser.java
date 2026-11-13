package controller;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Разбирает ключи командной строки в Options. Ничего не печатает и не запускает,
// при любой ошибке бросает IllegalArgumentException с понятным текстом
public final class ArgsParser {
    public static final String DEFAULT_FILE = "data.csv";
    public static final int DEFAULT_PERIOD = 2; // секунд
    public static final int MAX_PERIOD = 3600; // секунд

    private static final int MAX_SHOWN = 40; // длиннее этого чужой текст в сообщениях об ошибках режем
    private static final List<String> KEYS = List.of("--help", "-h", "--show", "--menu", "--replace",
            "--watch", "--export", "-f", "--file", "-p", "--period");

    public enum Mode {
        HELP, SHOW, MENU, REPLACE, WATCH, EXPORT
    }

    // что надо сделать и с каким файлом; replaceWith есть только у REPLACE, logFile только у EXPORT
    // (иначе null); periodSeconds как часто повторять, нужен только режимам с повтором
    public record Options(Mode mode, String file, String replaceWith, String logFile, int periodSeconds) {
    }

    private ArgsParser() {
    }

    public static Options parse(String[] args) {
        List<String> list = expand(args == null ? new String[0] : args);

        // справка важнее всего остального: если её просят, остальные ключи не проверяем
        if (list.isEmpty() || list.contains("--help") || list.contains("-h"))
            return new Options(Mode.HELP, DEFAULT_FILE, null, null, DEFAULT_PERIOD);

        Mode mode = null;
        String file = null;
        String replaceWith = null;
        String logFile = null;
        Integer period = null;
        for (int i = 0; i < list.size(); i++) {
            String arg = list.get(i);
            switch (arg) {
                case "--show" -> mode = chooseMode(mode, Mode.SHOW);
                case "--menu" -> mode = chooseMode(mode, Mode.MENU);
                case "--watch" -> mode = chooseMode(mode, Mode.WATCH);
                case "-f", "--file" -> {
                    if (file != null)
                        throw new IllegalArgumentException("Ключ " + arg + " указан дважды");
                    file = fileNameAfter(list, i);
                    i++;
                }
                case "-p", "--period" -> {
                    if (period != null)
                        throw new IllegalArgumentException("Ключ " + arg + " указан дважды");
                    period = parsePeriod(valueAfter(list, i, "нужно число секунд"));
                    i++;
                }
                case "--replace" -> {
                    mode = chooseMode(mode, Mode.REPLACE);
                    replaceWith = fileNameAfter(list, i);
                    i++;
                }
                case "--export" -> {
                    mode = chooseMode(mode, Mode.EXPORT);
                    logFile = fileNameAfter(list, i);
                    i++;
                }
                default -> throw unknown(arg);
            }
        }
        if (mode == null)
            throw new IllegalArgumentException("Не указан режим работы");
        if (period != null && mode != Mode.WATCH && mode != Mode.EXPORT)
            throw new IllegalArgumentException("Ключ -p нужен только вместе с --watch или --export");
        return new Options(mode, file == null ? DEFAULT_FILE : file, replaceWith, logFile,
                period == null ? DEFAULT_PERIOD : period);
    }

    // --ключ=значение превращаем в два аргумента: --ключ и значение
    private static List<String> expand(String[] args) {
        List<String> result = new ArrayList<>();
        for (String arg : args) {
            if (arg == null)
                continue;
            int eq = arg.indexOf('=');
            if (arg.startsWith("--") && eq > 2) {
                result.add(arg.substring(0, eq));
                result.add(arg.substring(eq + 1));
            } else {
                result.add(arg);
            }
        }
        return result;
    }

    private static Mode chooseMode(Mode current, Mode wanted) {
        if (current != null)
            throw new IllegalArgumentException("Режим должен быть один, а указано несколько");
        return wanted;
    }

    // то, что стоит после ключа list[i]; problem дописывается к ошибке, если значения нет
    private static String valueAfter(List<String> list, int i, String problem) {
        if (i + 1 >= list.size() || list.get(i + 1).startsWith("-"))
            throw new IllegalArgumentException("После " + list.get(i) + " " + problem);
        return list.get(i + 1);
    }

    // имя файла после ключа: непустое и такое, из которого можно сделать путь
    private static String fileNameAfter(List<String> list, int i) {
        String name = valueAfter(list, i, "нужно имя файла (если оно начинается с «-», напишите ./имя)");
        if (name.isBlank())
            throw new IllegalArgumentException("После " + list.get(i) + " имя файла не может быть пустым");
        try {
            Path.of(name);
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("Недопустимое имя файла: " + shown(name));
        }
        return name;
    }

    private static int parsePeriod(String text) {
        int seconds;
        try {
            seconds = Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Период должен быть целым числом секунд, а не " + shown(text));
        }
        if (seconds < 1 || seconds > MAX_PERIOD)
            throw new IllegalArgumentException("Период должен быть от 1 до " + MAX_PERIOD + " секунд");
        return seconds;
    }

    // ошибка про непонятный аргумент: если он похож на настоящий ключ, подсказываем какой
    private static IllegalArgumentException unknown(String arg) {
        if (!arg.isEmpty() && "-–—".indexOf(arg.charAt(0)) < 0)
            return new IllegalArgumentException("Лишний аргумент: " + shown(arg)
                    + " (ключи начинаются с --, а имя файла пишется после -f)");
        String near = nearestKey(arg);
        return new IllegalArgumentException("Неизвестный ключ: " + shown(arg)
                + (near == null ? "" : ". Возможно, имелось в виду " + near));
    }

    // самый похожий ключ или null, если похожих нет
    private static String nearestKey(String arg) {
        String word = bare(arg);
        String best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (String key : KEYS) {
            String keyWord = bare(key);
            int distance = distance(word, keyWord);
            int allowed = keyWord.length() >= 3 ? 2 : 0; // у коротких ключей только другой регистр
            if (distance <= allowed && distance < bestDistance) {
                best = key;
                bestDistance = distance;
            }
        }
        return best;
    }

    // слово без тире в начале (и без длинных тире, которые подставляют телефоны) и в нижнем регистре
    private static String bare(String arg) {
        int start = 0;
        while (start < arg.length() && "-–—".indexOf(arg.charAt(start)) >= 0)
            start++;
        return arg.substring(start).toLowerCase(Locale.ROOT);
    }

    // сколько правок (вставить, удалить, заменить букву) отделяет a от b
    private static int distance(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] cur = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++)
            prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int replace = prev[j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1);
                cur[j] = Math.min(replace, Math.min(prev[j] + 1, cur[j - 1] + 1));
            }
            int[] tmp = prev;
            prev = cur;
            cur = tmp;
        }
        return prev[b.length()];
    }

    // чужой текст для сообщения: длинный режем, управляющие символы заменяем, чтобы не испортить экран
    private static String shown(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length() && i < MAX_SHOWN; i++) {
            char c = text.charAt(i);
            sb.append(Character.isISOControl(c) ? '?' : c);
        }
        if (text.length() > MAX_SHOWN)
            sb.append("…");
        return sb.toString();
    }
}
