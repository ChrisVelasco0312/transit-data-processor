package edu.iudigital.transit;

import java.time.Duration;

public class Movimiento {
  private final String origen;
  private final String destino;
  private final Duration duracion;

  public Movimiento(String origen, String destino, Duration duracion) {
    this.origen = origen;
    this.destino = destino;
    this.duracion = duracion;
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
}
