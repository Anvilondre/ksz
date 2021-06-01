package lab02;

public class Main {
    public static void main(String[] args) {

        Data d = new Data();

        for (int i = 0; i < 5; i++) {
            new Worker(1, d);
            new Worker(2, d);
            new Worker(3, d);
        }
    }
}
