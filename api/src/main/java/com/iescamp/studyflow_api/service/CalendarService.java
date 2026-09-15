package com.iescamp.studyflow_api.service;

import com.iescamp.studyflow_api.model.BusyBlock;
import com.iescamp.studyflow_api.model.CalendarFeed;
import com.iescamp.studyflow_api.repository.BusyBlockRepository;
import com.iescamp.studyflow_api.repository.CalendarFeedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalendarService {

    private static final int MAX_FEED_BYTES = 2 * 1024 * 1024; // 2 MB cap

    private final CalendarFeedRepository feedRepository;
    private final BusyBlockRepository blockRepository;
    private final IcsParser parser = new IcsParser();
    private final com.iescamp.studyflow_api.security.FeedUrlGuard urlGuard;
    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Autowired
    public CalendarService(CalendarFeedRepository feedRepository, BusyBlockRepository blockRepository,
                           com.iescamp.studyflow_api.security.FeedUrlGuard urlGuard) {
        this.feedRepository = feedRepository;
        this.blockRepository = blockRepository;
        this.urlGuard = urlGuard;
    }

    public CalendarFeed addFeed(Integer userId, String name, String url) {
        urlGuard.check(url);
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Feed name is required");
        }
        if (url.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL too long");
        }

        CalendarFeed feed = new CalendarFeed();
        feed.setUserId(userId);
        feed.setName(name.trim());
        feed.setUrl(url.trim());
        return feedRepository.save(feed);
    }

    public List<CalendarFeed> listFeeds(Integer userId) {
        return feedRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteFeed(Integer userId, Integer feedId) {
        feedRepository.delete(findOwned(userId, feedId));
    }

    /** Fetch the ICS over HTTP and atomically replace this feed's busy blocks. */
    @Transactional
    public int syncFeed(Integer userId, Integer feedId) {
        CalendarFeed feed = findOwned(userId, feedId);
        urlGuard.check(feed.getUrl());

        String body;
        try {
            HttpResponse<byte[]> response = http.send(
                    HttpRequest.newBuilder(URI.create(feed.getUrl())).GET().build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Calendar returned HTTP " + response.statusCode());
            }
            if (response.body().length > MAX_FEED_BYTES) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Calendar feed too large");
            }
            body = new String(response.body(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Sync interrupted");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not fetch calendar: " + e.getMessage());
        }

        blockRepository.deleteByFeedId(feedId);
        int count = 0;
        for (IcsParser.Event event : parser.parse(body)) {
            BusyBlock block = new BusyBlock();
            block.setFeedId(feedId);
            block.setUid(event.uid());
            block.setStartAt(event.start());
            block.setEndAt(event.end());
            block.setSummary(event.summary() != null && event.summary().length() > 255
                    ? event.summary().substring(0, 255) : event.summary());
            blockRepository.save(block);
            count++;
        }
        feed.setLastSyncedAt(LocalDateTime.now());
        feedRepository.save(feed);
        return count;
    }

    /** Merged busy intervals across all of a user's feeds, overlapping [from, to). */
    public List<BusyBlock> busyBetween(Integer userId, LocalDateTime from, LocalDateTime to) {
        List<Integer> feedIds = feedRepository.findByUserId(userId).stream()
                .filter(f -> Boolean.TRUE.equals(f.getEnabled()))
                .map(CalendarFeed::getFeedId)
                .toList();
        if (feedIds.isEmpty()) return List.of();
        return blockRepository.findByFeedIdInAndStartAtLessThanAndEndAtGreaterThanEqual(feedIds, to, from);
    }

    /** Busy hours per day for the next N days - fed into Nyra's scheduling prompt. */
    public Map<LocalDate, Double> busyHoursPerDay(Integer userId, LocalDate from, int days) {
        Map<LocalDate, Double> result = new HashMap<>();
        List<BusyBlock> blocks = busyBetween(userId, from.atStartOfDay(), from.plusDays(days).atStartOfDay());
        for (BusyBlock b : blocks) {
            LocalDate cursor = b.getStartAt().toLocalDate();
            while (!cursor.isAfter(b.getEndAt().minusNanos(1).toLocalDate())) {
                LocalDateTime dayStart = cursor.atStartOfDay();
                LocalDateTime dayEnd = dayStart.plusDays(1);
                LocalDateTime overlapStart = b.getStartAt().isAfter(dayStart) ? b.getStartAt() : dayStart;
                LocalDateTime overlapEnd = b.getEndAt().isBefore(dayEnd) ? b.getEndAt() : dayEnd;
                if (overlapEnd.isAfter(overlapStart)) {
                    double hours = Duration.between(overlapStart, overlapEnd).toMinutes() / 60.0;
                    result.merge(cursor, hours, Double::sum);
                }
                cursor = cursor.plusDays(1);
            }
        }
        return result;
    }

    private CalendarFeed findOwned(Integer userId, Integer feedId) {
        return feedRepository.findById(feedId)
                .filter(f -> f.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feed not found"));
    }
}
