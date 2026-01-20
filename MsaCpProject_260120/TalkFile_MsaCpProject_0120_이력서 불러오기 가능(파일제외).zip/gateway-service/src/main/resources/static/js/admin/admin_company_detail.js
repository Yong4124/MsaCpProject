document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("ADMIN_TOKEN");
    const name = localStorage.getItem("ADMIN_NAME") || "관리자";

    // ✅ role 정규화: "ROLE_SUPER_ADMIN" / "SUPER_ADMIN" / "1" 등 → 일관된 코드로 변환
    const rawRole = (localStorage.getItem("ADMIN_ROLE") || "").trim();

    function normalizeRole(role) {
        const r = (role || "").trim().replace(/^ROLE_/, "");

        // 숫자 코드로 오는 경우 매핑
        if (r === "1") return "SUPER_ADMIN";
        if (r === "2") return "KCCI_ADMIN";

        return r;
    }

    const role = normalizeRole(rawRole);

    // ✅ 기업회원 승인 가능한 관리자: SUPER_ADMIN(1), KCCI_ADMIN(2)
    const canApprove = role === "SUPER_ADMIN" || role === "KCCI_ADMIN";

    if (!token) {
        location.replace("/admin");
        return;
    }

    const welcomeEl = document.getElementById("welcomeText");
    if (welcomeEl) welcomeEl.textContent = `${name}님 환영합니다.`;

    document.getElementById("logoutBtn")?.addEventListener("click", () => {
        localStorage.removeItem("ADMIN_TOKEN");
        localStorage.removeItem("ADMIN_NAME");
        localStorage.removeItem("ADMIN_ROLE");
        location.replace("/admin");
    });

    // ✅ seq 파라미터 읽기
    const params = new URLSearchParams(location.search);
    const seq = params.get("seq");
    if (!seq) {
        alert("잘못된 접근입니다.(seq 없음)");
        location.href = "/admin/company_list";
        return;
    }

    // ✅ 요소들
    const approvalCheckbox = document.getElementById("approvalCheckbox");
    const saveBtn = document.getElementById("saveBtn");

    // ✅ 권한 없으면 UI 차단(1차 방어)
    if (!canApprove) {
        if (approvalCheckbox) approvalCheckbox.disabled = true;
        if (saveBtn) {
            saveBtn.disabled = true;
            saveBtn.style.opacity = "0.5";
            saveBtn.style.cursor = "not-allowed";
            saveBtn.title = "승인은 슈퍼관리자/대한상공회의소 관리자만 가능합니다.";
        }
    }

    const fields = {
        company: document.getElementById("company"),
        parentCompanyCd: document.getElementById("parentCompanyCd"),
        businessRegistNum: document.getElementById("businessRegistNum"),
        presidentNm: document.getElementById("presidentNm"),
        companyAddress: document.getElementById("companyAddress"),
        loginId: document.getElementById("loginId"),
        managerNm: document.getElementById("managerNm"),
        phone: document.getElementById("phone"),
        email: document.getElementById("email"),
        department: document.getElementById("department"),
    };

    const logoLink = document.getElementById("logoLink");

    function setValue(el, v) {
        if (!el) return;
        el.value = v ?? "";
    }

    async function loadDetail() {
        const url = `/api/company/admin/companies/${encodeURIComponent(seq)}`;

        try {
            // ✅ fetch → adminFetch (Authorization은 adminFetch가 자동으로 붙임)
            const res = await adminFetch(url, {
                method: "GET",
                headers: { Accept: "application/json" },
            });

            // 401이면 adminFetch에서 이미 처리(로그아웃/이동)되었을 가능성이 큼
            if (res.status === 403) {
                alert("권한이 없습니다.");
                location.href = "/admin/company_list";
                return;
            }

            if (!res.ok) {
                alert(`상세 조회 실패 (${res.status})`);
                location.href = "/admin/company_list";
                return;
            }

            const json = await res.json();
            const data = json.data ?? json;

            setValue(fields.company, data.company);
            setValue(fields.parentCompanyCd, data.parentCompanyCd);
            setValue(fields.businessRegistNum, data.businessRegistNum);
            setValue(fields.presidentNm, data.presidentNm);
            setValue(fields.companyAddress, data.companyAddress);
            setValue(fields.loginId, data.loginId);
            setValue(fields.managerNm, data.managerNm);
            setValue(fields.phone, data.phone);
            setValue(fields.email, data.email);
            setValue(fields.department, data.department);

            // 승인 체크
            const approvalYn = (data.approvalYn ?? "").toString().toUpperCase();
            if (approvalCheckbox) approvalCheckbox.checked = approvalYn === "Y";

            // 로고(있으면 링크 표시)
            if (logoLink) {
                const logo = data.companyLogo ?? data.logo ?? data.logoPath ?? "";
                if (logo) {
                    logoLink.textContent = logo;
                    logoLink.href = logo;
                } else {
                    logoLink.textContent = "";
                    logoLink.href = "#";
                }
            }
        } catch (e) {
            // adminFetch가 401 처리하면서 throw 했을 수 있음
            if (String(e?.message || "") === "UNAUTHORIZED") return;
            console.error(e);
            alert("네트워크 오류");
        }
    }

    async function saveApproval() {
        // ✅ 권한 없으면 저장 막기(2차 방어)
        if (!canApprove) {
            alert("승인은 슈퍼관리자/대한상공회의소 관리자만 가능합니다.");
            return;
        }

        const approvalYn = approvalCheckbox?.checked ? "Y" : "N";
        const url = `/api/company/admin/companies/${encodeURIComponent(seq)}/approval`;

        try {
            // ✅ fetch → adminFetch
            const res = await adminFetch(url, {
                method: "PUT",
                headers: {
                    Accept: "application/json",
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ approvalYn }),
            });

            if (res.status === 403) {
                alert("권한이 없습니다.");
                return;
            }

            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                alert(err.message || `승인 저장 실패 (${res.status})`);
                return;
            }

            alert("저장되었습니다.");
            loadDetail();
        } catch (e) {
            if (String(e?.message || "") === "UNAUTHORIZED") return;
            console.error(e);
            alert("네트워크 오류");
        }
    }

    saveBtn?.addEventListener("click", saveApproval);

    loadDetail();
});
