package com.booksecommerce.inventory.exceptions;

import java.time.LocalDateTime;

public record ErrorDetails(LocalDateTime timestamp, String message, String details) {
}
