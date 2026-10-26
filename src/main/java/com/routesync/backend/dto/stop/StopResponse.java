package com.routesync.backend.dto.stop;

import com.routesync.backend.entity.enums.StopStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record StopResponse(

        UUID id,

        UUID routeId,

        String stopName,

        String address,

        Double latitude,

        Double longitude,

        Integer stopOrder,

        Integer estimatedArrivalOffsetMinutes,

        StopStatus status,

        Boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}