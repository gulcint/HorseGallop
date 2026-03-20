-- ============================================================
-- Migration: Ders rezervasyonu race condition düzeltmesi
-- UNIQUE kısıt + CHECK kısıt + atomic book_lesson RPC
-- ============================================================

-- Aynı kullanıcının aynı derse çift kaydını engelle
ALTER TABLE reservations
    ADD CONSTRAINT uq_user_lesson UNIQUE (user_id, lesson_id);

-- spots_available negatife düşemesin
ALTER TABLE lessons
    ADD CONSTRAINT chk_spots_non_negative CHECK (spots_available >= 0);

-- Atomic rezervasyon fonksiyonu: INSERT + decrement tek transaction içinde
CREATE OR REPLACE FUNCTION book_lesson(p_lesson_id TEXT, p_user_id UUID)
RETURNS SETOF reservations
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    -- Kontenjan kontrolü — kısıt ile de korunuyor ama erken hata tercih edilir
    PERFORM 1 FROM lessons
    WHERE id = p_lesson_id AND spots_available > 0
    FOR UPDATE; -- satırı kilitle

    IF NOT FOUND THEN
        RAISE EXCEPTION 'no_spots_available'
            USING ERRCODE = 'P0001';
    END IF;

    -- Rezervasyon ekle (uq_user_lesson çift kaydı engeller)
    INSERT INTO reservations (user_id, lesson_id, lesson_title, lesson_date, instructor_name, barn_id)
    SELECT p_user_id, l.id, l.title, l.lesson_date, l.instructor_name, l.barn_id
    FROM lessons l
    WHERE l.id = p_lesson_id;

    -- Kontenjanı azalt — SQL-side, client değerine güvenilmiyor
    UPDATE lessons
    SET spots_available = spots_available - 1
    WHERE id = p_lesson_id;

    RETURN QUERY
    SELECT * FROM reservations
    WHERE user_id = p_user_id AND lesson_id = p_lesson_id
    ORDER BY created_at DESC
    LIMIT 1;
END;
$$;

-- Sadece authenticated kullanıcılar çağırabilir
REVOKE ALL ON FUNCTION book_lesson(TEXT, UUID) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION book_lesson(TEXT, UUID) TO authenticated;
