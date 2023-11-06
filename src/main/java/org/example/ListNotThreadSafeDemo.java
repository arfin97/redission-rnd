package org.example;

import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListNotThreadSafeDemo {

    public static void main(String[] args) throws InterruptedException {

        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379"); // Use your Redis server address
        RedissonClient redisson = Redisson.create(config);

        RList<Integer> list = redisson.getList("anyList");

        // Thread A tries to add elements to the list
        Thread threadA = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                list.add(i);
            }
        });

        // Thread B tries to add elements to the list at the same time
        Thread threadB = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                list.add(i);
            }
        });

        threadA.start();
        threadB.start();

        // Wait for threads to finish
        threadA.join();
        threadB.join();

        // Check the size of the list
        System.out.println("Expected list size is 2000");
        System.out.println("Actual list size is " + list.size());
        list.clear();
        redisson.shutdown();
    }
}
