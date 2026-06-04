package com.ak.trailerji.stat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class SyncStatistics {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncStatistics.class);

    public int channelsChecked = 0;
    public int rawVideosFound = 0;
    public int officialTrailersKept = 0;
    public int newTrailersAddedToDb = 0;
    public int existingTrailersUpdated = 0;

    public final Map<String, String> channelErrors = new LinkedHashMap<>();

    public void recordChannelError(String channelId, String reason) {
        channelErrors.put(channelId, reason);
    }

    public void printReport() {
        LOGGER.info("=========================================");
        LOGGER.info("YOUTUBE TRAILER SYNC REPORT");
        LOGGER.info("=========================================");
        LOGGER.info("Channels Checked      : {}", channelsChecked);
        LOGGER.info("Channels with Errors  : {}", channelErrors.size());
        LOGGER.info("Raw Videos Found      : {}", rawVideosFound);
        LOGGER.info("Passed Filter (Kept)  : {}", officialTrailersKept);
        LOGGER.info("-----------------------------------------");
        LOGGER.info("Database Insertions   : {} (Brand New)", newTrailersAddedToDb);
        LOGGER.info("Database Updates      : {} (Refreshed)", existingTrailersUpdated);
        LOGGER.info("=========================================");

        if (!channelErrors.isEmpty()) {
            LOGGER.warn("--- Channel Errors ---");
            for (Map.Entry<String, String> entry : channelErrors.entrySet()) {
                LOGGER.warn("  {} => {}", entry.getKey(), entry.getValue());
            }
            LOGGER.warn("----------------------");
        }
    }
}