-- V2: Prevent duplicate reservations for the same vehicle and time.
-- Policy: CANCELED reservations do not occupy a reservation slot.
-- Prerequisite: Existing reservations schema has been baselined at V1.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.reservations
        WHERE status <> 'CANCELED'
        GROUP BY
            vehicle_id,
            reservation_date,
            reservation_time
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION
            'Duplicate active vehicle reservations exist. Migration aborted.';
END IF;
END
$$;

CREATE UNIQUE INDEX uq_reservation_vehicle_active_slot
    ON public.reservations (
                            vehicle_id,
                            reservation_date,
                            reservation_time
        )
    WHERE status <> 'CANCELED';