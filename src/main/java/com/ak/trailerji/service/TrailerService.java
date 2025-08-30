package com.ak.trailerji.service;

import com.ak.trailerji.dto.TrailerDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrailerService {
    
    @Value("${youtube.api.key}")
    private String youtubeApiKey;
    
    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Official movie studio channel IDs (verified channels only)
    private static final Set<String> OFFICIAL_STUDIO_CHANNELS = Set.of(
        "UCvC4D8onUfXzvjTOM-dBfEA", // Marvel Entertainment
        "UC_IRYSp4auq7hKLvziWVH6w", // Warner Bros. Pictures
        "UCOwaTlA0nHlqMf4HBEWmLaw", // Universal Pictures
        "UCiifkYAs_bq1pt_zbNAzYGg", // Sony Pictures Entertainment
        "UCF0cXjaNm5Vx7EdCK4cxsYQ", // Paramount Pictures
        "UC-sSERhv7LzPWA3TxjzXJkw", // 20th Century Studios
        "UCYK0VLj-8kI2xJZRCCYgYnw", // Disney
        "UCXwyXuLskuYsZiWNh0STJdw", // Paramount Movies UK
        "UCP6T93Fe7qKYlvb_5Cs8Z4g", // Sky Cinema
        "UC2QJf3QCNwYt8GeNPP5fJhg", // Universal Movies International
        "UCi8e0iOVk1fEOogdfu4YgfA"  // Rotten Tomatoes Trailers
    );
    
    // Keywords to include (official trailer indicators)
    private static final Set<String> INCLUDE_KEYWORDS = Set.of(
        "official trailer", "official teaser", "teaser trailer", 
        "first look", "new trailer", "trailer #", "main trailer"
    );
    
    // Keywords to exclude (non-official content)
    private static final Set<String> EXCLUDE_KEYWORDS = Set.of(
        "fan made", "concept", "parody", "homemade", "fan edit", 
        "recut", "mashup", "fake", "faux", "reaction", "review", 
        "breakdown", "explained", "spoof", "short film"
    );
    
    public List<TrailerDto> getLatestOfficialTrailers(int maxResults) {
        try {
            List<TrailerDto> allTrailers = new ArrayList<>();
            
            // Get trailers from TMDB first (most reliable for official trailers)
            List<TrailerDto> tmdbTrailers = getTrailersFromTMDB(maxResults / 2);
            allTrailers.addAll(tmdbTrailers);
            
            // Supplement with YouTube API for latest releases
            List<TrailerDto> youtubeTrailers = getTrailersFromYouTube(maxResults / 2);
            allTrailers.addAll(youtubeTrailers);
            
            // Remove duplicates and sort by date
            return allTrailers.stream()
                    .collect(Collectors.toMap(
                        trailer -> trailer.getVideoId(),
                        trailer -> trailer,
                        (existing, replacement) -> existing
                    ))
                    .values()
                    .stream()
                    .sorted((t1, t2) -> t2.getPublishedAt().compareTo(t1.getPublishedAt()))
                    .limit(maxResults)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error fetching trailers: ", e);
            return Collections.emptyList();
        }
    }
    
    private List<TrailerDto> getTrailersFromTMDB(int maxResults) {
        try {
            String url = String.format(
                "https://api.themoviedb.org/3/movie/popular?api_key=%s&language=en-US&page=1",
                tmdbApiKey
            );
            
            // This is a simplified implementation
            // In real implementation, you would parse the TMDB response
            // and extract YouTube trailer URLs from movie details
            
            return Collections.emptyList(); // Placeholder
            
        } catch (Exception e) {
            log.error("Error fetching TMDB trailers: ", e);
            return Collections.emptyList();
        }
    }
    
    private List<TrailerDto> getTrailersFromYouTube1(int maxResults) {
        try {
            List<TrailerDto> trailers = new ArrayList<>();
            
            // Search for trailers from official channels
            for (String channelId : OFFICIAL_STUDIO_CHANNELS) {
                String url = String.format(
                    "https://www.googleapis.com/youtube/v3/search?" +
                    "part=snippet&channelId=%s&maxResults=5&order=date&type=video" +
                    "&q=trailer&key=%s",
                    channelId, youtubeApiKey
                );
                
                // This is a simplified implementation
                // In real implementation, you would make HTTP requests
                // and parse JSON responses
                
                log.info("Fetching from channel: {}", channelId);
            }
            
            return trailers;
            
        } catch (Exception e) {
            log.error("Error fetching YouTube trailers: ", e);
            return Collections.emptyList();
        }
    }
    public List<TrailerDto> getTrailersFromYouTube(int maxResults) {
        List<TrailerDto> trailers = new ArrayList<>();
        try {
            for (String channelId : OFFICIAL_STUDIO_CHANNELS) {
                String url = String.format(
                        "https://www.googleapis.com/youtube/v3/search?" +
                                "part=snippet&channelId=%s&maxResults=%d&order=date&type=video" +
                                "&q=trailer&key=%s",
                        channelId, maxResults, youtubeApiKey
                );

                log.info("Fetching from channel: {}", channelId);

                String response = restTemplate.getForObject(url, String.class);
                JsonNode root = objectMapper.readTree(response);
                JsonNode items = root.get("items");
                if (items != null && items.isArray()) {
                    for (JsonNode item : items) {
                        JsonNode snippet = item.get("snippet");
                        String videoId = item.path("id").path("videoId").asText();
                        if (videoId == null || videoId.isEmpty()) continue;

                        TrailerDto dto = new TrailerDto();
                        dto.setVideoId(videoId);
                        dto.setTitle(snippet.path("title").asText());
                        dto.setDescription(snippet.path("description").asText());
                        dto.setChannelTitle(snippet.path("channelTitle").asText());
                        dto.setPublishedAt(snippet.path("publishedAt").asText());
                        dto.setThumbnailUrl(snippet.path("thumbnails").path("high").path("url").asText());
                        trailers.add(dto);
                    }
                }
            }

            // Sort by published date, most recent first, and limit
            return trailers.stream()
                    .sorted(Comparator.comparing(TrailerDto::getPublishedAt).reversed())
                    .limit(maxResults)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching YouTube trailers: ", e);
            return Collections.emptyList();
        }
    }


    private boolean isOfficialTrailer(String title, String description, String channelId) {
        String titleLower = title.toLowerCase();
        String descriptionLower = description.toLowerCase();
        
        // Must be from official channel
        if (!OFFICIAL_STUDIO_CHANNELS.contains(channelId)) {
            return false;
        }
        
        // Must contain official trailer keywords
        boolean hasIncludeKeywords = INCLUDE_KEYWORDS.stream()
                .anyMatch(keyword -> titleLower.contains(keyword) || descriptionLower.contains(keyword));
        
        // Must not contain excluded keywords
        boolean hasExcludeKeywords = EXCLUDE_KEYWORDS.stream()
                .anyMatch(keyword -> titleLower.contains(keyword) || descriptionLower.contains(keyword));
        
        return hasIncludeKeywords && !hasExcludeKeywords;
    }
    
    public List<TrailerDto> searchTrailersByMovieName(String movieName) {
        try {
            List<TrailerDto> results = new ArrayList<>();
            
            // Search in official channels only
            for (String channelId : OFFICIAL_STUDIO_CHANNELS) {
                String searchQuery = movieName + " trailer";
                String url = String.format(
                    "https://www.googleapis.com/youtube/v3/search?" +
                    "part=snippet&channelId=%s&maxResults=3&order=relevance&type=video" +
                    "&q=%s&key=%s",
                    channelId, searchQuery, youtubeApiKey
                );
                
                // Implementation would parse YouTube API response here
                log.info("Searching for '{}' in channel: {}", movieName, channelId);
            }
            
            return results;
            
        } catch (Exception e) {
            log.error("Error searching trailers for movie: {}", movieName, e);
            return Collections.emptyList();
        }
    }
}