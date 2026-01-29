package com.example.admin.repository;

import com.example.admin.model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    // 로그인 아이디(ID 컬럼)로 조회 (삭제되지 않은 사용자)
    Optional<AdminUser> findByLoginIdAndDelYn(String loginId, String delYn);

    // 로그인 아이디(ID 컬럼) 중복 체크 (삭제되지 않은 사용자 기준 권장)
    boolean existsByLoginIdAndDelYn(String loginId, String delYn);

    // 관리자 목록 (삭제되지 않은 사용자만)
    List<AdminUser> findAllByDelYnOrderBySeqNoC010Desc(String delYn);

    // ✅ 관리자 검색 (전체/아이디/성명)
    @Query("""
        select u from AdminUser u
        where u.delYn = 'N'
          and (
                (:field = 'all'  and (u.loginId like concat('%', :keyword, '%') or u.nm like concat('%', :keyword, '%')))
             or (:field = 'id'   and  u.loginId like concat('%', :keyword, '%'))
             or (:field = 'name' and  u.nm like concat('%', :keyword, '%'))
          )
        order by u.seqNoC010 desc
    """)
    List<AdminUser> searchAdmins(@Param("field") String field,
                                 @Param("keyword") String keyword);
}
