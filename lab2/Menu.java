import java.util.Scanner;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class Menu {
    private Scanner scanner;

    private List<String>                groupNames;
    private Map<Integer, Remote>        pool;

    Menu(Scanner scanner) {
        this.scanner = scanner;
        groupNames = new ArrayList<>();
        pool = new LinkedHashMap<>();
    }
        
    // Control methods
    private Remote editObject() {
        printHead();
        System.out.print("| Введите производителя (enter для пропуска): ");
        String manufacturerLine = scanner.nextLine();

        printHead();
        System.out.print("| Введите кол-во кнопок (enter для пропуска): ");
        String buttonsLine = scanner.nextLine();

        printHead();
        System.out.print("| Введите цену (enter для пропуска): ");
        String priceLine = scanner.nextLine();

        if (manufacturerLine.isBlank() && priceLine.isBlank() && buttonsLine.isBlank()) {
            return new Remote();
        }

        String manufacturer = "Unknown";
        int buttons = 0;
        double price = 0;

        if (!manufacturerLine.isBlank()) {
            manufacturer = manufacturerLine;
        }
        if (!buttonsLine.isBlank()) {
            buttons = Integer.parseInt(buttonsLine);
        }
        if (!priceLine.isBlank()) {
            price = Double.parseDouble(priceLine);
        }

        return new Remote(manufacturer, buttons, price);
    }

    private void listObjects() {
        printHead();
        if (pool.isEmpty()) {
            System.out.println("| В базе данных нет ни одного объекта                 |");
            System.out.println("-------------------------------------------------------");
            System.out.print("Нажмите Enter, чтобы вернуться в меню...");
            scanner.nextLine();
            return;
        }
        System.out.printf("| %-3s| %-15s| %-7s| %-7s| %-12s|%n", "ID", "Произв.", "Кнопки", "Цена", "Группа");
        System.out.println("-------------------------------------------------------");

        for (Map.Entry<Integer, Remote> entry : pool.entrySet()) {
            Remote item = entry.getValue();
            System.out.printf("| %-3d| %-15s| %-7d| %-7.2f| %-12s|%n",
                item.getId(), item.getManufacturer(), item.getButtons(), item.getPrice(), item.getGroup());
        }
    }

    // Main methods
    private void newObject() {
        Remote remote = editObject();
        pool.put(remote.getId(), remote);
    }

    private void addToArray() {
        listObjects();
        if (pool.isEmpty()) { return; }

        System.out.println("-------------------------------------------------------");
        System.out.print("| Введите ID объекта: ");
        int id = Integer.parseInt(scanner.nextLine());

        if (!pool.containsKey(id)) {
            System.out.println("| Объекта с таким ID не существует.");
            return;
        }

        System.out.println("-------------------------------------------------------");
        for (int i = 0; i < groupNames.size(); i++) {
            System.out.printf("| %-3d| %-47s|%n", (i + 1), groupNames.get(i));
        }
        System.out.printf("| %-3d| %-47s|%n", (groupNames.size() + 1), "Создать новую группу");

        System.out.println("-------------------------------------------------------");
        System.out.print("| Выберите группу: ");
        int choice = Integer.parseInt(scanner.nextLine());

        String targetGroup;
        if (choice == groupNames.size() + 1) {
            System.out.println("-------------------------------------------------------");
            System.out.print("| Введите название новой группы: ");
            targetGroup = scanner.nextLine();
            groupNames.add(targetGroup);
        } else {
            targetGroup = groupNames.get(choice - 1);
        }

        pool.get(id).setGroup(targetGroup);
    }

    private void printObject() {
        listObjects();
        if (pool.isEmpty()) { return; }

        System.out.println("-------------------------------------------------------");
        System.out.print("Нажмите Enter, чтобы вернуться в меню...");
        scanner.nextLine();
    }

    private void changeObject() {
        listObjects();
        if (pool.isEmpty()) { return; }

        System.out.println("-------------------------------------------------------");
        System.out.print("| Введите ID объекта: ");
        int id = Integer.parseInt(scanner.nextLine());

        if (!pool.containsKey(id)) {
            System.out.println("| Объекта с таким ID не существует.");
            return;
        }

        Remote remote = editObject();
        pool.get(id).setPrice(remote.getPrice());
        pool.get(id).setManufacturer(remote.getManufacturer());
        pool.get(id).setButtons(remote.getButtons());
    }

    private void removeObject() {
        listObjects();
        if (pool.isEmpty()) { return; }

        System.out.println("-------------------------------------------------------");
        System.out.print("| Введите ID объекта: ");
        int id = Integer.parseInt(scanner.nextLine());

        if (!pool.containsKey(id)) {
            System.out.println("| Объекта с таким ID не существует.");
            return;
        }
        
        System.out.println("-------------------------------------------------------");
        if (pool.get(id).getGroup() != Remote.defaultGroup) {
            System.out.printf("| %-3d| %-47s|%n", 1, "Удалить из группы " + pool.get(id).getGroup());
            System.out.printf("| %-3d| %-47s|%n", 2, "Удалить из базы данных");
            System.out.println("-------------------------------------------------------");
            System.out.print("| Выберите действие: ");
            int choice = Integer.parseInt(scanner.nextLine());
            System.out.println("-------------------------------------------------------");

        
            if (choice == 1) {
                System.out.printf("| Объект удален из группы %-28s|%n", pool.get(id).getGroup());
                pool.get(id).setGroup(Remote.defaultGroup);
            }
            else if (choice == 2) {
                pool.remove(id);
                System.out.println("| Объект удален из базы данных                        |");
            }
        }
        else {
            pool.remove(id);
            System.out.println("| Объект удален из базы данных                        |");
        }

        System.out.println("-------------------------------------------------------");
        System.out.print("Нажмите Enter, чтобы вернуться в меню...");
        scanner.nextLine();
    }

    private void calculateVar() {
        printHead();
        if (pool.isEmpty()) {
            System.out.println("| В базе данных нет ни одного объекта                 |");
        }
        else {
            int sum = 0;
            for (Remote item : pool.values()) {
                sum += item.getButtons();
            }
            System.out.printf("| Среднее кол-во кнопок: %-29.2f|%n", (double) sum / pool.size());
        }
        System.out.println("-------------------------------------------------------");
        System.out.print("Нажмите Enter, чтобы вернуться в меню...");
        scanner.nextLine();
    }

    // Terminal methods
    private void printHead() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println("-------------------------------------------------------");
        System.out.println("|     Лабораторная работа 2: пульты от телевизора     |");
        System.out.println("-------------------------------------------------------");
    }
    private void printTail() {
        System.out.println("-------------------------------------------------------");
    }

    private void printOptionsMain() {
        printHead();
        System.out.println("| Выберите опцию: (1-7)                               |");
        System.out.println("| 1. Создать новый объект                             |");
        System.out.println("| 2. Добавить объекты в массив                        |");
        System.out.println("| 3. Отобразить свойства объекта                      |");
        System.out.println("| 4. Изменить свойства объекта                        |");
        System.out.println("| 5. Удалить объекты из массива                       |");
        System.out.println("| 6. Вычислить заданный параметр                      |");
        System.out.println("| 7. Выход из программы                               |");
        printTail();
    }

    public void start() {
        while (true) {

            int option = 0;
            while (option < 1 || option > 7) {
                printOptionsMain();
                if (scanner.hasNextInt()) {
                    option = scanner.nextInt();
                    scanner.nextLine();
                }
                else {
                    scanner.nextLine();
                }
            }

            switch (option) {
                case 1 -> newObject();
                case 2 -> addToArray();
                case 3 -> printObject();
                case 4 -> changeObject();
                case 5 -> removeObject();
                case 6 -> calculateVar();
                case 7 -> { return; }
            }
        }
    }
}
