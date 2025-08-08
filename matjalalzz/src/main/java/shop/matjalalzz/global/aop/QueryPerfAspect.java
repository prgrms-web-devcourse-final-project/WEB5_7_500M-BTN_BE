//package shop.matjalalzz.global.aop;
//
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.*;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.springframework.stereotype.Component;
//import static shop.matjalalzz.global.config.DsProxyLoggingConfig.JDBC_NS;
//
//
//@Slf4j
//@Aspect
//@Component
//public class QueryPerfAspect {
//
//    // 측정 범위: repository와 usecase
//    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
//
//    @Around("execution(* shop.matjalalzz..repository..*(..)) || " +
//        "execution(* shop.matjalalzz..dao..*(..)) || " +
//        "execution(* shop.matjalalzz..usecase..*(..))")
//    public Object measure(ProceedingJoinPoint pjp) throws Throwable {
//        int depth = DEPTH.get();
//        boolean outer = depth == 0;
//
//        if (outer) {
//            JDBC_NS.set(0L); // 바깥 레벨에서만 JDBC 누적 초기화
//        }
//        DEPTH.set(depth + 1);
//
//        long t0 = System.nanoTime();
//        try {
//            return pjp.proceed();
//        } finally {
//            int cur = DEPTH.get() - 1;
//            DEPTH.set(cur);
//
//            if (cur == 0) { // 바깥 레벨에서만 로그 + ThreadLocal 정리
//                double total = (System.nanoTime() - t0) / 1_000_000.0;
//                double jdbc = JDBC_NS.get() / 1_000_000.0;
//                double overhead = Math.max(0, total - jdbc);
//
//                String sig = ((MethodSignature) pjp.getSignature()).toShortString();
//                log.info(String.format("[PERF] %s | total=%.3f ms | jdbc=%.3f ms | overhead=%.3f ms", sig, total, jdbc, overhead));
//
//                JDBC_NS.remove();
//                DEPTH.remove();
//            }
//        }
//    }
//}