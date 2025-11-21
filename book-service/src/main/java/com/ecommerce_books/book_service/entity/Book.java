package com.ecommerce_books.book_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 255)
    private String name;

    @NotBlank
    @Size(min = 2, max = 5000)
    private String description;

    @NotBlank
    private String bookCoverImage;

    @NotBlank
    @Column(unique = true)
    private String uniqueProductCode;

    @Positive
    private Long categoryId;

}
