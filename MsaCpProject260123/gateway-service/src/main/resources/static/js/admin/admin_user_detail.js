document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("ADMIN_TOKEN");

    // ✅ 토큰 없으면 /admin(로그인 화면)으로
    if (!token) {
        location.replace("/admin");
        return;
    }

    // ✅ 현재 로그인 관리자 역할(roleCode)
    const rawRole = localStorage.getItem("ADMIN_ROLE") || "";
    const role = rawRole.trim().replace(/^ROLE_/, "");

    // ✅ 승인 가능한 관리자: SUPER_ADMIN, KUSAF_ADMIN만 허용
    const canApprove =
        role === "SUPER_ADMIN" || role === "KUSAF_ADMIN" || role === "1" || role === "3";

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

    // ✅ (중요) personal-service가 토큰 검증을 안 하면 401이 안 떨어질 수 있음
    // 그래서 admin-service의 보호된 API를 한번 호출해서 만료를 확실히 감지한다.
    async function ensureAdminSession() {
        // 이 요청에서 401이면 adminFetch가 자동 로그아웃/이동 처리
        await adminFetch("/api/admin/users", {
            method: "GET",
            headers: { Accept: "application/json" },
        });
    }

    // ✅ 1건 조회
    async function loadDetail() {
        // 먼저 만료 체크 (확실하게)
        await ensureAdminSession();

        // fetch → adminFetch (Authorization 자동)
        const res = await adminFetch(`/api/personal/admin/users/${encodeURIComponent(seq)}`, {
            method: "GET",
            headers: { Accept: "application/json" },
        });

        if (res.status === 403) {
            alert("권한이 없습니다.");
            return null;
        }

        if (!res.ok) {
            alert("회원 정보를 불러오지 못했습니다.");
            return null;
        }

        return await res.json();
    }

    let d;
    try {
        d = await loadDetail();
        if (!d) return;
    } catch (e) {
        // adminFetch가 401 처리하면서 throw("UNAUTHORIZED") 했을 수 있음
        if (String(e?.message || "") === "UNAUTHORIZED") return;
        console.error(e);
        alert("네트워크 오류");
        return;
    }

    // 화면 바인딩
    const approvalEl = document.getElementById("approvalYn");
    const saveBtn = document.getElementById("btnSave");

    if (approvalEl) approvalEl.checked = d.approvalYn === "Y";
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
        if (approvalEl) approvalEl.disabled = true;
        if (saveBtn) {
            saveBtn.disabled = true;
            saveBtn.style.opacity = "0.5";
            saveBtn.style.cursor = "not-allowed";
            saveBtn.title = "승인은 슈퍼관리자/한미동맹재단 관리자만 가능합니다.";
        }
    }

    // 저장(승인여부만 변경)
    saveBtn?.addEventListener("click", async () => {
        // 2차 방어
        if (!canApprove) {
            alert("승인은 슈퍼관리자/한미동맹재단 관리자만 가능합니다.");
            return;
        }

        const approvalYn = approvalEl?.checked ? "Y" : "N";

        try {
            // 먼저 만료 체크 (확실하게)
            await ensureAdminSession();

            // fetch → adminFetch
            const saveRes = await adminFetch(
                `/api/personal/admin/users/${encodeURIComponent(seq)}/approval`,
                {
                    method: "PUT",
                    headers: {
                        Accept: "application/json",
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({ approvalYn }),
                }
            );

            if (saveRes.status === 403) {
                alert("권한이 없습니다.");
                return;
            }

            if (!saveRes.ok) {
                const err = await saveRes.json().catch(() => ({}));
                alert(err.message || "저장 실패");
                return;
            }

            alert("저장 완료");
        } catch (e) {
            if (String(e?.message || "") === "UNAUTHORIZED") return;
            console.error(e);
            alert("네트워크 오류");
        }
    });
});
