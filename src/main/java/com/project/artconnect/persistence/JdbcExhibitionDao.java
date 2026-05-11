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

public class JdbcExhibitionDao implements ExhibitionDao {

    private static final String SELECT_BASE =
        "SELECT e.exhibition_id AS e_id, e.title AS e_title, e.startDate, e.endDate, " +
        "       e.description AS e_desc, " +
        "       g.name AS g_name, g.location AS g_address, g.rating AS g_rating, " +
        "       aw.title AS aw_title " +
        "FROM Exhibition e " +
        "LEFT JOIN Gallery g ON g.gallery_id = e.gallery_id " +
        "LEFT JOIN Artwork_Exhibition ae ON ae.exhibition_id = e.exhibition_id " +
        "LEFT JOIN Artwork aw ON aw.artwork_id = ae.artwork_id ";



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



    @Override
    public void save(Exhibition exhibition) {
        final String sql =
            "INSERT INTO Exhibition (title, startDate, endDate, description, gallery_id) " +
            "VALUES (?, ?, ?, ?, (SELECT gallery_id FROM Gallery WHERE name=?))";

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

            linkArtworks(conn, exhibId, exhibition.getArtworks());
            conn.commit();
        } catch (SQLException e) {
            ConnectionManager.rollback(conn);
            throw new RuntimeException("Erreur save exhibition : " + e.getMessage(), e);
        } finally {
            ConnectionManager.close(conn);
        }
    }



    @Override
    public void update(Exhibition exhibition) {
        final String sql =
            "UPDATE Exhibition SET startDate=?, endDate=?, description=?, " +
            "                      gallery_id=(SELECT gallery_id FROM Gallery WHERE name=?) " +
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
                ps.setString(5, exhibition.getTitle());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Artwork_Exhibition WHERE exhibition_id=?")) {
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



    @Override
    public void delete(String title) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM Exhibition WHERE title=?")) {
            ps.setString(1, title);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete exhibition : " + e.getMessage(), e);
        }
    }



    private List<Exhibition> mapExhibitions(ResultSet rs) throws SQLException {
        Map<Long, Exhibition> map = new LinkedHashMap<>();
        while (rs.next()) {
            long id = rs.getLong("e_id");
            Exhibition ex = map.computeIfAbsent(id, k -> {
                try {
                    Exhibition e = new Exhibition();
                    e.setTitle(rs.getString("e_title"));
                    java.sql.Date sd = rs.getDate("startDate");
                    if (sd != null) e.setStartDate(sd.toLocalDate());
                    java.sql.Date ed = rs.getDate("endDate");
                    if (ed != null) e.setEndDate(ed.toLocalDate());
                    e.setDescription(rs.getString("e_desc"));
                    String gName = rs.getString("g_name");
                    if (gName != null) {
                        Gallery g = new Gallery();
                        g.setName(gName);
                        g.setLocation(rs.getString("g_address"));
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
    }

    private void linkArtworks(Connection conn, long exhibId, List<Artwork> artworks)
            throws SQLException {
        final String sql =
            "INSERT IGNORE INTO Artwork_Exhibition (exhibition_id, artwork_id) " +
            "SELECT ?, artwork_id FROM Artwork WHERE title=?";
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
                "SELECT exhibition_id FROM Exhibition WHERE title=?")) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("exhibition_id") : -1L;
            }
        }
    }
}
