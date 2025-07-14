package shop.matjalalzz.global.s3.app;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.s3.dto.PreSignedCompledItem;
import shop.matjalalzz.global.s3.dto.PreSignedCompledRequest;
import shop.matjalalzz.image.dao.ImageRepository;
import shop.matjalalzz.image.entity.Image;
import shop.matjalalzz.image.mapper.ImageMapper;
import shop.matjalalzz.shop.dao.ShopRepository;

@Service
@RequiredArgsConstructor
public class PreSignedService {
    private final ShopRepository shopRepository;
    private final PreSignedProvider preSignedProvider;
    private final ImageRepository imageRepository;



    // 프리사이드 url에 이미지가 성공적으로 저장 되었는지 확인 후 image db에 저장
    @Transactional
    // 발급 된 프리사이드 url에 이미지가 확실하게 저장되면 image DB에 저장, 여러 이미지 중 하나라도 false가 있으면 s3에 있던 이미지 전체 삭제
    public void imageCompletion (PreSignedCompledRequest request){

        shopRepository.findById(request.shopId()).orElseThrow(()-> new BusinessException((ErrorCode.NOT_FIND_SHOP)));

        int size = request.preSignedCompledItemList().size();

        List<String> allKeys = request.preSignedCompledItemList().stream().map(
            PreSignedCompledItem::key).toList();

        for (int i = 0; i < size; i++) {
            if (!request.preSignedCompledItemList().get(i).completion()){
                //s3에 올라간 이미지들 전체 삭제 후 에러 반환
                preSignedProvider.deleteImg(allKeys);
                throw new BusinessException(ErrorCode.IMAGE_SAVE_FAILED);
            }
        }

        // TODO 실제로 s3에도 객체 사진이 실제로 존재하는지 확인 여부 필요 (추가 사항)

        List<Image> images = ImageMapper.toimagesList(request);

        imageRepository.saveAll(images);

    }


}
