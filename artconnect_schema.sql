-- =============================================================================
--  ArtConnect Pro | Script de création de la base de données MySQL
--  Exécuter : mysql -u root -p < artconnect_schema.sql
-- =============================================================================

CREATE DATABASE IF NOT EXISTS artconnect_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE artconnect_db;

CREATE TABLE IF NOT EXISTS discipline (
    id   BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);


CREATE TABLE IF NOT EXISTS artist (
    id             BIGINT        AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(200)  NOT NULL UNIQUE,
    bio            TEXT,
    birth_year     INT,
    contact_email  VARCHAR(200),
    phone          VARCHAR(50),
    city           VARCHAR(100),
    website        VARCHAR(300),
    social_media   VARCHAR(300),
    is_active      TINYINT(1)    NOT NULL DEFAULT 1
);


CREATE TABLE IF NOT EXISTS artist_discipline (
    artist_id     BIGINT NOT NULL,
    discipline_id BIGINT NOT NULL,
    PRIMARY KEY (artist_id, discipline_id),
    FOREIGN KEY (artist_id)     REFERENCES artist(id)     ON DELETE CASCADE,
    FOREIGN KEY (discipline_id) REFERENCES discipline(id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS artwork (
    id            BIGINT        AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(300)  NOT NULL,
    creation_year INT,
    type          VARCHAR(100),
    medium        VARCHAR(200),
    dimensions    VARCHAR(200),
    description   TEXT,
    price         DECIMAL(15,2) DEFAULT 0,
    status        VARCHAR(20)   NOT NULL DEFAULT 'FOR_SALE',
    artist_id     BIGINT,
    UNIQUE KEY uq_artwork_title (title),
    FOREIGN KEY (artist_id) REFERENCES artist(id) ON DELETE SET NULL
);


CREATE TABLE IF NOT EXISTS artwork_tag (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    artwork_id BIGINT       NOT NULL,
    tag_name   VARCHAR(100) NOT NULL,
    FOREIGN KEY (artwork_id) REFERENCES artwork(id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS gallery (
    id            BIGINT        AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(200)  NOT NULL UNIQUE,
    address       VARCHAR(400),
    owner_name    VARCHAR(200),
    opening_hours VARCHAR(200),
    contact_phone VARCHAR(50),
    rating        DECIMAL(3,2)  DEFAULT 0,
    website       VARCHAR(300)
);


CREATE TABLE IF NOT EXISTS exhibition (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(300) NOT NULL UNIQUE,
    start_date   DATE,
    end_date     DATE,
    description  TEXT,
    gallery_id   BIGINT,
    curator_name VARCHAR(200),
    theme        VARCHAR(200),
    FOREIGN KEY (gallery_id) REFERENCES gallery(id) ON DELETE SET NULL
);


CREATE TABLE IF NOT EXISTS exhibition_artwork (
    exhibition_id BIGINT NOT NULL,
    artwork_id    BIGINT NOT NULL,
    PRIMARY KEY (exhibition_id, artwork_id),
    FOREIGN KEY (exhibition_id) REFERENCES exhibition(id) ON DELETE CASCADE,
    FOREIGN KEY (artwork_id)    REFERENCES artwork(id)    ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS workshop (
    id               BIGINT        AUTO_INCREMENT PRIMARY KEY,
    title            VARCHAR(300)  NOT NULL UNIQUE,
    date_time        DATETIME,
    duration_minutes INT           DEFAULT 60,
    max_participants INT           DEFAULT 10,
    price            DECIMAL(10,2) DEFAULT 0,
    instructor_id    BIGINT,
    location         VARCHAR(300),
    description      TEXT,
    level            VARCHAR(50),
    FOREIGN KEY (instructor_id) REFERENCES artist(id) ON DELETE SET NULL
);


CREATE TABLE IF NOT EXISTS community_member (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200) NOT NULL UNIQUE,
    email           VARCHAR(200),
    birth_year      INT,
    phone           VARCHAR(50),
    city            VARCHAR(100),
    membership_type VARCHAR(50)  DEFAULT 'free'
);


CREATE TABLE IF NOT EXISTS member_discipline (
    member_id     BIGINT NOT NULL,
    discipline_id BIGINT NOT NULL,
    PRIMARY KEY (member_id, discipline_id),
    FOREIGN KEY (member_id)     REFERENCES community_member(id) ON DELETE CASCADE,
    FOREIGN KEY (discipline_id) REFERENCES discipline(id)       ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS booking (
    id             BIGINT      AUTO_INCREMENT PRIMARY KEY,
    workshop_id    BIGINT,
    member_id      BIGINT,
    booking_date   DATETIME    DEFAULT CURRENT_TIMESTAMP,
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    FOREIGN KEY (workshop_id) REFERENCES workshop(id)         ON DELETE SET NULL,
    FOREIGN KEY (member_id)   REFERENCES community_member(id) ON DELETE SET NULL
);


CREATE TABLE IF NOT EXISTS review (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id   BIGINT,
    artwork_id  BIGINT,
    rating      INT  CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    review_date DATE DEFAULT (CURRENT_DATE),
    FOREIGN KEY (member_id)  REFERENCES community_member(id) ON DELETE SET NULL,
    FOREIGN KEY (artwork_id) REFERENCES artwork(id)          ON DELETE SET NULL
);


-- =============================================================================
--  Données de démonstration
-- =============================================================================

USE ArtConnect;

-- 1. Insertion des Artistes
INSERT INTO Artist (name, bio, birthYear, contactEmail, phone, city, website, socialMedia, isActive) VALUES
('Elena Rostova', 'Artiste peintre contemporaine.', 1985, 'elena@art.com', '0601020304', 'Paris', 'elenart.com', '@elena_art', TRUE),
('Marcus Dubois', 'Sculpteur métal et bois.', 1978, 'marcus@metal.com', '0611223344', 'Lyon', 'marcus-sculpt.com', '@marcus_d', TRUE),
('Sophie Lemoine', 'Photographe minimaliste.', 1990, 'sophie@photo.com', '0622334455', 'Bordeaux', 'sophiephoto.fr', '@sophie_l', TRUE),
('Julien Clerc', 'Artiste pluridisciplinaire', 1982, 'julien@create.com', '0633445566', 'Paris', NULL, NULL, FALSE);

-- 2. Insertion des Disciplines
INSERT INTO Discipline (name) VALUES ('Peinture'), ('Sculpture'), ('Photographie');

-- 3. Liaison Artist_Discipline
INSERT INTO Artist_Discipline (artist_id, discipline_id) VALUES (1, 1), (2, 2), (3, 3), (4, 1), (4, 2);

-- 4. Insertion des Œuvres
INSERT INTO Artwork (artist_id, title, creationYear, type, medium, dimensions, description, price, status) VALUES
(1, 'Aube Parisienne', 2023, 'Peinture', 'Huile', '100x80cm', 'Une vue de Paris à l aube.', 1200.00, 'FOR_SALE'),
(1, 'Reflets', 2021, 'Peinture', 'Aquarelle', '50x50cm', 'Jeu de reflets sur l eau.', 800.00, 'SOLD'),
(2, 'Structure 01', 2024, 'Sculpture', 'Acier', '200x100x100cm', 'Sculpture monumentale.', 3500.00, 'EXHIBITED'),
(2, 'Équilibre', 2022, 'Sculpture', 'Bois et Métal', '40x30x30cm', 'Petite sculpture de bureau.', 650.00, 'FOR_SALE'),
(3, 'Ombres Urbaines', 2023, 'Photographie', 'Argentique', '60x40cm', 'Tirage noir et blanc.', 450.00, 'FOR_SALE'),
(3, 'Silence', 2024, 'Photographie', 'Numérique', '90x60cm', 'Paysage minimaliste dans la neige.', 550.00, 'EXHIBITED');

-- 5. Insertion des Tags
INSERT INTO ArtworkTag (name) VALUES ('Abstrait'), ('Paysage'), ('Contemporain'), ('Noir et Blanc'), ('Monumental');

-- 6. Liaison Artwork_Tag
INSERT INTO Artwork_Tag (artwork_id, tag_id) VALUES (1, 2), (1, 3), (3, 3), (3, 5), (5, 4), (6, 2), (6, 4);

-- 7. Insertion des Galeries
INSERT INTO Gallery (name, location, capacity, contactEmail, phone, rating) VALUES
('Galerie Horizon', '12 Rue des Arts, Paris', 150, 'contact@horizon.fr', '0140506070', 4.8),
('Lumière Noire', '8 Place Bellecour, Lyon', 80, 'hello@lumierenoire.com', '0478809010', 4.5);

-- 8. Insertion des Expositions
INSERT INTO Exhibition (title, description, startDate, endDate, gallery_id) VALUES
('Visions Contemporaines', 'Les nouveaux talents de la scène française.', '2026-05-01', '2026-06-15', 1),
('Matière et Lumière', 'Exposition autour de la sculpture et de la photographie.', '2026-07-10', '2026-08-20', 2);

-- 9. Liaison Artwork_Exhibition
INSERT INTO Artwork_Exhibition (artwork_id, exhibition_id) VALUES (1, 1), (3, 2), (5, 2), (6, 2);

-- 10. Insertion des Membres de la communauté
INSERT INTO CommunityMember (name, email, birthYear, phone, city, membershipType) VALUES
('Alice Dubois', 'alice@mail.com', 1995, '0699887766', 'Paris', 'Premium'),
('Lucas Martin', 'lucas@mail.com', 1988, '0688776655', 'Lyon', 'Standard'),
('Emma Petit', 'emma@mail.com', 1992, '0677665544', 'Nantes', 'Standard');

-- 11. Insertion des Reviews (Livrées sur les Expositions dans ton modèle)
INSERT INTO Review (rating, comment, reviewDate, member_id, exhibition_id) VALUES
(5, 'Une exposition incroyable, très bien agencée.', '2026-05-10', 1, 1),
(4, 'Belles œuvres mais un peu trop de monde le week-end.', '2026-07-15', 2, 2);

-- 12. Insertion des Ateliers (Workshops)
INSERT INTO Workshop (title, description, startTime, durationHours, maxParticipants, price, instructor_id) VALUES
('Initiation à la Peinture à l huile', 'Découvrez les bases avec Elena.', '2026-06-05 14:00:00', 3, 10, 45.00, 1),
('Photographie Urbaine de Nuit', 'Sortie photo dans les rues.', '2026-06-12 21:00:00', 4, 8, 60.00, 3);

-- 13. Insertion des Réservations (Bookings)
INSERT INTO Booking (bookingDate, paymentStatus, member_id, workshop_id) VALUES
('2026-05-20 10:00:00', 'PAID', 1, 1),
('2026-05-21 11:30:00', 'PAID', 2, 1),
('2026-05-22 15:45:00', 'PENDING', 3, 1),
('2026-06-01 09:00:00', 'PAID', 1, 2);


