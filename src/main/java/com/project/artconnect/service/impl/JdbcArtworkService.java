package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.persistence.JdbcArtworkDao;
import com.project.artconnect.service.ArtworkService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JdbcArtworkService implements ArtworkService {

    private final ArtworkDao dao;

    public JdbcArtworkService() {
        this(new JdbcArtworkDao());
    }

    public JdbcArtworkService(ArtworkDao dao) {
        this.dao = dao;
    }

    @Override
    public List<Artwork> getAllArtworks() {
        return dao.findAll();
    }

    @Override
    public Optional<Artwork> getArtworkByTitle(String title) {
        return dao.findAll().stream()
                .filter(a -> a.getTitle().equals(title))
                .findFirst();
    }

    @Override
    public List<Artwork> getArtworksByArtist(Artist artist) {
        if (artist == null) return List.of();
        return dao.findByArtistName(artist.getName());
    }

    @Override
    public void createArtwork(Artwork artwork) {
        dao.save(artwork);
    }

    @Override
    public void updateArtwork(Artwork artwork) {
        dao.update(artwork);
    }

    @Override
    public void deleteArtwork(String title) {
        dao.delete(title);
    }
}
