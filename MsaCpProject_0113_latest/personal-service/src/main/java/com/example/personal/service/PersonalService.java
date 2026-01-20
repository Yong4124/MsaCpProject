package com.example.personal.service;

import com.example.personal.dto.MemberUpdateDto;
import com.example.personal.model.ApprovalYn;
import com.example.personal.model.DelYn;
import com.example.personal.model.Personal;
import com.example.personal.repository.PersonalRepository;
import com.example.personal.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalService {

    private final PersonalRepository personalRepository;

    // 회원가입
    @Transactional
    public Personal register(Personal personal){
        // ID 중복 체크
        if (personalRepository.existsByLoginId(personal.getLoginId())) {
            throw new IllegalArgumentException("이미 존재하는 ID입니다.");
        }

        // 이메일 중복 체크
        if (personalRepository.existsByEmail(personal.getEmail())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        // Salt 생성
        String salt = PasswordEncoder.generateSalt();
        personal.setSalt(salt);

        // 비밀번호 암호화
        String encodedPassword = PasswordEncoder.encode(personal.getPw(), salt);
        personal.setPw(encodedPassword);

        // 회원 저장
        return personalRepository.save(personal);
    }

    // ID 중복 체크
    public boolean checkIdDuplication(String loginId) {
        return personalRepository.existsByLoginId(loginId);
    }

    // 이메일 중복 체크
    public boolean checkEmailDuplication(String email) {
        return personalRepository.existsByEmail(email);
    }

    // ID로 회원 조회
    public Optional<Personal> findByLoginId(String loginId) {
        return personalRepository.findByLoginId(loginId);
    }

    // 전체 회원 조회
    public Iterable<Personal> findAll() {
        return personalRepository.findAll();
    }

    // 로그인 (비밀번호 검증)
    public Personal login(String loginId, String password) {
        // 사용자 조회
        Personal personal = personalRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        // 비밀번호 검증 (SHA-512 + Salt)
        if (!PasswordEncoder.matches(password, personal.getPw(), personal.getSalt())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // 탈퇴 회원 체크
        if (personal.getDelYn() == DelYn.Y) {
            throw new IllegalArgumentException("탈퇴한 회원입니다.");
        }

        // 승인 대기 회원 체크
        if (personal.getApprovalYn() == ApprovalYn.N) {
            throw new IllegalArgumentException("관리자 승인 대기 중입니다.");
        }

        return personal;
    }

    // 아이디 찾기
    public Optional<String> findIdByNameAndEmail(String name, String email) {
        return personalRepository.findAll().stream()
                .filter(personal -> personal.getName().equals(name) &&
                        personal.getEmail().equals(email) &&
                        personal.getDelYn() == DelYn.N)
                .map(Personal::getLoginId)
                .findFirst();
    }

    // 비밀번호 재설정 전 회원 정보 확인
    public boolean verifyUser(String loginId,String name, String email ) {
        return personalRepository.findByLoginId(loginId)
                .map(personal -> personal.getName().equals(name) &&
                        personal.getEmail().equals(email) &&
                        personal.getDelYn() == DelYn.N)
                .orElse(false);
    }

    // 비밀번호 재설정
    @Transactional
    public void resetPassword(String loginId, String newPassword) {
        Personal personal = personalRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // Salt 생성
        String salt = PasswordEncoder.generateSalt();
        personal.setSalt(salt);

        // 비밀번호 암호화 후 저장
        String encodedPassword = PasswordEncoder.encode(newPassword, salt);
        personal.setPw(encodedPassword);

        personalRepository.save(personal);
    }

    // 회원정보 조회 - MyPage
    public Personal getMemberInfo(String loginId) {
        return personalRepository.findByLoginId(loginId)
                .filter(personal -> personal.getDelYn() == DelYn.N)
                .orElseThrow(() -> new IllegalArgumentException("회원정보를 찾을 수 없습니다."));
    }

    // 회원정보 수정 - MyPage
    @Transactional
    public void updateMemberInfo(String loginId, MemberUpdateDto updateDto) {
        Personal personal = personalRepository.findByLoginId(loginId)
                .filter(p -> p.getDelYn() == DelYn.N)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // 수정 가능한 필드
        personal.setGender(updateDto.getGender());
        personal.setResidence(updateDto.getResidence());
        personal.setLastRank(updateDto.getLastRank());
        personal.setServiceCategory(updateDto.getServiceCategory());
        personal.setServiceBranch(updateDto.getServiceBranch());
        personal.setServiceYear(updateDto.getServiceYear());
        personal.setServiceStation(updateDto.getServiceStation());
        personal.setUnitPosition(updateDto.getUnitPosition());
        personal.setEmail(updateDto.getEmail());

        // 비밀번호 변경 시
        if (updateDto.getNewPassword() != null && !updateDto.getNewPassword().isEmpty()) {
            String newSalt = PasswordEncoder.generateSalt();
            String hashedPassword = PasswordEncoder.encode(updateDto.getNewPassword(), newSalt);
            personal.setPw(hashedPassword);
            personal.setSalt(newSalt);
        }

        personalRepository.save(personal);
    }

    // 회원 탈퇴
    @Transactional
    public void deleteMember(String loginId, String password) {
        Personal personal = personalRepository.findByLoginId(loginId)
                .filter(p -> p.getDelYn() == DelYn.N)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // 비밀번호 확인
        if (!PasswordEncoder.matches(password, personal.getPw(), personal.getSalt())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        personal.setDelYn(DelYn.Y);
        personalRepository.save(personal);
    }

    // admin
    public List<Personal> adminList() {
        return personalRepository.findAllByDelYnOrderBySeqNoM100Desc(DelYn.N);
    }

    public Optional<Personal> findBySeq(Integer seq) {
        return personalRepository.findById(seq);
    }

    // admin - 기간별 (총 가입수 + 년-월별 가입수)
    public Map<String, Object> getJoinStats(LocalDate from, LocalDate to) {

        // JPQL로 바꾼 Repository 기준: DelYn.N을 파라미터로 넘김
        long total = personalRepository.countTotalJoins(DelYn.N, from, to);

        List<Map<String, Object>> rows = personalRepository.countMonthlyJoins(DelYn.N, from, to).stream()
                .map(r -> Map.<String, Object>of(
                        // y,m을 "YYYY-MM" 형태로 만들어서 내려줌
                        "ym", String.format("%04d-%02d", r.getY(), r.getM()),
                        "count", r.getCnt()
                ))
                .toList();

        return Map.of(
                "total", total,
                "rows", rows
        );
    }
}
