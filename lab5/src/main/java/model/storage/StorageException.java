package model.storage;

import java.io.IOException;

// Ошибка с файлом, причину которой мы знаем сами и можем объяснить по-русски:
// это не обычный файл, он слишком большой, в него нельзя писать
public class StorageException extends IOException {
    private static final long serialVersionUID = 1L;

    public StorageException(String message) {
        super(message);
    }
}
