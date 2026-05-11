package com.project.artconnect.persistence;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcWorkshopDao implements WorkshopDao {

    private static final String SELECT_BASE =
        "SELECT w.workshop_id AS w_id, w.title, w.startTime, w.durationHours, " +
        "       w.maxParticipants, w.price, w.description, " +
        "       a.name AS a_name, a.city AS a_city " +
        "FROM Workshop w " +
        "LEFT JOIN Artist a ON a.artist_id = w.instructor_id ";

    @Override
    public Optional<Workshop> findById(Long id) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE w.workshop_id=?")) {
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
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY w.startTime");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) results.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll workshops : " + e.getMessage(), e);
        }
        return results;
    }



    @Override
    public void save(Workshop workshop) {
        final String sql =
            "INSERT INTO Workshop (title, description, startTime, durationHours, maxParticipants, price, instructor_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, (SELECT artist_id FROM Artist WHERE name=?))";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setWorkshopParams(ps, workshop);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save workshop : " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Workshop workshop) {
        final String sql =
            "UPDATE Workshop SET description=?, startTime=?, durationHours=?, maxParticipants=?, " +
            "                    price=?, instructor_id=(SELECT artist_id FROM Artist WHERE name=?) " +
            "WHERE title=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, workshop.getDescription());
            ps.setTimestamp(2, workshop.getDate() != null
                    ? java.sql.Timestamp.valueOf(workshop.getDate()) : null);
            ps.setInt(3, workshop.getDurationHours());
            ps.setInt(4, workshop.getMaxParticipants());
            ps.setDouble(5, workshop.getPrice());
            ps.setString(6, workshop.getInstructor() != null ? workshop.getInstructor().getName() : null);
            ps.setString(7, workshop.getTitle());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update workshop : " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String title) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM Workshop WHERE title=?")) {
            ps.setString(1, title);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete workshop : " + e.getMessage(), e);
        }
    }



    private void setWorkshopParams(PreparedStatement ps, Workshop w) throws SQLException {
        ps.setString(1, w.getTitle());
        ps.setString(2, w.getDescription());
        ps.setTimestamp(3, w.getDate() != null ? java.sql.Timestamp.valueOf(w.getDate()) : null);
        ps.setInt(4, w.getDurationHours());
        ps.setInt(5, w.getMaxParticipants());
        ps.setDouble(6, w.getPrice());
        ps.setString(7, w.getInstructor() != null ? w.getInstructor().getName() : null);
    }

    private Workshop mapRow(ResultSet rs) throws SQLException {
        Workshop w = new Workshop();
        w.setTitle(rs.getString("title"));
        Timestamp ts = rs.getTimestamp("startTime");
        if (ts != null) w.setDate(ts.toLocalDateTime());

        w.setDurationHours(rs.getInt("durationHours"));
        w.setMaxParticipants(rs.getInt("maxParticipants"));
        w.setPrice(rs.getDouble("price"));
        w.setDescription(rs.getString("description"));

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
