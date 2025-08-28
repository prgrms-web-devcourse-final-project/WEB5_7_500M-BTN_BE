package shop.matjalalzz.review.app;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.review.dao.ReviewRepository;
import shop.matjalalzz.review.entity.Review;
import shop.matjalalzz.shop.entity.Shop;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCommandService {

    private final ReviewRepository reviewRepository;

    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    public void addShopRating(Shop shop, Double rating) {
        Double currentRating = shop.getRating();
        int currentCount = reviewRepository.countReviewByShop(shop);
        double newRating = currentRating * currentCount + rating;
        newRating /= (currentCount + 1);
        shop.updateRating(newRating);
    }

    public void removeShopRating(Shop shop, Double rating) {
        Double currentRating = shop.getRating();
        int currentCount = reviewRepository.countReviewByShop(shop);
        double newRating = currentRating * currentCount - rating;
        newRating /= (currentCount - 1);
        if (newRating < 0) {
            newRating = 0.0;
        }
        shop.updateRating(newRating);
    }

}
