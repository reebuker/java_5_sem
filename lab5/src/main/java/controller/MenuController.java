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
                view.showMainMenu(filename, remotes.all().size());
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
        Path file = Path.of(filename);
        if (Files.notExists(file))
            return;
        try {
            remotes.replaceAll(new CsvStorage().load(file));
        } catch (IOException e) {
            view.showOpenError(filename);
            view.pause();
        }
    }

    // пишет пульты в текущий файл, чтобы другие запуски (--watch, --export) сразу видели изменение.
    // true, если записалось
    private boolean autosave() {
        try {
            new CsvStorage().save(remotes.all(), Path.of(filename));
            return true;
        } catch (IOException e) {
            view.showAutosaveError(filename);
            return false;
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
    private void readFromFile() {
        filename = view.askFilename(filename);
        try {
            CsvStorage storage = new CsvStorage();
            List<Remote> loaded = storage.load(Path.of(filename));
            remotes.replaceAll(loaded); // старые пульты пропадают, остаются только из файла
            view.showLoadReport(loaded, storage);
        } catch (IOException e) {
            view.showLoadError(e.getMessage());
        }
    }

    // пишем в тот же файл, имя спрашиваем так же, как при чтении
    private void saveToFile() {
        filename = view.askFilename(filename);
        List<Remote> all = remotes.all();
        try {
            new CsvStorage().save(all, Path.of(filename));
            view.showSaved(all.size());
        } catch (IOException e) {
            view.showSaveError(filename);
        }
    }
}
