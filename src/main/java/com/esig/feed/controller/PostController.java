package com.esig.feed.controller;

import com.esig.feed.model.Post;
import com.esig.feed.repository.PostRepository;
import com.esig.feed.service.CloudinaryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepository;
    private final CloudinaryService cloudinaryService;

    public PostController(PostRepository postRepository, CloudinaryService cloudinaryService) {
        this.postRepository = postRepository;
        this.cloudinaryService = cloudinaryService;
    }

    // 1. GET: Retorna os posts paginados (Mais recentes primeiro)
    @GetMapping
    public ResponseEntity<Page<Post>> getPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Post> posts = postRepository.findAll(pageable);
        return ResponseEntity.ok(posts);
    }

    // 2. POST: Cria um novo post
    @PostMapping
    public ResponseEntity<Post> createPost(
            @RequestParam("username") String username,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            String imageUrl = null;
            if (file != null && !file.isEmpty()) {
                imageUrl = cloudinaryService.uploadImage(file);
            }

            Post post = new Post();
            post.setUsername(username);
            post.setDescription(description);
            post.setImageUrl(imageUrl);
            post.setCreatedAt(LocalDateTime.now()); // Ajuste conforme a sua entidade

            Post savedPost = postRepository.save(post);
            return ResponseEntity.ok(savedPost);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 3. PUT: Atualiza a descrição e/ou a imagem
    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(
            @PathVariable Long id,
            @RequestParam("username") String username, // Opcional checar se o dono é o mesmo
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        Optional<Post> postOptional = postRepository.findById(id);

        if (postOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Post post = postOptional.get();

        try {
            // Se o usuário mandou uma foto nova, temos que apagar a velha e subir a nova!
            if (file != null && !file.isEmpty()) {
                // Apaga a antiga do Cloudinary para não gerar lixo na nuvem
                if (post.getImageUrl() != null) {
                    cloudinaryService.deleteImageByUrl(post.getImageUrl());
                }
                // Sobe a nova e atualiza a URL
                String newImageUrl = cloudinaryService.uploadImage(file);
                post.setImageUrl(newImageUrl);
            }

            post.setDescription(description);
            // Salva no banco
            Post updatedPost = postRepository.save(post);

            return ResponseEntity.ok(updatedPost);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 4. DELETE: Apaga o post e a imagem da nuvem
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        Optional<Post> postOptional = postRepository.findById(id);

        if (postOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Post post = postOptional.get();

        // Limpa a imagem lá do Cloudinary primeiro
        if (post.getImageUrl() != null) {
            cloudinaryService.deleteImageByUrl(post.getImageUrl());
        }

        // Apaga do banco de dados (Neon)
        postRepository.delete(post);

        return ResponseEntity.noContent().build();
    }
}