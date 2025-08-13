package shop.matjalalzz.global.config;


import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DsProxyLoggingConfig {

    // JDBC 누적 시간 저장용 ThreadLocal

    // JDBC 누적(ns)
    public static final ThreadLocal<Long> JDBC_NS = ThreadLocal.withInitial(() -> 0L);
    // 쿼리 단위 시작(ns)
    private static final ThreadLocal<Long> START_NS = new ThreadLocal<>();

    @Bean
    public QueryExecutionListener queryExecutionListener() {
        return new QueryExecutionListener() {
            @Override
            public void beforeQuery(ExecutionInfo ei, List<QueryInfo> qi) {
                START_NS.set(System.nanoTime()); // 나노초 시작
            }

            @Override
            public void afterQuery(ExecutionInfo ei, List<QueryInfo> qi) {
                long start = START_NS.get() != null ? START_NS.get() : System.nanoTime();
                START_NS.remove();

                long ns = System.nanoTime() - start;          // 나노초 경과
                JDBC_NS.set(JDBC_NS.get() + ns);              // 누적

                String sql = qi.isEmpty() ? "" : qi.get(0).getQuery();
                String oneLine = sql == null ? "" : sql.replaceAll("\\s+"," ").trim();
                double ms = ns / 1_000_000.0;
                log.info(String.format("[SQL] %.3f ms | %s", ms, oneLine));
            }
        };
    }
}