package menu;

import model.RemoteRepository;
import model.device.Device;
import model.remote.*;
import model.storage.CsvStorage;

import java.io.IOException;
import java.nio.file.Path;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Menu {
    // Data
    private final RemoteRepository remotes = new RemoteRepository();
    private final Scanner in = new Scanner(System.in);
    private String filename = "data.csv";

    // Constants
    private static final int DEFAULT_TYPE = 1;
    private static final int MAX_NAME_LENGTH = 20;
    private static final String DEFAULT_MANUFACTURER = "Samsung";
    private static final String DEFAULT_MODEL = "Basic";
    private static final String DEFAULT_BATTERY = "AA";
    private static final String[] BATTERY_TYPES = { "AA", "AAA" };
    private static final int VOLUME_STEP = 5;
    private static final int EXTRA_START = 6; // с этого номера идут действия конкретного пульта

    // Methods
    public void run() {
        try {
            boolean running = true;
            while (running) {
                clearScreen();
                printMenu();
                int choice = readInt("Выбор: ", 0, 7, null);
                clearScreen();
                switch (choice) {
                    case 1 -> {
                        addRemote();
                        pause();
                    }
                    case 2 -> {
                        showRemotes();
                        pause();
                    }
                    case 3 -> workWithRemote();
                    case 4 -> {
                        deleteRemote();
                        pause();
                    }
                    case 5 -> {
                        editRemote();
                        pause();
                    }
                    case 6 -> {
                        readFromFile();
                        pause();
                    }
                    case 7 -> {
                        saveToFile();
                        pause();
                    }
                    case 0 -> running = false;
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("\nВвод закончился, выход");
        }
    }

    private void printMenu() {
        System.out.println("1 - добавить пульт");
        System.out.println("2 - показать пульты");
        System.out.println("3 - работа с пультом");
        System.out.println("4 - удалить пульт");
        System.out.println("5 - изменить пульт");
        System.out.println("6 - прочитать пульты из файла");
        System.out.println("7 - записать пульты в файл");
        System.out.println("0 - выход");
    }

    // имя файла спрашиваем каждый раз, по умолчанию прошлое
    private void readFromFile() {
        filename = readString("Имя файла", filename);
        try {
            CsvStorage loader = new CsvStorage();
            List<Remote> loaded = loader.load(Path.of(filename));
            remotes.addAll(loaded);
            for (Remote r : loaded) {
                System.out.println(r);
            }
            System.out.println("\nНайдено объектов: " + loader.getFound()
                    + " (неизвестного типа пропущено: " + loader.getUnknown() + ")");
            System.out.println("Свойств прочитано: " + loader.getPropertiesRead()
                    + ", не найдено: " + loader.getPropertiesMissing()
                    + ", некорректных: " + loader.getPropertiesInvalid());
        } catch (IOException e) {
            System.out.println("Не получилось прочитать файл: " + e.getMessage());
        }
    }

    // пишем в тот же файл, имя спрашиваем так же, как при чтении
    private void saveToFile() {
        filename = readString("Имя файла", filename);
        List<Remote> all = remotes.all();
        try {
            new CsvStorage().save(all, Path.of(filename));
            System.out.println("Записано пультов: " + all.size());
        } catch (IOException e) {
            System.out.println("Не получилось записать файл: " + filename);
        }
    }

    private void addRemote() {
        System.out.println("1 - телевизор, 2 - стереосистема, 3 - умный дом");
        int type = readInt("Тип", 1, 3, DEFAULT_TYPE);

        String manufacturer = readName("Производитель", DEFAULT_MANUFACTURER);
        String model = readName("Модель", DEFAULT_MODEL);
        String battery = readBattery("Тип батареек (AA/AAA)", DEFAULT_BATTERY);

        Remote remote = switch (type) {
            case 1 -> new TvRemote(manufacturer, model, battery);
            case 2 -> new StereoRemote(manufacturer, model, battery);
            default -> new SmartHomeRemote(manufacturer, model, battery);
        };
        remotes.add(remote);
        System.out.println("\nДобавлен: " + remote);
    }

    private void showRemotes() {
        if (remotes.isEmpty()) {
            System.out.println("Список пуст");
            return;
        }
        for (Remote r : remotes.all()) {
            System.out.println(r);
        }
    }

    // null, если списка нет или id не найден
    private Remote pickRemote() {
        if (remotes.isEmpty()) {
            System.out.println("Список пуст");
            return null;
        }
        showRemotes();
        Remote r = remotes.get(readInt("\nId пульта", 0, Integer.MAX_VALUE, null));
        if (r == null)
            System.out.println("Пульта с таким id нет");
        return r;
    }

    private void deleteRemote() {
        Remote r = pickRemote();
        if (r == null)
            return;
        remotes.remove(r.getId());
        System.out.println("\nУдалён: " + r);
    }

    private void editRemote() {
        Remote r = pickRemote();
        if (r == null)
            return;

        System.out.println("\nEnter - оставить без изменений");
        r.setManufacturer(readName("Производитель", r.getManufacturer()));
        r.setModel(readName("Модель", r.getModel()));
        r.setBatteryType(readBattery("Тип батареек (AA/AAA)", r.getBatteryType()));
        System.out.println("\nОбновлён: " + r);
    }

    // Input / output
    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void pause() {
        readLine("\nEnter, чтобы продолжить...");
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    private String readString(String prompt, String def) {
        String s = readLine(prompt + " [" + def + "]: ");
        return s.isEmpty() ? def : s;
    }

    // как readString, но длиннее MAX_NAME_LENGTH не принимает
    private String readName(String prompt, String def) {
        while (true) {
            String s = readString(prompt, def);
            if (s.length() <= MAX_NAME_LENGTH)
                return s;
            System.out.println("Не длиннее " + MAX_NAME_LENGTH + " символов");
        }
    }

    // def = null значит значения по умолчанию нет, пустой ввод не принимается
    private int readInt(String prompt, int min, int max, Integer def) {
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

    private String readBattery(String prompt, String def) {
        while (true) {
            String s = readString(prompt, def).toUpperCase().replace('А', 'A'); // кириллическая А -> латинская
            for (String t : BATTERY_TYPES) {
                if (t.equals(s))
                    return t;
            }
            System.out.println("Допустимо только AA или AAA");
        }
    }

    // печатает сообщение по результату нажатия, true если кнопку нажали
    private boolean report(ActionResult result) {
        switch (result) {
            case NO_VOLUME -> System.out.println("В умном доме нет громкости");
            case REMOTE_DEAD -> System.out.println("Пульт разряжен и больше не работает");
            case REMOTE_OFF -> System.out.println("Пульт выключен, сначала включите");
            case OK -> {
            }
        }
        return result.pressed();
    }

    private void showBattery(Remote r) {
        System.out.println("Батарейки " + r.getBatteryType() + ": " + r.getBatteryLevel() + "%");
    }

    private void showDevices(Remote r) {
        for (Device d : r.getDevices())
            System.out.println(d);
    }

    private boolean chooseChannel(Remote r) {
        List<String> list = r.getChannels();
        if (list.isEmpty()) {
            System.out.println("Здесь нет каналов");
            return false;
        }
        if (!report(r.checkReady()))
            return false;
        for (int i = 0; i < list.size(); i++) {
            System.out.println((i + 1) + " - " + list.get(i));
        }
        return report(r.setChannel(readInt("Номер", 1, list.size(), null)));
    }

    private void workWithRemote() {
        Remote r = pickRemote();
        if (r == null) {
            pause();
            return;
        }

        boolean back = false;
        while (!back) {
            List<String> extras = r.getExtraActions();
            clearScreen();
            System.out.println(r);
            System.out.println("Заряд пульта: " + r.getBatteryLevel() + "%");
            showDevices(r);
            System.out.println("\n1 - " + (r.isOn() ? "выключить" : "включить"));
            System.out.println("2 - громкость +" + VOLUME_STEP);
            System.out.println("3 - громкость -" + VOLUME_STEP);
            System.out.println("4 - выбрать " + r.getChannelLabel());
            System.out.println("5 - батарейки");
            for (int i = 0; i < extras.size(); i++) {
                System.out.println((EXTRA_START + i) + " - " + extras.get(i));
            }
            System.out.println("0 - назад");

            // Выбор
            int choice = readInt("Выбор", 0, EXTRA_START - 1 + extras.size(), null);
            boolean wasDead = r.isDead();
            boolean needPause = false;
            switch (choice) {
                case 1 -> needPause = !report(r.togglePower());
                case 2 -> needPause = !report(r.changeVolume(VOLUME_STEP));
                case 3 -> needPause = !report(r.changeVolume(-VOLUME_STEP));
                case 4 -> needPause = !chooseChannel(r);
                case 5 -> {
                    showBattery(r);
                    needPause = true;
                }
                case 0 -> back = true;
                default -> needPause = !report(r.runExtra(choice - EXTRA_START));
            }
            // пульт сам не печатает, поэтому про то что батарейки сели говорим здесь
            if (!wasDead && r.isDead())
                System.out.println("Батарейки сели, пульт выключился навсегда");
            if (needPause || (r.isDead() && !back))
                pause();
        }
    }
}
