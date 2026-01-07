document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("ADMIN_TOKEN");
    const name = localStorage.getItem("ADMIN_NAME") || "관리자";

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
        // ✅ 상세 API
        const url = `/api/company/admin/companies/${encodeURIComponent(seq)}`;

        try {
            const res = await fetch(url, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    // 서버가 JWT 검증이면 사용
                    // "Authorization": `Bearer ${token}`,
                },
            });

            if (!res.ok) {
                alert(`상세 조회 실패 (${res.status})`);
                location.href = "/admin/company_list";
                return;
            }

            const json = await res.json();
            // 응답이 {success,data}거나 data 자체일 수 있어 대응
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
            approvalCheckbox.checked = approvalYn === "Y";

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
            console.error(e);
            alert("네트워크 오류");
        }
    }

    async function saveApproval() {
        const approvalYn = approvalCheckbox.checked ? "Y" : "N";

        const url = `/api/company/admin/companies/${encodeURIComponent(seq)}/approval`;

        try {
            const res = await fetch(url, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    // "Authorization": `Bearer ${token}`,
                },
                body: JSON.stringify({ approvalYn }),
            });

            if (!res.ok) {
                alert(`승인 저장 실패 (${res.status})`);
                return;
            }

            alert("저장되었습니다.");
            // 저장 후 다시 로드(혹시 서버에서 값 바뀌는 경우)
            loadDetail();

        } catch (e) {
            console.error(e);
            alert("네트워크 오류");
        }
    }

    saveBtn?.addEventListener("click", saveApproval);

    // 최초 로딩
    loadDetail();
});
