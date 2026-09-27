import java.util.Scanner;
import java.util.ArrayList;

public class Menu {
    private int exitOption = 8;
    private Scanner scanner;
    private ArrayList<Remote> items;


    Menu(Scanner scanner) {
        this.scanner = scanner;
        items = new ArrayList<Remote>();
    }

    // Control methods
    private void newObject() {
        printHead();
        System.out.print("| Введите производителя (enter для пропуска): ");
        String manufacturer = scanner.nextLine();

        printHead();
        System.out.print("| Введите кол-во кнопок (enter для пропуска): ");
        int buttons = 0;
        String buttonsLine = scanner.nextLine();
        if (!buttonsLine.isBlank()) {
            buttons = Integer.parseInt(buttonsLine);
        }

        printHead();
        System.out.print("| Введите цену (enter для пропуска): ");
        double price = 0;
        String priceLine = scanner.nextLine();
        if (!priceLine.isBlank()) {
            price = Double.parseDouble(priceLine);
        }

        Remote remote = new Remote(manufacturer, buttons, price);
        items.add(remote);
    }
    /*
    case 1 -> newObject();
    case 2 -> joinToArray();
    case 3 -> printObject();
    case 4 -> changeObject();
    case 5 -> addToArray();
    case 6 -> removeFromArray();
    case 7 -> calculateVar();
    */

    

    // Terminal methods
    private void printHead() {
        // System.out.print("\033[H\033[2J");
        // System.out.flush();
        System.out.println("-----------------------------------------------");
        System.out.println("| Лабораторная работа 2: пульты от телевизора |");
        System.out.println("-----------------------------------------------");
    }
    private void printTail() {
        System.out.println("-----------------------------------------------");
    }

    private void printOptionsMain() {
        printHead();
        System.out.println("| Выберите опцию: (1-8)                       |");
        System.out.println("| 1. Создать новый объект                     |");
        System.out.println("| 2. Объеденить объекты в массив              |");
        System.out.println("| 3. Отобразить свойства объекта              |");
        System.out.println("| 4. Изменить свойства объекта                |");
        System.out.println("| 5. Добавить объекты в массив                |");
        System.out.println("| 6. Удалить объекты из массива               |");
        System.out.println("| 7. Вычислить заданный параметр              |");
        System.out.println("| 8. Выход из программы                       |");
        printTail();
    }

    private int getValidInput(int end) {
        int input = 0;
        while (input < 1 || input > end) {
            if (scanner.hasNextInt()) {
                input = scanner.nextInt();
            }
            else {
                scanner.nextLine();
            }
        }
        return input;
    }

    public void start() {
        int option;
        while (true) {
            //option = getValidInput(8);

            int option = 0;
            while (option < 1 || option > 8) {
                printOptionMain();
                if (scanner.hasNextInt()) {
                    option = scanner.nextInt();
                }
                else {
                    scanner.nextLine();
                }
            }

            switch (option) {
                case 1 -> newObject();
                case 2 -> joinToArray();
                case 3 -> printObject();
                case 4 -> changeObject();
                case 5 -> addToArray();
                case 6 -> removeFromArray();
                case 7 -> calculateVar();
                case 8 -> return;
            }
        }
    }

}
