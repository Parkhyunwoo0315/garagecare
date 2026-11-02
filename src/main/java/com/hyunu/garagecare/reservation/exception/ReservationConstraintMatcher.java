package com.hyunu.garagecare.reservation.exception;

import org.hibernate.exception.ConstraintViolationException;

import java.sql.SQLException;

public final class ReservationConstraintMatcher {

    private static final String UNIQUE_VIOLATION = "23505";

    private static final String VEHICLE_SLOT_INDEX =
            "uq_reservation_vehicle_active_slot";

    private ReservationConstraintMatcher() {
    }

    public static boolean isDuplicateVehicleSlot(Throwable exception) {

        boolean uniqueViolation = false;
        boolean matchingConstraint = false;

        Throwable current = exception;

        while (current != null) {

            if (current instanceof SQLException sqlException) {
                if (UNIQUE_VIOLATION.equals(sqlException.getSQLState())) {
                    uniqueViolation = true;
                }
            }

            if (current instanceof ConstraintViolationException hibernateException) {
                if (VEHICLE_SLOT_INDEX.equals(
                        hibernateException.getConstraintName()
                )) {
                    matchingConstraint = true;
                }
            }

            current = current.getCause();
        }

        return uniqueViolation && matchingConstraint;
    }
}