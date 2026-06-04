package com.ak.trailerji.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "channel_configs")
public class ChannelConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String channelName;

    @Column(nullable = false, length = 255, unique = true)
    private String channelId;

    @Column(name = "uploads_playlist_id", length = 255)
    private String uploadsPlaylistId;

    public ChannelConfig() {}

    public ChannelConfig(String channelName, String channelId, String uploadsPlaylistId) {
        this.channelName = channelName;
        this.channelId = channelId;
        this.uploadsPlaylistId = uploadsPlaylistId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }
    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }
    public String getUploadsPlaylistId() { return uploadsPlaylistId; }
    public void setUploadsPlaylistId(String uploadsPlaylistId) { this.uploadsPlaylistId = uploadsPlaylistId; }
}
