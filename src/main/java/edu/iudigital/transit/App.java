package edu.iudigital.transit;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class App {

  private static final DateTimeFormatter FORMATEADOR_FECHA = DateTimeFormatter.ofPattern(
      "d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-ES"));
  private static final DateTimeFormatter FORMATEADOR_HORA = DateTimeFormatter.ofPattern(
      "hh:mm a", Locale.forLanguageTag("es-ES"));
  private static final double UMBRAL_OCUPACION_DIARIA = 5.0;

  public static void main(String[] args) {
    LectorDatos lector = new LectorDatos();
    List<Registro> registrosList = lector.retornarRegistrosDeJson();

    AnalizadorTransporte analizador = new AnalizadorTransporte(registrosList);

    Scanner scanner = new Scanner(System.in);
    int opcion;
    do {
      mostrarMenu();
      opcion = leerOpcion(scanner);
      ejecutarOpcion(opcion, analizador);
    } while (opcion != 0);

    scanner.close();
  }

  private static void mostrarMenu() {
    System.out.println();
    System.out.println("===========================================");
    System.out.println("  ANALISIS DE DATOS DE TRANSPORTE");
    System.out.println("===========================================");
    System.out.println("1. Usuarios por dia y estacion");
    System.out.println("2. Afluencia por hora del dia");
    System.out.println("3. Ranking de horas con mas afluencia");
    System.out.println("4. Ranking de rutas por volumen de uso");
    System.out.println("5. Estaciones que hacen parte de cada ruta");
    System.out.println("6. Estaciones visitadas por usuario");
    System.out.println("7. Tiempo promedio entre estaciones consecutivas");
    System.out.println("8. Rutas criticas por ocupacion diaria");
    System.out.println("0. Salir");
    System.out.println("===========================================");
  }

  private static int leerOpcion(Scanner scanner) {
    System.out.print("Seleccione una opcion: ");
    String linea = scanner.nextLine().trim();
    try {
      return Integer.parseInt(linea);
    } catch (NumberFormatException e) {
      System.out.println("Opcion invalida: debe ingresar un numero.");
      return -1;
    }
  }

  private static void ejecutarOpcion(int opcion, AnalizadorTransporte analizador) {
    switch (opcion) {
      case 1:
        usuariosPorDiaYEstacion(analizador);
        break;
      case 2:
        afluenciaPorHora(analizador);
        break;
      case 3:
        rankingHoras(analizador);
        break;
      case 4:
        rankingRutas(analizador);
        break;
      case 5:
        estacionesPorRuta(analizador);
        break;
      case 6:
        estacionesPorUsuario(analizador);
        break;
      case 7:
        tiempoEntreEstaciones(analizador);
        break;
      case 8:
        rutasCriticas(analizador);
        break;
      case 0:
        System.out.println("Saliendo...");
        break;
      default:
        System.out.println("Opcion invalida.");
    }
  }

  private static void usuariosPorDiaYEstacion(AnalizadorTransporte analizador) {
    Map<LocalDate, Map<String, Set<String>>> usuariosPorDiaYEstacion = analizador
        .usuariosPorDiaYEstacion();

    usuariosPorDiaYEstacion.forEach((fecha, estacionesMap) -> {
      System.out.println("---------------------------");
      System.out.println("Fecha: " + fecha.format(FORMATEADOR_FECHA));
      System.out.println("---------------------------");

      estacionesMap.forEach((estacion, usuarios) -> {
        System.out.println();
        System.out.println("Estacion: " + estacion);
        System.out.println("Total de usuarios: " + usuarios.size());
        System.out.println("Listado: " + usuarios);
        System.out.println();
      });
    });
  }

  private static void afluenciaPorHora(AnalizadorTransporte analizador) {
    Map<Integer, Long> afluenciaPorHora = analizador.afluenciaPorHora();

    System.out.println("Afluencia por hora: ");
    afluenciaPorHora.forEach((hora, total) -> {
      String horaFormateada = LocalTime.of(hora, 0).format(FORMATEADOR_HORA);
      System.out.println(String.format("%s -> Total de ingresos: %d", horaFormateada, total));
    });
  }

  private static void rankingHoras(AnalizadorTransporte analizador) {
    List<Map.Entry<Integer, Long>> rankingHoras = analizador.rankingHoras();

    System.out.println();
    System.out.println("Ranking de horas con más afluencia:");
    AtomicInteger puestoHora = new AtomicInteger(1);
    rankingHoras.forEach(entrada -> {
      String horaFormateada = LocalTime.of(entrada.getKey(), 0).format(FORMATEADOR_HORA);
      System.out.println(String.format("%d. %s -> Total de ingresos: %d",
          puestoHora.getAndIncrement(), horaFormateada, entrada.getValue()));
    });
  }

  private static void rankingRutas(AnalizadorTransporte analizador) {
    List<Map.Entry<String, Long>> rankingRutas = analizador.rankingRutas();

    System.out.println();
    System.out.println("Ranking de rutas por volumen de uso:");
    AtomicInteger puestoRuta = new AtomicInteger(1);
    rankingRutas.forEach(entrada -> {
      System.out.println(String.format("%d. Ruta %s -> Total de ingresos: %d",
          puestoRuta.getAndIncrement(), entrada.getKey(), entrada.getValue()));
    });
  }

  private static void estacionesPorRuta(AnalizadorTransporte analizador) {
    Map<String, Set<String>> estacionesPorRuta = analizador.estacionesPorRuta();

    estacionesPorRuta.forEach((ruta, estaciones) -> {
      System.out.println("---------------------------");
      System.out.println("Ruta: " + ruta);
      System.out.println("Estaciones (" + estaciones.size() + "):");
      estaciones.forEach(estacion -> System.out.println("  - " + estacion));
    });
  }

  private static void estacionesPorUsuario(AnalizadorTransporte analizador) {
    Map<String, List<String>> estacionesPorUsuario = analizador.estacionesPorUsuario();

    estacionesPorUsuario.forEach((usuario, estaciones) -> {
      System.out.println("---------------------------");
      System.out.println("Usuario: " + usuario);
      System.out.println("Estaciones visitadas (" + estaciones.size() + "):");
      estaciones.forEach(estacion -> System.out.println("  - " + estacion));
    });
  }

  private static void tiempoEntreEstaciones(AnalizadorTransporte analizador) {
    Map<String, Double> promedioMinutosPorTramo = analizador.promedioMinutosPorTramo();

    System.out.println();
    System.out.println("Tiempo promedio entre estaciones consecutivas:");
    promedioMinutosPorTramo.forEach((tramo, minutos) -> {
      long totalSegundos = Math.round(minutos * 60);
      System.out.println(String.format("%s -> Tiempo promedio: %d min %02d s",
          tramo, totalSegundos / 60, totalSegundos % 60));
    });
  }

  private static void rutasCriticas(AnalizadorTransporte analizador) {
    System.out.println();
    System.out.println("Rutas criticas (promedio diario mayor a " + UMBRAL_OCUPACION_DIARIA + "):");
    analizador.promedioOcupacionDiariaPorRuta().forEach((ruta, promedioDiario) -> {
      boolean critica = promedioDiario > UMBRAL_OCUPACION_DIARIA;
      System.out.println(String.format("%s -> Promedio diario: %.2f -> %s",
          ruta, promedioDiario, critica ? "CRITICA" : "Normal"));
    });
  }
}
