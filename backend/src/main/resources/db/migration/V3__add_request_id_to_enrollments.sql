-- request_id: Kafka tabanlı asenkron akış için idempotency anahtarı
-- Aynı requestId ile gelen tekrar mesajlar tek kayıt üretir (idempotency).
ALTER TABLE enrollments
    ADD COLUMN request_id VARCHAR(64);

-- Mevcut kayıtlar için null kabul edilebilir (geriye dönük uyumluluk).
-- Yeni kayıtlar için unique olmalı ama mevcut null değerleri sorun çıkarmasın diye partial index.
CREATE UNIQUE INDEX IF NOT EXISTS ux_enrollments_request_id
    ON enrollments(request_id)
    WHERE request_id IS NOT NULL;
