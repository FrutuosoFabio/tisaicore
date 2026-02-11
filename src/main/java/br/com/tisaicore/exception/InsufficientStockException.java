package br.com.tisaicore.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String productName, int available, int requested) {
        super("Insufficient stock for product '" + productName + "': available=" + available + ", requested=" + requested);
    }
}
