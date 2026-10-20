package com.medisalud.exception;

import java.time.LocalDate;

public class CancelacionTardiaException extends Exception {

    public CancelacionTardiaException(String codigoCita, LocalDate fechaCita) {
        super("La cita " + codigoCita + " no se puede cancelar: su fecha (" + fechaCita + ") ya che paso");
    }
}