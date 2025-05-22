package org.nurfet.springsecurity.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(Class<?> entityClass, Long id) {
        super(String.format("%s с ID %d не найден", entityClass.getSimpleName(), id));
    }

    public NotFoundException(String message) {
        super(message);
    }
}
