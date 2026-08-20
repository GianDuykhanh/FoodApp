package com.zephyr.FoodApp.review.services;


import com.zephyr.FoodApp.response.Response;
import com.zephyr.FoodApp.review.dtos.ReviewDTO;

import java.util.List;

public interface ReviewService {
    Response<ReviewDTO> createReview(ReviewDTO reviewDTO);
    Response<List<ReviewDTO>> getReviewsForMenu(Long menuId);
    Response<Double> getAverageRating(Long menuId);
}
