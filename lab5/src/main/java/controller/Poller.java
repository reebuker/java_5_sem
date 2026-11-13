package controller;

// Задача «делай что-то каждые N секунд, пока не прервут». Это Runnable, а сам поток
// создаёт и запускает AppController. Останавливается через interrupt(), как на лекции:
// цикл проверяет isInterrupted(), а sleep при прерывании бросает InterruptedException
public class Poller implements Runnable {
    private final long periodMillis;
    private final Runnable tick; // что делать каждый раз
    private final Runnable onStop; // что сделать перед завершением потока
    private volatile RuntimeException error; // выставляется в потоке задачи, читается из main

    public Poller(int periodSeconds, Runnable tick, Runnable onStop) {
        this.periodMillis = periodSeconds * 1000L;
        this.tick = tick;
        this.onStop = onStop;
    }

    @Override
    public void run() {
        try {
            // nanoTime, а не часы с датой: если время в системе переведут назад, ждать не станем часами
            long next = System.nanoTime();
            while (!Thread.currentThread().isInterrupted()) {
                tick.run();
                // ждём до следующего тика, а не period после конца работы, чтобы время не уплывало
                next += periodMillis * 1_000_000L;
                long wait = (next - System.nanoTime()) / 1_000_000L;
                if (wait > 0)
                    Thread.sleep(Math.min(wait, periodMillis));
                else
                    next = System.nanoTime(); // работа затянулась, догонять пропущенное не будем
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // возвращаем флаг, цикл выше уже не продолжится
        } catch (RuntimeException e) {
            error = e; // main заберёт её после join() и сообщит, а поток закончится как обычно
        } finally {
            onStop.run();
        }
    }

    // неожиданная ошибка, с которой упала задача, или null, если всё было нормально
    public RuntimeException error() {
        return error;
    }
}
