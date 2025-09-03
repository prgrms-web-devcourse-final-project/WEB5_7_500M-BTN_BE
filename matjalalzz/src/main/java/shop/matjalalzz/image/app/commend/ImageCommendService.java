package shop.matjalalzz.image.app.commend;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.matjalalzz.image.dao.ImageRepository;
import shop.matjalalzz.image.entity.Image;

@Service
@RequiredArgsConstructor
public class ImageCommendService {

    private final ImageRepository imageRepository;

    public void deleteAllImages(List<Image> images) {
        imageRepository.deleteAll(images);
    }

}
