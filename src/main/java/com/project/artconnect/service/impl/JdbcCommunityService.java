package com.project.artconnect.service.impl;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Review;
import com.project.artconnect.persistence.JdbcCommunityMemberDao;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCommunityService implements CommunityService {

    private final JdbcCommunityMemberDao dao = new JdbcCommunityMemberDao();

    @Override
    public List<CommunityMember> getAllMembers() { return dao.findAll(); }

    @Override
    public Optional<CommunityMember> getMemberByName(String name) {
        return dao.findAll().stream().filter(m -> m.getName().equals(name)).findFirst();
    }

    @Override
    public void createMember(CommunityMember member) { dao.save(member); }

    @Override
    public void updateMember(CommunityMember member) { dao.update(member); }

    @Override
    public void deleteMember(String name) { dao.delete(name); }

    @Override
    public List<Review> getReviewsByMember(CommunityMember member) {
        if (member == null) return List.of();

        final String sql =
            "SELECT r.rating, r.comment, r.reviewDate, e.title AS e_title " +
            "FROM Review r " +
            "JOIN CommunityMember m ON m.member_id = r.member_id " +
            "LEFT JOIN Exhibition e ON e.exhibition_id = r.exhibition_id " +
            "WHERE m.name = ?";
        List<Review> reviews = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getName());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Exhibition ex = new Exhibition();
                    ex.setTitle(rs.getString("e_title"));
                    Review r = new Review(member, ex, rs.getInt("rating"), rs.getString("comment"));
                    java.sql.Date rd = rs.getDate("reviewDate");
                    if (rd != null) r.setReviewDate(rd.toLocalDate());
                    reviews.add(r);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur getReviewsByMember : " + e.getMessage(), e);
        }
        return reviews;
    }
}
