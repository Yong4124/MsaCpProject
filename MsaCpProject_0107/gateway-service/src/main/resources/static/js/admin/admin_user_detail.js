document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("ADMIN_TOKEN");

    // ✅ 토큰 없으면 /admin(로그인 화면)으로
    if (!token) {
        location.replace("/admin");
        return;
    }

    // ✅ 현재 로그인 관리자 역할(roleCode)
    // 예: "SUPER_ADMIN", "KCCI_ADMIN", "KUSAF_ADMIN"
    const rawRole  = localStorage.getItem("ADMIN_ROLE") || "";
    const role = rawRole.trim().replace(/^ROLE_/, "");
    // ✅ 승인 가능한 관리자: SUPER_ADMIN, KUSAF_ADMIN만 허용
    const canApprove =
        role === "SUPER_ADMIN" || role === "KUSAF_ADMIN" ||
        role === "1" || role === "3";

    // ✅ 로그아웃 버튼
    document.getElementById("logoutBtn")?.addEventListener("click", () => {
        localStorage.removeItem("ADMIN_TOKEN");
        localStorage.removeItem("ADMIN_NAME");
        localStorage.removeItem("ADMIN_ROLE");
        location.replace("/admin");
    });

    const params = new URLSearchParams(location.search);
    const seq = params.get("seq");
    if (!seq) {
        alert("잘못된 접근입니다.");
        location.href = "/admin/user_list";
        return;
    }

    // 목록 버튼
    document.getElementById("btnBack")?.addEventListener("click", () => {
        location.href = "/admin/user_list";
    });

    // 코드값 -> 라벨
    const genderLabel = (g) => ({ M: "남성", F: "여성", O: "기타", N: "무응답" }[g] || g);
    const residenceLabel = (r) => ({ K: "대한민국", U: "미국", O: "기타" }[r] || r);

    // 1건 조회
    const res = await fetch(`/api/personal/admin/users/${seq}`, {
        headers: { "Authorization": `Bearer ${token}` }
    });

    if (!res.ok) {
        alert("회원 정보를 불러오지 못했습니다.");
        return;
    }

    const d = await res.json();

    // 화면 바인딩
    const approvalEl = document.getElementById("approvalYn");
    const saveBtn = document.getElementById("btnSave");

    approvalEl.checked = (d.approvalYn === "Y");
    document.getElementById("loginId").value = d.loginId ?? "";
    document.getElementById("name").value = d.name ?? "";
    document.getElementById("birthDate").value = (d.birthDate ?? "").toString().slice(0, 10);
    document.getElementById("gender").value = genderLabel(d.gender);
    document.getElementById("email").value = d.email ?? "";
    document.getElementById("residence").value = residenceLabel(d.residence);
    document.getElementById("lastRank").value = d.lastRank ?? "";

    document.getElementById("serviceBranch").value = d.serviceBranch ?? "";
    document.getElementById("serviceCategory").value = d.serviceCategory ?? "";
    document.getElementById("serviceStation").value = d.serviceStation ?? "";
    document.getElementById("serviceYear").value = d.serviceYear ?? "";
    document.getElementById("unitPosition").value = d.unitPosition ?? "";

    // ✅ 승인 권한 없으면: 승인 체크박스/저장 버튼 비활성화
    if (!canApprove) {
        if (approvalEl) {
            approvalEl.disabled = true;
        }
        if (saveBtn) {
            saveBtn.disabled = true;
            saveBtn.style.opacity = "0.5";
            saveBtn.style.cursor = "not-allowed";
            saveBtn.title = "승인은 슈퍼관리자/한미동맹재단 관리자만 가능합니다.";
        }
    }

    // 저장(승인여부만 변경)
    saveBtn?.addEventListener("click", async () => {
        // ✅ 버튼을 강제로 활성화해도 못 누르게 2차 방어
        if (!canApprove) {
            alert("승인은 슈퍼관리자/한미동맹재단 관리자만 가능합니다.");
            return;
        }

        const approvalYn = approvalEl.checked ? "Y" : "N";

        const saveRes = await fetch(`/api/personal/admin/users/${seq}/approval`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ approvalYn })
        });

        if (!saveRes.ok) {
            alert("저장 실패");
            return;
        }
        alert("저장 완료");
    });
});
