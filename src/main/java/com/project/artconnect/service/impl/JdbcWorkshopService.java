package com.project.artconnect.service.impl;

import com.project.artconnect.model.Booking;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.persistence.JdbcWorkshopDao;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation de {@link WorkshopService} utilisant {@link JdbcWorkshopDao}.
 *
 * <p>La méthode {@code bookWorkshop} insère une ligne dans la table
 * {@code booking} et met à jour l'objet en mémoire.</p>
 */
public class JdbcWorkshopService implements WorkshopService {

    private final JdbcWorkshopDao dao;

    public JdbcWorkshopService() {
        this.dao = new JdbcWorkshopDao();
    }

    @Override
    public List<Workshop> getAllWorkshops() {
        return dao.findAll();
    }

    @Override
    public Optional<Workshop> getWorkshopByTitle(String title) {
        return dao.findAll().stream()
                .filter(w -> w.getTitle().equals(title))
                .findFirst();
    }

    @Override
    public void bookWorkshop(Workshop workshop, CommunityMember member) {
        if (workshop == null || member == null) return;

        // Persiste la réservation en base
        final String sql =
            "INSERT INTO booking (workshop_id, member_id, payment_status) " +
            "SELECT w.id, m.id, 'PENDING' " +
            "FROM workshop w, community_member m " +
            "WHERE w.title=? AND m.name=?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, workshop.getTitle());
            ps.setString(2, member.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur bookWorkshop : " + e.getMessage(), e);
        }

        // Met aussi à jour l'objet en mémoire (cohérence immédiate)
        Booking b = new Booking(workshop, member);
        member.addBooking(b);
    }

    @Override
    public List<Booking> getBookingsByMember(CommunityMember member) {
        if (member == null) return List.of();
        return member.getBookings();
    }
}
