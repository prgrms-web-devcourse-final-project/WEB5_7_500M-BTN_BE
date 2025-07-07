package shop.matjalalzz.user.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;


//메인에서도 @ConfigurationPropertiesScan 이걸 해줘야 properties 파일들을 스캔해서 가져온다
//exp에 값을 먼저 넣고 Validation을 생성 후 그걸 가지고 validation 생성
@Getter
@ConfigurationProperties(prefix = "custom.jwt") //이 밑에 값들을 가져온다
@RequiredArgsConstructor
public class JwtConfiguration {
    private final Secrets secrets;
    private final ExpTime expTime;

    @Getter
    @RequiredArgsConstructor
    public static class Secrets{
        private final String originkey;
        private final String appkey;
    }

    @Getter
    @RequiredArgsConstructor
    public static class ExpTime{
        private final Long access;
        private final Long refresh;
    }

}
