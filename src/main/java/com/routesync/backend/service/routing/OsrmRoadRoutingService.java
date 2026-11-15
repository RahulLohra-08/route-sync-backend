package com.routesync.backend.service.routing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Service
public class OsrmRoadRoutingService implements RoadRoutingService {

    private static final Logger log =
            LoggerFactory.getLogger(OsrmRoadRoutingService.class);

    private final RestClient restClient;

    // JSON response parse karne ke liye local ObjectMapper.
    // Iske liye Spring bean ki dependency nahi rahegi.
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OsrmRoadRoutingService(
            @Value("${routesync.routing.base-url:https://router.project-osrm.org}")
            String baseUrl,

            @Value("${routesync.routing.connect-timeout-seconds:3}")
            int connectTimeoutSeconds,

            @Value("${routesync.routing.read-timeout-seconds:5}")
            int readTimeoutSeconds
    ) {
        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(connectTimeoutSeconds * 1000);
        factory.setReadTimeout(readTimeoutSeconds * 1000);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    @Override
    public Optional<Double> calculateRoadDistanceKm(
            List<RouteCoordinate> coordinates
    ) {
        if (coordinates == null || coordinates.size() < 2) {
            return Optional.empty();
        }

        String joinedCoordinates = coordinates.stream()
                .map(point ->
                        point.longitude() + "," + point.latitude())
                .reduce((first, second) -> first + ";" + second)
                .orElse("");

        try {
            String response = restClient.get()
                    .uri("/route/v1/driving/{coordinates}"
                                    + "?overview=false&steps=false",
                            joinedCoordinates)
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank()) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response);

            if (!"Ok".equals(root.path("code").asText())) {
                log.warn("OSRM route calculation failed: {}",
                        root.path("code").asText());
                return Optional.empty();
            }

            JsonNode routes = root.path("routes");

            if (!routes.isArray() || routes.isEmpty()) {
                return Optional.empty();
            }

            double distanceMetres = routes.get(0)
                    .path("distance")
                    .asDouble(-1);

            if (!Double.isFinite(distanceMetres)
                    || distanceMetres <= 0) {
                return Optional.empty();
            }

            return Optional.of(distanceMetres / 1000.0);

        } catch (Exception exception) {
            log.warn("Road routing request failed: {}",
                    exception.getMessage());

            return Optional.empty();
        }
    }
}