package com.example.groceryserver.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.groceryserver.dto.ProductRequest;
import com.example.groceryserver.exception.ProductUploadException;
import com.example.groceryserver.model.Product;
import com.example.groceryserver.repository.ProductRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService {

	private static final long MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024;
	private static final String CLOUDINARY_FOLDER = "grocery/products";

	private final Cloudinary cloudinary;
	private final ProductRepository productRepository;

	public ProductService(Cloudinary cloudinary, ProductRepository productRepository) {
		this.cloudinary = cloudinary;
		this.productRepository = productRepository;
	}

	public void addProduct(ProductRequest request, List<MultipartFile> images) {
		if (!isCloudinaryConfigured()) {
			throw new ProductUploadException(
					"Cloudinary is not configured", HttpStatus.SERVICE_UNAVAILABLE);
		}
		if (images == null || images.isEmpty()) {
			throw new IllegalArgumentException("At least one product image is required");
		}
		images.forEach(this::validateImage);

		List<UploadedImage> uploadedImages = new ArrayList<>();
		try {
			for (MultipartFile image : images) {
				Map<?, ?> result = cloudinary.uploader().upload(
						image.getBytes(), ObjectUtils.asMap("folder", CLOUDINARY_FOLDER));
				Object secureUrl = result.get("secure_url");
				Object publicId = result.get("public_id");
				if (!(secureUrl instanceof String url) || !(publicId instanceof String id)) {
					throw new IOException("Cloudinary returned incomplete image details");
				}
				uploadedImages.add(new UploadedImage(url, id));
			}
		} catch (IOException | RuntimeException exception) {
			deleteUploadedImages(uploadedImages);
			throw new ProductUploadException(
					"Product images could not be uploaded", HttpStatus.BAD_GATEWAY, exception);
		}

		Product product = new Product();
		product.setName(request.getName().trim());
		product.setDescription(request.getDescription().trim());
		product.setPrice(request.getPrice());
		product.setOfferPrice(request.getOfferPrice());
		product.setCategory(request.getCategory().trim());
		product.setInStock(request.getInStock());
		product.setImages(uploadedImages.stream().map(UploadedImage::url).toList());
		try {
			productRepository.save(product);
		} catch (RuntimeException exception) {
			deleteUploadedImages(uploadedImages);
			throw new ProductUploadException(
					"Product could not be saved", HttpStatus.INTERNAL_SERVER_ERROR, exception);
		}
	}

	public List<Product> getProducts() {
		return productRepository.findAll();
	}

	public Optional<Product> getProductById(String id) {
		return productRepository.findById(id);
	}

	private void validateImage(MultipartFile image) {
		if (image.isEmpty()) {
			throw new IllegalArgumentException("Product images must not be empty");
		}
		if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
			throw new IllegalArgumentException("Each product image must be 10 MB or smaller");
		}
		String contentType = image.getContentType();
		if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
			throw new IllegalArgumentException("Only image files are allowed");
		}
	}

	private boolean isCloudinaryConfigured() {
		Map<?, ?> configuration = cloudinary.config.asMap();
		return hasValue(configuration.get("cloud_name"))
				&& hasValue(configuration.get("api_key"))
				&& hasValue(configuration.get("api_secret"));
	}

	private boolean hasValue(Object value) {
		return value instanceof String text && !text.isBlank();
	}

	private void deleteUploadedImages(List<UploadedImage> images) {
		for (UploadedImage image : images) {
			try {
				cloudinary.uploader().destroy(image.publicId(), ObjectUtils.emptyMap());
			} catch (IOException | RuntimeException exception) {
				log.warn("Could not remove a product image after a failed product upload", exception);
			}
		}
	}

	private record UploadedImage(String url, String publicId) {
	}
}
