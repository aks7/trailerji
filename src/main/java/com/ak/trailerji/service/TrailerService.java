package com.ak.trailerji.service;

import com.ak.trailerji.dto.TrailerDto;
import com.ak.trailerji.entity.CachedTrailer;
import com.ak.trailerji.repository.CachedTrailerRepository;
import com.ak.trailerji.stat.SyncStatistics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Value("${cache.refresh.interval.ms:3600000}")
    private long cacheRefreshInterval;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CachedTrailerRepository cachedTrailerRepository;

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
            "UCq-Fj5jknLsUf-MWSy4_brA", // T-Series (Verified)
            "UCbTLwN10NoCU4WDzLf1JMOA", // YRF - Yash Raj Films (Verified)
            "UC3jMepkLKF8y4iiwWmAB3RA", // Zee Studios (Verified)
            "UC56gTxNs4f9xZ7Pa2i5xNzg"  // Sony
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
        return getLatestOfficialTrailers(maxResults, new SyncStatistics());
    }

    public List<TrailerDto> getLatestOfficialTrailers(int maxResults, SyncStatistics stats) {
        try {
            List<TrailerDto> allTrailers = new ArrayList<>();

            List<TrailerDto> tmdbTrailers = getTrailersFromTMDB(maxResults / 2);
            allTrailers.addAll(tmdbTrailers);

            List<TrailerDto> youtubeTrailers = getTrailersFromYouTube(maxResults / 2, stats);
            allTrailers.addAll(youtubeTrailers);

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

    public List<TrailerDto> getTrailersFromYouTube(int maxResults, SyncStatistics stats) {
        List<TrailerDto> trailers = new ArrayList<>();
        int perChannel = Math.max(1, maxResults / OFFICIAL_STUDIO_CHANNELS.size());

        for (String channelId : OFFICIAL_STUDIO_CHANNELS) {
            stats.channelsChecked++; // Track channels checked
            try {
                String uploadsPlaylistId = channelId.replaceFirst("^UC", "UU");
                String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/playlistItems")
                        .queryParam("part", "snippet,contentDetails")
                        .queryParam("playlistId", uploadsPlaylistId)
                        .queryParam("maxResults", perChannel)
                        .queryParam("key", youtubeApiKey)
                        .toUriString();

                String response = restTemplate.getForObject(url, String.class);
                JsonNode root = objectMapper.readTree(response);
                JsonNode items = root.get("items");

                if (items != null && items.isArray()) {
                    for (JsonNode item : items) {
                        stats.rawVideosFound++; // Track every video we see

                        JsonNode snippet = item.get("snippet");
                        String videoId = snippet.path("resourceId").path("videoId").asText();
                        if (videoId.isEmpty()) continue;

                        TrailerDto dto = new TrailerDto();
                        dto.setVideoId(videoId);
                        dto.setChannelId(snippet.path("channelId").asText());
                        dto.setTitle(snippet.path("title").asText());
                        dto.setDescription(snippet.path("description").asText());
                        dto.setChannelTitle(snippet.path("channelTitle").asText());
                        dto.setPublishedAt(snippet.path("publishedAt").asText());
                        dto.setThumbnailUrl(snippet.path("thumbnails").path("high").path("url").asText());

                        // Check if it passes our filter
                        if (isOfficialTrailer(dto.getTitle(), dto.getDescription(), dto.getChannelId())) {
                            trailers.add(dto);
                            stats.officialTrailersKept++; // Track the ones that pass the filter
                        }
                    }
                }
            } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
                stats.recordChannelError(channelId, "Uploads playlist not found (404)");
                log.warn("Uploads playlist not found for channel: {}. Skipping...", channelId);
            } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
                stats.recordChannelError(channelId, "API quota exceeded (429)");
                log.error("CRITICAL: YouTube API Quota Exceeded!");
                break;
            } catch (Exception e) {
                stats.recordChannelError(channelId, e.getClass().getSimpleName() + ": " + shortenError(e.getMessage()));
                log.error("Error fetching trailers for channel: {}. Skipping...", channelId, e);
            }
        }

        try {
            return trailers.stream()
                    .sorted(Comparator.comparing(TrailerDto::getPublishedAt).reversed())
                    .limit(maxResults)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return trailers;
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

    /**
     * Fetches trailers from external APIs and stores them in the cache
     *
     * @param maxResults Maximum number of trailers to fetch and store
     */
    @Transactional
    public void fetchAndStoreTrailers(int maxResults) {
        // 1. Create the tracker
        SyncStatistics stats = new SyncStatistics();

        // 2. Pass it down to the fetcher (NOTE: If you are using TMDB too,
        // you would pass it into getLatestOfficialTrailers instead)
        List<TrailerDto> trailers = getTrailersFromYouTube(maxResults, stats);

        // 3. Save to database and track new vs updated
        for (TrailerDto dto : trailers) {
            LocalDateTime publishedAt;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                publishedAt = LocalDateTime.parse(dto.getPublishedAt(), formatter);
            } catch (Exception e) {
                publishedAt = LocalDateTime.now();
            }

            if (cachedTrailerRepository.findByVideoId(dto.getVideoId()).isPresent()) {
                stats.existingTrailersUpdated++;
            } else {
                stats.newTrailersAddedToDb++;
            }

            cachedTrailerRepository.upsertTrailer(
                    dto.getVideoId(),
                    dto.getTitle(),
                    dto.getDescription(),
                    dto.getChannelTitle(),
                    publishedAt,
                    dto.getThumbnailUrl()
            );
        }

        // 4. Print the final report to the logs!
        stats.printReport();
    }

    /**
     * Gets trailers from the cache
     *
     * @param limit Maximum number of trailers to return
     * @return List of trailers (mapped to DTO)
     */
    public List<TrailerDto> getCachedTrailers(int limit) {
        Page<CachedTrailer> page = cachedTrailerRepository.findByOrderByPublishedAtDesc(PageRequest.of(0, limit));
        return page.stream().map(this::mapToDto).toList();
    }

    private TrailerDto mapToDto(CachedTrailer entity) {
        TrailerDto dto = new TrailerDto();
        dto.setVideoId(entity.getVideoId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setChannelTitle(entity.getChannelTitle());
        dto.setPublishedAt(entity.getPublishedAt().toString());
        dto.setThumbnailUrl(entity.getThumbnailUrl());
        return dto;
    }

    private static String shortenError(String message) {
        if (message == null) return "Unknown error";
        int newlineIdx = message.indexOf('\n');
        int braceIdx = message.indexOf("{\n");
        int cutoff = (braceIdx >= 0 && braceIdx < 200) ? braceIdx : Math.min(newlineIdx >= 0 ? newlineIdx : message.length(), 150);
        return message.substring(0, cutoff).trim();
    }

    /**
     * Scheduled method to refresh the cache periodically.
     */
    @Scheduled(fixedDelayString = "${cache.refresh.interval.ms:3600000}") // Default 1 hour
    @Transactional
    public void refreshCache() {
        log.info("Refreshing trailer cache");
        fetchAndStoreTrailers(100); // Fetch 100 trailers by default
    }
}