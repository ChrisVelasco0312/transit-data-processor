package edu.iudigital.transit;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;


public class LectorDatos {
  List<Registro> registros;

  public List<Registro> retornarRegistrosDeJson() {
    try (Jsonb jsonb = JsonbBuilder.create();
          InputStream inputJsonStream = App.class.getResourceAsStream("/registros.json") ) {

      if (inputJsonStream == null) {
        System.err.println("Error: fileTest.json not found!");
        return registros;
      }
     
      // parse the JSON array into an array of the POJO class
      Registro[] registrosArreglo = jsonb.fromJson(inputJsonStream, Registro[].class);
      // convert the array to a List for convenience methods
      List<Registro> registrosList = Arrays.asList(registrosArreglo);

      return registrosList;          
    } catch (Exception e) {
      e.printStackTrace();
      return registros;
    }
  }

}
