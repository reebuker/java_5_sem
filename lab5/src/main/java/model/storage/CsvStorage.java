package model.storage;

import model.device.Device;
import model.remote.Remote;
import model.remote.RemoteState;
import model.remote.SmartHomeRemote;
import model.remote.StereoRemote;
import model.remote.TvRemote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

// база пультов в csv, одна строка это один пульт, порядок столбцов как в HEADER.
// Читает терпимо: строка неизвестного типа пропускается, а свойство, которого нет или
// которое кривое, заменяется умолчанием, так что бред в пульт не попадает
public class CsvStorage {
    // Data
    public static final List<String> HEADER = List.of("type", "manufacturer", "model", "batteryType",
            "batteryLevel", "on", "volume", "position", "appliances", "extra1", "extra2");

    // номера столбцов, как в HEADER
    private static final int TYPE = 0;
    private static final int MANUFACTURER = 1;
    private static final int MODEL = 2;
    private static final int BATTERY_TYPE = 3;
    private static final int BATTERY_LEVEL = 4;
    private static final int ON = 5;
    private static final int VOLUME = 6;
    private static final int POSITION = 7;
    private static final int APPLIANCES = 8;
    private static final int EXTRA1 = 9;
    private static final int EXTRA2 = 10;

    private static final Set<String> TYPES = Set.of("TvRemote", "StereoRemote", "SmartHomeRemote");
    private static final Pattern APPLIANCES_VALUE = Pattern.compile("[01]+");
    private static final int MAX_POSITION = 1000;
    private static final int MAX_COUNTER = Remote.MAX_COUNTER;

    // защита от огромных и подложных файлов: больше этого не читаем, чтобы не съесть всю память
    public static final int MAX_REMOTES = 10_000;
    private static final int MAX_FILE_BYTES = 8 * 1024 * 1024;

    // итоги последнего чтения
    private int found; // строк известных типов, на каждую создаётся пульт
    private int unknown; // строк неизвестных типов, они пропущены
    private int tooMany; // строк известных типов сверх MAX_REMOTES, они пропущены
    private int propertiesRead; // свойства прочитаны и корректны
    private int propertiesMissing; // свойства нет или оно пустое, оставлено умолчание
    private int propertiesInvalid; // свойство есть, но некорректное, оставлено умолчание

    // Getters
    public int getFound() {
        return found;
    }

    public int getTooMany() {
        return tooMany;
    }

    public int getUnknown() {
        return unknown;
    }

    public int getPropertiesRead() {
        return propertiesRead;
    }

    public int getPropertiesMissing() {
        return propertiesMissing;
    }

    public int getPropertiesInvalid() {
        return propertiesInvalid;
    }

