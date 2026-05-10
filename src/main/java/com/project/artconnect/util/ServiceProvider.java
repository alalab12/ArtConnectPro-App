package com.project.artconnect.util;

import com.project.artconnect.service.*;
import com.project.artconnect.service.impl.*;

/**
 * Fournisseur de services singleton.
 *
 * <p>Pour basculer vers les services en mémoire (tests, démonstration sans BDD),
 * remplacer {@code USE_JDBC = true} par {@code false}.</p>
 *
 * <pre>
 * ┌──────────────────────────────────────────┐
 * │  USE_JDBC = true  → services JDBC (BDD)  │
 * │  USE_JDBC = false → services InMemory    │
 * └──────────────────────────────────────────┘
 * </pre>
 */
public class ServiceProvider {

    /**
     * Commutateur central.
     * <ul>
     *   <li>{@code true}  : l'application utilise MySQL via JDBC</li>
     *   <li>{@code false} : l'application utilise des données en mémoire (demo)</li>
     * </ul>
     */
    private static final boolean USE_JDBC = true;

    // ── Instances singleton ───────────────────────────────────────────────────

    private static final ArtistService    artistService;
    private static final ArtworkService   artworkService;
    private static final GalleryService   galleryService;
    private static final WorkshopService  workshopService;
    private static final CommunityService communityService;

    static {
        if (USE_JDBC) {
            // ── Couche JDBC – données persistées dans MySQL ──────────────────
            artistService    = new JdbcArtistService();
            artworkService   = new JdbcArtworkService();
            galleryService   = new JdbcGalleryService();
            workshopService  = new JdbcWorkshopService();
            communityService = new JdbcCommunityService();
        } else {
            // ── Couche InMemory – pour les tests / démonstration ─────────────
            InMemoryArtistService   memArtist  = new InMemoryArtistService();
            InMemoryArtworkService  memArtwork = new InMemoryArtworkService();
            InMemoryGalleryService  memGallery = new InMemoryGalleryService();
            InMemoryWorkshopService memWorkshop= new InMemoryWorkshopService();
            InMemoryCommunityService memCom    = new InMemoryCommunityService();

            memArtwork.initData(memArtist);
            memGallery.initData(memArtwork);
            memWorkshop.initData(memArtist);
            memCom.initData(memArtwork);

            artistService    = memArtist;
            artworkService   = memArtwork;
            galleryService   = memGallery;
            workshopService  = memWorkshop;
            communityService = memCom;
        }
    }

    // ── Accesseurs ────────────────────────────────────────────────────────────

    public static ArtistService    getArtistService()    { return artistService; }
    public static ArtworkService   getArtworkService()   { return artworkService; }
    public static GalleryService   getGalleryService()   { return galleryService; }
    public static WorkshopService  getWorkshopService()  { return workshopService; }
    public static CommunityService getCommunityService() { return communityService; }

    private ServiceProvider() {}
}
