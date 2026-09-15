package com.iescamp.studyflow_api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetAddress;
import java.net.URI;

/**
 * Blocks calendar feeds that point at private/loopback networks (SSRF).
 * `studyflow.calendar.allow-private=true` exists ONLY for integration tests
 * that serve fixtures from an embedded local server - never enable in prod.
 */
@Component
public class FeedUrlGuard {

    private final boolean allowPrivate;

    public FeedUrlGuard(@Value("${studyflow.calendar.allow-private:false}") boolean allowPrivate) {
        this.allowPrivate = allowPrivate;
    }

    public void check(String url) {
        URI uri;
        try {
            uri = URI.create(url == null ? "" : url.trim());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid URL");
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme();
        if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only http/https feeds are allowed");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL has no host");
        }
        if (allowPrivate) return; // test escape hatch

        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isLoopbackAddress() || address.isSiteLocalAddress()
                        || address.isAnyLocalAddress() || address.isLinkLocalAddress()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Private network feeds are not allowed");
                }
            }
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot resolve feed host");
        }
    }
}
