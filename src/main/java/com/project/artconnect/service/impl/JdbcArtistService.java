package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.persistence.JdbcArtistDao;
import com.project.artconnect.service.ArtistService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implémentation de {@link ArtistService} utilisant {@link JdbcArtistDao}.
 *
 * <p>Respecte exactement l'interface définie dans le contrat du projet ;
 * délègue toutes les opérations persistantes au DAO JDBC.</p>
 */
public class JdbcArtistService implements ArtistService {

    private final ArtistDao dao;

    /** Constructeur par défaut : utilise le DAO JDBC de production. */
    public JdbcArtistService() {
        this(new JdbcArtistDao());
    }

    /** Constructeur injectable (facilite les tests). */
    public JdbcArtistService(ArtistDao dao) {
        this.dao = dao;
    }

    @Override
    public List<Artist> getAllArtists() {
        return dao.findAll();
    }

    @Override
    public Optional<Artist> getArtistByName(String name) {
        return dao.findAll().stream()
                .filter(a -> a.getName().equals(name))
                .findFirst();
    }

    @Override
    public void createArtist(Artist artist) {
        dao.save(artist);
    }

    @Override
    public void updateArtist(Artist artist) {
        dao.update(artist);
    }

    @Override
    public void deleteArtist(String name) {
        dao.delete(name);
    }

    /**
     * Retourne la liste des disciplines distinctes issues des artistes en base.
     * (Aucune table dédiée n'est requise : elles sont extraites des artistes.)
     */
    @Override
    public List<Discipline> getAllDisciplines() {
        return dao.findAll().stream()
                .flatMap(a -> a.getDisciplines().stream())
                .map(Discipline::getName)
                .distinct()
                .map(Discipline::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<Artist> searchArtists(String query, String disciplineName, String city) {
        return dao.findAll().stream()
                .filter(a -> query == null || query.isEmpty()
                        || a.getName().toLowerCase().contains(query.toLowerCase()))
                .filter(a -> city == null || city.isEmpty()
                        || a.getCity().equalsIgnoreCase(city))
                .filter(a -> disciplineName == null || disciplineName.isEmpty()
                        || a.getDisciplines().stream()
                               .anyMatch(d -> d.getName().equals(disciplineName)))
                .collect(Collectors.toList());
    }
}
