import java.util.Scanner;

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
    public static void main() {
        Scanner scanner = new Scanner(System.in);
        
    }
}
