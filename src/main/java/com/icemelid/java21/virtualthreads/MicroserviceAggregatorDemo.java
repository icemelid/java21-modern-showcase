package com.icemelid.java21.virtualthreads;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MicroserviceAggregatorDemo {

    // DTO inmutable moderno (Record de Java 21) para devolver al frontend Angular
    public record DashboardUsuarioDTO(
            String nombreUsuario,
            List<String> ultimosPedidos,
            int puntosFidelidad
    ) {}

    public static void main(String[] args) throws Exception {
        System.out.println("=== AGREGADOR DE MICROSERVICIOS CON VIRTUAL THREADS ===\n");

        Instant inicio = Instant.now();

        // Ejecutamos las 3 consultas externas en paralelo usando Virtual Threads
        DashboardUsuarioDTO dashboard = obtenerDashboardCompleto("user-123");

        long tiempoTotal = Duration.between(inicio, Instant.now()).toMillis();

        System.out.println("\n📦 Respuesta consolidada lista para enviar a Angular:");
        System.out.println("   -> " + dashboard);
        System.out.printf("⏱️ Tiempo total de respuesta: %d ms (En lugar de ~2600 ms secuenciales)%n", tiempoTotal);
    }

    public static DashboardUsuarioDTO obtenerDashboardCompleto(String userId) 
            throws InterruptedException, ExecutionException {

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // 1. Lanzamos las 3 llamadas simultáneamente (no bloquean todavía)
            Future<String> futuroPerfil = executor.submit(() -> llamarServicioPerfil(userId));
            Future<List<String>> futuroPedidos = executor.submit(() -> llamarServicioPedidos(userId));
            Future<Integer> futuroPuntos = executor.submit(() -> llamarServicioPuntos(userId));

            // 2. Recogemos los resultados con .get()
            // Aunque .get() es bloqueante, aquí estamos dentro de un Virtual Thread, así que es barato y limpio
            String perfil = futuroPerfil.get();
            List<String> pedidos = futuroPedidos.get();
            int puntos = futuroPuntos.get();

            return new DashboardUsuarioDTO(perfil, pedidos, puntos);
        }
    }

    // --- Simulación de llamadas HTTP / Base de Datos externas ---

    private static String llamarServicioPerfil(String userId) throws InterruptedException {
        Thread.sleep(Duration.ofMillis(800));
        System.out.println("✔️ [800ms]  Perfil obtenido desde MS-Usuarios");
        return "Elena Martínez (VIP)";
    }

    private static List<String> llamarServicioPedidos(String userId) throws InterruptedException {
        Thread.sleep(Duration.ofMillis(1200));
        System.out.println("✔️ [1200ms] Pedidos obtenidos desde MS-Ventas");
        return List.of("Portátil Pro", "Monitor 4K");
    }

    private static int llamarServicioPuntos(String userId) throws InterruptedException {
        Thread.sleep(Duration.ofMillis(600));
        System.out.println("✔️ [600ms]  Puntos obtenidos desde MS-Fidelización");
        return 4500;
    }
}

/* Resultado

=== AGREGADOR DE MICROSERVICIOS CON VIRTUAL THREADS ===

✔️ [600ms]  Puntos obtenidos desde MS-Fidelización
✔️ [800ms]  Perfil obtenido desde MS-Usuarios
✔️ [1200ms] Pedidos obtenidos desde MS-Ventas

📦 Respuesta consolidada lista para enviar a Angular:
   -> DashboardUsuarioDTO[nombreUsuario=Elena Martínez (VIP), ultimosPedidos=[Portátil Pro, Monitor 4K], puntosFidelidad=4500]
⏱️ Tiempo total de respuesta: 1218 ms (En lugar de ~2600 ms secuenciales)

*/
