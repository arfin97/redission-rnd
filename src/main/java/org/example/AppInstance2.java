package org.example;

public class AppInstance2 {
    public static void main(String[] args) throws InterruptedException {
        // Sleep to let AppInstance1 add some items first
        Thread.sleep(2000);

        // Add to Java list
        SharedLogic.addToJavaList("Instance2-Item1");
        // Add to Redisson list
        SharedLogic.addToRedissonList("Instance2-Item1");

        SharedLogic.getValue("key1");

        // Print both lists
        SharedLogic.printJavaList();
        SharedLogic.printRedissonList();
    }
}
