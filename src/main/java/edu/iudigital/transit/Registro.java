package edu.iudigital.transit;

import java.time.LocalDateTime;

/**
 * Registro
 */
public class Registro {
  private String idUsuario;
  private String ruta;
  private String estacion;
  private Accion accion;

  LocalDateTime fecha;

  public Registro() {}

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

  public void setIdUsuario(String idUsuario) {
    this.idUsuario = idUsuario;
  }

  public void setRuta(String ruta) {
    this.ruta = ruta;
  }

  public void setEstacion(String estacion) {
    this.estacion = estacion;
  }

  public void setAccion(Accion accion) {
    this.accion = accion;
  }

  public void setFecha(LocalDateTime fecha) {
    this.fecha = fecha;
  }

}
