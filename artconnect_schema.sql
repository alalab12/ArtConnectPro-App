

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



-- disciplines
INSERT IGNORE INTO discipline (name) VALUES
    ('Painting'), ('Sculpture'), ('Photography'), ('Digital Art'), ('Music');

-- artists
INSERT IGNORE INTO artist (name, bio, birth_year, contact_email, city, is_active) VALUES
    ('Leonardo Vinci',  'Renaissance master and polymath.',                                        1452, 'leo@vincistudio.it',   'Florence',    1),
    ('Claude Monet',    'Founder of French Impressionist painting.',                              1840, 'claude@monet.fr',      'Giverny',     1),
    ('Ansel Adams',     'American landscape photographer and environmentalist.',                   1902, 'ansel@adams.co',       'San Francisco',1),
    ('Frida Kahlo',     'Mexican painter known for her many portraits and self-portraits.',        1907, 'frida@kahlo.mx',       'Mexico City', 1),
    ('Auguste Rodin',   'French sculptor, founder of modern sculpture.',                          1840, 'auguste@rodin.fr',     'Paris',       1);

-- artist_discipline
INSERT IGNORE INTO artist_discipline (artist_id, discipline_id)
SELECT a.id, d.id FROM artist a, discipline d
WHERE (a.name='Leonardo Vinci'  AND d.name IN ('Painting','Sculpture'))
   OR (a.name='Claude Monet'    AND d.name='Painting')
   OR (a.name='Ansel Adams'     AND d.name='Photography')
   OR (a.name='Frida Kahlo'     AND d.name='Painting')
   OR (a.name='Auguste Rodin'   AND d.name='Sculpture');

-- artworks
INSERT IGNORE INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'Mona Lisa', 1503, 'Painting', 'Oil on poplar panel', '77x53 cm',
       'Iconic portrait painted by Leonardo da Vinci.', 850000000.00, 'EXHIBITED', id
FROM artist WHERE name='Leonardo Vinci';

INSERT IGNORE INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'The Last Supper', 1498, 'Painting', 'Tempera on gesso', '460x880 cm',
       'Mural depicting the last supper of Jesus with his apostles.', 450000000.00, 'EXHIBITED', id
FROM artist WHERE name='Leonardo Vinci';

INSERT IGNORE INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'Water Lilies', 1919, 'Painting', 'Oil on canvas', '200x426 cm',
       'Series of approximately 250 oil paintings by Monet.', 40000000.00, 'FOR_SALE', id
FROM artist WHERE name='Claude Monet';

INSERT IGNORE INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'The Two Fridas', 1939, 'Painting', 'Oil on canvas', '173x173 cm',
       'Double self-portrait by Frida Kahlo.', 5000000.00, 'FOR_SALE', id
FROM artist WHERE name='Frida Kahlo';

INSERT IGNORE INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'The Thinker', 1904, 'Sculpture', 'Bronze', 'H: 186 cm',
       'Bronze sculpture by Auguste Rodin.', 15000000.00, 'EXHIBITED', id
FROM artist WHERE name='Auguste Rodin';

INSERT IGNORE INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'Monolith, The Face of Half Dome', 1927, 'Photography', 'Gelatin silver print', '40x50 cm',
       'Iconic photograph of Half Dome by Ansel Adams.', 100000.00, 'FOR_SALE', id
FROM artist WHERE name='Ansel Adams';

-- galleries
INSERT IGNORE INTO gallery (name, address, rating) VALUES
    ('Louvre Art House',  'Rue de Rivoli, Paris',         4.9),
    ('The British Gallery','Great Russell St, London',    4.7),
    ('Metropolitan Hub',  '1000 5th Ave, New York',       4.8);

-- exhibitions
INSERT IGNORE INTO exhibition (title, start_date, end_date, gallery_id, curator_name, theme, description)
SELECT 'Renaissance Revival',
       DATE_SUB(CURDATE(), INTERVAL 1 MONTH),
       DATE_ADD(CURDATE(), INTERVAL 2 MONTH),
       g.id, 'Dr. Elena Rossi', 'Classic Renaissance',
       'Masterpieces from the Italian Renaissance.'
