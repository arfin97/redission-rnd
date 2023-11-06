package org.example;

import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.ArrayList;
import java.util.List;

public class SharedLogic {

    private static List<String> javaList = new ArrayList<>();

    public static void addToJavaList(String item) {
        javaList.add(item);
    }

    public static void printJavaList() {
        System.out.println("Java List: " + javaList);
    }

    public static void addToRedissonList(String item) {
        RedissonClient redisson = getRedissonClient();
        RList<String> redissonList = redisson.getList("distributedList");
        redissonList.add(item);
        redisson.shutdown();
    }

    public static void printRedissonList() {
        RedissonClient redisson = getRedissonClient();
        RList<String> redissonList = redisson.getList("distributedList");
        System.out.println("Redisson List: " + redissonList);
        redisson.shutdown();
    }

    public static void getValue(String key){
        RedissonClient redisson = getRedissonClient();
        RMap<String, String> map = redisson.getMap("myMap");
        System.out.println("Value: " + map.get(key));
        redisson.shutdown();
    }

    public static void setValue(String key, String value){
        RedissonClient redisson = getRedissonClient();
        RMap<String, String> map = redisson.getMap("myMap");
        map.put(key, value);
        redisson.shutdown();
    }

    private static RedissonClient getRedissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        return Redisson.create(config);
    }
}
