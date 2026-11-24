package com.icemelid.java21.virtualthreads;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class CarrierThreadsInspector {

    public static void main(String[] args) {
        int nucleosCPU = Runtime.getRuntime().availableProcessors();
        System.out.println("=== INSPECTOR DE CARRIER THREADS (Núcleos CPU disponibles: " + nucleosCPU + ") ===\n");

        // Lanzamos 15 Virtual Threads para ver cómo se reparten entre los pocos núcleos reales
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(1, 16).forEach(tareaId -> {
                executor.submit(() -> {
                    // 1. Antes de bloquearse: vemos qué Carrier Thread (hilo real del SO) lo está cargando
                    String hiloAntes = Thread.currentThread().toString();
                    System.out.printf("🟢 Tarea #%02d INICIA  en -> %s%n", tareaId, hiloAntes);

                    try {
                        // Simulamos bloqueo de red/BD: aquí el Virtual Thread se DESMONTA del núcleo
                        Thread.sleep(Duration.ofMillis(500));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    // 2. Después de despertar: la JVM lo MONTA en cualquier núcleo que esté libre
                    String hiloDespues = Thread.currentThread().toString();
                    System.out.printf("🔵 Tarea #%02d TERMINA en -> %s%n", tareaId, hiloDespues);
                });
            });
        }
    }
}

/* Resultado ejecución

=== INSPECTOR DE CARRIER THREADS (Núcleos CPU disponibles: 2) ===

🟢 Tarea #01 INICIA  en -> VirtualThread[#37]/runnable@ForkJoinPool-1-worker-1
🟢 Tarea #03 INICIA  en -> VirtualThread[#42]/runnable@ForkJoinPool-1-worker-2
🟢 Tarea #04 INICIA  en -> VirtualThread[#43]/runnable@ForkJoinPool-1-worker-2
🟢 Tarea #10 INICIA  en -> VirtualThread[#49]/runnable@ForkJoinPool-1-worker-2
🟢 Tarea #02 INICIA  en -> VirtualThread[#39]/runnable@ForkJoinPool-1-worker-2
🟢 Tarea #05 INICIA  en -> VirtualThread[#44]/runnable@ForkJoinPool-1-worker-1
🟢 Tarea #06 INICIA  en -> VirtualThread[#45]/runnable@ForkJoinPool-1-worker-1
🟢 Tarea #07 INICIA  en -> VirtualThread[#46]/runnable@ForkJoinPool-1-worker-1
🟢 Tarea #08 INICIA  en -> VirtualThread[#47]/runnable@ForkJoinPool-1-worker-1
🟢 Tarea #09 INICIA  en -> VirtualThread[#48]/runnable@ForkJoinPool-1-worker-1
🟢 Tarea #11 INICIA  en -> VirtualThread[#50]/runnable@ForkJoinPool-1-worker-1
🟢 Tarea #12 INICIA  en -> VirtualThread[#51]/runnable@ForkJoinPool-1-worker-1
🟢 Tarea #13 INICIA  en -> VirtualThread[#52]/runnable@ForkJoinPool-1-worker-1
🟢 Tarea #14 INICIA  en -> VirtualThread[#53]/runnable@ForkJoinPool-1-worker-1
🟢 Tarea #15 INICIA  en -> VirtualThread[#54]/runnable@ForkJoinPool-1-worker-1
🔵 Tarea #01 TERMINA en -> VirtualThread[#37]/runnable@ForkJoinPool-1-worker-2
🔵 Tarea #03 TERMINA en -> VirtualThread[#42]/runnable@ForkJoinPool-1-worker-1
🔵 Tarea #04 TERMINA en -> VirtualThread[#43]/runnable@ForkJoinPool-1-worker-1
🔵 Tarea #10 TERMINA en -> VirtualThread[#49]/runnable@ForkJoinPool-1-worker-1
🔵 Tarea #05 TERMINA en -> VirtualThread[#44]/runnable@ForkJoinPool-1-worker-1
🔵 Tarea #02 TERMINA en -> VirtualThread[#39]/runnable@ForkJoinPool-1-worker-2
🔵 Tarea #06 TERMINA en -> VirtualThread[#45]/runnable@ForkJoinPool-1-worker-2
🔵 Tarea #08 TERMINA en -> VirtualThread[#47]/runnable@ForkJoinPool-1-worker-2
🔵 Tarea #07 TERMINA en -> VirtualThread[#46]/runnable@ForkJoinPool-1-worker-2
🔵 Tarea #09 TERMINA en -> VirtualThread[#48]/runnable@ForkJoinPool-1-worker-1
🔵 Tarea #12 TERMINA en -> VirtualThread[#51]/runnable@ForkJoinPool-1-worker-2
🔵 Tarea #11 TERMINA en -> VirtualThread[#50]/runnable@ForkJoinPool-1-worker-2
🔵 Tarea #13 TERMINA en -> VirtualThread[#52]/runnable@ForkJoinPool-1-worker-1
🔵 Tarea #14 TERMINA en -> VirtualThread[#53]/runnable@ForkJoinPool-1-worker-1
🔵 Tarea #15 TERMINA en -> VirtualThread[#54]/runnable@ForkJoinPool-1-worker-2

*/