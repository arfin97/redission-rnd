package org.example.redission;

import org.redisson.Redisson;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedissonRMapCacheThreadSafeDemo {

    public static void main(String[] args) throws InterruptedException {
        // Configure Redisson client
        Config config = new Config();
        config.useSingleServer()
          .setAddress("redis://127.0.0.1:6379");

        // Create Redisson client instance
        RedissonClient redisson = Redisson.create(config);

        // Get a Redisson-backed map
        RMapCache<Integer, Integer> map = redisson.getMapCache("myMap");


        // Define the number of threads and operations
        int numberOfThreads = 10;
        int numberOfOperations = 1000;

        // Create threads and run them
        Thread[] threads = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int op = 0; op < numberOfOperations; op++) {
                    map.fastPut(op, op);
                }
            });
            threads[i].start();
        }

        // Wait for all threads to finish
        for (Thread thread : threads) {
            thread.join();
        }

        // Check the size of the map
        System.out.println("Expected map size is " + numberOfOperations);
        System.out.println("Actual map size is " + map.size());
        map.clear();
        // Shutdown Redisson client
        redisson.shutdown();
    }
}
