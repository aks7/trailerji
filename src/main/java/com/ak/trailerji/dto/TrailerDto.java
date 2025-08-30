package com.ak.trailerji.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrailerDto {
    private String videoId;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String channelId;
    private String channelTitle;
    private String publishedAt;
    private String duration;
    private Long viewCount;
    private String movieTitle;
    private String genre;
    private String releaseDate;
    private String studioName;
    private String trailerType; // "Official Trailer", "Teaser", "Final Trailer"
}