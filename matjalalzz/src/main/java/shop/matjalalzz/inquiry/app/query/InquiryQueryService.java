package shop.matjalalzz.inquiry.app.query;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.inquiry.dao.InquiryRepository;
import shop.matjalalzz.inquiry.entity.Inquiry;

@Service
@RequiredArgsConstructor
public class InquiryQueryService {
    private final InquiryRepository inquiryRepository;


    @Transactional(readOnly = true)
    public Slice<Inquiry> getAllInquiry(Long cursor, int size) {
       return inquiryRepository.findByCursor(cursor, PageRequest.of(0, size));
    }


    @Transactional(readOnly = true)
    public Optional<Inquiry> getOneInquiry(Long inquiryId) {
        return inquiryRepository.findById(inquiryId);
    }


}
