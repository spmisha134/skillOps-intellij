package com.spmisha134.skillops.insights.parser

import java.time.Instant
import java.time.OffsetDateTime

object InsightTimestamp {
    fun parseMillis(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        return runCatching { Instant.parse(value).toEpochMilli() }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(value).toInstant().toEpochMilli() }.getOrNull()
    }
}
