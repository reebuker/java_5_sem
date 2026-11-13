package model.storage;

import model.remote.Remote;
import model.remote.RemoteState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// Журнал изменяющихся свойств в csv. Одна строка это снимок одного пульта в момент time:
// заряд, включён ли, громкость, номер канала или трека, приборы и счётчики пульта.
// Файл только дописывается, а шапка пишется, если файл новый или пустой.
// Название, модель и тип батареек не меняются, поэтому в журнал не попадают
public class LogWriter {
    public static final List<String> HEADER = List.of("time", "id", "type", "batteryLevel", "on",
            "volume", "position", "appliances", "extra1", "extra2");

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // проверяет, что в файл можно писать, и создаёт его с шапкой, если его ещё нет.
    // Бросает IOException, если писать нельзя
    public void prepare(Path file) throws IOException {
        if (Files.exists(file) && (!Files.isRegularFile(file) || !Files.isWritable(file)))
            throw new StorageException("в файл нельзя писать");
        write(file, List.of());
    }

    // дописывает по строке на каждый пульт, возвращает сколько строк записано
    public int append(Path file, LocalDateTime time, Collection<Remote> remotes) throws IOException {
        List<String> rows = new ArrayList<>();
        for (Remote r : remotes)
            rows.add(Csv.join(row(time, r)));
        write(file, rows);
        return rows.size();
    }

    // одним вызовом дописывает шапку (если нужна) и строки
    private void write(Path file, List<String> rows) throws IOException {
        List<String> lines = new ArrayList<>();
        if (Files.notExists(file) || Files.size(file) == 0)
            lines.add(Csv.join(HEADER));
        lines.addAll(rows);
        if (lines.isEmpty())
            return;
        Files.write(file, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private List<String> row(LocalDateTime time, Remote r) {
        RemoteState s = r.state();
        return List.of(time.format(TIME), String.valueOf(r.getId()), r.getClass().getSimpleName(),
                Csv.text(s.batteryLevel()), Csv.text(s.on()), Csv.text(s.volume()), Csv.text(s.position()),
                Csv.text(s.appliances()), Csv.text(s.extra1()), Csv.text(s.extra2()));
    }
}
