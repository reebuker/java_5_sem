import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

class Menu {
    // Data
    private final List<Remote> remotes = new ArrayList<>();
    private final Scanner in = new Scanner(System.in);

    // Methods
    public void run() {
        try {
            boolean running = true;
            while (running) {
                printMenu();
                switch (readLine("Выбор: ")) {
                    case "1" -> showRemotes();
                    case "0" -> running = false;
                    default -> System.out.println("Нет такого пункта");
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("\nВвод закончился, выход");
        }
    }

    private void printMenu() {
        System.out.println("\n1 - показать пульты");
        System.out.println("0 - выход");
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

    // Input
    private String readLine(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }
}
