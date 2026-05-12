package com.project.artconnect.persistence;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.*;

public class JdbcGalleryDao implements GalleryDao {

    @Override
    public Optional<Gallery> findById(Long id) {
        final String sql = buildSelectSql() + "WHERE g.gallery_id = ?";
        return findBySql(sql, id);
    }

    @Override
    public List<Gallery> findAll() {
        final String sql = buildSelectSql() + "ORDER BY g.name";
        List<Gallery> results = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            results = mapGalleries(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll galleries : " + e.getMessage(), e);
        }
        return results;
    }



    private String buildSelectSql() {
        return
            "SELECT g.gallery_id AS g_id, g.name AS g_name, g.location AS g_location, " +
            "       g.capacity, g.contactEmail AS g_email, g.phone AS g_phone, g.rating, " +
            "       e.exhibition_id AS e_id, e.title AS e_title, e.startDate, e.endDate, " +
            "       e.description AS e_desc, " +
            "       aw.title AS aw_title, aw.type AS aw_type, aw.price AS aw_price, " +
            "       aw.status AS aw_status " +
            "FROM Gallery g " +
            "LEFT JOIN Exhibition e ON e.gallery_id = g.gallery_id " +
            "LEFT JOIN Artwork_Exhibition ae ON ae.exhibition_id = e.exhibition_id " +
            "LEFT JOIN Artwork aw ON aw.artwork_id = ae.artwork_id ";
    }

    private Optional<Gallery> findBySql(String sql, long idParam) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idParam);
            try (ResultSet rs = ps.executeQuery()) {
                List<Gallery> list = mapGalleries(rs);
                return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById gallery : " + e.getMessage(), e);
        }
    }

    private List<Gallery> mapGalleries(ResultSet rs) throws SQLException {
        Map<Long, Gallery>    galleries   = new LinkedHashMap<>();
        Map<Long, Exhibition> exhibitions = new LinkedHashMap<>();

        while (rs.next()) {
            long gId = rs.getLong("g_id");
            Gallery g = galleries.computeIfAbsent(gId, k -> {
                try { return mapGalleryRow(rs); }
                catch (SQLException e) { throw new RuntimeException(e); }
            });

            long eId = rs.getLong("e_id");
            if (rs.wasNull()) continue;

            Exhibition ex = exhibitions.computeIfAbsent(eId, k -> {
                try {
                    Exhibition e = mapExhibitionRow(rs, g);
                    g.addExhibition(e);
                    return e;
                } catch (SQLException e) { throw new RuntimeException(e); }
            });

            String awTitle = rs.getString("aw_title");
            if (awTitle != null) {
                Artwork aw = new Artwork();
                aw.setTitle(awTitle);
                aw.setType(rs.getString("aw_type"));
                aw.setPrice(rs.getDouble("aw_price"));
                String st = rs.getString("aw_status");
                if (st != null) {
                    try { aw.setStatus(Artwork.Status.valueOf(st)); }
                    catch (IllegalArgumentException ignored) {}
                }
                boolean alreadyAdded = ex.getArtworks().stream()
                        .anyMatch(a -> a.getTitle().equals(awTitle));
                if (!alreadyAdded) ex.getArtworks().add(aw);
            }
        }
        return new ArrayList<>(galleries.values());
    }

    private Gallery mapGalleryRow(ResultSet rs) throws SQLException {
        Gallery g = new Gallery();
        g.setName(rs.getString("g_name"));
        g.setLocation(rs.getString("g_location"));
        g.setCapacity(rs.getInt("capacity"));
        g.setContactEmail(rs.getString("g_email"));
        g.setContactPhone(rs.getString("g_phone"));
        g.setRating(rs.getDouble("rating"));
        return g;
    }



    @Override
    public void save(Gallery gallery) {
        final String sql =
            "INSERT INTO Gallery (name, location, capacity, contactEmail, phone, rating) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gallery.getName());
            ps.setString(2, gallery.getLocation());
            ps.setInt(3, gallery.getCapacity());
            ps.setString(4, gallery.getContactEmail());
            ps.setString(5, gallery.getContactPhone());
            ps.setDouble(6, gallery.getRating());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save gallery : " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Gallery gallery) {
        final String sql =
            "UPDATE Gallery SET location=?, capacity=?, contactEmail=?, phone=?, rating=? " +
            "WHERE name=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gallery.getLocation());
            ps.setInt(2, gallery.getCapacity());
            ps.setString(3, gallery.getContactEmail());
            ps.setString(4, gallery.getContactPhone());
            ps.setDouble(5, gallery.getRating());
            ps.setString(6, gallery.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update gallery : " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String name) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Exhibition SET gallery_id=NULL WHERE gallery_id = " +
                    "(SELECT gallery_id FROM Gallery WHERE name=?)")) {
                ps.setString(1, name);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Gallery WHERE name=?")) {
                ps.setString(1, name);
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            ConnectionManager.rollback(conn);
            throw new RuntimeException("Erreur delete gallery : " + e.getMessage(), e);
        } finally {
            ConnectionManager.close(conn);
        }
    }

    private Exhibition mapExhibitionRow(ResultSet rs, Gallery gallery) throws SQLException {
        Exhibition e = new Exhibition();
        e.setTitle(rs.getString("e_title"));
        java.sql.Date sd = rs.getDate("startDate");
        if (sd != null) e.setStartDate(sd.toLocalDate());
        java.sql.Date ed = rs.getDate("endDate");
        if (ed != null) e.setEndDate(ed.toLocalDate());
        e.setDescription(rs.getString("e_desc"));
        e.setGallery(gallery);
        return e;
    }
}
