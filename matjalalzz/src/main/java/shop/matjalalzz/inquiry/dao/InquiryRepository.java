package shop.matjalalzz.inquiry.dao;

import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import shop.matjalalzz.inquiry.entity.Inquiry;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {


    @Query("select i from Inquiry i where i.id = :inquiryId and i.user.id = :userId")
    Optional<Inquiry> findByIdAndUser(@Param("inquiryId") Long inquiryId, @Param("userId") Long userId);


    @Query("select i from Inquiry i where i.id < :cursor or :cursor IS NULL ORDER BY i.id DESC")
    Slice<Inquiry> findByCursor(@Param("cursor") Long cursor, PageRequest of);
}
