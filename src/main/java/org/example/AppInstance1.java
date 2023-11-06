package org.example;

public class AppInstance1 {
    public static void main(String[] args) throws InterruptedException {
        // Add to Java list
        SharedLogic.addToJavaList("Instance1-Item1");
        // Add to Redisson list
        SharedLogic.addToRedissonList("Instance1-Item1");

        SharedLogic.setValue("key1", "this value was set from instance 1");

        // Sleep to simulate some delay
        Thread.sleep(5000);

        // Print both lists
        SharedLogic.printJavaList();
        SharedLogic.printRedissonList();
    }
}
