package com.htttql.crmmodule.crmrequest.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory sliding-window rate limiter per key (e.g., IP).
 */
public class RateLimiter {

    private final Map<String, Deque<Instant>> keyToHits = new ConcurrentHashMap<>();

    private final int maxRequests;
    private final Duration window;

    public RateLimiter(int maxRequests, Duration window) {
        this.maxRequests = maxRequests;
        this.window = window;
    }

    public boolean allow(String key) {
        Instant now = Instant.now();
        Instant threshold = now.minus(window);

        Deque<Instant> hits = keyToHits.computeIfAbsent(key, k -> new ArrayDeque<>());

        while (!hits.isEmpty() && hits.peekFirst().isBefore(threshold)) {
            hits.pollFirst();
        }

        if (hits.size() >= maxRequests) {
            return false;
        }

        hits.addLast(now);
        return true;
    }
}
