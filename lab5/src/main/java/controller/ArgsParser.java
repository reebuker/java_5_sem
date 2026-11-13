package controller;

// Разбирает ключи командной строки в Options. Ничего не печатает и не запускает,
// при ошибке бросает IllegalArgumentException с понятным текстом
public final class ArgsParser {
    public static final String DEFAULT_FILE = "data.csv";
    public static final int DEFAULT_PERIOD = 2; // секунд
    public static final int MAX_PERIOD = 3600; // секунд

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
        if (args.length == 0)
            return new Options(Mode.HELP, DEFAULT_FILE, null, null, DEFAULT_PERIOD);

        Mode mode = null;
        String file = null;
        String replaceWith = null;
        String logFile = null;
        Integer period = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--help", "-h" -> mode = chooseMode(mode, Mode.HELP);
                case "--show" -> mode = chooseMode(mode, Mode.SHOW);
                case "--menu" -> mode = chooseMode(mode, Mode.MENU);
                case "--watch" -> mode = chooseMode(mode, Mode.WATCH);
                case "-f", "--file" -> {
                    if (file != null)
                        throw new IllegalArgumentException("Ключ " + arg + " указан дважды");
                    file = valueAfter(args, i, "имя файла");
                    i++;
                }
                case "-p", "--period" -> {
                    if (period != null)
                        throw new IllegalArgumentException("Ключ " + arg + " указан дважды");
                    period = parsePeriod(valueAfter(args, i, "число секунд"));
                    i++;
                }
                case "--replace" -> {
                    mode = chooseMode(mode, Mode.REPLACE);
                    replaceWith = valueAfter(args, i, "имя файла");
                    i++;
                }
                case "--export" -> {
                    mode = chooseMode(mode, Mode.EXPORT);
                    logFile = valueAfter(args, i, "имя файла");
                    i++;
                }
                default -> throw new IllegalArgumentException("Неизвестный ключ: " + arg);
            }
        }
        if (mode == null)
            throw new IllegalArgumentException("Не указан режим работы");
        if (period != null && mode != Mode.WATCH && mode != Mode.EXPORT)
            throw new IllegalArgumentException("Ключ -p нужен только вместе с --watch или --export");
        return new Options(mode, file == null ? DEFAULT_FILE : file, replaceWith, logFile,
                period == null ? DEFAULT_PERIOD : period);
    }

    // то, что стоит после ключа args[i]; what нужно только для текста ошибки
    private static String valueAfter(String[] args, int i, String what) {
        if (i + 1 >= args.length || args[i + 1].startsWith("-"))
            throw new IllegalArgumentException("После " + args[i] + " нужно " + what);
        return args[i + 1];
    }

    private static int parsePeriod(String text) {
        int seconds;
        try {
            seconds = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Период должен быть целым числом секунд, а не " + text);
        }
        if (seconds < 1 || seconds > MAX_PERIOD)
            throw new IllegalArgumentException("Период должен быть от 1 до " + MAX_PERIOD + " секунд");
        return seconds;
    }

    private static Mode chooseMode(Mode current, Mode wanted) {
        if (current != null)
            throw new IllegalArgumentException("Режим должен быть один, а указано несколько");
        return wanted;
    }
}
