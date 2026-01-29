package com.example.company.repository;

import com.example.company.model.ApprovalYn;
import com.example.company.model.Company;
import com.example.company.model.DelYn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {

    // ID로 회원 찾기 (삭제되지 않은 회원)
    Optional<Company> findByLoginIdAndDelYn(String loginId, DelYn delYn);

    // ID 중복 체크 (삭제되지 않은 회원)
    boolean existsByLoginIdAndDelYn(String loginId, DelYn delYn);

    // 이메일로 회원 찾기 (삭제되지 않은 회원)
    Optional<Company> findByEmailAndDelYn(String email, DelYn delYn);

    // 이메일 중복 체크 (삭제되지 않은 회원)
    boolean existsByEmailAndDelYn(String email, DelYn delYn);

    // ✅ 아이디 찾기 최적화 (DB에서 바로 조회)
    Optional<Company> findByEmailAndManagerNmAndDelYn(String email, String managerNm, DelYn delYn);

    // ✅ 비밀번호 재설정 요청에서 회원 검증 최적화
    Optional<Company> findByLoginIdAndEmailAndDelYn(String loginId, String email, DelYn delYn);

    // ✅ 비밀번호 찾기 회원 검증 (아이디 + 담당자명 + 이메일)
    Optional<Company> findByLoginIdAndManagerNmAndEmailAndDelYn(String loginId, String managerNm, String email, DelYn delYn);

    // 사업자등록번호 중복 체크 (정책상 탈퇴와 무관하게 막고 싶으면 그대로)
    boolean existsByBusinessRegistNum(String businessRegistNum);

    // admin - 삭제되지 않은 기업회원 목록 (최신순)
    List<Company> findAllByDelYnOrderBySeqNoM200Desc(DelYn delYn);

    // admin 통계 - 월별 가입수(년/월)
    interface MonthlyJoinCount {
        Integer getY();
        Integer getM();
        Long getCnt();
    }

    @Query("""
        select year(c.insertDate) as y,
               month(c.insertDate) as m,
               count(c) as cnt
        from Company c
        where c.delYn = :delYn
          and c.insertDate between :from and :to
        group by year(c.insertDate), month(c.insertDate)
        order by year(c.insertDate), month(c.insertDate)
    """)
    List<MonthlyJoinCount> countMonthlyJoins(
            @Param("delYn") DelYn delYn,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // 총 가입수
    @Query("""
        select count(c)
        from Company c
        where c.delYn = :delYn
          and c.insertDate between :from and :to
    """)
    long countTotalJoins(
            @Param("delYn") DelYn delYn,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // 관리자 기업 회원 검색
    @Query("""
        select c from Company c
        where c.delYn = :delYn
          and (:approval is null or c.approvalYn = :approval)
          and (
                :keyword is null or :keyword = '' or
                (:field = 'all' and (
                    c.company like concat('%', :keyword, '%') or
                    c.managerNm like concat('%', :keyword, '%') or
                    c.loginId like concat('%', :keyword, '%')
                ))
             or (:field = 'company' and c.company like concat('%', :keyword, '%'))
             or (:field = 'manager' and c.managerNm like concat('%', :keyword, '%'))
             or (:field = 'id'      and c.loginId like concat('%', :keyword, '%'))
          )
        order by c.seqNoM200 desc
    """)
    List<Company> searchForAdmin(
            @Param("delYn") DelYn delYn,
            @Param("approval") ApprovalYn approval,
            @Param("field") String field,
            @Param("keyword") String keyword
    );
}
