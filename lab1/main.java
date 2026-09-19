import java.util.Scanner;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;

public class Main {   
    public static int[] getArray(Scanner scanner, int number, int size) {
        String[] tokens = new String[0]; 
        while (tokens.length != size) {
            System.out.println("Введите числа в %d массив (%d чисел): ".formatted(number, size));
            String input = scanner.nextLine();
            tokens = input.trim().split("\\s+"); 
        }
        int[] arr = new int[tokens.length];

        for (int i = 0; i < tokens.length; i++) {
            arr[i] = Integer.parseInt(tokens[i]);
        }

        return arr;
    }
    public static void fillList(Scanner scanner, int number, int size, List<Integer> list) {
        String[] tokens = new String[0]; 
        while (tokens.length != size) {
            System.out.println("Введите числа в %d список (%d чисел): ".formatted(number, size));
            tokens = scanner.nextLine().trim().split("\\s+"); 
        }
        for (String token : tokens) {
            list.add(Integer.parseInt(token));
        }
    }

    public static int findMaxId(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("Пустой список передан в findMaxid");
            return -1;
        }
        int id = 0;
        int max = list.get(0);
        int i = 0;
        for (int value : list) {
            if (value  > max) {
                max = value;
                id = i;
            }
            i++;
        }
        return id;
    }
    public static int findMaxId(int[] arr) {
        if (arr == null || arr.length == 0) {
            System.out.println("Пустой массив передан в findMaxId");
            return -1;
        }
        int id = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > arr[id]) {
                id = i;
            }
        }
        return id;
    }

    public static void swapFirstToMax(int[] arr, int id) {
        if (arr == null || arr.length == 0) {
            System.out.println("Пустой массив передан в swapFirstToMax");
            return;
        }
        if (id >= arr.length || id < 0) {
            System.out.println("Некорректный id");
            return;
        }
        int temp = arr[id];
        arr[id] = arr[0];
        arr[0] = temp;
    }

    public static void swapFirstToMax(List<Integer> list, int id) {
        if (list == null || list.isEmpty()) {
            System.out.println("Пустой список передан в swapFirstToMax");
            return;
        }
        if (id >= list.size() || id < 0) {
            System.out.println("Некорректный id");
            return;
        }
        Collections.swap(list, 0, id);
    }

    public static void handleTask(Scanner scanner, int number, int size) {
        int[] arr = getArray(scanner, number, size);

        System.out.println("Получен список #%d:".formatted(number));
        System.out.println(arr);

        int id = findMaxId(arr);
        System.out.println("Индекс макс. элемента: " + id);

        swapFirstToMax(arr, id);
        System.out.println("список #%d после свапа первого и макс. элемента: ".formatted(number));
        System.out.println(Arrays.toString(arr));
    }

    public static void handleTask(Scanner scanner, int number, int size, List<Integer> list) {
        fillList(scanner, number, size, list);

        System.out.println("Получен список #%d:".formatted(number));
        System.out.println(list);

        int id = findMaxId(list);
        System.out.println("Индекс макс. элемента: " + id);

        swapFirstToMax(list, id);
        System.out.println("список #%d после свапа первого и макс. элемента: ".formatted(number));
        System.out.println(list);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        handleTask(scanner, 1, 6);
        handleTask(scanner, 2, 3, new ArrayList<>());
        handleTask(scanner, 3, 4, new LinkedList<>());
    }
}
