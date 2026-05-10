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
        final String sql = buildSelectSql() + "WHERE g.id = ?";
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
            "SELECT g.id AS g_id, g.name AS g_name, g.address, g.owner_name, " +
            "       g.opening_hours, g.contact_phone, g.rating, g.website AS g_website, " +
            "       e.id AS e_id, e.title AS e_title, e.start_date, e.end_date, " +
            "       e.description AS e_desc, e.curator_name, e.theme, " +
            "       aw.title AS aw_title, aw.type AS aw_type, aw.price AS aw_price, " +
            "       aw.status AS aw_status " +
            "FROM gallery g " +
            "LEFT JOIN exhibition e ON e.gallery_id = g.id " +
            "LEFT JOIN exhibition_artwork ea ON ea.exhibition_id = e.id " +
            "LEFT JOIN artwork aw ON aw.id = ea.artwork_id ";
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
        Map<Long, Gallery>     galleries   = new LinkedHashMap<>();
        Map<Long, Exhibition>  exhibitions = new LinkedHashMap<>();

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
                // Évite les doublons (plusieurs lignes par expo si plusieurs œuvres)
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
        g.setAddress(rs.getString("address"));
        g.setOwnerName(rs.getString("owner_name"));
        g.setOpeningHours(rs.getString("opening_hours"));
        g.setContactPhone(rs.getString("contact_phone"));
        g.setRating(rs.getDouble("rating"));
        g.setWebsite(rs.getString("g_website"));
        return g;
    }

    private Exhibition mapExhibitionRow(ResultSet rs, Gallery gallery) throws SQLException {
        Exhibition e = new Exhibition();
        e.setTitle(rs.getString("e_title"));
        java.sql.Date sd = rs.getDate("start_date");
        if (sd != null) e.setStartDate(sd.toLocalDate());
        java.sql.Date ed = rs.getDate("end_date");
        if (ed != null) e.setEndDate(ed.toLocalDate());
        e.setDescription(rs.getString("e_desc"));
        e.setCuratorName(rs.getString("curator_name"));
        e.setTheme(rs.getString("theme"));
        e.setGallery(gallery);
        return e;
    }
}
