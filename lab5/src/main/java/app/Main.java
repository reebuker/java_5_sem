package app;

import controller.AppController;
import model.RemoteRepository;
import view.ConsoleView;

// Здесь только собираем три части MVC вместе: модель, view и controller
public class Main {
    public static void main(String[] args) {
        RemoteRepository remotes = new RemoteRepository(); // Model
        ConsoleView view = new ConsoleView(); // View
        int code = new AppController(remotes, view).run(args); // Controller
        if (code != 0)
            System.exit(code);
    }
}
