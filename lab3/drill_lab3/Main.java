abstract class Animal {
    void sound() {
        System.out.println("Animal sound()");
    }
    void sound(String s) {
        System.out.println("Animal sound(s)");
    }
    abstract void hello();
}

class Dog extends Animal {
    @Override
    void sound() {
        System.out.println("overrided sound()");
    } 

    void sound(String s) {
        System.out.println("overrided sound(s)");
    }
    
    void hello() {
        System.out.println("overrided abstract hello");
    }
}

interface Logger {
    void log(String message);
    default void info() { System.out.println("default mthod");}
}

class Api implements Logger {
    @Override
    public void log(String message) {
        System.out.println("Log log");
    }
}

class Main {
    public static void main(String[] args) {
        // Animal a = new Animal();
        Animal d = new Dog();

        Dog d2 = (Dog) d;
        
        d.sound();
        d.sound("s");
        d.hello();


    }
}
