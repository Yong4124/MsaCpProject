package com.example.company.service;

import com.example.company.dto.CompanyLoginRequest;
import com.example.company.dto.CompanyRegisterRequest;
import com.example.company.model.ApprovalYn;
import com.example.company.model.Company;
import com.example.company.model.DelYn;
import com.example.company.model.ParentCompany;
import com.example.company.repository.CompanyRepository;
import com.example.company.util.PasswordEncoder;
import com.example.company.util.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final VerificationService verificationService;

    // 파일 업로드 경로 (application.properties에서 설정 가능)
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    /**
     * 회원가입
     */
    @Transactional
    public Map<String, Object> register(CompanyRegisterRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // ID 중복 체크 (삭제되지 않은 회원 기준)
            if (companyRepository.existsByLoginIdAndDelYn(request.getLoginId(), DelYn.N)) {
                result.put("success", false);
                result.put("message", "이미 존재하는 ID입니다.");
                return result;
            }

            // 이메일 중복 체크 (삭제되지 않은 회원 기준)
            if (companyRepository.existsByEmailAndDelYn(request.getEmail(), DelYn.N)) {
                result.put("success", false);
                result.put("message", "이미 사용중인 이메일입니다.");
                return result;
            }

            // 사업자등록번호 중복 체크 (정책에 따라 delYn 고려할 수도 있음)
            if (companyRepository.existsByBusinessRegistNum(request.getBusinessRegistNum())) {
                result.put("success", false);
                result.put("message", "이미 등록된 사업자등록번호입니다.");
                return result;
            }

            Company company = new Company();
            company.setLoginId(request.getLoginId());
            company.setManagerNm(request.getManagerNm());
            company.setDepartment(request.getDepartment());
            company.setPhone(request.getPhone());
            company.setEmail(request.getEmail());
            company.setBusinessRegistNum(request.getBusinessRegistNum());
            company.setCompany(request.getCompany());
            company.setPresidentNm(request.getPresidentNm());
            company.setCompanyAddress(request.getCompanyAddress());

            // ParentCompany 설정
            if (request.getParentCompanyCd() != null && !request.getParentCompanyCd().isEmpty()) {
                try {
                    company.setParentCompanyCd(ParentCompany.valueOf(request.getParentCompanyCd()));
                } catch (IllegalArgumentException e) {
                    company.setParentCompanyCd(null);
                }
            }

            // Salt 생성 및 비밀번호 암호화
            String salt = PasswordEncoder.generateSalt();
            company.setSalt(salt);
            String encodedPassword = PasswordEncoder.encode(request.getPw(), salt);
            company.setPw(encodedPassword);

            // 기본값 설정
            company.setInsertDate(LocalDate.now());
            company.setApprovalYn(ApprovalYn.N);
            company.setDelYn(DelYn.N);

            companyRepository.save(company);

            result.put("success", true);
            result.put("message", "회원가입이 완료되었습니다. 관리자 승인 후 로그인이 가능합니다.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "회원가입에 실패했습니다: " + e.getMessage());
        }

        return result;
    }

    /**
     * 로그인
     */
    public Map<String, Object> login(CompanyLoginRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // ✅ 삭제되지 않은 회원만 조회
            Optional<Company> optCompany =
                    companyRepository.findByLoginIdAndDelYn(request.getLoginId(), DelYn.N);

            if (optCompany.isEmpty()) {
                result.put("success", false);
                result.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
                return result;
            }

            Company company = optCompany.get();

            // 비밀번호 검증
            if (!PasswordEncoder.matches(request.getPw(), company.getPw(), company.getSalt())) {
                result.put("success", false);
                result.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
                return result;
            }

            // 승인 대기 회원 체크
            if (company.getApprovalYn() == ApprovalYn.N) {
                result.put("success", false);
                result.put("message", "관리자 승인 대기 중입니다.");
                return result;
            }

            result.put("success", true);
            result.put("message", "로그인 성공");
            result.put("data", convertToMap(company));

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "로그인에 실패했습니다: " + e.getMessage());
        }

        return result;
    }

    /**
     * 아이디 중복 확인
     */
    public Map<String, Object> checkIdExists(String loginId) {
        Map<String, Object> result = new HashMap<>();
        boolean exists = companyRepository.existsByLoginIdAndDelYn(loginId, DelYn.N);
        result.put("exists", exists);
        return result;
    }

    /**
     * 이메일 인증번호 발송
     */
    public Map<String, Object> sendVerificationEmail(String email) {
        Map<String, Object> result = new HashMap<>();

        try {
            String code = verificationService.generateAndSaveCode(email);
            result.put("success", true);
            result.put("message", "인증번호가 발송되었습니다.");
            result.put("code", code); // 개발 모드에서만 반환
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "인증번호 발송에 실패했습니다.");
        }

        return result;
    }

    /**
     * 내 정보 조회
     */
    public Map<String, Object> getMemberInfo(String loginId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<Company> optCompany = companyRepository.findByLoginIdAndDelYn(loginId, DelYn.N);

            if (optCompany.isEmpty()) {
                result.put("success", false);
                result.put("message", "회원정보를 찾을 수 없습니다.");
                return result;
            }

            result.put("success", true);
            result.put("data", convertToMap(optCompany.get()));

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "회원정보 조회에 실패했습니다: " + e.getMessage());
        }

        return result;
    }

    /**
     * 회원정보 수정 (파일 업로드 지원)
     */
    @Transactional
    public Map<String, Object> updateMemberInfo(String loginId, Map<String, Object> request, 
                                                 MultipartFile logoFile, MultipartFile photoFile) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<Company> optCompany = companyRepository.findByLoginIdAndDelYn(loginId, DelYn.N);

            if (optCompany.isEmpty()) {
                result.put("success", false);
                result.put("message", "회원정보를 찾을 수 없습니다.");
                return result;
            }

            Company company = optCompany.get();

            // 수정 가능한 필드 업데이트
            if (request.get("company") != null) company.setCompany((String) request.get("company"));
            if (request.get("businessRegistNum") != null) company.setBusinessRegistNum((String) request.get("businessRegistNum"));
            if (request.get("presidentNm") != null) company.setPresidentNm((String) request.get("presidentNm"));
            if (request.get("companyAddress") != null) company.setCompanyAddress((String) request.get("companyAddress"));
            if (request.get("managerNm") != null) company.setManagerNm((String) request.get("managerNm"));
            if (request.get("phone") != null) company.setPhone((String) request.get("phone"));
            if (request.get("department") != null) company.setDepartment((String) request.get("department"));
            if (request.get("email") != null) company.setEmail((String) request.get("email"));

            // ParentCompany 설정
            if (request.get("parentCompanyCd") != null) {
                String parentCode = (String) request.get("parentCompanyCd");
                if (!parentCode.isEmpty()) {
                    try {
                        company.setParentCompanyCd(ParentCompany.valueOf(parentCode));
                    } catch (IllegalArgumentException e) {
                        company.setParentCompanyCd(null);
                    }
                } else {
                    company.setParentCompanyCd(null);
                }
            }

            // 비밀번호 변경
            if (request.get("newPassword") != null && !((String) request.get("newPassword")).isEmpty()) {
                String newPassword = (String) request.get("newPassword");
                String newSalt = PasswordEncoder.generateSalt();
                String hashedPassword = PasswordEncoder.encode(newPassword, newSalt);
                company.setPw(hashedPassword);
                company.setSalt(newSalt);
            }

            // ⭐ 로고 파일 삭제
            Boolean logoDelete = (Boolean) request.get("logoDelete");
            if (Boolean.TRUE.equals(logoDelete) && company.getLogoPath() != null) {
                deleteFile(company.getLogoPath());
                company.setLogoPath(null);
            }

            // ⭐ 기업전경 파일 삭제
            Boolean photoDelete = (Boolean) request.get("photoDelete");
            if (Boolean.TRUE.equals(photoDelete) && company.getPhotoPath() != null) {
                deleteFile(company.getPhotoPath());
                company.setPhotoPath(null);
            }

            // ⭐ 로고 파일 업로드
            if (logoFile != null && !logoFile.isEmpty()) {
                // 기존 파일 삭제
                if (company.getLogoPath() != null) {
                    deleteFile(company.getLogoPath());
                }
                String logoPath = saveFile(logoFile, "logos");
                company.setLogoPath(logoPath);
            }

            // ⭐ 기업전경 파일 업로드
            if (photoFile != null && !photoFile.isEmpty()) {
                // 기존 파일 삭제
                if (company.getPhotoPath() != null) {
                    deleteFile(company.getPhotoPath());
                }
                String photoPath = saveFile(photoFile, "photos");
                company.setPhotoPath(photoPath);
            }

            companyRepository.save(company);

            result.put("success", true);
            result.put("message", "회원정보가 수정되었습니다.");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "회원정보 수정에 실패했습니다: " + e.getMessage());
        }

        return result;
    }

    /**
     * 파일 저장
     */
    private String saveFile(MultipartFile file, String subDir) throws IOException {
        // 업로드 디렉토리 생성
        Path dirPath = Paths.get(uploadPath, subDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 고유 파일명 생성
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID().toString() + extension;

        // 파일 저장
        Path filePath = dirPath.resolve(newFileName);
        Files.copy(file.getInputStream(), filePath);

        // 상대 경로 반환 (DB 저장용)
        return "/" + subDir + "/" + newFileName;
    }

    /**
     * 파일 삭제
     */
    private void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) return;
        
        try {
            Path path = Paths.get(uploadPath, filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("파일 삭제 실패: " + filePath);
        }
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public Map<String, Object> deleteAccount(String loginId, String password) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<Company> optCompany = companyRepository.findByLoginIdAndDelYn(loginId, DelYn.N);

            if (optCompany.isEmpty()) {
                result.put("success", false);
                result.put("message", "회원정보를 찾을 수 없습니다.");
                return result;
            }

            Company company = optCompany.get();

            // 비밀번호 확인
            if (!PasswordEncoder.matches(password, company.getPw(), company.getSalt())) {
                result.put("success", false);
                result.put("message", "비밀번호가 일치하지 않습니다.");
                return result;
            }

            company.setDelYn(DelYn.Y);
            companyRepository.save(company);

            result.put("success", true);
            result.put("message", "회원 탈퇴가 완료되었습니다.");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "회원 탈퇴에 실패했습니다: " + e.getMessage());
        }

        return result;
    }

    /**
     * 아이디 찾기
     */
    public Map<String, Object> findId(String email, String managerNm) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<Company> optCompany =
                    companyRepository.findByEmailAndManagerNmAndDelYn(email, managerNm, DelYn.N);

            if (optCompany.isPresent()) {
                result.put("success", true);
                result.put("loginId", optCompany.get().getLoginId());
            } else {
                result.put("success", false);
                result.put("message", "일치하는 회원정보를 찾을 수 없습니다.");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "아이디 찾기에 실패했습니다: " + e.getMessage());
        }

        return result;
    }

    /**
     * 비밀번호 재설정 요청
     */
    public Map<String, Object> resetPasswordRequest(String loginId, String email) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<Company> optCompany =
                    companyRepository.findByLoginIdAndEmailAndDelYn(loginId, email, DelYn.N);

            if (optCompany.isEmpty()) {
                result.put("success", false);
                result.put("message", "회원정보를 찾을 수 없습니다.");
                return result;
            }

            // 인증 코드 발송
            String code = verificationService.generateAndSaveCode(email);

            result.put("success", true);
            result.put("message", "인증번호가 발송되었습니다.");
            result.put("code", code); // 개발 모드

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "비밀번호 재설정 요청에 실패했습니다: " + e.getMessage());
        }

        return result;
    }

    /**
     * 비밀번호 재설정
     * (현재 토큰 검증/저장 로직이 없어서 임시 유지)
     */
    @Transactional
    public Map<String, Object> resetPassword(String token, String newPassword) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "비밀번호 재설정 기능은 아직 구현되지 않았습니다.");
        return result;
    }

    /**
     * Company 엔티티를 Map으로 변환
     */
    private Map<String, Object> convertToMap(Company company) {
        Map<String, Object> map = new HashMap<>();
        map.put("seqNoM200", company.getSeqNoM200());
        map.put("loginId", company.getLoginId());
        map.put("managerNm", company.getManagerNm());
        map.put("department", company.getDepartment());
        map.put("phone", company.getPhone());
        map.put("email", company.getEmail());
        map.put("businessRegistNum", company.getBusinessRegistNum());
        map.put("company", company.getCompany());
        map.put("presidentNm", company.getPresidentNm());
        map.put("companyAddress", company.getCompanyAddress());
        map.put("parentCompanyCd", company.getParentCompanyCd() != null ? company.getParentCompanyCd().name() : null);
        map.put("approvalYn", company.getApprovalYn().name());
        map.put("insertDate", company.getInsertDate() != null ? company.getInsertDate().toString() : null);
        map.put("logoPath", company.getLogoPath());
        map.put("photoPath", company.getPhotoPath());
        return map;
    }
}
