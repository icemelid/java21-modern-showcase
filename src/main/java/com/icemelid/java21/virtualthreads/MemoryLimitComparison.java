package com.icemelid.java21.virtualthreads;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class MemoryLimitComparison {

    private static final int TOTAL_HILOS = 50_000;

    public static void main(String[] args) {
        System.out.println("=== PRUEBA DE CAPACIDAD Y MEMORIA (" + TOTAL_HILOS + " HILOS) ===\n");

        // 1. Probamos con Virtual Threads primero (Sobrevive sin problemas)
        probarVirtualThreads();

        System.out.println("\n--------------------------------------------------\n");

        // 2. Probamos con Hilos Tradicionales sin límite (CachedThreadPool)
        // ADVERTENCIA: Esto intentará crear miles de hilos del Sistema Operativo y fallará con OutOfMemoryError
        probarHilosTradicionalesSinLimite();
    }

    private static void probarVirtualThreads() {
        long memoriaInicial = obtenerMemoriaUsadaMB();
        AtomicInteger completados = new AtomicInteger(0);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, TOTAL_HILOS).forEach(i -> {
                executor.submit(() -> {
                    try {
                        Thread.sleep(Duration.ofSeconds(1));
                        completados.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            });
            // Medimos la memoria justo cuando los 50,000 hilos virtuales están vivos a la vez
            long memoriaPico = obtenerMemoriaUsadaMB();
            System.out.println("✅ [Virtual Threads] Los " + TOTAL_HILOS + " hilos están activos simultáneamente.");
            System.out.println("📊 Memoria RAM utilizada en el pico: ~" + (memoriaPico - memoriaInicial) + " MB");
        }
        System.out.println("🏁 Tareas completadas con éxito: " + completados.get());
    }

    private static void probarHilosTradicionalesSinLimite() {
        System.out.println("⚠️ Intentando levantar " + TOTAL_HILOS + " Platform Threads (Hilos del SO)...");
        AtomicInteger creados = new AtomicInteger(0);

        try (var executor = Executors.newCachedThreadPool()) {
            IntStream.range(0, TOTAL_HILOS).forEach(i -> {
                executor.submit(() -> {
                    creados.incrementAndGet();
                    try {
                        Thread.sleep(Duration.ofSeconds(5));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            });
        } catch (OutOfMemoryError e) {
            System.err.println("💥 ¡COLAPSO! OutOfMemoryError capturado.");
            System.err.println("❌ El Sistema Operativo no pudo crear más hilos. Se quedó en: " + creados.get() + " hilos.");
            System.err.println("📝 Causa: " + e.getMessage());
        }
    }

    private static long obtenerMemoriaUsadaMB() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }
}
