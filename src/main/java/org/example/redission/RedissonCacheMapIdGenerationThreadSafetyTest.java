package org.example.redission;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RedissonCacheMapIdGenerationThreadSafetyTest {

    public static void main(String[] args) throws InterruptedException {
        // Configure RedissonClient
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        RedissonClient redisson = Redisson.create(config);

        int testSize = 4000;

        // Get a RCache map
        RMapCache<String, String> map = redisson.getMapCache("RedissonCacheMapIdGenerationThreadSafetyTest");
        map.clear();

        //create a normal map
//        Map<String, String> map = new HashMap<>();

        // Populate the map with 10 strings
        for (int i = 1; i <= testSize; i++) {
            String key = "IB0" + i;
            map.put(key, key);
        }
        int idGenerated = map.size();

        // A thread-safe set to store the retrieved values
        HashMap<String, Integer> valueCountMap = new HashMap<>();
        RLock lock = redisson.getLock("myLock");

        // Executor service with a fixed thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(testSize);

        int idIndex = 1;
        AtomicInteger linePrinted = new AtomicInteger(1);
        // Submit 10 tasks to the executor service
//        for (int i = 1; i <= testSize; i++) {
//            final String key = "IB0" + idIndex++;
//            executorService.submit(() -> {
//                try {
//                    String value = map.get(key);
//                    if (value != null) {
//                        // If value is retrieved, immediately evict the key.
//                        map.remove(key);
//                        System.out.println((linePrinted.getAndIncrement()) + ". Thread " + Thread.currentThread().getName() +
//                                           " retrieved and evicted key: " + key + ", value: " + value);
//                        // Update the count of this value in the map
//                        valueCountMap.merge(value, 1, Integer::sum);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//        }

        Thread[] threads = new Thread[testSize];

        // Create 1000 threads
        for (int i = 0; i < threads.length; i++) {
            AtomicInteger threadNumber = new AtomicInteger(i);
            final String key = "IB0" + 1;
            threads[i] = new Thread(() -> {
                threadNumber.getAndIncrement();
                try {
                    // wait for lock aquisition up to 100 seconds
                    // and automatically unlock it after 10 seconds
                    boolean res = lock.tryLock(120, 2, TimeUnit.SECONDS);
                    if (res) {
                        String value = map.get(key);
                        if (value != null) {
                            // If value is retrieved, immediately evict the key.
                            map.remove(key);
                            System.out.println((linePrinted.getAndIncrement()) + ". Thread " + Thread.currentThread().getName() +
                                    " retrieved and evicted key: " + key + ", value: " + value);
                            // Update the count of this value in the map
                            valueCountMap.merge(value, 1, Integer::sum);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (lock != null && lock.isLocked() && lock.isHeldByCurrentThread()) {
                        try {
                            lock.unlock();
                            System.out.println(threadNumber + ". Thread " + Thread.currentThread().getName() + " unlocked");
                        } catch (Exception e){
                            System.out.println("unlock: " + Thread.currentThread().getName());
                            System.out.println("lock.isLocked(): " + lock.isLocked());
                            System.out.println("lock.isHeldByCurrentThread(): " + lock.isHeldByCurrentThread());
                            e.printStackTrace();
                        }

                    }
                }
            });
        }

        // Start the threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for the threads to finish
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread interrupted: " + e.getMessage());
        }

        System.out.println("All threads have completed.");


        // Shutdown the executor and wait for all tasks to complete
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // Check for duplicates in the valueCountMap
        System.out.println("genereated id count: " + idGenerated);
        System.out.println("valuCountMap size: " + valueCountMap.size());
        valueCountMap.forEach((k, v) -> {
            if (v > 1) {
                System.out.println("Duplicate value found: " + k + ", count: " + v);
                throw new RuntimeException("Duplicate value found: " + k + ", count: " + v);
            }
        });
        System.out.println("No duplicates found in matching test");
        map.clear();

        // Shutdown the executor and wait for all tasks to complete
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // Close Redisson client
        redisson.shutdown();
    }
}
