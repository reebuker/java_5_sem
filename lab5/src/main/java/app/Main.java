package app;

import controller.MenuController;
import model.RemoteRepository;
import view.ConsoleView;

// Здесь только собираем три части MVC вместе: модель, view и controller
public class Main {
    public static void main(String[] args) {
        RemoteRepository remotes = new RemoteRepository(); // Model
        ConsoleView view = new ConsoleView(); // View
        new MenuController(remotes, view).run(); // Controller
    }
}
