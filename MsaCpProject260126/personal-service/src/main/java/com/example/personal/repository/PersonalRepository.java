package com.example.personal.repository;

import com.example.personal.model.DelYn;
import com.example.personal.model.Personal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonalRepository extends JpaRepository<Personal, Integer> {

    // ID로 회원 찾기 (로그인용)
    Optional<Personal> findByLoginId(String loginId);

    // ID 중복 체크
    boolean existsByLoginId(String loginId);

    // 이메일로 회원 찾기
    Optional<Personal> findByEmail(String email);

    // 이메일 중복 체크
    boolean existsByEmail(String email);

    // admin
    List<Personal> findAllByDelYnOrderBySeqNoM100Desc(DelYn delYn);

    // admin 통계부분  월별 가입수(년/월) - JPQL (DB 함수 의존 줄임)
    interface MonthlyJoinCount {
        Integer getY();
        Integer getM();
        Long getCnt();
    }

    @Query("""
        select year(p.insertDate) as y,
               month(p.insertDate) as m,
               count(p) as cnt
        from Personal p
        where p.delYn = :delYn
          and p.insertDate between :from and :to
        group by year(p.insertDate), month(p.insertDate)
        order by year(p.insertDate), month(p.insertDate)
    """)
    List<MonthlyJoinCount> countMonthlyJoins(
            @Param("delYn") DelYn delYn,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // ✅ 총 가입수 - JPQL
    @Query("""
        select count(p)
        from Personal p
        where p.delYn = :delYn
          and p.insertDate between :from and :to
    """)
    long countTotalJoins(
            @Param("delYn") DelYn delYn,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}