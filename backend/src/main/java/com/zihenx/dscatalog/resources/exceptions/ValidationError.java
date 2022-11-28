package com.zihenx.dscatalog.resources.exceptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe que herda as caracteristicas do StandardError para adicionar
 * a listagem dos campos de validação que não passaram
 */
public class ValidationError extends StandardError implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<FieldMessage> errors = new ArrayList<>();

    public List<FieldMessage> getErros() {
        return errors;
    }

    /**
     * Metodo que adiciona um novo campo com erro na listagem
     * @param fieldName -> campo do erro
     * @param message -> mensagem do campo
     */
    public void addError(String fieldName, String message) {
        errors.add(new FieldMessage(fieldName, message));
    }
}
