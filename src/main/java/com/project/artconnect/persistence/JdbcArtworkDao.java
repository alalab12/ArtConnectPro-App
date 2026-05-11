package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JdbcArtworkDao implements ArtworkDao {

    private static final String SELECT_BASE =
        "SELECT aw.title, aw.creationYear, aw.type, aw.medium, aw.dimensions, " +
        "       aw.description, aw.price, aw.status, " +
        "       ar.name AS artist_name, ar.bio AS artist_bio, " +
        "       ar.birthYear AS artist_birth_year, ar.contactEmail AS artist_email, " +
        "       ar.city AS artist_city, ar.isActive AS artist_active " +
        "FROM Artwork aw " +
        "LEFT JOIN Artist ar ON ar.artist_id = aw.artist_id ";



    @Override
    public List<Artwork> findAll() {
        List<Artwork> results = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY aw.title");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll artworks : " + e.getMessage(), e);
        }
        return results;
    }



    @Override
    public List<Artwork> findByArtistName(String artistName) {
        List<Artwork> results = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT_BASE + "WHERE ar.name = ? ORDER BY aw.title")) {

            ps.setString(1, artistName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByArtistName : " + e.getMessage(), e);
        }
        return results;
    }



    @Override
    public void save(Artwork artwork) {
        final String sql =
            "INSERT INTO Artwork (title, creationYear, type, medium, dimensions, " +
            "                     description, price, status, artist_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, " +
            "        (SELECT artist_id FROM Artist WHERE name=?))";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setArtworkParams(ps, artwork);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save artwork : " + e.getMessage(), e);
        }
    }



    @Override
    public void update(Artwork artwork) {
        final String sql =
            "UPDATE Artwork SET creationYear=?, type=?, medium=?, dimensions=?, " +
            "                   description=?, price=?, status=?, " +
            "                   artist_id=(SELECT artist_id FROM Artist WHERE name=?) " +
            "WHERE title=?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, artwork.getCreationYear(), Types.INTEGER);
            ps.setString(2, artwork.getType());
            ps.setString(3, artwork.getMedium());
            ps.setString(4, artwork.getDimensions());
            ps.setString(5, artwork.getDescription());
            ps.setDouble(6, artwork.getPrice());
            ps.setString(7, artwork.getStatus() != null ? artwork.getStatus().name() : null);
            ps.setString(8, artwork.getArtist() != null ? artwork.getArtist().getName() : null);
            ps.setString(9, artwork.getTitle());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update artwork : " + e.getMessage(), e);
        }
    }



    @Override
    public void delete(String title) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM Artwork WHERE title=?")) {
            ps.setString(1, title);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete artwork : " + e.getMessage(), e);
        }
    }



    private Artwork mapRow(ResultSet rs) throws SQLException {
        Artwork aw = new Artwork();
        aw.setTitle(rs.getString("title"));
        int cy = rs.getInt("creationYear");
        aw.setCreationYear(rs.wasNull() ? null : cy);
        aw.setType(rs.getString("type"));
        aw.setMedium(rs.getString("medium"));
        aw.setDimensions(rs.getString("dimensions"));
        aw.setDescription(rs.getString("description"));
        aw.setPrice(rs.getDouble("price"));

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try { aw.setStatus(Artwork.Status.valueOf(statusStr)); }
            catch (IllegalArgumentException ignored) {}
        }

        String artistName = rs.getString("artist_name");
        if (artistName != null) {
            Artist ar = new Artist();
            ar.setName(artistName);
            ar.setBio(rs.getString("artist_bio"));
            int by = rs.getInt("artist_birth_year");
            ar.setBirthYear(rs.wasNull() ? null : by);
            ar.setContactEmail(rs.getString("artist_email"));
            ar.setCity(rs.getString("artist_city"));
            ar.setActive(rs.getBoolean("artist_active"));
            aw.setArtist(ar);
        }
        return aw;
    }

    private void setArtworkParams(PreparedStatement ps, Artwork aw) throws SQLException {
        ps.setString(1, aw.getTitle());
        ps.setObject(2, aw.getCreationYear(), Types.INTEGER);
        ps.setString(3, aw.getType());
        ps.setString(4, aw.getMedium());
        ps.setString(5, aw.getDimensions());
        ps.setString(6, aw.getDescription());
        ps.setDouble(7, aw.getPrice());
        ps.setString(8, aw.getStatus() != null ? aw.getStatus().name() : "FOR_SALE");
        ps.setString(9, aw.getArtist() != null ? aw.getArtist().getName() : null);
    }
}
