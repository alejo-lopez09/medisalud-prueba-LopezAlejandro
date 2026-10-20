package com.medisalud.service;

import com.medisalud.entity.Cita;
import com.medisalud.entity.EstadoCita;
import com.medisalud.entity.Medico;
import com.medisalud.entity.Paciente;
import com.medisalud.exception.CancelacionTardiaException;
import com.medisalud.exception.CitaNoEncontradaException;
import com.medisalud.exception.TransicionInvalidaException;
import com.medisalud.notificacion.ObservadorCita;
import com.medisalud.repository.RepositorioCitas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServicioCitas {

    private final RepositorioCitas repositorioCitas;
    private final List<ObservadorCita> observadores = new ArrayList<>();

    public ServicioCitas(RepositorioCitas repositorioCitas) {
        this.repositorioCitas = repositorioCitas;
    }

    public void registrarObservador(ObservadorCita observador) {
        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public Cita agendarCita(
            String codigo,
            Paciente paciente,
            Medico medico,
            LocalDate fecha) {

        Cita cita = new Cita(codigo, paciente, medico, fecha);
        repositorioCitas.guardar(cita);
        return cita;
    }

    public Cita buscarPorCodigo(String codigo)
            throws CitaNoEncontradaException {

        Cita cita = repositorioCitas.buscarPorCodigo(codigo);

        if (cita == null) {
            throw new CitaNoEncontradaException(codigo);
        }

        return cita;
    }

    public void confirmarCita(String codigo)
            throws CitaNoEncontradaException, TransicionInvalidaException {

        cambiarEstado(codigo, EstadoCita.CONFIRMADA);
    }

    public void atenderCita(String codigo, String diagnostico)
            throws CitaNoEncontradaException, TransicionInvalidaException {

        Cita cita = cambiarEstado(codigo, EstadoCita.ATENDIDA);

        cita.getPaciente()
                .getHistoriaClinica()
                .agregarConsulta(diagnostico);
    }

    public Cita cancelarCita(String codigo, String motivo)
            throws CitaNoEncontradaException,
            TransicionInvalidaException,
            CancelacionTardiaException {

        Cita cita = buscarPorCodigo(codigo);

        if (!cita.getEstado().puedeTransicionarA(EstadoCita.CANCELADA)) {
            throw new TransicionInvalidaException(
                    codigo,
                    cita.getEstado(),
                    EstadoCita.CANCELADA
            );
        }

        if (cita.getFecha().isBefore(LocalDate.now())) {
            throw new CancelacionTardiaException(
                    codigo,
                    cita.getFecha()
            );
        }

        conectarObservadores(cita);

        cita.cancelar(motivo);
        repositorioCitas.guardar(cita);

        return cita;
    }

    public List<Cita> listarPorMedico(String codigoMedico) {
        return repositorioCitas.listarPorMedico(codigoMedico);
    }

    
    public Map<String, Long> cancelacionesPorMedico() {

        Map<String, Long> conteo = repositorioCitas.listarTodas()
                .stream()
                .filter(cita ->
                        cita.getEstado() == EstadoCita.CANCELADA)
                .collect(Collectors.groupingBy(
                        cita -> cita.getMedico().getCodigo(),
                        Collectors.counting()
                ));

        Map<String, Long> ordenado = new LinkedHashMap<>();

        conteo.entrySet()
                .stream()
                .sorted(
                        Map.Entry.<String, Long>comparingByValue(
                                Comparator.reverseOrder()
                        ).thenComparing(Map.Entry.comparingByKey())
                )
                .forEach(fila ->
                        ordenado.put(fila.getKey(), fila.getValue())
                );

        return ordenado;
    }

    private void conectarObservadores(Cita cita) {
        for (ObservadorCita observador : observadores) {
            cita.agregarObservador(observador);
        }
    }

    private Cita cambiarEstado(String codigo, EstadoCita nuevoEstado)
            throws CitaNoEncontradaException, TransicionInvalidaException {

        Cita cita = buscarPorCodigo(codigo);

        if (!cita.getEstado().puedeTransicionarA(nuevoEstado)) {
            throw new TransicionInvalidaException(
                    codigo,
                    cita.getEstado(),
                    nuevoEstado
            );
        }

        conectarObservadores(cita);
        cita.cambiarEstado(nuevoEstado);
        repositorioCitas.guardar(cita);

        return cita;
    }
}