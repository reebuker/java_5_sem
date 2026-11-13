package controller;

// Разбирает ключи командной строки в Options. Ничего не печатает и не запускает,
// при ошибке бросает IllegalArgumentException с понятным текстом
public final class ArgsParser {
    public static final String DEFAULT_FILE = "data.csv";

    public enum Mode {
        HELP, SHOW, MENU
    }

    // что надо сделать и с каким файлом
    public record Options(Mode mode, String file) {
    }

    private ArgsParser() {
    }

    public static Options parse(String[] args) {
        if (args.length == 0)
            return new Options(Mode.HELP, DEFAULT_FILE);

        Mode mode = null;
        String file = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--help", "-h" -> mode = chooseMode(mode, Mode.HELP);
                case "--show" -> mode = chooseMode(mode, Mode.SHOW);
                case "--menu" -> mode = chooseMode(mode, Mode.MENU);
                case "-f", "--file" -> {
                    if (file != null)
                        throw new IllegalArgumentException("Ключ " + arg + " указан дважды");
                    if (i + 1 >= args.length || args[i + 1].startsWith("-"))
                        throw new IllegalArgumentException("После " + arg + " нужно имя файла");
                    file = args[++i];
                }
                default -> throw new IllegalArgumentException("Неизвестный ключ: " + arg);
            }
        }
        if (mode == null)
            throw new IllegalArgumentException("Не указан режим работы");
        return new Options(mode, file == null ? DEFAULT_FILE : file);
    }

    private static Mode chooseMode(Mode current, Mode wanted) {
        if (current != null)
            throw new IllegalArgumentException("Режим должен быть один, а указано несколько");
        return wanted;
    }
}