    // Methods
    // пустые строки и строки с # в начале пропускаются, заголовок необязателен
    public List<Remote> load(Path file) throws IOException {
        found = unknown = tooMany = 0;
        propertiesRead = propertiesMissing = propertiesInvalid = 0;

        // папку, устройство или канал читать нельзя: канал и вовсе зависнет, пока в него не напишут
        if (!Files.isRegularFile(file)) {
            if (Files.notExists(file))
                throw new NoSuchFileException(file.toString());
            throw new StorageException("это не обычный файл");
        }
        byte[] bytes;
        try (InputStream in = Files.newInputStream(file)) {
            bytes = in.readNBytes(MAX_FILE_BYTES + 1);
        }
        if (bytes.length > MAX_FILE_BYTES)
            throw new StorageException("файл больше " + MAX_FILE_BYTES / (1024 * 1024) + " МБ");
        // new String, а не Files.newBufferedReader: битые байты он заменяет, а не падает
        String text = new String(bytes, StandardCharsets.UTF_8);

        List<Remote> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
            boolean firstLine = true;
            boolean headerPossible = true;
            String line;
            while ((line = reader.readLine()) != null) {
                if (firstLine && line.startsWith("﻿")) // метка порядка байт, её ставит excel
                    line = line.substring(1);
                firstLine = false;

                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                List<String> columns = Csv.parseLine(line);
                boolean isHeader = headerPossible && get(columns, TYPE).equalsIgnoreCase("type");
                headerPossible = false; // заголовок может быть только первой строкой
                if (isHeader)
                    continue;
                addRemote(result, columns);
            }
        }
        return result;
    }

    // пишет во временный файл и подменяет целиком, чтобы тот, кто читает файл в это же
    // время, не увидел его недописанным
    public void save(Collection<Remote> remotes, Path file) throws IOException {
        file = realTarget(file);
        List<String> lines = new ArrayList<>();
        lines.add(Csv.join(HEADER));
        for (Remote r : remotes)
            lines.add(Csv.join(row(r)));

        Path tmp = Files.createTempFile(directoryOf(file), "remotes", ".tmp");
        try {
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            moveOver(tmp, file);
        } finally {
            Files.deleteIfExists(tmp); // после удачной подмены его уже нет
        }
    }

    // заменяет target файлом source целиком, как есть: сначала копия рядом с target,
    // потом атомарная подмена, поэтому читающий видит или старый файл, или новый, но не половину.
    // false, если source и target это один и тот же файл и менять нечего
    public boolean replaceFile(Path source, Path target) throws IOException {
        target = realTarget(target);
        if (Files.exists(target) && Files.isSameFile(source, target))
            return false;
        Path tmp = Files.createTempFile(directoryOf(target), "remotes", ".tmp");
        try {
            Files.copy(source, tmp, StandardCopyOption.REPLACE_EXISTING);
            moveOver(tmp, target);
        } finally {
            Files.deleteIfExists(tmp);
        }
        return true;
    }

    // папка, в которой лежит (или будет лежать) файл; у корня «/» её нет
    private static Path directoryOf(Path file) throws IOException {
        Path dir = file.toAbsolutePath().getParent();
        if (dir == null)
            throw new StorageException("у такого файла нет папки");
        return dir;
    }

    // если file это ссылка на другой файл, менять надо тот файл, а не саму ссылку
    private static Path realTarget(Path file) throws IOException {
        return Files.isSymbolicLink(file) && Files.exists(file) ? file.toRealPath() : file;
    }

    // временный файл создаётся с правами только для владельца; возвращаем прежние права файла
    // (у нового файла обычные rw-r--r--), иначе после первой же записи другой пользователь
    // или другая программа файл бы уже не прочитали. Если файловая система прав не знает, пропускаем
    private void copyPermissions(Path tmp, Path file) {
        try {
            Set<PosixFilePermission> permissions = Files.isRegularFile(file)
                    ? Files.getPosixFilePermissions(file)
                    : PosixFilePermissions.fromString("rw-r--r--");
            Files.setPosixFilePermissions(tmp, permissions);
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            // права не поменялись, файл всё равно запишется
        }
    }

    // атомарно, а если файловая система так не умеет, то обычным move
    private void moveOver(Path tmp, Path file) throws IOException {
        copyPermissions(tmp, file);
        try {
            Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private List<String> row(Remote r) {
        RemoteState s = r.state();
        return List.of(r.getClass().getSimpleName(), r.getManufacturer(), r.getModel(), r.getBatteryType(),
                Csv.text(s.batteryLevel()), Csv.text(s.on()), Csv.text(s.volume()), Csv.text(s.position()),
                Csv.text(s.appliances()), Csv.text(s.extra1()), Csv.text(s.extra2()));
    }

    // столбца нет в строке: считаем пустым
    private String get(List<String> columns, int index) {
        return index < columns.size() ? columns.get(index).trim() : "";
    }

    private void addRemote(List<Remote> result, List<String> columns) {
        String type = get(columns, TYPE);
        if (!TYPES.contains(type)) {
            unknown++;
            return;
        }
        if (result.size() >= MAX_REMOTES) {
            tooMany++;
            return;
        }
        found++;

        String manufacturer = checkName(get(columns, MANUFACTURER), Remote.DEFAULT_MANUFACTURER);
        String model = checkName(get(columns, MODEL), Remote.DEFAULT_MODEL);
        String battery = checkBattery(get(columns, BATTERY_TYPE));

        Remote remote = switch (type) {
            case "TvRemote" -> new TvRemote(manufacturer, model, battery);
            case "StereoRemote" -> new StereoRemote(manufacturer, model, battery);
            default -> new SmartHomeRemote(manufacturer, model, battery);
        };

        // проверяем только то, что бывает у этого типа, остальные столбцы не считаются
        boolean smartHome = type.equals("SmartHomeRemote");
        RemoteState state = new RemoteState(
                checkInt(get(columns, BATTERY_LEVEL), 0, Remote.MAX_BATTERY),
                checkBoolean(get(columns, ON)),
                smartHome ? null : checkInt(get(columns, VOLUME), 0, Device.MAX_VOLUME),
                smartHome ? null : checkInt(get(columns, POSITION), 1, MAX_POSITION),
                smartHome ? checkAppliances(get(columns, APPLIANCES)) : null,
                checkInt(get(columns, EXTRA1), 0, MAX_COUNTER),
                checkInt(get(columns, EXTRA2), smartHome ? -1 : 0, MAX_COUNTER));
        remote.restore(state);
        result.add(remote);
    }

    // нет, пусто, длиннее допустимого или с управляющими символами -> умолчание
    private String checkName(String raw, String def) {
        if (raw.isEmpty()) {
            propertiesMissing++;
            return def;
        }
        if (!Remote.isValidName(raw)) { // слишком длинное или с управляющими символами
            propertiesInvalid++;
            return def;
        }
        propertiesRead++;
        return raw;
    }

    // нет, пусто или не AA и не AAA -> умолчание
    private String checkBattery(String raw) {
        if (raw.isEmpty()) {
            propertiesMissing++;
            return Remote.DEFAULT_BATTERY;
        }
        String battery = Remote.normalizeBattery(raw);
        if (battery == null) {
            propertiesInvalid++;
            return Remote.DEFAULT_BATTERY;
        }
        propertiesRead++;
        return battery;
    }

    // число в границах, иначе null и пульт оставит то, что у него было
    private Integer checkInt(String raw, int min, int max) {
        if (raw.isEmpty()) {
            propertiesMissing++;
            return null;
        }
        try {
            int value = Integer.parseInt(raw);
            if (value >= min && value <= max) {
                propertiesRead++;
                return value;
            }
        } catch (NumberFormatException e) {
            // не число, считаем некорректным ниже
        }
        propertiesInvalid++;
        return null;
    }

    // true, false, 1 или 0
    private Boolean checkBoolean(String raw) {
        if (raw.isEmpty()) {
            propertiesMissing++;
            return null;
        }
        if (raw.equalsIgnoreCase("true") || raw.equals("1")) {
            propertiesRead++;
            return true;
        }
        if (raw.equalsIgnoreCase("false") || raw.equals("0")) {
            propertiesRead++;
            return false;
        }
        propertiesInvalid++;
        return null;
    }

    // только нули и единицы, например 101
    private String checkAppliances(String raw) {
        if (raw.isEmpty()) {
            propertiesMissing++;
            return null;
        }
        if (!APPLIANCES_VALUE.matcher(raw).matches()) {
            propertiesInvalid++;
            return null;
        }
        propertiesRead++;
        return raw;
    }
}
