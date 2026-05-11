package com.project.artconnect.service.impl;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.service.ExhibitionService;
import com.project.artconnect.service.GalleryService;

import java.util.List;
import java.util.stream.Collectors;

public class InMemoryExhibitionService implements ExhibitionService {

    private final GalleryService galleryService;

    public InMemoryExhibitionService(GalleryService galleryService) {
        this.galleryService = galleryService;
    }

    @Override
    public List<Exhibition> getAllExhibitions() {
        return galleryService.getAllGalleries().stream()
                .flatMap(g -> g.getExhibitions().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllGalleryNames() {
        return galleryService.getAllGalleries().stream()
                .map(g -> g.getName())
                .collect(Collectors.toList());
    }

    @Override
    public void createExhibition(Exhibition exhibition) {
        if (exhibition.getGallery() != null) {
            galleryService.getGalleryByName(exhibition.getGallery().getName())
                    .ifPresent(g -> g.addExhibition(exhibition));
        }
    }

    @Override
    public void updateExhibition(Exhibition exhibition) {
        getAllExhibitions().stream()
                .filter(e -> e.getTitle().equals(exhibition.getTitle()))
                .findFirst()
                .ifPresent(e -> {
                    e.setStartDate(exhibition.getStartDate());
                    e.setEndDate(exhibition.getEndDate());
                    e.setDescription(exhibition.getDescription());
                    e.setGallery(exhibition.getGallery());
                });
    }

    @Override
    public void deleteExhibition(String title) {
        galleryService.getAllGalleries().forEach(g ->
                g.getExhibitions().removeIf(e -> e.getTitle().equals(title)));
    }
}
