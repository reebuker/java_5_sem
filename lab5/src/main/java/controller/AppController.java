package controller;

import controller.ArgsParser.Options;
import model.RemoteRepository;
import model.remote.Remote;
import model.storage.CsvStorage;
import view.ConsoleView;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
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
            case WATCH -> watch(options.file(), options.periodSeconds());
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

    // каждые periodSeconds заново читаем файл и показываем пульты, пока не нажмут Ctrl+C
    private int watch(String filename, int periodSeconds) {
        Poller poller = new Poller(periodSeconds, () -> watchTick(filename), view::showWatchStopped);
        view.showWatchStart(filename, periodSeconds);
        return runPoller(poller);
    }

    // один тик: файл читается в новую базу, а прошлая забывается
    private void watchTick(String filename) {
        view.showTick(LocalDateTime.now(), filename);
        try {
            RemoteRepository current = new RemoteRepository();
            current.replaceAll(new CsvStorage().load(Path.of(filename)));
            view.showStates(current.all());
        } catch (IOException e) {
            // файла может не быть прямо сейчас, это не повод падать: попробуем на следующем тике
            view.showLoadError(filename);
        }
    }

    // запускает задачу в своём потоке и ждёт её, а по Ctrl+C прерывает и дожидается конца
    private int runPoller(Poller poller) {
        Thread thread = new Thread(poller, "poller");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stop(thread), "stopper"));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            stop(thread);
        }
        return poller.failed() ? EXIT_FAILED : EXIT_OK;
    }

    private void stop(Thread thread) {
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
