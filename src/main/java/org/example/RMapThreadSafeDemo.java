package org.example;

import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.HashMap;
import java.util.Map;

public class RMapThreadSafeDemo {

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379"); // Use your Redis server address
        RedissonClient redisson = Redisson.create(config);
        final RMap<Integer, Integer> map = redisson.getMap("myMap2");

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
        map.clear();
    }
}
