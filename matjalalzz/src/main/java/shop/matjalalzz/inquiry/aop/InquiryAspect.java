package shop.matjalalzz.inquiry.aop;


import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import shop.matjalalzz.inquiry.entity.Inquiry;

@Aspect
@Component
@RequiredArgsConstructor
public class InquiryAspect {

    private final ApplicationEventPublisher eventPublisher;
    //스프링 이벤트 발행기로 이벤트 객체를 다른 컴포넌트에 전파하는 역할

    @AfterReturning( pointcut = "execution(* shop.matjalalzz.inquiry.dao..*Repository.save(..))",  returning = "result" )

    //AOP가 작동할 지점 선정으로 repository save가 현재 대상
    //returning = "result"는 실제 save로 db에 저장된 결과.
    public void afterSave(JoinPoint joinPoint, Object result) {
        Object arg = joinPoint.getArgs()[0];
        if(arg instanceof Inquiry inquiry) {
            eventPublisher.publishEvent(inquiry);
            //가져온 정보들로 이벤트 생성 후 이벤트 퍼블리셔로 발행 -> 나중에 이를 수신하는 @EventListener 등이 처리 가능
        }
    }
}
