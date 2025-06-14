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

    @Transactional
    public Review createReview(Review review) {
        // For testing purposes, we'll set verifiedPurchase to true
        review.setVerifiedPurchase(true);
        
        // Validate rating
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Validate comment length
        if (review.getComment() != null && review.getComment().length() > 500) {
            throw new IllegalArgumentException("Comment must not exceed 500 characters");
        }

        // Buscar el producto
        Product product = productRepository.findById(review.getProduct().getId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Asignar el producto a la reseña
        review.setProduct(product);

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
} 