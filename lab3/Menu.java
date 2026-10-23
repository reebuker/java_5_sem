// Menu.java
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

class Menu {
    // Data
    private final List<Remote> remotes = new ArrayList<>();
    private final Scanner in = new Scanner(System.in);

    // Constants
    private static final int DEFAULT_TYPE = 1;
    private static final String DEFAULT_MANUFACTURER = "Samsung";
    private static final String DEFAULT_MODEL = "Basic";
    private static final String DEFAULT_BATTERY = "AA";
    private static final String[] BATTERY_TYPES = {"AA", "AAA"};
    private static final int VOLUME_STEP = 5;
    private static final int MAX_CHANNEL = 10;

    // Methods
    public void run() {
        try {
            boolean running = true;
            while (running) {
                clearScreen();
                printMenu();
                int choice = readInt("Выбор: ", 0, 4, null);
                clearScreen();
                switch (choice) {
                    case 1 -> { addRemote(); pause(); }
                    case 2 -> { showRemotes(); pause(); }
                    case 3 -> workWithRemote();
                    case 4 -> { deleteRemote(); pause(); }
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
        System.out.println("0 - выход");
    }

    private void addRemote() {
        System.out.println("1 - телевизор, 2 - стереосистема, 3 - умный дом");
        int type = readInt("Тип", 1, 3, DEFAULT_TYPE);

        String manufacturer = readString("Производитель", DEFAULT_MANUFACTURER);
        String model = readString("Модель", DEFAULT_MODEL);
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
        for (Remote r : remotes) {
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
        Remote r = findRemote(readInt("\nId пульта", 0, Integer.MAX_VALUE, null));
        if (r == null) System.out.println("Пульта с таким id нет");
        return r;
    }

    private void deleteRemote() {
        Remote r = pickRemote();
        if (r == null) return;
        remotes.remove(r);
        System.out.println("\nУдалён: " + r);
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

    // def = null значит значения по умолчанию нет, пустой ввод не принимается
    private int readInt(String prompt, int min, int max, Integer def) {
        String hint = (def == null) ? ": " : " [" + def + "]: ";
        while (true) {
            String s = readLine(prompt + hint);
            if (s.isEmpty() && def != null) return def;
            try {
                int v = Integer.parseInt(s);
                if (v >= min && v <= max) return v;
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
                if (t.equals(s)) return t;
            }
            System.out.println("Допустимо только AA или AAA");
        }
    }

    private Remote findRemote(int id) {
        for (Remote r : remotes) {
            if (r.getId() == id) return r;
        }
        return null;
    }

    private boolean chooseChannel(Remote r) {
        List<String> list = r.getChannels();
        if (list.isEmpty()) {
            System.out.println("Здесь нет каналов");
            return false;
        }
        if (!r.checkReady()) return false;
        for (int i = 0; i < list.size(); i++) {
            System.out.println((i + 1) + " - " + list.get(i));
        }
        return r.setChannel(readInt("Номер", 1, list.size(), null));
    }

    private void workWithRemote() {
        if (remotes.isEmpty()) {
            System.out.println("Список пуст");
            pause();
            return;
        }
        Remote r = pickRemote();
        if (r == null) {
            pause();
            return;
        }

        boolean back = false;
        while (!back) {
            clearScreen();
            System.out.println(r);
            System.out.println("Заряд пульта: " + r.getBatteryLevel() + "%");
            r.showDeviceState();
            System.out.println("\n1 - " + (r.isOn() ? "выключить" : "включить"));
            System.out.println("2 - громкость +" + VOLUME_STEP);
            System.out.println("3 - громкость -" + VOLUME_STEP);
            System.out.println("4 - выбрать канал / трек");
            System.out.println("5 - батарейки");
            System.out.println("0 - назад");

            // Выбор
            int choice = readInt("Выбор", 0, 5, null);
            boolean needPause = false;
            switch (choice) {
                case 1 -> needPause = !r.togglePower();
                case 2 -> needPause = !r.changeVolume(VOLUME_STEP);
                case 3 -> needPause = !r.changeVolume(-VOLUME_STEP);
                case 4 -> needPause = !chooseChannel(r);
                case 5 -> { r.showBatteryState(); needPause = true; }
                case 0 -> back = true;
            }
            if (needPause || (r.isDead() && !back)) pause();
        }
    }
}
