package com.ecommerce_books.book_service.exceptions;

import java.time.LocalDateTime;

public record ErrorDetails (LocalDateTime timestamp, String message, String details) {
}
