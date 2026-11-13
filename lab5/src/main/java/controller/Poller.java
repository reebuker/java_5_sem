package controller;

// Задача «делай что-то каждые N секунд, пока не прервут». Это Runnable, а сам поток
// создаёт и запускает AppController. Останавливается через interrupt(), как на лекции:
// цикл проверяет isInterrupted(), а sleep при прерывании бросает InterruptedException
public class Poller implements Runnable {
    private final long periodMillis;
    private final Runnable tick; // что делать каждый раз
    private final Runnable onStop; // что сделать перед завершением потока
    private volatile boolean failed = false; // выставляется в потоке задачи, читается из main

    public Poller(int periodSeconds, Runnable tick, Runnable onStop) {
        this.periodMillis = periodSeconds * 1000L;
        this.tick = tick;
        this.onStop = onStop;
    }

    @Override
    public void run() {
        try {
            long next = System.currentTimeMillis();
            while (!Thread.currentThread().isInterrupted()) {
                tick.run();
                // ждём до следующего тика, а не period после конца работы, чтобы время не уплывало
                next += periodMillis;
                long wait = next - System.currentTimeMillis();
                if (wait > 0)
                    Thread.sleep(wait);
                else
                    next = System.currentTimeMillis(); // работа затянулась, догонять пропущенное не будем
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // возвращаем флаг, цикл выше уже не продолжится
        } catch (RuntimeException e) {
            failed = true;
            throw e; // ошибку в программе скрывать не надо, пусть её напечатает JVM
        } finally {
            onStop.run();
        }
    }

    // true, если задача упала с неожиданной ошибкой
    public boolean failed() {
        return failed;
    }
}