FROM gallery g WHERE g.name='Louvre Art House';

INSERT IGNORE INTO exhibition (title, start_date, end_date, gallery_id, curator_name, theme, description)
SELECT 'Sculpting the Soul',
       DATE_SUB(CURDATE(), INTERVAL 15 DAY),
       DATE_ADD(CURDATE(), INTERVAL 1 MONTH),
       g.id, 'Marcus Thorne', 'Modern & Classical Sculpture',
       'Journey through three centuries of sculpture.'
FROM gallery g WHERE g.name='The British Gallery';

INSERT IGNORE INTO exhibition (title, start_date, end_date, gallery_id, curator_name, theme, description)
SELECT 'Impressionist Dreams',
       DATE_SUB(CURDATE(), INTERVAL 2 MONTH),
       DATE_ADD(CURDATE(), INTERVAL 3 MONTH),
       g.id, 'Sarah Jenkins', 'Light and Color',
       'The impressionist movement and its legacy.'
FROM gallery g WHERE g.name='Metropolitan Hub';

-- exhibition_artwork links
INSERT IGNORE INTO exhibition_artwork (exhibition_id, artwork_id)
SELECT e.id, a.id FROM exhibition e, artwork a
WHERE (e.title='Renaissance Revival'  AND a.title IN ('Mona Lisa','The Last Supper'))
   OR (e.title='Sculpting the Soul'   AND a.title='The Thinker')
   OR (e.title='Impressionist Dreams' AND a.title='Water Lilies');

-- workshops
INSERT IGNORE INTO workshop (title, date_time, duration_minutes, max_participants, price, instructor_id, location, level)
SELECT 'Mastering Oil Painting',
       DATE_ADD(NOW(), INTERVAL 5 DAY), 180, 10, 150.00,
       a.id, 'Florence Studio', 'Intermediate'
FROM artist a WHERE a.name='Leonardo Vinci';

INSERT IGNORE INTO workshop (title, date_time, duration_minutes, max_participants, price, instructor_id, location, level)
SELECT 'Impressionist Landscapes',
       DATE_ADD(NOW(), INTERVAL 10 DAY), 180, 10, 120.00,
       a.id, 'Giverny Gardens', 'Beginner'
FROM artist a WHERE a.name='Claude Monet';

INSERT IGNORE INTO workshop (title, date_time, duration_minutes, max_participants, price, instructor_id, location, level)
SELECT 'Sculpting Modernity',
       DATE_ADD(NOW(), INTERVAL 15 DAY), 180, 10, 200.00,
       a.id, 'Paris Workshop', 'Advanced'
FROM artist a WHERE a.name='Auguste Rodin';

-- community_members
INSERT IGNORE INTO community_member (name, email, city, membership_type) VALUES
    ('Alice Wonderland', 'alice@art.com',       'Paris',    'Premium'),
    ('Bob Ross',         'bob@happytrees.com',   'London',   'Premium'),
    ('Charlie Brown',    'charlie@peanuts.com',  'New York', 'free');

-- reviews
INSERT IGNORE INTO review (member_id, artwork_id, rating, comment)
SELECT m.id, a.id, 5, 'Unbelievable detail!'
FROM community_member m, artwork a WHERE m.name='Alice Wonderland' AND a.title='Mona Lisa';

INSERT IGNORE INTO review (member_id, artwork_id, rating, comment)
SELECT m.id, a.id, 4, 'The colors are stunning.'
FROM community_member m, artwork a WHERE m.name='Bob Ross' AND a.title='Water Lilies';

INSERT IGNORE INTO review (member_id, artwork_id, rating, comment)
SELECT m.id, a.id, 5, 'Deeply moving.'
FROM community_member m, artwork a WHERE m.name='Charlie Brown' AND a.title='The Thinker';
