package shop.matjalalzz.inquiry.app.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.inquiry.dao.InquiryRepository;
import shop.matjalalzz.inquiry.entity.Inquiry;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class InquiryCommandService {
    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;


    @Transactional
    public long createNewInquiry(Inquiry inquiry, User user)  {
        inquiryRepository.save(inquiry);
        return inquiry.getId();
    }




}
