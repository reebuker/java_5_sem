package controller;

import model.RemoteRepository;
import model.remote.Remote;
import model.remote.SmartHomeRemote;
import model.remote.StereoRemote;
import model.remote.TvRemote;
import model.storage.CsvStorage;
import view.ConsoleView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

// Controller: главное меню. Спрашивает выбор у view, меняет список пультов в
// модели и читает или пишет файл
public class MenuController {
    private final RemoteRepository remotes;
    private final ConsoleView view;
    private final RemoteController remoteController;
    private String filename; // файл с пультами: из него читаем при запуске и в него сохраняем после каждого изменения
    private boolean autosaveOn = true; // выключается, если файл при запуске не прочитался: сохранение его затёрло бы

    public MenuController(RemoteRepository remotes, ConsoleView view, String filename) {
        this.remotes = remotes;
        this.view = view;
        this.filename = filename;
        this.remoteController = new RemoteController(view, this::autosave);
    }

    public void run() {
        try {
            openFile();
            boolean running = true;
            while (running) {
                view.clear();
                view.showMainMenu(filename, remotes.all().size(), autosaveOn);
                int choice = view.askMainChoice();
                view.clear();
                switch (choice) {
                    case 1 -> {
                        addRemote();
                        autosave();
                        view.pause();
                    }
                    case 2 -> {
                        showRemotes();
                        view.pause();
                    }
                    case 3 -> workWithRemote();
                    case 4 -> {
                        if (deleteRemote())
                            autosave();
                        view.pause();
                    }
                    case 5 -> {
                        if (editRemote())
                            autosave();
                        view.pause();
                    }
                    case 6 -> {
                        readFromFile();
                        view.pause();
                    }
                    case 7 -> {
                        saveToFile();
                        view.pause();
                    }
                    case 0 -> running = false;
                }
            }
        } catch (NoSuchElementException e) {
            view.showInputEnded();
        }
    }

    // при запуске берём пульты из файла, если он есть. Если не открыть так, первое же сохранение
    // затёрло бы то, что в файле лежит. Нет файла: начинаем с пустого списка, файл появится при первом изменении
    private void openFile() {
        Path file = toPath(filename);
        if (file == null) { // имя файла нельзя превратить в путь, значит и сохранять некуда
            autosaveOn = false;
            view.pause();
            return;
        }
        if (Files.notExists(file))
            return;
        try {
            remotes.replaceAll(new CsvStorage().load(file));
        } catch (IOException e) {
            // файл есть, но прочитать его не вышло: если сохранять поверх, мы затрём то, что в нём лежит
            autosaveOn = false;
            view.showOpenError(filename, e);
            view.pause();
        }
    }

    // пишет пульты в текущий файл, чтобы другие запуски (--watch, --export) сразу видели изменение.
    // true, если записалось
    private boolean autosave() {
        if (!autosaveOn)
            return true; // об этом уже сказано при запуске и написано над меню
        try {
            new CsvStorage().save(remotes.all(), Path.of(filename));
            return true;
        } catch (IOException e) {
            view.showAutosaveError(filename, e);
            return false;
        }
    }

    // путь из имени, которое ввёл пользователь, или null, если из него нельзя сделать путь
    // (например, там нулевой символ или буквы, которых нет в кодировке системы)
    private Path toPath(String name) {
        try {
            return Path.of(name);
        } catch (InvalidPathException e) {
            view.showBadFilename(name);
            return null;
        }
    }

    private void addRemote() {
        int type = view.askRemoteType();
        String manufacturer = view.askName("Производитель", Remote.DEFAULT_MANUFACTURER);
        String model = view.askName("Модель", Remote.DEFAULT_MODEL);
        String battery = view.askBattery("Тип батареек (AA/AAA)", Remote.DEFAULT_BATTERY);

        Remote remote = switch (type) {
            case 1 -> new TvRemote(manufacturer, model, battery);
            case 2 -> new StereoRemote(manufacturer, model, battery);
            default -> new SmartHomeRemote(manufacturer, model, battery);
        };
        remotes.add(remote);
        view.showAdded(remote);
    }

    private void showRemotes() {
        if (remotes.isEmpty()) {
            view.showEmptyList();
            return;
        }
        view.showRemotes(remotes.all());
    }

    // null, если списка нет или id не найден
    private Remote pickRemote() {
        if (remotes.isEmpty()) {
            view.showEmptyList();
            return null;
        }
        view.showRemotes(remotes.all());
        Remote r = remotes.get(view.askRemoteId());
        if (r == null)
            view.showNoSuchRemote();
        return r;
    }

    private void workWithRemote() {
        Remote r = pickRemote();
        if (r == null)
            view.pause();
        else
            remoteController.work(r);
    }

    // true, если пульт удалён
    private boolean deleteRemote() {
        Remote r = pickRemote();
        if (r == null)
            return false;
        remotes.remove(r.getId());
        view.showRemoved(r);
        return true;
    }

    // true, если пульт изменён
    private boolean editRemote() {
        Remote r = pickRemote();
        if (r == null)
            return false;

        view.showEditHint();
        r.setManufacturer(view.askName("Производитель", r.getManufacturer()));
        r.setModel(view.askName("Модель", r.getModel()));
        r.setBatteryType(view.askBattery("Тип батареек (AA/AAA)", r.getBatteryType()));
        view.showUpdated(r);
        return true;
    }

    // имя файла спрашиваем каждый раз, по умолчанию прошлое
    // основным файлом он становится, только если прочитался
    private void readFromFile() {
        String name = view.askFilename(filename);
        Path file = toPath(name);
        if (file == null)
            return;
        try {
            CsvStorage storage = new CsvStorage();
            List<Remote> loaded = storage.load(file);
            remotes.replaceAll(loaded); // старые пульты пропадают, остаются только из файла
            filename = name;
            autosaveOn = true;
            view.showLoadReport(loaded, storage);
        } catch (IOException e) {
            view.showLoadError(name, e);
        }
    }

    // пишем в тот же файл, имя спрашиваем так же, как при чтении
    private void saveToFile() {
        String name = view.askFilename(filename);
        Path file = toPath(name);
        if (file == null)
            return;
        List<Remote> all = remotes.all();
        try {
            new CsvStorage().save(all, file);
            filename = name; // дальше сохраняем в него
            autosaveOn = true;
            view.showSaved(all.size());
        } catch (IOException e) {
            view.showSaveError(name, e);
        }
    }
}
