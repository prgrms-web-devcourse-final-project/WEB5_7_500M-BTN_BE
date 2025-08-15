package shop.matjalalzz.global.aop;

import static shop.matjalalzz.global.config.DsProxyLoggingConfig.JDBC_NS;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class QueryPerfAspect {

    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    // app 패키지 하위의 모든 메서드가 대상
    @Around("execution(* shop.matjalalzz..app..*(..))")
    public Object measure(ProceedingJoinPoint pjp) throws Throwable {
        int depth = DEPTH.get();
        if (depth == 0) {
            JDBC_NS.set(0L); // 바깥 레벨에서만 JDBC 누적 초기화
        }
        DEPTH.set(depth + 1);

        long t0 = System.nanoTime();
        try {
            return pjp.proceed(); //실제 대상 메서드 실행
        } finally {
            int cur = DEPTH.get() - 1;
            DEPTH.set(cur);

            if (cur == 0) { // 바깥 레벨에서만 로그 + ThreadLocal 정리
                double total = (System.nanoTime() - t0) / 1_000_000.0; //바깥 메서드 전체 수행시간
                double jdbc = JDBC_NS.get() / 1_000_000.0; //DsProxy 리스너가 누적해둔 db 오아복 시간의 합
                double overhead = Math.max(0, total - jdbc);

                String sig = pjp.getSignature().toShortString();
                log.info(String.format(
                    "[PERF] %s | total=%.3f ms | jdbc=%.3f ms | overhead=%.3f ms",
                    sig, total, jdbc, overhead));

                JDBC_NS.remove(); //ThreadLocal 정리
                DEPTH.remove();
            }
        }
    }
}
