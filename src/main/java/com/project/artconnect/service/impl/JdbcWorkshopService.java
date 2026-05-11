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
    public void createWorkshop(Workshop workshop) {
        dao.save(workshop);
    }

    @Override
    public void updateWorkshop(Workshop workshop) {
        dao.update(workshop);
    }

    @Override
    public void deleteWorkshop(String title) {
        dao.delete(title);
    }

    @Override
    public void bookWorkshop(Workshop workshop, CommunityMember member) {
        if (workshop == null || member == null) return;

        final String sql =
            "INSERT INTO Booking (workshop_id, member_id, paymentStatus, bookingDate) " +
            "SELECT w.workshop_id, m.member_id, 'PENDING', NOW() " +
            "FROM Workshop w, CommunityMember m " +
            "WHERE w.title=? AND m.name=?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, workshop.getTitle());
            ps.setString(2, member.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur bookWorkshop : " + e.getMessage(), e);
        }

        Booking b = new Booking(workshop, member);
        member.addBooking(b);
    }

    @Override
    public List<Booking> getBookingsByMember(CommunityMember member) {
        if (member == null) return List.of();
        return member.getBookings();
    }
}
