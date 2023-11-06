package org.example;

import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ListConcurrencyDemo {

    public static void main(String[] args) throws InterruptedException {
        // Initialize Redisson client
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379"); // Use your Redis server address
        RedissonClient redisson = Redisson.create(config);

        // Get a thread-safe Redisson list
        RList<String> rList = redisson.getList("myList");
        rList.clear(); // Clear the Redisson list to start fresh

        // Create a non-thread-safe Java ArrayList
        List<String> arrayList = new ArrayList<>();

        // Executor service for handling concurrent tasks
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // Perform concurrent operations on both lists
        for (int i = 0; i < 100; i++) {
            int index = i; // Effectively final for lambda usage
            executorService.submit(() -> {
                String threadName = Thread.currentThread().getName();
                String element = threadName + "-" + index;

                // Add element to Redisson list
                rList.add(element);

                // Attempt to add element to ArrayList (this is not thread-safe!)
                synchronized (arrayList) {
                    arrayList.add(element);
                }

                System.out.println("Added by " + threadName + ": " + element);
            });
        }

        // Shutdown executor and wait for all tasks to complete
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // Print out Redisson list elements to confirm they are intact
        System.out.println("Redisson RList Elements:");
        rList.forEach(System.out::println);

        // Print out ArrayList elements to confirm if there were issues
        System.out.println("ArrayList Elements:");
        synchronized (arrayList) {
            arrayList.forEach(System.out::println);
        }

        // Cleanup and close Redisson client
        rList.clear();
        redisson.shutdown();
    }
}
