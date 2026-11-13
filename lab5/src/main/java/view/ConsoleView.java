package view;

import model.device.Device;
import model.device.SmartHome;
import model.device.Stereo;
import model.device.Tv;
import model.remote.ActionResult;
import model.remote.Remote;
import model.remote.RemoteState;
import model.remote.SmartHomeRemote;
import model.remote.StereoRemote;
import model.remote.TvRemote;
import model.storage.CsvStorage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

// View: всё, что видит и вводит пользователь в консоли. Ничего не решает и ничего
// не меняет в модели, только показывает её и спрашивает ввод
public class ConsoleView {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Scanner in = new Scanner(System.in);

    // ---------- экран ----------

    public void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void pause() {
        readLine("\nEnter, чтобы продолжить...");
    }

    // ---------- ввод ----------

    private String readLine(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    // пустой ввод даёт значение по умолчанию
    public String askString(String prompt, String def) {
        String s = readLine(prompt + " [" + def + "]: ");
        return s.isEmpty() ? def : s;
    }

    // как askString, но длиннее Remote.MAX_NAME_LENGTH не принимает
    public String askName(String prompt, String def) {
        while (true) {
            String s = askString(prompt, def);
            if (s.length() <= Remote.MAX_NAME_LENGTH)
                return s;
            System.out.println("Не длиннее " + Remote.MAX_NAME_LENGTH + " символов");
        }
    }

    public String askBattery(String prompt, String def) {
        while (true) {
            String battery = Remote.normalizeBattery(askString(prompt, def));
            if (battery != null)
                return battery;
            System.out.println("Допустимо только AA или AAA");
        }
    }

    // def = null значит значения по умолчанию нет, пустой ввод не принимается
    public int askInt(String prompt, int min, int max, Integer def) {
        String hint = (def == null) ? ": " : " [" + def + "]: ";
        while (true) {
            String s = readLine(prompt + hint);
            if (s.isEmpty() && def != null)
                return def;
            try {
                int v = Integer.parseInt(s);
                if (v >= min && v <= max)
                    return v;
                System.out.println("Число должно быть от " + min + " до " + max);
            } catch (NumberFormatException e) {
                System.out.println("Нужно целое число");
            }
        }
    }

    // 1 - телевизор, 2 - стереосистема, 3 - умный дом
    public int askRemoteType() {
        System.out.println("1 - телевизор, 2 - стереосистема, 3 - умный дом");
        return askInt("Тип", 1, 3, 1);
    }

    public int askRemoteId() {
        return askInt("\nId пульта", 0, Integer.MAX_VALUE, null);
    }

    public String askFilename(String def) {
        return askString("Имя файла", def);
    }

    // ---------- главное меню ----------

    public void showMainMenu(String filename, int count) {
        System.out.println("Файл с пультами: " + filename + " (пультов: " + count
                + "), изменения записываются сразу");
        System.out.println();
        System.out.println("1 - добавить пульт");
        System.out.println("2 - показать пульты");
        System.out.println("3 - работа с пультом");
        System.out.println("4 - удалить пульт");
        System.out.println("5 - изменить пульт");
        System.out.println("6 - заменить пульты пультами из файла");
        System.out.println("7 - записать пульты в другой файл");
        System.out.println("0 - выход");
    }

    public int askMainChoice() {
        return askInt("Выбор: ", 0, 7, null);
    }

    // ---------- список пультов ----------

    public void showRemotes(List<Remote> remotes) {
        for (Remote r : remotes)
            System.out.println(describe(r));
    }

    public void showEmptyList() {
        System.out.println("Список пуст");
    }

    public void showNoSuchRemote() {
        System.out.println("Пульта с таким id нет");
    }

    public void showAdded(Remote r) {
        System.out.println("\nДобавлен: " + describe(r));
    }

    public void showRemoved(Remote r) {
        System.out.println("\nУдалён: " + describe(r));
    }

    public void showUpdated(Remote r) {
        System.out.println("\nОбновлён: " + describe(r));
    }

    public void showEditHint() {
        System.out.println("\nEnter - оставить без изменений");
    }

    // ---------- файл ----------

    public void showLoadReport(List<Remote> loaded, CsvStorage stats) {
        showRemotes(loaded);
        showLoadStats(stats);
    }

    public void showLoadStats(CsvStorage stats) {
        System.out.println("\nНайдено объектов: " + stats.getFound()
                + " (неизвестного типа пропущено: " + stats.getUnknown() + ")");
        System.out.println("Свойств прочитано: " + stats.getPropertiesRead()
                + ", не найдено: " + stats.getPropertiesMissing()
                + ", некорректных: " + stats.getPropertiesInvalid());
    }

    public void showLoadError(String filename) {
        System.out.println("Не получилось прочитать файл: " + filename);
    }

    public void showOpenError(String filename) {
        System.out.println("Не получилось прочитать файл " + filename + ", начинаю с пустого списка");
    }

    public void showAutosaveError(String filename) {
        System.out.println("Не получилось сохранить файл " + filename + ", изменения остались только в памяти");
    }

    public void showSaved(int count) {
        System.out.println("Записано пультов: " + count);
    }

    public void showSaveError(String filename) {
        System.out.println("Не получилось записать файл: " + filename);
    }

    // ---------- состояние для режимов с ключами ----------

    public void showHelp(String defaultFile) {
        System.out.println("Использование: java -jar target/main.jar <режим> [-f файл]");
        System.out.println();
        System.out.println("Режимы (нужен ровно один):");
        System.out.println("  --help, -h        эта справка");
        System.out.println("  --show            один раз вывести состояние пультов из файла");
        System.out.println("  --menu            меню, как в ЛР4, только файл читается при запуске");
        System.out.println("                    и изменения сразу сохраняются в него");
        System.out.println("  --replace <файл>  заменить рабочий файл этим (старое содержимое пропадёт)");
        System.out.println("  --watch           показывать пульты снова и снова, каждый раз перечитывая файл");
        System.out.println("                    (останавливается по Ctrl+C)");
        System.out.println("  --export <журнал> каждый период дописывать в журнал (csv) состояние пультов");
        System.out.println("                    с временем, журнал не должен быть файлом с пультами");
        System.out.println();
        System.out.println("Остальные ключи:");
        System.out.println("  -f, --file <файл> файл с пультами, по умолчанию " + defaultFile);
        System.out.println("  -p, --period <с>  как часто повторять в секундах, для --watch и --export");
    }

    // ошибки в ключах идут в stderr, чтобы их не путать с обычным выводом
    public void showArgsError(String message) {
        System.err.println(message);
        System.err.println("Справка: java -jar target/main.jar --help");
    }

    // по пульту блок: сам пульт, его заряд и состояние устройств
    public void showStates(List<Remote> remotes) {
        if (remotes.isEmpty()) {
            showEmptyList();
            return;
        }
        for (Remote r : remotes) {
            System.out.println(describe(r));
            System.out.println("  Заряд пульта: " + r.getBatteryLevel() + "%");
            for (Device d : r.getDevices())
                System.out.println("  " + describe(d));
        }
    }

    public void showWatchStart(String filename, int periodSeconds) {
        System.out.println("Слежу за файлом " + filename + ", обновляю каждые " + periodSeconds
                + " с. Остановить: Ctrl+C");
    }

    public void showExportStart(String filename, String logName, int periodSeconds) {
        System.out.println("Пишу состояние пультов из " + filename + " в журнал " + logName + " каждые "
                + periodSeconds + " с. Остановить: Ctrl+C");
    }

    public void showExported(LocalDateTime time, int rows, String logName) {
        System.out.println(time.format(TIME) + ": в журнал " + logName + " записано строк: " + rows);
    }

    public void showLogError(String logName) {
        System.out.println("Не получилось записать журнал: " + logName);
    }

    public void showLogIsSourceFile(String logName) {
        System.out.println("Журнал не может быть файлом с пультами: " + logName);
    }

    public void showTick(LocalDateTime time, String filename) {
        System.out.println("\n=== " + time.format(TIME) + ", файл " + filename + " ===");
    }

    public void showWatchStopped() {
        System.out.println("\nОстановлено");
    }

    public void showReplaced(String target, String source, int count) {
        System.out.println("Файл " + target + " заменён файлом " + source + ", пультов в нём: " + count);
    }

    public void showReplaceRejected(String source) {
        System.out.println("В файле " + source + " нет ни одного пульта, замена отменена");
    }

    public void showSameFile(String filename) {
        System.out.println("Это один и тот же файл, менять нечего: " + filename);
    }

    public void showReplaceError(String source, String target) {
        System.out.println("Не получилось заменить файл " + target + " файлом " + source);
    }

    // ---------- работа с одним пультом ----------

    // extraStart: с какого номера в меню идут действия конкретного пульта
    public void showControlPanel(Remote r, int volumeStep, int extraStart) {
        System.out.println(describe(r));
        System.out.println("Заряд пульта: " + r.getBatteryLevel() + "%");
        for (Device d : r.getDevices())
            System.out.println(describe(d));
        System.out.println("\n1 - " + (r.isOn() ? "выключить" : "включить"));
        System.out.println("2 - громкость +" + volumeStep);
        System.out.println("3 - громкость -" + volumeStep);
        System.out.println("4 - выбрать " + (r instanceof SmartHomeRemote ? "прибор" : "канал / трек"));
        System.out.println("5 - батарейки");
        List<String> extras = r.getExtraActions();
        for (int i = 0; i < extras.size(); i++)
            System.out.println((extraStart + i) + " - " + extras.get(i));
        System.out.println("0 - назад");
    }

    public void showBattery(Remote r) {
        System.out.println("Батарейки " + r.getBatteryType() + ": " + r.getBatteryLevel() + "%");
    }

    public void showNoChannels() {
        System.out.println("Здесь нет каналов");
    }

    // список каналов, треков или приборов с номерами
    public void showChannels(Remote r) {
        List<String> names = r.getChannels();
        String marks = r.state().appliances(); // есть только у умного дома: 1 включён, 0 выключен
        for (int i = 0; i < names.size(); i++) {
            String line = (i + 1) + " - " + names.get(i);
            if (marks != null)
                line += " (" + (marks.charAt(i) == '1' ? "вкл" : "выкл") + ")";
            System.out.println(line);
        }
    }

    public void showResult(ActionResult result) {
        switch (result) {
            case NO_VOLUME -> System.out.println("В умном доме нет громкости");
            case REMOTE_DEAD -> System.out.println("Пульт разряжен и больше не работает");
            case REMOTE_OFF -> System.out.println("Пульт выключен, сначала включите");
            case OK -> {
            }
        }
    }

    public void showBatteryDied() {
        System.out.println("Батарейки сели, пульт выключился навсегда");
    }

    public void showInputEnded() {
        System.out.println("\nВвод закончился, выход");
    }

    // ---------- как показывать модель ----------

    // строка про пульт: общая часть и то, что есть только у этого вида пульта
    private String describe(Remote r) {
        String text = r.getClass().getSimpleName() + " #" + r.getId() + ": " + r.getManufacturer()
                + " " + r.getModel() + ", батарейки " + r.getBatteryType();
        RemoteState s = r.state();
        return text + switch (r) {
            case TvRemote t -> ", прошлый канал: " + orNone(s.extra1(), 0)
                    + ", последний случайный: " + orNone(s.extra2(), 0);
            case StereoRemote t -> ", треков включено: " + s.extra1()
                    + ", последний случайный: " + orNone(s.extra2(), 0);
            case SmartHomeRemote t -> ", «выключить всё» нажато раз: " + s.extra1()
                    + ", после сценария включено: " + (s.extra2() < 0 ? "нет" : s.extra2());
            default -> "";
        };
    }

    // значение, а если оно равно «ещё не было», то слово «нет»
    private String orNone(int value, int none) {
        return value == none ? "нет" : String.valueOf(value);
    }

    // строка про устройство: имя, включено ли и его состояние
    private String describe(Device d) {
        String status = switch (d) {
            case Tv tv -> "канал " + tv.getChannel() + " (" + tv.getChannels().get(tv.getChannel() - 1)
                    + "), громкость " + tv.getVolume();
            case Stereo st -> "трек " + st.getTrack() + " (" + st.getTracks().get(st.getTrack() - 1)
                    + "), громкость " + st.getVolume();
            case SmartHome home -> home.getAppliances().toString();
            default -> "";
        };
        return d.getName() + ": " + (d.isOn() ? "вкл" : "выкл") + ", " + status;
    }
}
