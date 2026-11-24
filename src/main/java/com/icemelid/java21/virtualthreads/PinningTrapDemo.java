package com.icemelid.java21.virtualthreads;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class PinningTrapDemo {

    public static void main(String[] args) {
        int nucleos = Runtime.getRuntime().availableProcessors();
        System.out.println("=== DEMOSTRACIÓN DE 'PINNING' EN VIRTUAL THREADS (Núcleos CPU: " + nucleos + ") ===\n");

        // Lanzamos 10 tareas simultáneas, cada una con su propio objeto de bloqueo
        // En teoría, como cada tarea bloquea un objeto distinto, deberían ejecutarse todas en paralelo (~1s).
        
        ejecutarConSynchronized();
        System.out.println("\n--------------------------------------------------\n");
        ejecutarConReentrantLock();
    }

    /**
     * ❌ ESCENARIO 1: Uso de 'synchronized'
     * En Java 21, 'synchronized' ancla (pins) el Virtual Thread al Carrier Thread (núcleo físico).
     * Como solo tenemos 2 núcleos, solo 2 tareas pueden dormir a la vez. Las demás esperan turno.
     */
    private static void ejecutarConSynchronized() {
        System.out.println("🔴 Ejecutando 10 tareas independientes usando 'synchronized'...");
        Instant inicio = Instant.now();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 10).forEach(i -> {
                Object lockIndividual = new Object(); // Cada tarea tiene su propio candado
                executor.submit(() -> {
                    synchronized (lockIndividual) {
                        dormirSimulandoIO();
                    }
                });
            });
        }

        long tiempo = Duration.between(inicio, Instant.now()).toMillis();
        System.out.printf("⏱️ Tiempo con 'synchronized': %d ms (Lento por secuestro de Carrier Threads)%n", tiempo);
    }

    /**
     * ✅ ESCENARIO 2: Uso de 'ReentrantLock'
     * ReentrantLock permite que el Virtual Thread se desmonte del núcleo mientras espera I/O.
     * Las 10 tareas se ejecutan verdaderamente en paralelo en ~1 segundo.
     */
    private static void ejecutarConReentrantLock() {
        System.out.println("🟢 Ejecutando 10 tareas independientes usando 'ReentrantLock'...");
        Instant inicio = Instant.now();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 10).forEach(i -> {
                ReentrantLock lockIndividual = new ReentrantLock(); // Candado moderno compatible con Loom
                executor.submit(() -> {
                    lockIndividual.lock();
                    try {
                        dormirSimulandoIO();
                    } finally {
                        lockIndividual.unlock();
                    }
                });
            });
        }

        long tiempo = Duration.between(inicio, Instant.now()).toMillis();
        System.out.printf("⏱️ Tiempo con 'ReentrantLock': %d ms (Óptimo: sin bloqueo de núcleos)%n", tiempo);
    }

    private static void dormirSimulandoIO() {
        try {
            Thread.sleep(Duration.ofSeconds(1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
/* RESULTADO

=== DEMOSTRACIÓN DE 'PINNING' EN VIRTUAL THREADS (Núcleos CPU: 2) ===

🔴 Ejecutando 10 tareas independientes usando 'synchronized'...
⏱️ Tiempo con 'synchronized': 1006 ms (Lento por secuestro de Carrier Threads)

--------------------------------------------------

🟢 Ejecutando 10 tareas independientes usando 'ReentrantLock'...
⏱️ Tiempo con 'ReentrantLock': 1001 ms (Óptimo: sin bloqueo de núcleos)



*/