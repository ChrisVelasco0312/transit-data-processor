package edu.iudigital.transit;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class App {
  public static void main(String[] args) {
    LectorDatos lector = new LectorDatos();
    List<Registro> registrosList = lector.retornarRegistrosDeJson();
    // List<String> registrosEntrada = registrosList.stream()
    // .filter(r -> r.getAccion().equals(Accion.ENTRADA))
    // .map(r -> r.getIdUsuario())
    // .collect(Collectors.toList());

    // Map<String, Set<String>> usuariosPorEstacion = registrosList.stream()
    // .filter(r -> r.getAccion().equals(Accion.ENTRADA))
    // .collect(Collectors.groupingBy(
    // Registro::getEstacion,
    // Collectors.mapping(Registro::getIdUsuario, Collectors.toSet())
    // ));

    // Set<LocalDate> diasConRegistros = registrosList.stream()
    // .map(r -> r.getFecha().toLocalDate())
    // .collect(Collectors.toSet());

    Map<LocalDate, Map<String, Set<String>>> usuariosPorDiaYEstacion = registrosList.stream()
        .filter(r -> r.getAccion().equals(Accion.ENTRADA))
        .filter(r -> r.getIdUsuario() != null && !r.getIdUsuario().trim().isEmpty())
        .collect(Collectors.groupingBy(
            r -> r.getFecha().toLocalDate(),
            Collectors.groupingBy(
                Registro::getEstacion,
                Collectors.mapping(Registro::getIdUsuario, Collectors.toSet()))));

    DateTimeFormatter formateador = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy",
        Locale.forLanguageTag("es-ES"));

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

    // AnalizadorTransporte analizador = new AnalizadorTransporte();
    // analizador.usuariosPorEstacion();
  }
}
