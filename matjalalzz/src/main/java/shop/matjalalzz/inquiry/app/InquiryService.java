package shop.matjalalzz.inquiry.app;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.matjalalzz.shop.app.ShopService;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final ShopService shopService;



}
