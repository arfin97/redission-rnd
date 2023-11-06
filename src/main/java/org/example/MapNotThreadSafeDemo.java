package org.example;

import java.util.HashMap;
import java.util.Map;

public class MapNotThreadSafeDemo {

    public static void main(String[] args) throws InterruptedException {
        final Map<Integer, Integer> map = new HashMap<>();

        // Thread A: Inserts elements into the map
        Thread threadA = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                map.put(i, i);
            }
        });

        // Thread B: Inserts elements into the map
        Thread threadB = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                map.put(i, i);
            }
        });

        threadA.start();
        threadB.start();

        // Wait for both threads to finish
        threadA.join();
        threadB.join();

        // Check the size of the map
        System.out.println("Expected map size is 1000");
        System.out.println("Actual map size is " + map.size());
    }
}
