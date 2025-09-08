package com.sistema.sistema.infrastructure.exception;

import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Validación de request
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    if (error instanceof FieldError) {
                        return ((FieldError) error).getField() + ": " + error.getDefaultMessage();
                    } else {
                        return error.getDefaultMessage();
                    }
                })
                .collect(Collectors.toList());

        return ApiResponseFactory.error(HttpStatus.BAD_REQUEST, "Error de validación", errors);
    }

    // 2️. Errores de base de datos
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDatabaseExceptions(DataIntegrityViolationException ex) {
        String rootMessage = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        String userMessage;

        if (rootMessage != null) {
            String lower = rootMessage.toLowerCase();

            // Caso 1: UNIQUE constraint (duplicados)
            if (lower.contains("duplicate key") || lower.contains("llave duplicada")) {
                String field = extractFieldFromConstraint(rootMessage);
                String value = extractValueFromDetail(rootMessage);

                if (field != null && value != null) {
                    userMessage = "El valor '" + value + "' ya existe para el campo '" + field + "'.";
                } else if (field != null) {
                    userMessage = "Ya existe un registro con el mismo valor en el campo '" + field + "'.";
                } else {
                    userMessage = "Ya existe un registro con valores duplicados en un campo único.";
                }
            }
            // Caso 2: FOREIGN KEY constraint
            else if (lower.contains("violates foreign key constraint") || lower.contains("clave foránea")) {
                userMessage = "No se puede eliminar o modificar porque existen registros relacionados.";
            }
            // Caso 3: Otro error de integridad
            else {
                userMessage = "Error en la base de datos.";
            }
        } else {
            userMessage = "Error en la base de datos.";
        }

        return ApiResponseFactory.error(HttpStatus.CONFLICT, userMessage, List.of(rootMessage));
    }

    // 3️. Entidades no encontradas
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFound(EntityNotFoundException ex) {
        return ApiResponseFactory.error(HttpStatus.NOT_FOUND, "Recurso no encontrado", List.of(ex.getMessage()));
    }

    // 4. Errores de negocio personalizados
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException ex) {
        return ApiResponseFactory.error(ex.getStatus(), ex.getMessage(), List.of());
    }

    // 5️. Cualquier otra excepción no controlada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralExceptions(Exception ex) {
        return ApiResponseFactory.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", List.of(ex.getMessage()));
    }

    // --- Métodos auxiliares para parsear mensajes de Postgres ---

    /**
     * Extrae el campo afectado desde el constraint.
     * Ejemplo: roles_name_key -> name
     */
    private String extractFieldFromConstraint(String message) {
        try {
            // Busca entre comillas latinas «...»
            int start = message.indexOf("«");
            int end = message.indexOf("»", start + 1);

            if (start != -1 && end != -1) {
                String constraint = message.substring(start + 1, end); // ej: roles_name_key
                if (constraint.contains("_")) {
                    String[] parts = constraint.split("_");
                    return parts.length >= 2 ? parts[1] : constraint; // devuelve "name"
                }
                return constraint;
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Extrae el valor duplicado desde el "Detail".
     * Ejemplo: (name)=(ROLE_TEST) -> ROLE_TEST
     */
    /**
     * Extrae el valor duplicado desde el "Detail".
     * Ejemplo: (name)=(ROLE_TEST) -> ROLE_TEST
     */
    private String extractValueFromDetail(String message) {
        try {
            if (message.contains("Detail:") || message.contains("Detalle:")) {
                // Buscar la parte con (campo)=(valor)
                int parenEqual = message.indexOf(")=");
                if (parenEqual != -1) {
                    // Busca el primer "(" después del "="
                    int startValue = message.indexOf("(", parenEqual);
                    int endValue = message.indexOf(")", startValue + 1);

                    if (startValue != -1 && endValue != -1) {
                        return message.substring(startValue + 1, endValue).trim(); // ROLE_TEST
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }


}
