package controller;

import model.remote.ActionResult;
import model.remote.Remote;
import view.ConsoleView;

import java.util.List;
import java.util.function.BooleanSupplier;

// Controller: работа с одним пультом. Читает выбор из view, нажимает кнопки у
// пульта из модели и просит view показать, что получилось
public class RemoteController {
    private static final int VOLUME_STEP = 5;
    private static final int EXTRA_START = 6; // с этого номера идут действия конкретного пульта

    private final ConsoleView view;
    private final BooleanSupplier save; // сохраняет базу, true если вышло

    public RemoteController(ConsoleView view, BooleanSupplier save) {
        this.view = view;
        this.save = save;
    }

    public void work(Remote r) {
        boolean back = false;
        while (!back) {
            List<String> extras = r.getExtraActions();
            view.clear();
            view.showControlPanel(r, VOLUME_STEP, EXTRA_START);

            int choice = view.askInt("Выбор", 0, EXTRA_START - 1 + extras.size(), null);
            boolean wasDead = r.isDead();
            boolean needPause = false;
            switch (choice) {
                case 1 -> needPause = !report(r.togglePower());
                case 2 -> needPause = !report(r.changeVolume(VOLUME_STEP));
                case 3 -> needPause = !report(r.changeVolume(-VOLUME_STEP));
                case 4 -> needPause = !chooseChannel(r);
                case 5 -> {
                    view.showBattery(r);
                    needPause = true;
                }
                case 0 -> back = true;
                default -> needPause = !report(r.runExtra(choice - EXTRA_START));
            }
            // пульт сам не печатает, поэтому про то что батарейки сели говорим здесь
            if (!wasDead && r.isDead())
                view.showBatteryDied();
            // после нажатия кнопки сразу сохраняем, чтобы --watch и --export видели изменение.
            // Выход назад и показ батареек пульт не меняют
            boolean saved = (choice == 0 || choice == 5) || save.getAsBoolean();
            if (needPause || !saved || (r.isDead() && !back))
                view.pause();
        }
    }

    // показывает сообщение по результату нажатия, true если всё прошло без сообщения.
    // Иначе нужна пауза, а то экран очистится раньше, чем сообщение успеют прочитать
    private boolean report(ActionResult result) {
        view.showResult(result);
        return result == ActionResult.OK;
    }

    private boolean chooseChannel(Remote r) {
        List<String> list = r.getChannels();
        if (list.isEmpty()) {
            view.showNoChannels();
            return false;
        }
        if (!report(r.checkReady()))
            return false;
        view.showChannels(r);
        return report(r.setChannel(view.askInt("Номер", 1, list.size(), null)));
    }
}
