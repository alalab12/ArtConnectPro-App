package com.project.artconnect.persistence;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implémentation JDBC de {@link ExhibitionDao}.
 */
public class JdbcExhibitionDao implements ExhibitionDao {

    private static final String SELECT_BASE =
        "SELECT e.id AS e_id, e.title AS e_title, e.start_date, e.end_date, " +
        "       e.description AS e_desc, e.curator_name, e.theme, " +
        "       g.name AS g_name, g.address AS g_address, g.rating AS g_rating, " +
        "       aw.title AS aw_title " +
        "FROM exhibition e " +
        "LEFT JOIN gallery g ON g.id = e.gallery_id " +
        "LEFT JOIN exhibition_artwork ea ON ea.exhibition_id = e.id " +
        "LEFT JOIN artwork aw ON aw.id = ea.artwork_id ";

    // ── findAll ───────────────────────────────────────────────────────────────

    @Override
    public List<Exhibition> findAll() {
        List<Exhibition> results = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY e.title");
             ResultSet rs = ps.executeQuery()) {
            results = mapExhibitions(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll exhibitions : " + e.getMessage(), e);
        }
        return results;
    }

    // ── save ──────────────────────────────────────────────────────────────────

    @Override
    public void save(Exhibition exhibition) {
        final String sql =
            "INSERT INTO exhibition (title, start_date, end_date, description, " +
            "                        gallery_id, curator_name, theme) " +
            "VALUES (?, ?, ?, ?, (SELECT id FROM gallery WHERE name=?), ?, ?)";

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            long exhibId;
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setExhibitionParams(ps, exhibition);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Clé non générée pour exhibition.");
                    exhibId = keys.getLong(1);
                }
            }

            // Lier les œuvres
            linkArtworks(conn, exhibId, exhibition.getArtworks());
            conn.commit();
        } catch (SQLException e) {
            ConnectionManager.rollback(conn);
            throw new RuntimeException("Erreur save exhibition : " + e.getMessage(), e);
        } finally {
            ConnectionManager.close(conn);
        }
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Override
    public void update(Exhibition exhibition) {
        final String sql =
            "UPDATE exhibition SET start_date=?, end_date=?, description=?, " +
            "                      gallery_id=(SELECT id FROM gallery WHERE name=?), " +
            "                      curator_name=?, theme=? " +
            "WHERE title=?";

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            long exhibId = findIdByTitle(conn, exhibition.getTitle());
            if (exhibId < 0) throw new SQLException("Exhibition introuvable : " + exhibition.getTitle());

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, exhibition.getStartDate() != null
                        ? Date.valueOf(exhibition.getStartDate()) : null);
                ps.setDate(2, exhibition.getEndDate() != null
                        ? Date.valueOf(exhibition.getEndDate()) : null);
                ps.setString(3, exhibition.getDescription());
                ps.setString(4, exhibition.getGallery() != null ? exhibition.getGallery().getName() : null);
                ps.setString(5, exhibition.getCuratorName());
                ps.setString(6, exhibition.getTheme());
                ps.setString(7, exhibition.getTitle());
                ps.executeUpdate();
            }

            // Recalcul des liens œuvres
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM exhibition_artwork WHERE exhibition_id=?")) {
                ps.setLong(1, exhibId);
                ps.executeUpdate();
            }
            linkArtworks(conn, exhibId, exhibition.getArtworks());
            conn.commit();
        } catch (SQLException e) {
            ConnectionManager.rollback(conn);
            throw new RuntimeException("Erreur update exhibition : " + e.getMessage(), e);
        } finally {
            ConnectionManager.close(conn);
        }
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Override
    public void delete(String title) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM exhibition WHERE title=?")) {
            ps.setString(1, title);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete exhibition : " + e.getMessage(), e);
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private List<Exhibition> mapExhibitions(ResultSet rs) throws SQLException {
        Map<Long, Exhibition> map = new LinkedHashMap<>();
        while (rs.next()) {
            long id = rs.getLong("e_id");
            Exhibition ex = map.computeIfAbsent(id, k -> {
                try {
                    Exhibition e = new Exhibition();
                    e.setTitle(rs.getString("e_title"));
                    java.sql.Date sd = rs.getDate("start_date");
                    if (sd != null) e.setStartDate(sd.toLocalDate());
                    java.sql.Date ed = rs.getDate("end_date");
                    if (ed != null) e.setEndDate(ed.toLocalDate());
                    e.setDescription(rs.getString("e_desc"));
                    e.setCuratorName(rs.getString("curator_name"));
                    e.setTheme(rs.getString("theme"));
                    String gName = rs.getString("g_name");
                    if (gName != null) {
                        Gallery g = new Gallery();
                        g.setName(gName);
                        g.setAddress(rs.getString("g_address"));
                        g.setRating(rs.getDouble("g_rating"));
                        e.setGallery(g);
                    }
                    return e;
                } catch (SQLException ex2) { throw new RuntimeException(ex2); }
            });
            String awTitle = rs.getString("aw_title");
            if (awTitle != null) {
                boolean dup = ex.getArtworks().stream().anyMatch(a -> a.getTitle().equals(awTitle));
                if (!dup) {
                    Artwork aw = new Artwork();
                    aw.setTitle(awTitle);
                    ex.getArtworks().add(aw);
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    private void setExhibitionParams(PreparedStatement ps, Exhibition e) throws SQLException {
        ps.setString(1, e.getTitle());
        ps.setDate(2, e.getStartDate() != null ? Date.valueOf(e.getStartDate()) : null);
        ps.setDate(3, e.getEndDate()   != null ? Date.valueOf(e.getEndDate())   : null);
        ps.setString(4, e.getDescription());
        ps.setString(5, e.getGallery() != null ? e.getGallery().getName() : null);
        ps.setString(6, e.getCuratorName());
        ps.setString(7, e.getTheme());
    }

    private void linkArtworks(Connection conn, long exhibId, List<Artwork> artworks)
            throws SQLException {
        final String sql =
            "INSERT IGNORE INTO exhibition_artwork (exhibition_id, artwork_id) " +
            "SELECT ?, id FROM artwork WHERE title=?";
        for (Artwork aw : artworks) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, exhibId);
                ps.setString(2, aw.getTitle());
                ps.executeUpdate();
            }
        }
    }

    private long findIdByTitle(Connection conn, String title) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM exhibition WHERE title=?")) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("id") : -1L;
            }
        }
    }
}
