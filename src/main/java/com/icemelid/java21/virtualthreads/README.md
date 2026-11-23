# 🚀 Java 21 Modern Showcase

Este repositorio contiene pruebas de concepto (PoC) y benchmarks prácticos sobre las características clave introducidas y consolidadas en **Java 21 LTS**, enfocadas en mejorar el rendimiento, la escalabilidad y la legibilidad del código en arquitecturas backend modernas.

---

## 🧪 Ejercicio 1: Virtual Threads vs. Platform Threads (Project Loom)

### ¿Qué hace el código?
La clase `VirtualThreadsDemo.java` ejecuta una comparativa directa de rendimiento simulando un escenario real de alta concurrencia en un microservicio: **procesar 10,000 peticiones simultáneas donde cada una realiza una operación bloqueante de Entrada/Salida (I/O) de 1 segundo** (simulando una consulta a base de datos o una llamada a una API externa).

El benchmark compara dos estrategias:
1. **Hilos Tradicionales (`Executors.newFixedThreadPool(200)`):** Simula el comportamiento estándar de servidores tradicionales (como Tomcat por defecto), donde los hilos del Sistema Operativo se bloquean esperando la respuesta de I/O, creando un cuello de botella.
2. **Virtual Threads (`Executors.newVirtualThreadPerTaskExecutor()`):** Utiliza hilos ligeros gestionados por la JVM. Cuando un hilo virtual encuentra una operación bloqueante, la JVM lo "aparca" sin bloquear el hilo físico del Sistema Operativo, permitiendo procesar miles de tareas concurrentes con un consumo mínimo de memoria.

### 📊 Resultados Esperados
* **Hilos Tradicionales (Pool 200):** ~50.00 segundos (procesamiento en lotes de 200).
* **Virtual Threads (Java 21):** ~1.05 segundos (procesamiento masivo en paralelo).

### 🛠️ Requisitos y Ejecución
* **JDK:** Java 21 o superior (configurado en `pom.xml` mediante `<maven.compiler.release>21</maven.compiler.release>`).
* **Build Tool:** Apache Maven 3.8+

Para compilar y ejecutar el benchmark desde la terminal:

```bash
mvn compile exec:java -Dexec.mainClass="com.icemelid.java21.virtualthreads.VirtualThreadsDemo"
```

---

## 🌱 Aplicación Real: Activar Virtual Threads en Spring Boot 3.2+

En un entorno empresarial con **Spring Boot 3.2 o superior** (ejecutándose sobre Java 21+), no es necesario crear ejecutores manuales para beneficiarse de Project Loom en una API REST. 

Spring Boot permite que el servidor embebido (Tomcat/Jetty) utilice un **Virtual Thread por cada petición HTTP entrante** añadiendo una única propiedad en el archivo `application.properties` (o `application.yml`):

**En `application.properties`:**
```properties
spring.threads.virtual.enabled=true
```

**En `application.yml`:**
```yaml
spring:
  threads:
    virtual:
      enabled: true
```

### 💡 Notas de Arquitectura (Cuándo usarlos)
* **Ideales para (I/O-Bound):** Microservicios que realizan múltiples llamadas a bases de datos (JDBC/JPA), clientes HTTP (`RestClient`, `WebClient`), colas de mensajería o lectura de archivos. Permiten mantener un código imperativo limpio y fácil de depurar sin recurrir a la complejidad de la programación reactiva.
* **No recomendados para (CPU-Bound):** Tareas intensivas de cálculo matemático puro, procesamiento de imágenes/video o encriptación pesada, donde el cuello de botella es el procesador y no la espera de red.
