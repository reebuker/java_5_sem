# Лабораторная работа 5

Вариант 8, пульты управления.

## Сборка и запуск

Нужны Java 21 и Maven.

    cd lab5
    mvn package
    java -jar target/main.jar

Пока программа работает как lab4, но пульты хранятся в csv: открывается меню,
пункт 6 заменяет все пульты теми, что лежат в файле (старые забываются),
пункт 7 записывает пульты в файл. В broken.csv лежат кривые строки для проверки
терпимого чтения.

## Структура (MVC)

    src/main/java
    ├── app/Main.java                    собирает три части вместе
    ├── model/                           Model: данные и правила, ничего не печатает
    │   ├── RemoteRepository.java        все пульты в памяти, выдаёт id
    │   ├── remote/                      пульты (Remote и три вида), RemoteState, ActionResult
    │   ├── device/                      устройства (Tv, Stereo, SmartHome)
    │   └── storage/                     чтение и запись csv (Csv, CsvStorage)
    ├── view/ConsoleView.java            View: весь вывод и ввод в консоли
    └── controller/                      Controller: решает, что делать
        ├── MenuController.java          главное меню, список пультов, файл
        └── RemoteController.java        работа с одним пультом

Поток такой: контроллер спрашивает выбор у view, меняет модель и просит view
показать результат. View только показывает модель и ничего в ней не меняет,
а в модели и контроллере нет System.out и Scanner.

## Файл с пультами

Одна строка это один пульт, первая строка с названиями столбцов необязательна.
Столбцы по порядку:

    type,manufacturer,model,batteryType,batteryLevel,on,volume,position,appliances,extra1,extra2

- type: TvRemote, StereoRemote или SmartHomeRemote
- position: номер канала или трека, appliances: приборы умного дома, например 101
- extra1 и extra2: свои поля пульта (у телевизора прошлый и случайный канал)
- пустое или кривое значение заменяется умолчанием, строка неизвестного типа пропускается
