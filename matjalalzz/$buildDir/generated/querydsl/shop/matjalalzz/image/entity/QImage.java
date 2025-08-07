package shop.matjalalzz.image.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QImage is a Querydsl query type for Image
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QImage extends EntityPathBase<Image> {

    private static final long serialVersionUID = -1871162887L;

    public static final QImage image = new QImage("image");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> imageIndex = createNumber("imageIndex", Long.class);

    public final NumberPath<Long> inquiryId = createNumber("inquiryId", Long.class);

    public final NumberPath<Long> reviewId = createNumber("reviewId", Long.class);

    public final StringPath s3Key = createString("s3Key");

    public final NumberPath<Long> shopId = createNumber("shopId", Long.class);

    public QImage(String variable) {
        super(Image.class, forVariable(variable));
    }

    public QImage(Path<? extends Image> path) {
        super(path.getType(), path.getMetadata());
    }

    public QImage(PathMetadata metadata) {
        super(Image.class, metadata);
    }

}

