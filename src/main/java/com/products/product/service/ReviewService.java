package com.products.product.service;

import com.products.product.entity.Product;
import com.products.product.entity.Review;
import com.products.product.repository.ProductRepository;
import com.products.product.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DeliveryValidationService deliveryValidationService;

    @Transactional
    public Review createReview(Review review) {
        // Validar que el rating esté entre 1 y 5
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Validar longitud del comentario
        if (review.getComment() != null && review.getComment().length() > 500) {
            throw new IllegalArgumentException("Comment must not exceed 500 characters");
        }

        // Buscar el producto
        Product product = productRepository.findById(review.getProduct().getId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Asignar el producto a la reseña
        review.setProduct(product);

        // Validar que el usuario no haya dejado ya una reseña para este producto
        if (reviewRepository.existsByUserIdAndProductId(review.getUserId(), product.getId())) {
            throw new IllegalArgumentException("User has already reviewed this product");
        }

        // Validar que el usuario haya recibido el producto (estado = "ENTREGADA" en tabla deliveries)
        if (!deliveryValidationService.hasUserReceivedProduct(review.getUserId(), product.getId())) {
            throw new IllegalArgumentException("User must have received the product before leaving a review");
        }

        // Marcar como compra verificada ya que pasó la validación de entrega
        review.setVerifiedPurchase(true);

        return reviewRepository.save(review);
    }

    public List<Review> getProductReviews(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    public List<Review> getUserReviews(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    public Review updateReview(Long reviewId, Review updatedReview) {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (updatedReview.getRating() != null) {
            if (updatedReview.getRating() < 1 || updatedReview.getRating() > 5) {
                throw new IllegalArgumentException("Rating must be between 1 and 5");
            }
            existingReview.setRating(updatedReview.getRating());
        }

        if (updatedReview.getComment() != null) {
            if (updatedReview.getComment().length() > 500) {
                throw new IllegalArgumentException("Comment must not exceed 500 characters");
            }
            existingReview.setComment(updatedReview.getComment());
        }

        return reviewRepository.save(existingReview);
    }

    /**
     * Verifica si un usuario puede dejar una reseña para un producto específico
     * @param userId ID del usuario
     * @param productId ID del producto
     * @return true si puede dejar reseña, false en caso contrario
     */
    public boolean canUserReviewProduct(Long userId, Long productId) {
        // Verificar que no haya dejado ya una reseña
        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            return false;
        }

        // Verificar que haya recibido el producto
        return deliveryValidationService.hasUserReceivedProduct(userId, productId);
    }
} 