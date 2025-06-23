package com.products.product.pruebas_Unitarias;

import com.products.product.controller.ReviewController;
import com.products.product.entity.Review;
import com.products.product.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    private Review testReview;
    private List<Review> reviews;

    @BeforeEach
    void setUp() {
        testReview = new Review();
        testReview.setId(1L);
        testReview.setUserId(1L);
        testReview.setRating(5);
        testReview.setComment("Great product!");

        Review review2 = new Review();
        review2.setId(2L);
        review2.setUserId(1L);
        review2.setRating(4);
        review2.setComment("Good product");

        reviews = Arrays.asList(testReview, review2);
    }

    @Test
    void createReview_ShouldReturnCreatedReview() {
        // Arrange
        when(reviewService.createReview(any(Review.class))).thenReturn(testReview);

        // Act
        ResponseEntity<?> response = reviewController.createReview(testReview);
        Review result = (Review) response.getBody();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(reviewService).createReview(any(Review.class));
    }

    @Test
    void createReview_ShouldReturnBadRequestWhenValidationFails() {
        // Arrange
        when(reviewService.createReview(any(Review.class))).thenThrow(new IllegalArgumentException("Invalid rating"));

        // Act
        ResponseEntity<?> response = reviewController.createReview(testReview);
        String result = (String) response.getBody();

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid rating", result);
        verify(reviewService).createReview(any(Review.class));
    }

    @Test
    void getProductReviews_ShouldReturnReviews() {
        // Arrange
        Long productId = 1L;
        when(reviewService.getProductReviews(productId)).thenReturn(reviews);

        // Act
        ResponseEntity<List<Review>> response = reviewController.getProductReviews(productId);
        List<Review> result = response.getBody();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(reviewService).getProductReviews(productId);
    }

    @Test
    void getUserReviews_ShouldReturnReviews() {
        // Arrange
        Long userId = 1L;
        when(reviewService.getUserReviews(userId)).thenReturn(reviews);

        // Act
        ResponseEntity<List<Review>> response = reviewController.getUserReviews(userId);
        List<Review> result = response.getBody();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(reviewService).getUserReviews(userId);
    }

    @Test
    void deleteReview_ShouldReturnOk() {
        // Arrange
        Long reviewId = 1L;
        doNothing().when(reviewService).deleteReview(reviewId);

        // Act
        ResponseEntity<Void> response = reviewController.deleteReview(reviewId);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(reviewService).deleteReview(reviewId);
    }

    @Test
    void updateReview_ShouldReturnUpdatedReview() {
        // Arrange
        Long reviewId = 1L;
        when(reviewService.updateReview(reviewId, testReview)).thenReturn(testReview);

        // Act
        ResponseEntity<Review> response = reviewController.updateReview(reviewId, testReview);
        Review result = response.getBody();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(reviewService).updateReview(reviewId, testReview);
    }

    @Test
    void canUserReviewProduct_ShouldReturnResponse() {
        // Arrange
        Long userId = 1L;
        Long productId = 1L;
        when(reviewService.canUserReviewProduct(userId, productId)).thenReturn(true);

        // Act
        ResponseEntity<Map<String, Object>> response = reviewController.canUserReviewProduct(userId, productId);
        Map<String, Object> result = response.getBody();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(result);
        assertTrue((Boolean) result.get("canReview"));
        verify(reviewService).canUserReviewProduct(userId, productId);
    }
}
