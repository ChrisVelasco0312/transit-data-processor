package edu.iudigital.transit;

import java.time.LocalDateTime;

/**
 * Registro
 */
public class Registro {
  String idUsuario;
  String ruta;
  String estacion;
  Accion accion;

  LocalDateTime fecha;

  public Registro(String idUsuario, String ruta, String estacion, LocalDateTime fecha, Accion accion) {
    this.idUsuario = idUsuario;
    this.ruta = ruta;
    this.estacion = estacion;
    this.fecha = fecha;
    this.accion = accion;
  }

  public String getIdUsuario() {
    return idUsuario;
  }

  public String getRuta() {
    return ruta;
  }

  public String getEstacion() {
    return estacion;
  }

  public Accion getAccion() {
    return accion;
  }

  public LocalDateTime getFecha() {
    return fecha;
  }

}
