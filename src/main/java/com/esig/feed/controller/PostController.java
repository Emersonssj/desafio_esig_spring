package com.esig.feed.controller;

import com.esig.feed.model.Post;
import com.esig.feed.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    // Pasta onde as imagens enviadas pelo celular serão salvas
    private final String UPLOAD_DIR = "uploads/";

    @GetMapping
    public ResponseEntity<Page<Post>> getAllPosts(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        // O findAll agora recebe o pageable e retorna uma Page em vez de uma List
        Page<Post> posts = postRepository.findAll(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        return postRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Agora recebe multipart/form-data
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Post> createPost(
            @RequestParam("username") String username,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        Post post = new Post();
        post.setUsername(username);
        post.setDescription(description);

        if (file != null && !file.isEmpty()) {
            String fileName = saveImage(file);
            // Gera a URL completa para o Flutter acessar depois: http://localhost:8080/uploads/nome-da-foto.jpg
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();
            post.setImageUrl(fileDownloadUri);
        }

        Post savedPost = postRepository.save(post);
        return ResponseEntity.ok(savedPost);
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Post> updatePost(
            @PathVariable Long id,
            @RequestParam("username") String username,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        return postRepository.findById(id)
                .map(post -> {
                    post.setUsername(username);
                    post.setDescription(description);

                    if (file != null && !file.isEmpty()) {
                        String fileName = saveImage(file);
                        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/uploads/")
                                .path(fileName)
                                .toUriString();
                        post.setImageUrl(fileDownloadUri);
                    }

                    Post updated = postRepository.save(post);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Método auxiliar para salvar o arquivo físico no computador/servidor
    private String saveImage(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            // Cria um nome único para não sobrescrever imagens com o mesmo nome
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar a imagem", e);
        }
    }
}