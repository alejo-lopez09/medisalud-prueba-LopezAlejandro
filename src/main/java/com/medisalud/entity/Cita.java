package com.medisalud.entity;

import com.medisalud.notificacion.ObservadorCita;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cita implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int LONGITUD_MAXIMA_MOTIVO = 200;

    private final String codigo;
    private final Paciente paciente;
    private final Medico medico;
    private final LocalDate fecha;
    private EstadoCita estado;
    private String motivoCancelacion;
    private transient List<ObservadorCita> observadores = new ArrayList<>();

    public Cita(String codigo, Paciente paciente, Medico medico, LocalDate fecha) {
        this.codigo = codigo;
        this.paciente = paciente;
        this.medico = medico;
        this.fecha = fecha;
        this.estado = EstadoCita.PENDIENTE;
    }

    public void agregarObservador(ObservadorCita observador) {
        if (!observadores.contains(observador)) {
         observadores.add(observador);
            }
    }

    public void cambiarEstado(EstadoCita nuevoEstado) {
        this.estado = nuevoEstado;
        for (ObservadorCita observador : observadores) {
            observador.notificarCambioEstado(this);
        }
    }

    public void cancelar(String motivo) {
    if (motivo == null || motivo.trim().isEmpty()) {
        throw new IllegalArgumentException("El motivo de cancelacion es obligatorio");
    }

    String motivoLimpio = motivo.trim();
    if (motivoLimpio.length() > LONGITUD_MAXIMA_MOTIVO) {
        throw new IllegalArgumentException("El motivo de cancelacion no puede superar "
                + LONGITUD_MAXIMA_MOTIVO + " caracteres (recibidos: " + motivoLimpio.length() + ")");
    }

    this.motivoCancelacion = motivoLimpio;
    cambiarEstado(EstadoCita.CANCELADA);
}
    
    public Cita(String codigo, Paciente paciente, Medico medico, LocalDate fecha,
            EstadoCita estado, String motivoCancelacion) {
        this(codigo, paciente, medico, fecha);
        this.estado = estado;
        this.motivoCancelacion = motivoCancelacion;
}

    public String getCodigo() {
        return codigo;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public Medico getMedico() {
        return medico;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    
public String getMotivoCancelacion() {
    return motivoCancelacion;
}

    @Override
    public String toString() {
        return "Cita " + codigo + " [" + estado + "] " + paciente.getNombreCompleto()
                + " con " + medico.getNombreCompleto() + " el " + fecha;
    }

    private void readObject(ObjectInputStream entrada) throws IOException, ClassNotFoundException {
        entrada.defaultReadObject();
        observadores = new ArrayList<>();
    }
}
