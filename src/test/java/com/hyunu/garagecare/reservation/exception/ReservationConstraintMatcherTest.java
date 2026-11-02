package com.hyunu.garagecare.reservation.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationConstraintMatcherTest {

    @Test
    void matchesDuplicateVehicleSlot() {
        SQLException sql = new SQLException(
                "duplicate key",
                "23505"
        );

        ConstraintViolationException hibernate =
                new ConstraintViolationException(
                        "unique constraint violation",
                        sql,
                        "uq_reservation_vehicle_active_slot"
                );

        DataIntegrityViolationException exception =
                new DataIntegrityViolationException(
                        "database constraint violation",
                        hibernate
                );

        assertThat(
                ReservationConstraintMatcher
                        .isDuplicateVehicleSlot(exception)
        ).isTrue();
    }

    @Test
    void doesNotMatchOtherUniqueConstraint() {
        SQLException sql = new SQLException(
                "duplicate key",
                "23505"
        );

        ConstraintViolationException hibernate =
                new ConstraintViolationException(
                        "unique constraint violation",
                        sql,
                        "uk_vehicle_number"
                );

        assertThat(
                ReservationConstraintMatcher
                        .isDuplicateVehicleSlot(hibernate)
        ).isFalse();
    }

    @Test
    void doesNotMatchForeignKeyViolation() {
        SQLException sql = new SQLException(
                "foreign key violation",
                "23503"
        );

        ConstraintViolationException hibernate =
                new ConstraintViolationException(
                        "foreign key violation",
                        sql,
                        "uq_reservation_vehicle_active_slot"
                );

        assertThat(
                ReservationConstraintMatcher
                        .isDuplicateVehicleSlot(hibernate)
        ).isFalse();
    }
}