package com.project.artconnect.model;

import java.time.LocalDate;

public class Review {
    private CommunityMember reviewer;
    private Exhibition exhibition;
    private int rating;
    private String comment;
    private LocalDate reviewDate;

    public Review() {
    }

    public Review(CommunityMember reviewer, Exhibition exhibition, int rating, String comment) {
        this.reviewer = reviewer;
        this.exhibition = exhibition;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = LocalDate.now();
    }

    public CommunityMember getReviewer() { return reviewer; }
    public void setReviewer(CommunityMember reviewer) { this.reviewer = reviewer; }

    public Exhibition getExhibition() { return exhibition; }
    public void setExhibition(Exhibition exhibition) { this.exhibition = exhibition; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDate getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }
}
