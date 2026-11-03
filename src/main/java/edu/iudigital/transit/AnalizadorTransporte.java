package edu.iudigital.transit;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * AnalizadorTransporte
 */
public class AnalizadorTransporte {

  private final List<Registro> registros;

  public AnalizadorTransporte(List<Registro> registros) {
    this.registros = registros;
  }

  /** Usuarios distintos por dia y estacion. */
  public Map<LocalDate, Map<String, Set<String>>> usuariosPorDiaYEstacion() {
    return registros.stream()
        .filter(this::esEntrada)
        .filter(r -> r.getIdUsuario() != null && !r.getIdUsuario().trim().isEmpty())
        .collect(Collectors.groupingBy(
            r -> r.getFecha().toLocalDate(),
            Collectors.groupingBy(
                Registro::getEstacion,
                Collectors.mapping(Registro::getIdUsuario, Collectors.toSet()))));
  }

  /** Total de ingresos por hora del dia. */
  public Map<Integer, Long> afluenciaPorHora() {
    return registros.stream()
        .filter(this::esEntrada)
        .collect(Collectors.groupingBy(
            r -> r.getFecha().getHour(),
            Collectors.counting()));
  }

  /** Horas ordenadas de mayor a menor afluencia. */
  public List<Map.Entry<Integer, Long>> rankingHoras() {
    return afluenciaPorHora().entrySet().stream()
        .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed()
            .thenComparing(Map.Entry.comparingByKey()))
        .collect(Collectors.toList());
  }

  /** Total de ingresos por ruta. */
  public Map<String, Long> usoPorRuta() {
    return registros.stream()
        .filter(this::esEntrada)
        .filter(r -> r.getRuta() != null && !r.getRuta().trim().isEmpty())
        .collect(Collectors.groupingBy(
            Registro::getRuta,
            Collectors.counting()));
  }

  /** Rutas ordenadas de mayor a menor volumen de uso. */
  public List<Map.Entry<String, Long>> rankingRutas() {
    return usoPorRuta().entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
            .thenComparing(Map.Entry.comparingByKey()))
        .collect(Collectors.toList());
  }

  /** Estaciones observadas en cada ruta, sin duplicados y en orden de aparicion. */
  public Map<String, Set<String>> estacionesPorRuta() {
    return registros.stream()
        .filter(r -> r != null)
        .filter(r -> r.getRuta() != null && !r.getRuta().trim().isEmpty())
        .filter(r -> r.getEstacion() != null && !r.getEstacion().trim().isEmpty())
        .collect(Collectors.groupingBy(
            Registro::getRuta,
            LinkedHashMap::new,
            Collectors.mapping(
                Registro::getEstacion,
                Collectors.toCollection(LinkedHashSet::new))));
  }

  /** Estaciones visitadas por usuario, en orden cronologico. */
  public Map<String, List<String>> estacionesPorUsuario() {
    return registros.stream()
        .filter(this::esEntrada)
        .filter(r -> r.getIdUsuario() != null && !r.getIdUsuario().trim().isEmpty())
        .sorted(Comparator.comparing(Registro::getFecha))
        .collect(Collectors.groupingBy(
            Registro::getIdUsuario,
            Collectors.mapping(Registro::getEstacion, Collectors.toList())));
  }

  /**
   * Movimientos entre estaciones consecutivas de cada usuario.
   * Se consideran tanto ENTRADA como SALIDA para emparejar el origen con el
   * destino de cada viaje; filtrar solo ENTRADA dejaba los tramos vacios.
   */
  public List<Movimiento> movimientos() {
    Map<String, List<Registro>> registrosPorUsuario = registros.stream()
        .filter(r -> r.getIdUsuario() != null && !r.getIdUsuario().trim().isEmpty())
        .filter(r -> r.getEstacion() != null && !r.getEstacion().trim().isEmpty())
        .sorted(Comparator.comparing(Registro::getFecha))
        .collect(Collectors.groupingBy(Registro::getIdUsuario));

    return registrosPorUsuario.values().stream()
        .flatMap(lista -> IntStream.range(0, lista.size() - 1)
            .mapToObj(i -> new Movimiento(
                lista.get(i).getEstacion(),
                lista.get(i + 1).getEstacion(),
                Duration.between(
                    lista.get(i).getFecha(),
                    lista.get(i + 1).getFecha()),
                lista.get(i).getRuta())))
        .filter(m -> !m.getOrigen().equals(m.getDestino()))
        .filter(m -> m.getDuracion().toMinutes() > 0 && m.getDuracion().toMinutes() <= 180)
        .collect(Collectors.toList());
  }

  /** Tiempo promedio en minutos por tramo origen -> destino. */
  public Map<String, Double> promedioMinutosPorTramo() {
    return movimientos().stream()
        .collect(Collectors.groupingBy(
            m -> m.getOrigen() + " -> " + m.getDestino(),
            Collectors.averagingDouble(m -> m.getDuracion().toSeconds() / 60.0)));
  }

  /** Rutas ordenadas de mayor a menor por duracion promedio de sus viajes. */
  public Map<String, Double> promedioDuracionPorRuta() {
    return movimientos().stream()
        .filter(m -> m.getRuta() != null && !m.getRuta().trim().isEmpty())
        .collect(Collectors.groupingBy(
            Movimiento::getRuta,
            Collectors.averagingDouble(m -> m.getDuracion().toSeconds() / 60.0)))
        .entrySet().stream()
        .sorted(Map.Entry.<String, Double>comparingByValue().reversed()
            .thenComparing(Map.Entry.comparingByKey()))
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (a, b) -> a,
            LinkedHashMap::new));
  }

  /** Promedio diario de ingresos por ruta, de mayor a menor. */
  public Map<String, Double> promedioOcupacionDiariaPorRuta() {
    Map<String, Map<LocalDate, Long>> ocupacionPorRutaYDia = registros.stream()
        .filter(this::esEntrada)
        .filter(r -> r.getRuta() != null && !r.getRuta().trim().isEmpty())
        .collect(Collectors.groupingBy(
            Registro::getRuta,
            Collectors.groupingBy(
                r -> r.getFecha().toLocalDate(),
                Collectors.counting())));

    return ocupacionPorRutaYDia.entrySet().stream()
        .sorted(Comparator.comparingDouble(
            (Map.Entry<String, Map<LocalDate, Long>> e) -> e.getValue().values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0))
            .reversed())
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> e.getValue().values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0),
            (a, b) -> a,
            LinkedHashMap::new));
  }

  private boolean esEntrada(Registro r) {
    return r != null
        && r.getFecha() != null
        && r.getAccion() != null
        && r.getAccion().equals(Accion.ENTRADA);
  }
}
