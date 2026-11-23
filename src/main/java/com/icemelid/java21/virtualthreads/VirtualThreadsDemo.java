package com.icemelid.java21.virtualthreads;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class VirtualThreadsDemo {

    private static final int TOTAL_TAREAS = 10_000;

    public static void main(String[] args) {
        System.out.println("Iniciando comparativa de concurrencia con " + TOTAL_TAREAS + " tareas...\n");

        // 1. Ejecución con Hilos Tradicionales (Pool limitado a 200 hilos, similar a Tomcat por defecto)
        ejecutarPrueba("Hilos Tradicionales (FixedThreadPool 200)", Executors.newFixedThreadPool(200));

        // 2. Ejecución con Virtual Threads (Java 21 - Project Loom)
        ejecutarPrueba("Virtual Threads (Java 21)", Executors.newVirtualThreadPerTaskExecutor());
    }

    private static void ejecutarPrueba(String nombrePrueba, ExecutorService executor) {
        Instant inicio = Instant.now();

        // Usamos try-with-resources: en Java 21, ExecutorService implementa AutoCloseable
        // y espera automáticamente a que terminen todas las tareas antes de cerrar.
        try (executor) {
            IntStream.range(0, TOTAL_TAREAS).forEach(i -> {
                executor.submit(() -> {
                    simularLlamadaBaseDeDatos();
                    return i;
                });
            });
        }

        Instant fin = Instant.now();
        long tiempoTotal = Duration.between(inicio, fin).toMillis();
        System.out.printf("✅ %-42s -> Tiempo total: %d ms (%.2f segundos)%n", 
                nombrePrueba, tiempoTotal, tiempoTotal / 1000.0);
    }

    private static void simularLlamadaBaseDeDatos() {
        try {
            // Simulamos una operación bloqueante de Entrada/Salida (I/O) de 1 segundo
            Thread.sleep(Duration.ofSeconds(1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
