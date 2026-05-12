package com.project.artconnect.persistence;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.sql.Types;
import java.util.*;

public class JdbcCommunityMemberDao implements CommunityMemberDao {

    @Override
    public Optional<CommunityMember> findById(Long id) {
        final String sql = buildSql() + "WHERE m.member_id=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<CommunityMember> list = mapMembers(rs);
                return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById member : " + e.getMessage(), e);
        }
    }

    @Override
    public List<CommunityMember> findAll() {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(buildSql() + "ORDER BY m.name");
             ResultSet rs = ps.executeQuery()) {
            return mapMembers(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll members : " + e.getMessage(), e);
        }
    }



    private String buildSql() {
        return
            "SELECT m.member_id, m.name, m.email, m.birthYear, m.phone, m.city, m.membershipType " +
            "FROM CommunityMember m ";
    }

    @Override
    public void save(CommunityMember member) {
        final String sql =
            "INSERT INTO CommunityMember (name, email, birthYear, phone, city, membershipType) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setObject(3, member.getBirthYear(), Types.INTEGER);
            ps.setString(4, member.getPhone());
            ps.setString(5, member.getCity());
            ps.setString(6, member.getMembershipType() != null ? member.getMembershipType() : "Standard");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur save member : " + e.getMessage(), e);
        }
    }

    @Override
    public void update(CommunityMember member) {
        final String sql =
            "UPDATE CommunityMember SET email=?, birthYear=?, phone=?, city=?, membershipType=? " +
            "WHERE name=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getEmail());
            ps.setObject(2, member.getBirthYear(), Types.INTEGER);
            ps.setString(3, member.getPhone());
            ps.setString(4, member.getCity());
            ps.setString(5, member.getMembershipType() != null ? member.getMembershipType() : "Standard");
            ps.setString(6, member.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update member : " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String name) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Booking WHERE member_id = " +
                    "(SELECT member_id FROM CommunityMember WHERE name=?)")) {
                ps.setString(1, name);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Review WHERE member_id = " +
                    "(SELECT member_id FROM CommunityMember WHERE name=?)")) {
                ps.setString(1, name);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM CommunityMember WHERE name=?")) {
                ps.setString(1, name);
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            ConnectionManager.rollback(conn);
            throw new RuntimeException("Erreur delete member : " + e.getMessage(), e);
        } finally {
            ConnectionManager.close(conn);
        }
    }

    private List<CommunityMember> mapMembers(ResultSet rs) throws SQLException {
        List<CommunityMember> list = new ArrayList<>();
        while (rs.next()) {
            CommunityMember cm = new CommunityMember();
            cm.setName(rs.getString("name"));
            cm.setEmail(rs.getString("email"));
            int by = rs.getInt("birthYear");
            cm.setBirthYear(rs.wasNull() ? null : by);
            cm.setPhone(rs.getString("phone"));
            cm.setCity(rs.getString("city"));
            cm.setMembershipType(rs.getString("membershipType"));
            list.add(cm);
        }
        return list;
    }
}
