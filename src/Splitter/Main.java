package Splitter;

import java.io.IOException;

class Main {
    public static void main(String[] args) {
        try {
            Splitter s = new Splitter(args);
            s.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}