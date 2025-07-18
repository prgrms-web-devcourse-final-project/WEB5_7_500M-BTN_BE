package shop.matjalalzz.global.s3.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@RequiredArgsConstructor
public class AwsConfig {

    @Value("${aws.credentials.AWS_ACCESS_KEY}")
    private String accessKey;

    @Value("${aws.credentials.AWS_SECRET_KEY}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            ))
            .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))

//            .credentialsProvider(DefaultCredentialsProvider.builder().build()) //- 이게 인증정보를 알아서 찾는거 같긴한데 테스트를 못하는 상황이라 흐음 create는 depreated..
            // https://team05-500m-btn.s3.ap-northeast-2.amazonaws.com/shops/cat/cat_img_0

            //DefaultCredentialsProvider 클래스가 인증정보 찾는 순서가 아래와 같습니다. https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/DefaultCredentialsProvider.html
            //Java System Properties - aws.accessKeyId and aws.secretAccessKey
            //Environment Variables - AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY -->
            //Web Identity Token credentials from system properties or environment variables
            //Credential profiles file at the default location (~/.aws/credentials) shared by all AWS SDKs and the AWS CLI
            //Credentials delivered through the Amazon EC2 container service if AWS_CONTAINER_CREDENTIALS_RELATIVE_URI" environment variable is set and security manager has permission to access the variable,
            //Instance profile credentials delivered through the Amazon EC2 metadata service
            .serviceConfiguration(S3Configuration.builder().build())
            .build();
    }
}
