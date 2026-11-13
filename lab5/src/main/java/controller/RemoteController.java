package controller;

import model.remote.ActionResult;
import model.remote.Remote;
import view.ConsoleView;

import java.util.List;

// Controller: работа с одним пультом. Читает выбор из view, нажимает кнопки у
// пульта из модели и просит view показать, что получилось
public class RemoteController {
    private static final int VOLUME_STEP = 5;
    private static final int EXTRA_START = 6; // с этого номера идут действия конкретного пульта

    private final ConsoleView view;

    public RemoteController(ConsoleView view) {
        this.view = view;
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
            if (needPause || (r.isDead() && !back))
                view.pause();
        }
    }

    // показывает сообщение по результату нажатия, true если кнопку нажали
    private boolean report(ActionResult result) {
        view.showResult(result);
        return result.pressed();
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
