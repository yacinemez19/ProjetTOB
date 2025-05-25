package examples;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutorServiceExample {
    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable task = () -> {
            System.out.println("Tic ! Temps : " + System.currentTimeMillis());
        };

        // Exécuter la tâche toutes les 1 seconde (1000 millisecondes)
        scheduler.scheduleAtFixedRate(task, 0, 1000, TimeUnit.MILLISECONDS);

        // Laisser tourner pendant quelques secondes puis arrêter
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        scheduler.shutdown();
    }
}
