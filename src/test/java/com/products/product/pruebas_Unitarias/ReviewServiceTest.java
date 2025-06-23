package com.products.product.pruebas_Unitarias;

import com.products.product.entity.Product;
import com.products.product.entity.Review;
import com.products.product.repository.ProductRepository;
import com.products.product.repository.ReviewRepository;
import com.products.product.service.DeliveryValidationService;
import com.products.product.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private DeliveryValidationService deliveryValidationService;

    @InjectMocks
    private ReviewService reviewService;

    private Review testReview;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setNombre("Test Product");

        testReview = new Review();
        testReview.setId(1L);
        testReview.setUserId(1L);
        testReview.setProduct(testProduct);
        testReview.setRating(5);
        testReview.setComment("Great product!");
        testReview.setVerifiedPurchase(true);
    }

    @Test
    void createReview_ShouldReturnReviewWhenValid() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(reviewRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(false);
        when(deliveryValidationService.hasUserReceivedProduct(1L, 1L)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // Act
        Review result = reviewService.createReview(testReview);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertTrue(result.isVerifiedPurchase());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_ShouldThrowWhenInvalidRating() {
        // Arrange
        testReview.setRating(0);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(testReview));
    }

    @Test
    void createReview_ShouldThrowWhenCommentTooLong() {
        // Arrange
        testReview.setComment(new String(new char[501])); // 501 characters

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(testReview));
    }

    @Test
    void createReview_ShouldThrowWhenProductNotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(testReview));
    }

    @Test
    void createReview_ShouldThrowWhenDuplicateReview() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(reviewRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(testReview));
    }

    @Test
    void createReview_ShouldThrowWhenNotReceivedProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(reviewRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(false);
        when(deliveryValidationService.hasUserReceivedProduct(1L, 1L)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(testReview));
    }

    @Test
    void getProductReviews_ShouldReturnReviews() {
        // Arrange
        when(reviewRepository.findByProductId(1L)).thenReturn(List.of(testReview));

        // Act
        List<Review> result = reviewService.getProductReviews(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reviewRepository).findByProductId(1L);
    }

    @Test
    void getUserReviews_ShouldReturnReviews() {
        // Arrange
        when(reviewRepository.findByUserId(1L)).thenReturn(List.of(testReview));

        // Act
        List<Review> result = reviewService.getUserReviews(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reviewRepository).findByUserId(1L);
    }

    @Test
    void deleteReview_ShouldDeleteReview() {
        // Arrange
        doNothing().when(reviewRepository).deleteById(1L);

        // Act
        reviewService.deleteReview(1L);

        // Assert
        verify(reviewRepository).deleteById(1L);
    }

    @Test
    void updateReview_ShouldUpdateRatingAndComment() {
        // Arrange
        Review updates = new Review();
        updates.setRating(4);
        updates.setComment("Updated comment");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // Act
        Review result = reviewService.updateReview(1L, updates);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getRating());
        assertEquals("Updated comment", result.getComment());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void canUserReviewProduct_ShouldReturnTrueWhenEligible() {
        // Arrange
        when(reviewRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(false);
        when(deliveryValidationService.hasUserReceivedProduct(1L, 1L)).thenReturn(true);

        // Act
        boolean result = reviewService.canUserReviewProduct(1L, 1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void canUserReviewProduct_ShouldReturnFalseWhenAlreadyReviewed() {
        // Arrange
        when(reviewRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(true);

        // Act
        boolean result = reviewService.canUserReviewProduct(1L, 1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void canUserReviewProduct_ShouldReturnFalseWhenNotReceived() {
        // Arrange
        when(reviewRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(false);
        when(deliveryValidationService.hasUserReceivedProduct(1L, 1L)).thenReturn(false);

        // Act
        boolean result = reviewService.canUserReviewProduct(1L, 1L);

        // Assert
        assertFalse(result);
    }
}