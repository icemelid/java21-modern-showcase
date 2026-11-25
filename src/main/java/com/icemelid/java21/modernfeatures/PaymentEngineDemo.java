package com.icemelid.java21.modernfeatures;

import java.math.BigDecimal;
import java.util.List;

public class PaymentEngineDemo {

    // 1. SEALED INTERFACE: Define una jerarquía cerrada.
    // El compilador sabe que NINGUNA otra clase fuera de estas 3 puede implementar MetodoPago.
    public sealed interface MetodoPago permits PagoTarjeta, PagoBizum, PagoTransferencia {}

    // 2. RECORDS: DTOs inmutables en una sola línea (generan constructor, getters, equals, hashCode y toString).
    // Además, podemos añadir validación compacta en el constructor.
    public record PagoTarjeta(String numeroEnmascarado, BigDecimal monto, boolean esInternacional) implements MetodoPago {
        public PagoTarjeta {
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El monto debe ser mayor a cero");
            }
        }
    }

    public record PagoBizum(String telefono, BigDecimal monto) implements MetodoPago {}

    public record PagoTransferencia(String iban, String banco, BigDecimal monto) implements MetodoPago {}

    public static void main(String[] args) {
        System.out.println("=== MOTOR DE PAGOS CON PATTERN MATCHING Y SEALED INTERFACES (JAVA 21) ===\n");

        List<MetodoPago> transacciones = List.of(
                new PagoTarjeta("**** 4532", new BigDecimal("100.00"), false),
                new PagoTarjeta("**** 9910", new BigDecimal("200.00"), true),
                new PagoBizum("+34 600 123 456", new BigDecimal("45.50")),
                new PagoTransferencia("ES91 2100...", "CaixaBank", new BigDecimal("1500.00")),
                new PagoTransferencia("ES12 0049...", "OtroBanco", new BigDecimal("1500.00")),
                new PagoTransferencia("ES55 0182...", "BBVA", new BigDecimal("300.00"))
        );

        transacciones.forEach(pago -> {
            BigDecimal comision = calcularComision(pago);
            String resumen = generarMensajeAuditoria(pago);
            System.out.printf("💳 %-62s | Comisión: %6.2f €%n", resumen, comision);
        });
    }

    /**
     * 3. PATTERN MATCHING EN SWITCH + RECORD PATTERNS + GUARDAS (when):
     * - Desestructura el Record directamente en variables locales (ej. 'monto', 'esInternacional').
     * - Evalúa condiciones extra con la cláusula 'when'.
     * - Exhaustividad: No requiere 'default' porque 'MetodoPago' es sealed y cubrimos los 3 tipos.
     */
    public static BigDecimal calcularComision(MetodoPago pago) {
        return switch (pago) {
            case PagoTarjeta(var num, var monto, boolean esInternacional) when esInternacional ->
                    monto.multiply(new BigDecimal("0.035")); // 3.5% si es internacional

            case PagoTarjeta(var num, var monto, var esInternacional) ->
                    monto.multiply(new BigDecimal("0.015")); // 1.5% nacional

            case PagoBizum(var telefono, var monto) ->
                    BigDecimal.ZERO; // Bizum sin comisión

            case PagoTransferencia(var iban, var banco, var monto) when "CaixaBank".equalsIgnoreCase(banco) ->
                    BigDecimal.ZERO; // Convenio sin comisión

            case PagoTransferencia(var iban, var banco, var monto) when monto.compareTo(new BigDecimal("1000")) >= 0 ->
                    new BigDecimal("2.00"); // Tarifa plana de 2€ para transferencias >= 1000€

            case PagoTransferencia(var iban, var banco, var monto) ->
                    monto.multiply(new BigDecimal("0.005")); // 0.5% resto de transferencias
        };
    }

    /**
     * Ejemplo de manejo nativo de 'null' dentro del switch (Novedad de Java 21).
     * Evita el clásico NullPointerException antes de entrar al switch.
     */
    public static String generarMensajeAuditoria(MetodoPago pago) {
        return switch (pago) {
            case null -> "⚠️ Transacción nula ignorada";
            case PagoTarjeta(var num, var monto, var intl) ->
                    String.format("Tarjeta %s (%s) por %.2f €", num, intl ? "Intl" : "Nac", monto);
            case PagoBizum(var tel, var monto) ->
                    String.format("Bizum al %s por %.2f €", tel, monto);
            case PagoTransferencia(var iban, var banco, var monto) ->
                    String.format("Transferencia (%s) por %.2f €", banco, monto);
        };
    }
}
/* RESULTADO

=== MOTOR DE PAGOS CON PATTERN MATCHING Y SEALED INTERFACES (JAVA 21) ===

💳 Tarjeta **** 4532 (Nac) por 100.00 €                           | Comisión:   1.50 €
💳 Tarjeta **** 9910 (Intl) por 200.00 €                          | Comisión:   7.00 €
💳 Bizum al +34 600 123 456 por 45.50 €                           | Comisión:   0.00 €
💳 Transferencia (CaixaBank) por 1500.00 €                        | Comisión:   0.00 €
💳 Transferencia (OtroBanco) por 1500.00 €                        | Comisión:   2.00 €
💳 Transferencia (BBVA) por 300.00 €                              | Comisión:   1.50 €


*/
