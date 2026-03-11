package com.esig.feed.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Table(name = "posts")
@Entity(name = "Post")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String description; // Trocado de 'content' para 'description'

    private String imageUrl; // Vai armazenar o nome/caminho do arquivo salvo

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}