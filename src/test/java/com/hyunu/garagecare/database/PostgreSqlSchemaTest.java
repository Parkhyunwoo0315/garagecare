package com.hyunu.garagecare.database;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class PostgreSqlSchemaTest {

    @Autowired
    EntityManager entityManager;

    @Test
    @DisplayName("PostgreSQL에 GarageCare 핵심 테이블 생성")
    void tablesAreCreated() {

        List<String> tables = entityManager
                .createNativeQuery("""
                        SELECT table_name
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                        """, String.class)
                .getResultList();

        assertThat(tables)
                .contains(
                        "members",
                        "vehicles",
                        "maintenance_items",
                        "reservations",
                        "reservation_items"
                );
    }

    @Test
    @DisplayName("Reservation 복합 인덱스 생성")
    void reservationCompositeIndexExists() {

        List<String> indexes = entityManager
                .createNativeQuery("""
                        SELECT indexname
                        FROM pg_indexes
                        WHERE schemaname = 'public'
                          AND tablename = 'reservations'
                         """, String.class)
                .getResultList();

        assertThat(indexes)
                .contains("idx_reservation_member_date_time");
    }
}