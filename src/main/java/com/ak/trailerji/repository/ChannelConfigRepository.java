package com.ak.trailerji.repository;

import com.ak.trailerji.entity.ChannelConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChannelConfigRepository extends JpaRepository<ChannelConfig, Long> {
    Optional<ChannelConfig> findByChannelId(String channelId);
}
