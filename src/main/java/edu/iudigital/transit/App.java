package edu.iudigital.transit;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

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
    try (Jsonb jsonb = JsonbBuilder.create();
          InputStream inputJsonStream = App.class.getResourceAsStream("/registros.json") ) {

      if (inputJsonStream == null) {
        System.err.println("Error: fileTest.json not found!");
        return;
      }
     
      // parse the JSON array into an array of the POJO class
      Registro[] registrosArreglo = jsonb.fromJson(inputJsonStream, Registro[].class);

      // convert the array to a List for convenience methods
      List<Registro> registrosList = Arrays.asList(registrosArreglo);

      for (Registro registro : registrosList) {
        System.out.println("User ID: " + registro.getIdUsuario());
      }
          
    } catch (Exception e) {
      e.printStackTrace();
    }


  }
}
