package com.project.artconnect.service.impl;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.persistence.JdbcGalleryDao;
import com.project.artconnect.service.GalleryService;

import java.util.List;
import java.util.Optional;

/**
 * Implémentation de {@link GalleryService} utilisant {@link JdbcGalleryDao}.
 */
public class JdbcGalleryService implements GalleryService {

    private final JdbcGalleryDao dao;

    public JdbcGalleryService() {
        this.dao = new JdbcGalleryDao();
    }

    @Override
    public List<Gallery> getAllGalleries() {
        return dao.findAll();
    }

    @Override
    public Optional<Gallery> getGalleryByName(String name) {
        return dao.findAll().stream()
                .filter(g -> g.getName().equals(name))
                .findFirst();
    }

    @Override
    public List<Exhibition> getExhibitionsByGallery(Gallery gallery) {
        if (gallery == null) return List.of();
        return gallery.getExhibitions();
    }
}
