package controller;

import controller.ArgsParser.Options;
import model.RemoteRepository;
import model.remote.Remote;
import model.storage.CsvStorage;
import view.ConsoleView;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

// Controller верхнего уровня: разбирает ключи и запускает нужный режим.
// Возвращает код выхода: 0 всё хорошо, 1 не вышло прочитать файл, 2 неправильные ключи
public class AppController {
    public static final int EXIT_OK = 0;
    public static final int EXIT_FAILED = 1;
    public static final int EXIT_USAGE = 2;

    private final RemoteRepository remotes;
    private final ConsoleView view;

    public AppController(RemoteRepository remotes, ConsoleView view) {
        this.remotes = remotes;
        this.view = view;
    }

    public int run(String[] args) {
        Options options;
        try {
            options = ArgsParser.parse(args);
        } catch (IllegalArgumentException e) {
            view.showArgsError(e.getMessage());
            return EXIT_USAGE;
        }

        return switch (options.mode()) {
            case HELP -> {
                view.showHelp(ArgsParser.DEFAULT_FILE);
                yield EXIT_OK;
            }
            case SHOW -> show(options.file());
            case REPLACE -> replace(options.file(), options.source());
            case MENU -> {
                new MenuController(remotes, view, options.file()).run();
                yield EXIT_OK;
            }
        };
    }

    // рабочий файл target заменяем файлом source. Новый файл сначала проверяем,
    // и если с ним что-то не так, старый остаётся как был
    private int replace(String target, String source) {
        CsvStorage storage = new CsvStorage();
        try {
            List<Remote> loaded = storage.load(Path.of(source));
            if (loaded.isEmpty()) {
                view.showReplaceRejected(source);
                view.showLoadStats(storage);
                return EXIT_FAILED;
            }
            if (storage.replaceFile(Path.of(source), Path.of(target)))
                view.showReplaced(target, source, loaded.size());
            else
                view.showSameFile(source);
            return EXIT_OK;
        } catch (IOException e) {
            view.showReplaceError(source, target);
            return EXIT_FAILED;
        }
    }

    // один раз читаем файл и показываем состояние пультов
    private int show(String filename) {
        try {
            CsvStorage storage = new CsvStorage();
            List<Remote> loaded = storage.load(Path.of(filename));
            remotes.replaceAll(loaded);
            view.showStates(remotes.all());
            view.showLoadStats(storage);
            return EXIT_OK;
        } catch (IOException e) {
            view.showLoadError(e.getMessage());
            return EXIT_FAILED;
        }
    }
}
