package com.reynan.notificationservice.exceptions.model;

import java.time.Instant;
import java.util.UUID;

public record StandardError(Instant timestamp, String message, String path, UUID traceId) {}
