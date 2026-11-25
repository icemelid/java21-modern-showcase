# 🚀 Java 21 Modern Showcase

Este repositorio reúne pruebas de concepto (PoC) y benchmarks prácticos sobre las características clave introducidas y consolidadas en **Java 21 LTS**, enfocadas en mejorar la escalabilidad, el consumo de recursos y la legibilidad del código en arquitecturas backend con **Spring Boot 3.x**.

---

## 🧵 Suite 1: Virtual Threads (Project Loom)

Los Virtual Threads permiten escribir código imperativo tradicional y fácil de depurar, logrando una escalabilidad masiva en operaciones bloqueantes de Entrada/Salida (I/O) sin la complejidad de la programación reactiva.

### 📂 Índice de Clases y Benchmarks (`com.icemelid.java21.virtualthreads`)

| Clase | Objetivo de la Prueba | Resultados Obtenidos (2 vCPU) |
| :--- | :--- | :--- |
| **`VirtualThreadsDemo`** | Benchmark de latencia procesando **10,000 tareas bloqueantes** (1s cada una) frente a un pool fijo de 200 hilos. | • **FixedThreadPool(200):** `50,064 ms` (50.06s)<br>• **Virtual Threads:** `1,151 ms` (1.15s) — **97% más rápido** |
| **`MemoryLimitComparison`** | Prueba de estrés y consumo de RAM levantando **50,000 hilos simultáneos** frente a `newCachedThreadPool()`. | • **Virtual Threads:** Sobrevive con consumo mínimo de MB.<br>• **Platform Threads:** Colapsa con `OutOfMemoryError`. |
| **`CarrierThreadsInspector`** | Inspección interna del montaje y desmontaje (*mounting/unmounting*) de Virtual Threads sobre los núcleos físicos de CPU. | Demuestra cómo un mismo `VirtualThread` inicia en `worker-1`, se desmonta al dormir y reanuda en `worker-2`. |
| **`MicroserviceAggregatorDemo`** | Caso de uso real: consulta paralela a **3 microservicios externos** (`800ms`, `1200ms`, `600ms`) usando `Future` y `Records`. | Respuesta consolidada en **`~1,205 ms`** (tiempo del servicio más lento) frente a `2,600 ms` secuenciales. |
| **`PinningTrapDemo`** | Análisis de concurrencia bloqueante dentro de bloques `synchronized` vs. `ReentrantLock`. | Explica el fenómeno de **Thread Pinning** en Java 21 y su solución con `ReentrantLock` (y mejora en JDK 24+ vía JEP 491). |

---

### 🔍 Detalle Técnico Clave: El problema del *Pinning* en Java 21

En **Java 21 LTS**, ejecutar operaciones bloqueantes de red o base de datos dentro de un bloque `synchronized` provoca **Thread Pinning**: el Virtual Thread queda anclado (*pinned*) al *Carrier Thread* (hilo físico del procesador), impidiendo que otros hilos virtuales utilicen ese núcleo mientras dura la espera.

* **Solución recomendada en Java 21:** Sustituir los bloques `synchronized` en rutas críticas de I/O por `java.util.concurrent.locks.ReentrantLock`.
* **Nota de evolución (JDK 24+ / JEP 491):** En versiones recientes de la JVM (como OpenJDK 25), los monitores de `synchronized` fueron rediseñados para desmontar Virtual Threads sin necesidad de `ReentrantLock`, aunque en entornos productivos bajo **Java 21 LTS** sigue siendo una buena práctica obligatoria.

---

## 🌱 Integración en Producción con Spring Boot 3.2+

En aplicaciones empresariales con **Spring Boot 3.2 o superior** corriendo sobre Java 21+, el servidor embebido (Tomcat/Jetty) puede gestionar cada petición HTTP entrante mediante un Virtual Thread añadiendo una única propiedad:

**En `application.properties`:***
```properties
spring.threads.virtual.enabled=true
```

### 💡 Cuándo usar Virtual Threads (Regla de Arquitectura)
* ✅ **Ideales para (I/O-Bound):** Microservicios con alta concurrencia que realizan consultas a bases de datos (JDBC/JPA), llamadas HTTP a APIs externas o lectura de colas/archivos.
* ❌ **No recomendados para (CPU-Bound):** Procesamiento matemático intensivo, compresión de video o encriptación pesada, donde el cuello de botella es el procesador y no la espera de red.

---

## 🛠️ Requisitos y Ejecución

* **JDK:** Java 21 LTS o superior (compilación fijada a nivel 21 en `pom.xml` mediante `<maven.compiler.release>21</maven.compiler.release>`).
* **Build Tool:** Apache Maven 3.8+

Para ejecutar cualquiera de las clases desde la terminal:

```bash
# Ejemplo ejecutando el Agregador de Microservicios:
mvn compile exec:java -Dexec.mainClass="com.icemelid.java21.virtualthreads.MicroserviceAggregatorDemo"
```
