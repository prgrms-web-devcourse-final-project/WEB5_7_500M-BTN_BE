package shop.matjalalzz.inquiry.event;

import lombok.Getter;
import org.w3c.dom.Text;
import shop.matjalalzz.inquiry.entity.Inquiry;
import shop.matjalalzz.user.entity.User;

@Getter
public class InquiryCreateEvent {
    private final User user;
    private final String title;
    private final String Content;

    public InquiryCreateEvent(Inquiry inquiry) {
        this.user = inquiry.getUser();
        this.title = inquiry.getTitle();
        Content = inquiry.getContent();
    }
}
