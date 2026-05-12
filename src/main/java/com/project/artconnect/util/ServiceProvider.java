package com.project.artconnect.util;

import com.project.artconnect.service.*;
import com.project.artconnect.service.impl.*;

public class ServiceProvider {


    private static final boolean USE_JDBC = true;



    private static final ArtistService      artistService;
    private static final ArtworkService     artworkService;
    private static final GalleryService     galleryService;
    private static final ExhibitionService  exhibitionService;
    private static final WorkshopService    workshopService;
    private static final CommunityService   communityService;

    static {
        if (USE_JDBC) {

            artistService      = new JdbcArtistService();
            artworkService     = new JdbcArtworkService();
            galleryService     = new JdbcGalleryService();
            exhibitionService  = new JdbcExhibitionService();
            workshopService    = new JdbcWorkshopService();
            communityService   = new JdbcCommunityService();
        } else {

            InMemoryArtistService   memArtist  = new InMemoryArtistService();
            InMemoryArtworkService  memArtwork = new InMemoryArtworkService();
            InMemoryGalleryService  memGallery = new InMemoryGalleryService();
            InMemoryWorkshopService memWorkshop= new InMemoryWorkshopService();
            InMemoryCommunityService memCom    = new InMemoryCommunityService();

            memArtwork.initData(memArtist);
            memGallery.initData(memArtwork);
            memWorkshop.initData(memArtist);
            memCom.initData();

            artistService      = memArtist;
            artworkService     = memArtwork;
            galleryService     = memGallery;
            exhibitionService  = new InMemoryExhibitionService(memGallery);
            workshopService    = memWorkshop;
            communityService   = memCom;
        }
    }



    public static ArtistService     getArtistService()     { return artistService; }
    public static ArtworkService    getArtworkService()    { return artworkService; }
    public static GalleryService    getGalleryService()    { return galleryService; }
    public static ExhibitionService getExhibitionService() { return exhibitionService; }
    public static WorkshopService   getWorkshopService()   { return workshopService; }
    public static CommunityService  getCommunityService()  { return communityService; }

    private ServiceProvider() {}
}
