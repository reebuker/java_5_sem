package model;

import model.remote.Remote;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// все пульты в памяти. id выдаётся по порядку добавления, а новая база это
// новый репозиторий, поэтому нумерация у каждой базы своя
public class RemoteRepository {
    // Data
    private final Map<Integer, Remote> remotes = new LinkedHashMap<>(); // порядок добавления сохраняется
    private int nextId = 0;

    // Methods
    public void add(Remote remote) {
        remote.assignId(nextId);
        remotes.put(nextId, remote);
        nextId++;
    }

    // замена одной базы другой: старые пульты забываются, нумерация снова с нуля
    public void replaceAll(Collection<Remote> list) {
        remotes.clear();
        nextId = 0;
        for (Remote remote : list)
            add(remote);
    }

    // null, если пульта с таким id нет
    public Remote get(int id) {
        return remotes.get(id);
    }

    public void remove(int id) {
        remotes.remove(id);
    }

    // копия, чтобы снаружи нельзя было поменять состав
    public List<Remote> all() {
        return List.copyOf(remotes.values());
    }

    public boolean isEmpty() {
        return remotes.isEmpty();
    }
}
