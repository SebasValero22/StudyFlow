package com.iescamp.studyflow_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Response to POST /api/sync/push.
 * idMap keys: "task:-3" / "lifeArea:-1" / "focusSession:-7" → server-generated id.
 * Desktop rewrites local negative primary keys using this map.
 */
@Data
@NoArgsConstructor
public class SyncPushResponseDTO {

    private Map<String, Integer> idMap = new HashMap<>();

    /**
     * Rows the server refused to write (validation failure), keyed like idMap
     * ("task:-3" → reason). The desktop keeps these PENDING_SYNC instead of
     * marking them synced, so no data is silently lost.
     */
    private Map<String, String> rejected = new HashMap<>();

    private LocalDateTime serverTime;

    /** Wire protocol version the server speaks. */
    private Integer protocolVersion;
}
