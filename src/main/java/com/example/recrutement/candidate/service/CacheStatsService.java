package com.example.recrutement.candidate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CacheStatsService {

    private long cacheHits = 0;
    private long cacheMisses = 0;
    private long apiCalls = 0;

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        long total = cacheHits + cacheMisses;
        
        stats.put("hitCount", cacheHits);
        stats.put("missCount", cacheMisses);
        stats.put("hitRate", total > 0 ? String.format("%.2f%%", (cacheHits * 100.0 / total)) : "0%");
        stats.put("apiCalls", apiCalls);
        stats.put("estimatedSize", 0);
        stats.put("savedApiCalls", cacheHits);
        stats.put("savedPercentage", total > 0 ? String.format("%.2f%%", (cacheHits * 100.0 / total)) : "0%");
        
        return stats;
    }

    public void clearCache() {
        cacheHits = 0;
        cacheMisses = 0;
        apiCalls = 0;
        log.info("Cache vidé");
    }

    public void recordHit() {
        cacheHits++;
    }

    public void recordMiss() {
        cacheMisses++;
    }

    public void recordApiCall() {
        apiCalls++;
    }
}