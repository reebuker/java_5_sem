import java.util.Scanner;
import java.util.Arrays;
import java.util.LinkedList;

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

    public static int findMaxId(int[] arr) {
        if (arr == null || arr.length == 0) {
            System.out.println("Пустой массив передан в findMaxid");
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

    public static void handleTask(Scanner scanner, int number, int size) {
        int[] arr = getArray(scanner, number, size);

        System.out.println("Получен массив #%d:".formatted(number));
        System.out.println(Arrays.toString(arr));

        int id = findMaxId(arr);
        System.out.println("Индекс макс. элемента: " + id);

        swapFirstToMax(arr, id);
        System.out.println("Массив #%d после свапа первого и макс. элемента: ".formatted(number));
        System.out.println(Arrays.toString(arr));
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        handleTask(scanner, 1, 6);
        handleTask(scanner, 2, 3);
        handleTask(scanner, 3, 4);
    }
}
