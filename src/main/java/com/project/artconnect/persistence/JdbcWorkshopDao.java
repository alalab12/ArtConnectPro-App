package com.project.artconnect.persistence;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation JDBC de {@link WorkshopDao}.
 */
public class JdbcWorkshopDao implements WorkshopDao {

    private static final String SELECT_BASE =
        "SELECT w.id AS w_id, w.title, w.date_time, w.duration_minutes, " +
        "       w.max_participants, w.price, w.location, w.description, w.level, " +
        "       a.name AS a_name, a.city AS a_city " +
        "FROM workshop w " +
        "LEFT JOIN artist a ON a.id = w.instructor_id ";

    @Override
    public Optional<Workshop> findById(Long id) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE w.id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById workshop : " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Workshop> findAll() {
        List<Workshop> results = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY w.date_time");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) results.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll workshops : " + e.getMessage(), e);
        }
        return results;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Workshop mapRow(ResultSet rs) throws SQLException {
        Workshop w = new Workshop();
        w.setTitle(rs.getString("title"));
        Timestamp ts = rs.getTimestamp("date_time");
        if (ts != null) w.setDate(ts.toLocalDateTime());
        w.setDurationMinutes(rs.getInt("duration_minutes"));
        w.setMaxParticipants(rs.getInt("max_participants"));
        w.setPrice(rs.getDouble("price"));
        w.setLocation(rs.getString("location"));
        w.setDescription(rs.getString("description"));
        w.setLevel(rs.getString("level"));

        String aName = rs.getString("a_name");
        if (aName != null) {
            Artist a = new Artist();
            a.setName(aName);
            a.setCity(rs.getString("a_city"));
            w.setInstructor(a);
        }
        return w;
    }
}
