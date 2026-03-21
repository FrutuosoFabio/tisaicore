package br.com.tisaicore.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String productName, int available, int requested) {
        super("Estoque insuficiente para '" + productName + "': disponível=" + available + ", solicitado=" + requested);
    }
}
