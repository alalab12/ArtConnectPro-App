package com.project.artconnect.service.impl;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.persistence.JdbcExhibitionDao;
import com.project.artconnect.persistence.JdbcGalleryDao;
import com.project.artconnect.service.ExhibitionService;

import java.util.List;
import java.util.stream.Collectors;

public class JdbcExhibitionService implements ExhibitionService {

    private final JdbcExhibitionDao exhibitionDao;
    private final JdbcGalleryDao    galleryDao;

    public JdbcExhibitionService() {
        this.exhibitionDao = new JdbcExhibitionDao();
        this.galleryDao    = new JdbcGalleryDao();
    }

    @Override
    public List<Exhibition> getAllExhibitions() {
        return exhibitionDao.findAll();
    }

    @Override
    public List<String> getAllGalleryNames() {
        return galleryDao.findAll().stream()
                .map(Gallery::getName)
                .collect(Collectors.toList());
    }

    @Override
    public void createExhibition(Exhibition exhibition) {
        exhibitionDao.save(exhibition);
    }

    @Override
    public void updateExhibition(Exhibition exhibition) {
        exhibitionDao.update(exhibition);
    }

    @Override
    public void deleteExhibition(String title) {
        exhibitionDao.delete(title);
    }
}
