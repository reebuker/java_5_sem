package app;

import controller.AppController;
import model.RemoteRepository;
import view.ConsoleView;

// Здесь только собираем три части MVC вместе: модель, view и controller
public class Main {
    public static void main(String[] args) {
        RemoteRepository remotes = new RemoteRepository(); // Model
        ConsoleView view = new ConsoleView(); // View
        int code;
        try {
            code = new AppController(remotes, view).run(args); // Controller
        } catch (RuntimeException | StackOverflowError e) {
            // ошибка в самой программе: говорим об этом коротко вместо длинной трассировки
            view.showInternalError(e);
            code = AppController.EXIT_INTERNAL;
        }
        if (code != 0)
            System.exit(code);
    }
}
