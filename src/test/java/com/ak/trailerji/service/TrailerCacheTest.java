package com.ak.trailerji.service;

import com.ak.trailerji.dto.TrailerDto;
import com.ak.trailerji.repository.CachedTrailerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class TrailerCacheTest {

    @Autowired
    private TrailerService trailerService;

    @Autowired
    private CachedTrailerRepository cachedTrailerRepository;

    @MockBean
    private RestTemplate restTemplate;

    private String youtubeStub;
    private String youtubeStubSecondCall;

    @BeforeEach
    void setUp() {
        cachedTrailerRepository.deleteAll();
        String marvelId = "UCvC4D8onUfXzvjTOM-dBfEA";
        String warnerId = "UC_IRYSp4auq7hKLvziWVH6w";
        String universalId = "UCOwaTlA0nHlqMf4HBEWmLaw";
        String sonyId = "UCiifkYAs_bq1pt_zbNAzYGg";

        youtubeStub = """
            {
              "items": [
                {
                  "snippet": {
                    "channelId": "%s",
                    "title": "Official Trailer One",
                    "description": "Description of official trailer one",
                    "channelTitle": "Marvel Entertainment",
                    "publishedAt": "2025-12-01T00:00:00Z",
                    "thumbnails": {
                      "high": { "url": "https://example.com/thumb1.jpg" }
                    },
                    "resourceId": {
                      "kind": "youtube#video",
                      "videoId": "vid001"
                    }
                  }
                },
                {
                  "snippet": {
                    "channelId": "%s",
                    "title": "Official Trailer Two",
                    "description": "Description of official trailer two",
                    "channelTitle": "Warner Bros. Pictures",
                    "publishedAt": "2025-11-15T00:00:00Z",
                    "thumbnails": {
                      "high": { "url": "https://example.com/thumb2.jpg" }
                    },
                    "resourceId": {
                      "kind": "youtube#video",
                      "videoId": "vid002"
                    }
                  }
                },
                {
                  "snippet": {
                    "channelId": "%s",
                    "title": "Official Trailer Three",
                    "description": "Description of official trailer three",
                    "channelTitle": "Universal Pictures",
                    "publishedAt": "2025-10-20T00:00:00Z",
                    "thumbnails": {
                      "high": { "url": "https://example.com/thumb3.jpg" }
                    },
                    "resourceId": {
                      "kind": "youtube#video",
                      "videoId": "vid003"
                    }
                  }
                }
              ]
            }
            """.formatted(marvelId, warnerId, universalId);

        youtubeStubSecondCall = """
            {
              "items": [
                {
                  "snippet": {
                    "channelId": "%s",
                    "title": "Official Trailer One UPDATED",
                    "description": "Updated description",
                    "channelTitle": "Marvel Entertainment",
                    "publishedAt": "2025-12-01T00:00:00Z",
                    "thumbnails": {
                      "high": { "url": "https://example.com/thumb1_updated.jpg" }
                    },
                    "resourceId": {
                      "kind": "youtube#video",
                      "videoId": "vid001"
                    }
                  }
                },
                {
                  "snippet": {
                    "channelId": "%s",
                    "title": "Official Trailer Four",
                    "description": "Brand new official trailer",
                    "channelTitle": "Sony Pictures",
                    "publishedAt": "2025-09-01T00:00:00Z",
                    "thumbnails": {
                      "high": { "url": "https://example.com/thumb4.jpg" }
                    },
                    "resourceId": {
                      "kind": "youtube#video",
                      "videoId": "vid004"
                    }
                  }
                }
              ]
            }
            """.formatted(marvelId, sonyId);
    }

    @Test
    void fetchAndStoreTrailers_shouldPersistTrailersFromApi() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(youtubeStub);

        trailerService.fetchAndStoreTrailers(100);

        List<TrailerDto> cached = trailerService.getCachedTrailers(10);
        assertThat(cached).hasSize(3);
        assertThat(cached).extracting(TrailerDto::getVideoId)
                .containsExactlyInAnyOrder("vid001", "vid002", "vid003");
    }

    @Test
    void fetchAndStoreTrailers_shouldMapAllFieldsCorrectly() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(youtubeStub);

        trailerService.fetchAndStoreTrailers(100);

        List<TrailerDto> cached = trailerService.getCachedTrailers(10);
        TrailerDto first = cached.stream()
                .filter(t -> t.getVideoId().equals("vid001"))
                .findFirst().orElseThrow();

        assertThat(first.getTitle()).isEqualTo("Official Trailer One");
        assertThat(first.getDescription()).isEqualTo("Description of official trailer one");
        assertThat(first.getChannelTitle()).isEqualTo("Marvel Entertainment");
        assertThat(first.getPublishedAt()).startsWith("2025-12-01");
        assertThat(first.getThumbnailUrl()).isEqualTo("https://example.com/thumb1.jpg");
    }

    @Test
    void fetchAndStoreTrailers_shouldOrderByPublishedAtDesc() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(youtubeStub);

        trailerService.fetchAndStoreTrailers(100);

        List<TrailerDto> cached = trailerService.getCachedTrailers(10);
        assertThat(cached).hasSize(3);
        assertThat(cached.get(0).getVideoId()).isEqualTo("vid001");
        assertThat(cached.get(1).getVideoId()).isEqualTo("vid002");
        assertThat(cached.get(2).getVideoId()).isEqualTo("vid003");
    }

    @Test
    void fetchAndStoreTrailers_shouldRespectLimit() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(youtubeStub);

        trailerService.fetchAndStoreTrailers(100);

        List<TrailerDto> limited = trailerService.getCachedTrailers(2);
        assertThat(limited).hasSize(2);
        assertThat(limited.get(0).getVideoId()).isEqualTo("vid001");
        assertThat(limited.get(1).getVideoId()).isEqualTo("vid002");
    }

    @Test
    void fetchAndStoreTrailers_upsertShouldNotCreateDuplicates() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(youtubeStub);

        trailerService.fetchAndStoreTrailers(100);
        assertThat(cachedTrailerRepository.count()).isEqualTo(3);

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(youtubeStubSecondCall);

        trailerService.fetchAndStoreTrailers(100);
        assertThat(cachedTrailerRepository.count()).isEqualTo(4);

        List<TrailerDto> cached = trailerService.getCachedTrailers(10);
        assertThat(cached).hasSize(4);
        TrailerDto updated = cached.stream()
                .filter(t -> t.getVideoId().equals("vid001"))
                .findFirst().orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Official Trailer One UPDATED");
        assertThat(updated.getThumbnailUrl()).isEqualTo("https://example.com/thumb1_updated.jpg");
    }

    @Test
    void getCachedTrailers_shouldReturnEmptyWhenNoCache() {
        List<TrailerDto> result = trailerService.getCachedTrailers(10);
        assertThat(result).isEmpty();
    }

    @Test
    void fetchAndStoreTrailers_shouldHandleApiErrorGracefully() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("API unavailable"));

        trailerService.fetchAndStoreTrailers(100);

        List<TrailerDto> cached = trailerService.getCachedTrailers(10);
        assertThat(cached).isEmpty();
    }

    @Test
    void directRepositoryFindByVideoId_shouldFindAfterUpsert() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(youtubeStub);

        trailerService.fetchAndStoreTrailers(100);

        assertThat(cachedTrailerRepository.findByVideoId("vid001")).isPresent();
        assertThat(cachedTrailerRepository.findByVideoId("vid002")).isPresent();
        assertThat(cachedTrailerRepository.findByVideoId("nonexistent")).isNotPresent();
    }
}
