package com.project.artconnect.persistence;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.*;


public class JdbcCommunityMemberDao implements CommunityMemberDao {

    @Override
    public Optional<CommunityMember> findById(Long id) {
        final String sql = buildSql() + "WHERE m.id=?";
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
            "SELECT m.id, m.name, m.email, m.birth_year, m.phone, m.city, m.membership_type, " +
            "       d.name AS disc_name " +
            "FROM community_member m " +
            "LEFT JOIN member_discipline md ON md.member_id = m.id " +
            "LEFT JOIN discipline d ON d.id = md.discipline_id ";
    }

    private List<CommunityMember> mapMembers(ResultSet rs) throws SQLException {
        Map<Long, CommunityMember> map = new LinkedHashMap<>();
        while (rs.next()) {
            long id = rs.getLong("id");
            CommunityMember m = map.computeIfAbsent(id, k -> {
                try {
                    CommunityMember cm = new CommunityMember();
                    cm.setName(rs.getString("name"));
                    cm.setEmail(rs.getString("email"));
                    int by = rs.getInt("birth_year");
                    cm.setBirthYear(rs.wasNull() ? null : by);
                    cm.setPhone(rs.getString("phone"));
                    cm.setCity(rs.getString("city"));
                    cm.setMembershipType(rs.getString("membership_type"));
                    return cm;
                } catch (SQLException e) { throw new RuntimeException(e); }
            });
            String discName = rs.getString("disc_name");
            if (discName != null) {
                m.getFavoriteDisciplines().add(new Discipline(discName));
            }
        }
        return new ArrayList<>(map.values());
    }
}
