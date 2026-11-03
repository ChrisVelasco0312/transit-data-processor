package edu.iudigital.transit;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class App {
  public static void main(String[] args) {
    LectorDatos lector = new LectorDatos();
    List<Registro> registrosList = lector.retornarRegistrosDeJson();

    DateTimeFormatter formateador = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy",
        Locale.forLanguageTag("es-ES"));
    DateTimeFormatter formateadorAmPm = DateTimeFormatter.ofPattern("hh:mm a",
        Locale.forLanguageTag("es-ES"));

    // 1. Usuarios por dia y estacion
    Map<LocalDate, Map<String, Set<String>>> usuariosPorDiaYEstacion = registrosList.stream()
        .filter(r -> r.getAccion().equals(Accion.ENTRADA))
        .filter(r -> r.getIdUsuario() != null && !r.getIdUsuario().trim().isEmpty())
        .collect(Collectors.groupingBy(
            r -> r.getFecha().toLocalDate(),
            Collectors.groupingBy(
                Registro::getEstacion,
                Collectors.mapping(Registro::getIdUsuario, Collectors.toSet()))));

    usuariosPorDiaYEstacion.forEach((fecha, estacionesMap) -> {
      System.out.println("---------------------------");
      System.out.println("Fecha: " + fecha.format(formateador));
      System.out.println("---------------------------");

      estacionesMap.forEach((estacion, usuarios) -> {
        System.out.println();
        System.out.println("Estacion: " + estacion);
        System.out.println("Total de usuarios: " + usuarios.size());
        System.out.println("Listado: " + usuarios);
        System.out.println();
      });
    });

    // 2. Afluencia por hora del dia
    Map<Integer, Long> afluenciaPorHora = registrosList.stream()
        .filter(r -> r != null && r.getFecha() != null)
        .filter(r -> r.getAccion().equals(Accion.ENTRADA))
        .collect(Collectors.groupingBy(
            r -> r.getFecha().getHour(),
            Collectors.counting()));

    System.out.println("Afluencia por hora: ");
    afluenciaPorHora.forEach((hora, total) -> {
      String horaFormateada = LocalTime.of(hora, 0).format(formateadorAmPm);
      System.out.println(String.format("%s <-> Total de ingresos: %d", horaFormateada, total));
    });

    // 3. Ranking de horas con mas afluencia
    List<Map.Entry<Integer, Long>> rankingHoras = afluenciaPorHora.entrySet().stream()
        .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed()
            .thenComparing(Map.Entry.comparingByKey()))
        .collect(Collectors.toList());

    System.out.println();
    System.out.println("Ranking de horas con más afluencia:");
    AtomicInteger puestoHora = new AtomicInteger(1);
    rankingHoras.forEach(entrada -> {
      String horaFormateada = LocalTime.of(entrada.getKey(), 0).format(formateadorAmPm);
      System.out.println(String.format("%d. %s <-> Total de ingresos: %d",
          puestoHora.getAndIncrement(), horaFormateada, entrada.getValue()));
    });

    // 4. Ranking de rutas por volumen de uso
    Map<String, Long> usoPorRuta = registrosList.stream()
        .filter(r -> r != null && r.getRuta() != null && !r.getRuta().trim().isEmpty())
        .filter(r -> r.getAccion().equals(Accion.ENTRADA))
        .collect(Collectors.groupingBy(
            Registro::getRuta,
            Collectors.counting()));

    List<Map.Entry<String, Long>> rankingRutas = usoPorRuta.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
            .thenComparing(Map.Entry.comparingByKey()))
        .collect(Collectors.toList());

    System.out.println();
    System.out.println("Ranking de rutas por volumen de uso:");
    AtomicInteger puestoRuta = new AtomicInteger(1);
    rankingRutas.forEach(entrada -> {
      System.out.println(String.format("%d. Ruta %s <-> Total de ingresos: %d",
          puestoRuta.getAndIncrement(), entrada.getKey(), entrada.getValue()));
    });

    // =========================================================
    // 5. Estaciones que hacen parte de cada ruta
    // =========================================================
    Map<String, Set<String>> estacionesPorRuta = registrosList.stream()
        .filter(r -> r != null)
        .filter(r -> r.getRuta() != null && !r.getRuta().trim().isEmpty())
        .filter(r -> r.getEstacion() != null && !r.getEstacion().trim().isEmpty())
        .collect(Collectors.groupingBy(
            Registro::getRuta,
            LinkedHashMap::new,
            Collectors.mapping(
                Registro::getEstacion,
                Collectors.toCollection(LinkedHashSet::new))));

    estacionesPorRuta.forEach((ruta, estaciones) -> {
      System.out.println("---------------------------");
      System.out.println("Ruta: " + ruta);
      System.out.println("Estaciones (" + estaciones.size() + "):");
      estaciones.forEach(estacion -> System.out.println("  - " + estacion));
    });

    // =========================================================
    // 6. Estaciones visitadas por usuario, en orden del registro
    // =========================================================
    Map<String, List<String>> estacionesPorUsuario = registrosList.stream()
        .filter(r -> r != null && r.getFecha() != null)
        .filter(r -> r.getIdUsuario() != null && !r.getIdUsuario().trim().isEmpty())
        .filter(r -> r.getAccion().equals(Accion.ENTRADA))
        .sorted(Comparator.comparing(Registro::getFecha))
        .collect(Collectors.groupingBy(
            Registro::getIdUsuario,
            Collectors.mapping(Registro::getEstacion, Collectors.toList())));

    estacionesPorUsuario.forEach((usuario, estaciones) -> {
      System.out.println("---------------------------");
      System.out.println("Usuario: " + usuario);
      System.out.println("Estaciones visitadas (" + estaciones.size() + "):");
      estaciones.forEach(estacion -> System.out.println("  - " + estacion));
    });

    // 7. Tiempo estimado entre estaciones consecutivas
    Map<String, List<Registro>> registrosPorUsuario = registrosList.stream()
        .filter(r -> r != null && r.getFecha() != null)
        .filter(r -> r.getIdUsuario() != null && !r.getIdUsuario().trim().isEmpty())
        .filter(r -> r.getAccion().equals(Accion.ENTRADA))
        .sorted(Comparator.comparing(Registro::getFecha))
        .collect(Collectors.groupingBy(Registro::getIdUsuario));

    List<Movimiento> movimientos = registrosPorUsuario.values().stream()
        .flatMap(lista -> IntStream.range(0, lista.size() - 1)
            .mapToObj(i -> new Movimiento(
                lista.get(i).getEstacion(),
                lista.get(i + 1).getEstacion(),
                Duration.between(
                    lista.get(i).getFecha(),
                    lista.get(i + 1).getFecha()))))
        .filter(m -> !m.getOrigen().equals(m.getDestino()))
        .filter(m -> m.getDuracion().toMinutes() > 0 && m.getDuracion().toMinutes() <= 180)
        .collect(Collectors.toList());

    Map<String, Double> promedioMinutosPorTramo = movimientos.stream()
        .collect(Collectors.groupingBy(
            m -> m.getOrigen() + " -> " + m.getDestino(),
            Collectors.averagingDouble(m -> m.getDuracion().toSeconds() / 60.0)));

    System.out.println();
    System.out.println("Tiempo promedio entre estaciones consecutivas:");
    promedioMinutosPorTramo.forEach((tramo, minutos) -> {
      long totalSegundos = Math.round(minutos * 60);
      System.out.println(String.format("%s <-> Tiempo promedio: %d min %02d s",
          tramo, totalSegundos / 60, totalSegundos % 60));
    });

    // 8. Rutas criticas por ocupacion diaria
    final double umbralOcupacionDiaria = 5.0;

    Map<String, Map<LocalDate, Long>> ocupacionPorRutaYDia = registrosList.stream()
        .filter(r -> r != null && r.getFecha() != null)
        .filter(r -> r.getRuta() != null && !r.getRuta().trim().isEmpty())
        .filter(r -> r.getAccion().equals(Accion.ENTRADA))
        .collect(Collectors.groupingBy(
            Registro::getRuta,
            Collectors.groupingBy(
                r -> r.getFecha().toLocalDate(),
                Collectors.counting())));

    System.out.println();
    System.out.println("Rutas criticas (promedio diario mayor a " + umbralOcupacionDiaria + "):");
    ocupacionPorRutaYDia.entrySet().stream()
        .sorted(Comparator.comparingDouble(
            (Map.Entry<String, Map<LocalDate, Long>> e) -> e.getValue().values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0))
            .reversed())
        .forEach(entrada -> {
          double promedioDiario = entrada.getValue().values().stream()
              .mapToLong(Long::longValue)
              .average()
              .orElse(0.0);
          boolean critica = promedioDiario > umbralOcupacionDiaria;
          System.out.println(String.format("%s <-> Promedio diario: %.2f <-> %s",
              entrada.getKey(), promedioDiario, critica ? "CRITICA" : "Normal"));
        });
  }
}
