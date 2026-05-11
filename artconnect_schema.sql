-- =============================================================================
--  ArtConnect Pro | Script de création de la base de données MySQL
--  Exécuter : mysql -u root -p < artconnect_schema.sql
-- =============================================================================

DROP DATABASE IF EXISTS ArtConnect;
CREATE DATABASE ArtConnect;

USE ArtConnect;

CREATE TABLE Artist (
artist_id INT AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(100) NOT NULL,
bio TEXT,
birthYear INT,
contactEmail VARCHAR(100),
phone VARCHAR(30),
city VARCHAR(100),
website VARCHAR(255),
socialMedia VARCHAR(255),
  	isActive BOOLEAN
);

CREATE TABLE Discipline (
 	discipline_id INT AUTO_INCREMENT PRIMARY KEY,
  	name VARCHAR(100) NOT NULL
);

CREATE TABLE Artist_Discipline (
  	artist_id INT,
discipline_id INT,
  	PRIMARY KEY (artist_id, discipline_id),
  	FOREIGN KEY (artist_id) REFERENCES Artist(artist_id),
  	FOREIGN KEY (discipline_id) REFERENCES Discipline(discipline_id)
);

CREATE TABLE Artwork (
  	artwork_id INT AUTO_INCREMENT PRIMARY KEY,
  	artist_id INT NOT NULL,
  	title VARCHAR(200),
  	creationYear INT,
  	type VARCHAR(100),
  	medium VARCHAR(100),
  	dimensions VARCHAR(100),
  	description TEXT,
  	price DECIMAL(10 ,2),
  	status ENUM('FOR_SALE', 'SOLD', 'EXHIBITED'),
  	FOREIGN KEY (artist_id) REFERENCES Artist(artist_id)
);

CREATE TABLE ArtworkTag (
  	tag_id INT AUTO_INCREMENT PRIMARY KEY,
  	name VARCHAR(100)
);

CREATE TABLE Artwork_Tag (
  	artwork_id INT,
  	tag_id INT,
  	PRIMARY KEY (artwork_id, tag_id),
  	FOREIGN KEY (artwork_id) REFERENCES Artwork(artwork_id),
  	FOREIGN KEY (tag_id) REFERENCES ArtworkTag(tag_id)
);

CREATE TABLE Gallery (
  	gallery_id INT AUTO_INCREMENT PRIMARY KEY,
  	name VARCHAR(150),
  	location VARCHAR(255),
  	capacity INT,
  	contactEmail VARCHAR(100),
  	phone VARCHAR(30),
  	rating DOUBLE
);

CREATE TABLE Exhibition (
  	exhibition_id INT AUTO_INCREMENT PRIMARY KEY,
  	title VARCHAR(200),
  	description TEXT,
  	startDate DATE,
  	endDate DATE,
  	gallery_id INT,
  	FOREIGN KEY (gallery_id) REFERENCES Gallery(gallery_id)
);

CREATE TABLE Artwork_Exhibition (
  	artwork_id INT,
  	exhibition_id INT,
  	PRIMARY KEY (artwork_id, exhibition_id),
  	FOREIGN KEY (artwork_id) REFERENCES Artwork(artwork_id),
  	FOREIGN KEY (exhibition_id) REFERENCES Exhibition(exhibition_id)
);

CREATE TABLE CommunityMember (
  	member_id INT AUTO_INCREMENT PRIMARY KEY,
  	name VARCHAR(100),
  	email VARCHAR(100) UNIQUE,
  	birthYear INT,
  	phone VARCHAR(30),
  	city VARCHAR(100),
  	membershipType VARCHAR(50)
);

CREATE TABLE Review (
  	review_id INT AUTO_INCREMENT PRIMARY KEY,
  	rating INT CHECK (rating BETWEEN 1 AND 5),
  	comment TEXT,
  	reviewDate DATE,
  	member_id INT,
  	exhibition_id INT,
  	FOREIGN KEY (member_id) REFERENCES CommunityMember(member_id),
  	FOREIGN KEY (exhibition_id) REFERENCES Exhibition(exhibition_id)
);

CREATE TABLE Workshop (
  	workshop_id INT AUTO_INCREMENT PRIMARY KEY,
  	title VARCHAR(200),
  	description TEXT,
  	startTime DATETIME,
  	durationHours INT,
  	maxParticipants INT,
  	price DECIMAL(10,2),
  	instructor_id INT,
  	FOREIGN KEY (instructor_id) REFERENCES Artist(artist_id)
);

CREATE TABLE Booking (
  	booking_id INT AUTO_INCREMENT PRIMARY KEY,
  	bookingDate DATETIME,
  	paymentStatus VARCHAR(50),
  	member_id INT,
  	workshop_id INT,
  	FOREIGN KEY (member_id) REFERENCES CommunityMember(member_id),
  	FOREIGN KEY (workshop_id) REFERENCES Workshop(workshop_id)
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


