-- Thêm các cột TMDB vào bảng movie
ALTER TABLE movie
    ADD COLUMN tmdb_id      INT          NULL,
    ADD COLUMN trailer_url  VARCHAR(512) NULL,
    ADD COLUMN release_date DATE         NULL;
