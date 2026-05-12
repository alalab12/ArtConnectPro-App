package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JdbcArtistDao implements ArtistDao {



    @Override
    public List<Artist> findAll() {
        final String sql =
            "SELECT a.artist_id, a.name, a.bio, a.birthYear, a.contactEmail, " +
            "       a.phone, a.city, a.website, a.socialMedia, a.isActive, " +
            "       d.name AS disc_name " +
            "FROM Artist a " +
            "LEFT JOIN Artist_Discipline ad ON ad.artist_id = a.artist_id " +
            "LEFT JOIN Discipline d         ON d.discipline_id = ad.discipline_id " +
            "ORDER BY a.artist_id";

        Map<Long, Artist> map = new LinkedHashMap<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("artist_id");
                Artist artist = map.computeIfAbsent(id, k -> mapRow(rs));
                String discName = rs.getString("disc_name");
                if (discName != null) {
                    artist.getDisciplines().add(new Discipline(discName));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll artists : " + e.getMessage(), e);
        }
        return new ArrayList<>(map.values());
    }



    @Override
    public List<Artist> findByCity(String city) {
        final String sql =
            "SELECT a.artist_id, a.name, a.bio, a.birthYear, a.contactEmail, " +
            "       a.phone, a.city, a.website, a.socialMedia, a.isActive, " +
            "       d.name AS disc_name " +
            "FROM Artist a " +
            "LEFT JOIN Artist_Discipline ad ON ad.artist_id = a.artist_id " +
            "LEFT JOIN Discipline d         ON d.discipline_id = ad.discipline_id " +
            "WHERE a.city = ? " +
            "ORDER BY a.artist_id";

        Map<Long, Artist> map = new LinkedHashMap<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, city);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("artist_id");
                    Artist artist = map.computeIfAbsent(id, k -> mapRow(rs));
                    String discName = rs.getString("disc_name");
                    if (discName != null) {
                        artist.getDisciplines().add(new Discipline(discName));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByCity : " + e.getMessage(), e);
        }
        return new ArrayList<>(map.values());
    }



    @Override
    public void save(Artist artist) {
        final String insertArtist =
            "INSERT INTO Artist (name, bio, birthYear, contactEmail, phone, " +
            "                    city, website, socialMedia, isActive) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            long artistId;
            try (PreparedStatement ps = conn.prepareStatement(
                    insertArtist, Statement.RETURN_GENERATED_KEYS)) {
                setArtistParams(ps, artist);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Aucune clé générée pour artist.");
                    artistId = keys.getLong(1);
                }
            }

            saveDisciplineLinks(conn, artistId, artist.getDisciplines());

            conn.commit();
        } catch (SQLException e) {
            ConnectionManager.rollback(conn);
            throw new RuntimeException("Erreur save artist : " + e.getMessage(), e);
        } finally {
            ConnectionManager.close(conn);
        }
    }



    @Override
    public void update(Artist artist) {
        final String updateArtist =
            "UPDATE Artist SET bio=?, birthYear=?, contactEmail=?, phone=?, " +
            "                  city=?, website=?, socialMedia=?, isActive=? " +
            "WHERE name=?";

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            long artistId = findIdByName(conn, artist.getName());
            if (artistId < 0) throw new SQLException("Artiste introuvable : " + artist.getName());

            try (PreparedStatement ps = conn.prepareStatement(updateArtist)) {
                ps.setString(1, artist.getBio());
                ps.setObject(2, artist.getBirthYear(), Types.INTEGER);
                ps.setString(3, artist.getContactEmail());
                ps.setString(4, artist.getPhone());
                ps.setString(5, artist.getCity());
                ps.setString(6, artist.getWebsite());
                ps.setString(7, artist.getSocialMedia());
                ps.setBoolean(8, artist.isActive());
                ps.setString(9, artist.getName());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Artist_Discipline WHERE artist_id=?")) {
                ps.setLong(1, artistId);
                ps.executeUpdate();
            }
            saveDisciplineLinks(conn, artistId, artist.getDisciplines());

            conn.commit();
        } catch (SQLException e) {
            ConnectionManager.rollback(conn);
            throw new RuntimeException("Erreur update artist : " + e.getMessage(), e);
        } finally {
            ConnectionManager.close(conn);
        }
    }



    @Override
    public void delete(String artistName) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            long artistId = findIdByName(conn, artistName);
            if (artistId < 0) { conn.commit(); return; }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Artwork_Exhibition WHERE artwork_id IN " +
                    "(SELECT artwork_id FROM Artwork WHERE artist_id=?)")) {
                ps.setLong(1, artistId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Artwork WHERE artist_id=?")) {
                ps.setLong(1, artistId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Artist_Discipline WHERE artist_id=?")) {
                ps.setLong(1, artistId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Workshop SET instructor_id=NULL WHERE instructor_id=?")) {
                ps.setLong(1, artistId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Artist WHERE artist_id=?")) {
                ps.setLong(1, artistId);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            ConnectionManager.rollback(conn);
            throw new RuntimeException("Erreur delete artist : " + e.getMessage(), e);
        } finally {
            ConnectionManager.close(conn);
        }
    }



    private Artist mapRow(ResultSet rs) {
        try {
            Artist a = new Artist();
            a.setName(rs.getString("name"));
            a.setBio(rs.getString("bio"));
            int birthYear = rs.getInt("birthYear");
            a.setBirthYear(rs.wasNull() ? null : birthYear);
            a.setContactEmail(rs.getString("contactEmail"));
            a.setPhone(rs.getString("phone"));
            a.setCity(rs.getString("city"));
            a.setWebsite(rs.getString("website"));
            a.setSocialMedia(rs.getString("socialMedia"));
            a.setActive(rs.getBoolean("isActive"));
            return a;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mapRow artist : " + e.getMessage(), e);
        }
    }

    private void setArtistParams(PreparedStatement ps, Artist a) throws SQLException {
        ps.setString(1, a.getName());
        ps.setString(2, a.getBio());
        ps.setObject(3, a.getBirthYear(), Types.INTEGER);
        ps.setString(4, a.getContactEmail());
        ps.setString(5, a.getPhone());
        ps.setString(6, a.getCity());
        ps.setString(7, a.getWebsite());
        ps.setString(8, a.getSocialMedia());
        ps.setBoolean(9, a.isActive());
    }

    private void saveDisciplineLinks(Connection conn, long artistId,
                                     List<Discipline> disciplines) throws SQLException {
        final String upsertDisc = "INSERT IGNORE INTO Discipline (name) VALUES (?)";
        final String selectDisc = "SELECT discipline_id FROM Discipline WHERE name=?";
        final String linkDisc   = "INSERT IGNORE INTO Artist_Discipline (artist_id, discipline_id) VALUES (?,?)";

        for (Discipline disc : disciplines) {
            try (PreparedStatement ps = conn.prepareStatement(upsertDisc)) {
                ps.setString(1, disc.getName());
                ps.executeUpdate();
            }
            long discId;
            try (PreparedStatement ps = conn.prepareStatement(selectDisc)) {
                ps.setString(1, disc.getName());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) continue;
                    discId = rs.getLong("discipline_id");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(linkDisc)) {
                ps.setLong(1, artistId);
                ps.setLong(2, discId);
                ps.executeUpdate();
            }
        }
    }

    private long findIdByName(Connection conn, String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT artist_id FROM Artist WHERE name=?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("artist_id") : -1L;
            }
        }
    }
}
