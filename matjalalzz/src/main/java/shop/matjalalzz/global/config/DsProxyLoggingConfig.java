package shop.matjalalzz.global.config;

import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class DsProxyLoggingConfig {

    // 스레드별 JDBC 누적 소요 시간
    public static final ThreadLocal<Long> JDBC_NS = ThreadLocal.withInitial(() -> 0L);
    // 개별 쿼리 시작 시간
    public static final ThreadLocal<Long> START_NS = new ThreadLocal<>();

    //쿼리 훅 리스너 - JDBC가 호출될 때 실행
    @Bean
    public QueryExecutionListener queryExecutionListener() {
        return new QueryExecutionListener() {
            @Override
            public void beforeQuery(ExecutionInfo executionInfo, List<QueryInfo> list) {
                START_NS.set(System.nanoTime()); // 현재 시각을 nanoTime()으로 저장
            }

            @Override
            public void afterQuery(ExecutionInfo executionInfo, List<QueryInfo> qi) {
                long start = START_NS.get() != null ? START_NS.get() : System.nanoTime();
                START_NS.remove(); //스레드풀 재사용 고려

                long ns = System.nanoTime() - start; //개별 쿼리 단위 경과 시간
                JDBC_NS.set(JDBC_NS.get() + ns); //스레드별 누적

                double ms = ns / 1_000_000.0;
//                String sql = qi.isEmpty() ? "" : qi.get(0).getQuery();
//                String oneLine = sql == null ? "" : sql.replaceAll("\\s+", " ").trim();
//                log.info(String.format("[SQL] %.3f ms | %s", ms, oneLine));

                // 여러 SQL이 있을 수 있으니 모두 로그로 남기고 싶으면 loop
                for (QueryInfo q : qi) {
                    String sql = q.getQuery();
                    String oneLine = sql == null ? "" : sql.replaceAll("\\s+", " ").trim();
                    log.info("[SQL] {} ms | {}", String.format("%.3f", ms), oneLine);
                }
            }
        };
    }

    // HikariDataSource → ProxyDataSource로 감싸기
    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties, QueryExecutionListener listener) {
        // 기본 HikariDataSource 생성
        HikariDataSource hikari = properties.initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();

        // DsProxy로 감싸서 Listener 연결
        return ProxyDataSourceBuilder.create(hikari)
            .listener(listener) // queryExecutionListener 연결
            .build();
    }

}
