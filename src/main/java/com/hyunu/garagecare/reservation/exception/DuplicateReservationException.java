package com.hyunu.garagecare.reservation.exception;

public class DuplicateReservationException extends RuntimeException {
  public DuplicateReservationException(String message) {
    super(message);
  }
}
