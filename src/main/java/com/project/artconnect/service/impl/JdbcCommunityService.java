package com.project.artconnect.service.impl;

import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.CommunityMember;
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
    public void createMember(CommunityMember member) {
        final String sql =
            "INSERT INTO community_member (name, email, birth_year, phone, city, membership_type) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setObject(3, member.getBirthYear(), Types.INTEGER);
            ps.setString(4, member.getPhone());
            ps.setString(5, member.getCity());
            ps.setString(6, member.getMembershipType() != null ? member.getMembershipType() : "free");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur createMember : " + e.getMessage(), e);
        }
    }

    @Override
    public void updateMember(CommunityMember member) {
        final String sql =
            "UPDATE community_member SET email=?, birth_year=?, phone=?, city=?, membership_type=? " +
            "WHERE name=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getEmail());
            ps.setObject(2, member.getBirthYear(), Types.INTEGER);
            ps.setString(3, member.getPhone());
            ps.setString(4, member.getCity());
            ps.setString(5, member.getMembershipType() != null ? member.getMembershipType() : "free");
            ps.setString(6, member.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur updateMember : " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteMember(String name) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM community_member WHERE name=?")) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur deleteMember : " + e.getMessage(), e);
        }
    }

    @Override
    public List<Review> getReviewsByMember(CommunityMember member) {
        if (member == null) return List.of();
        final String sql =
            "SELECT r.rating, r.comment, r.review_date, aw.title AS aw_title " +
            "FROM review r " +
            "JOIN community_member m ON m.id = r.member_id " +
            "LEFT JOIN artwork aw ON aw.id = r.artwork_id " +
            "WHERE m.name = ?";
        List<Review> reviews = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getName());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Artwork aw = new Artwork();
                    aw.setTitle(rs.getString("aw_title"));
                    Review r = new Review(member, aw, rs.getInt("rating"), rs.getString("comment"));
                    java.sql.Date rd = rs.getDate("review_date");
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
