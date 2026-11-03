package edu.iudigital.transit;

import java.util.List;

// import java.time.LocalDateTime;

public class App {
  public static void main(String[] args) {
    // Registro datoRuta = new Registro(
    //     "U001",
    //     "L01-04",
    //     "La Paz",
    //     LocalDateTime.parse("2026-03-15T22:24:00"),
    //     Accion.ENTRADA);
    //
    // System.out.println(datoRuta.getAccion());
      
      LectorDatos lector = new LectorDatos();
      List<Registro> registrosList = lector.retornarRegistrosDeJson();

      for (Registro registro : registrosList) {
        System.out.println("User ID: " + registro.getIdUsuario());
      }

  }
}
