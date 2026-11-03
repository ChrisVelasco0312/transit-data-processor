package edu.iudigital.transit;

import java.time.Duration;

public class Movimiento {
  private final String origen;
  private final String destino;
  private final Duration duracion;
  private final String ruta;

  public Movimiento(String origen, String destino, Duration duracion, String ruta) {
    this.origen = origen;
    this.destino = destino;
    this.duracion = duracion;
    this.ruta = ruta;
  }

  public String getOrigen() {
    return origen;
  }

  public String getDestino() {
    return destino;
  }

  public Duration getDuracion() {
    return duracion;
  }

  public String getRuta() {
    return ruta;
  }
}
