package com.esig.feed.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult.get("secure_url").toString();
    }

    // NOVO: Método para apagar a imagem velha
    public void deleteImageByUrl(String imageUrl) {
        if (imageUrl != null && imageUrl.contains("res.cloudinary.com")) {
            try {
                // Extrai o ID público da imagem a partir da URL
                String[] parts = imageUrl.split("/");
                String lastPart = parts[parts.length - 1];
                String publicId = lastPart.substring(0, lastPart.lastIndexOf('.'));

                // Manda o Cloudinary destruir o arquivo
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            } catch (Exception e) {
                System.out.println("Erro ao deletar imagem do Cloudinary: " + e.getMessage());
            }
        }
    }
}