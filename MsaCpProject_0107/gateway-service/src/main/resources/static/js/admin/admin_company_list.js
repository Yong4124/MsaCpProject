console.log("admin_company_list.js loaded!");
document.addEventListener("DOMContentLoaded", () => {
    // ✅ 관리자 로그인 토큰 체크
    const token = localStorage.getItem("ADMIN_TOKEN");
    const name = localStorage.getItem("ADMIN_NAME") || "관리자";

    if (!token) {
        location.replace("/admin");
        return;
    }

    // ✅ 상단 환영 문구
    const welcomeEl = document.getElementById("welcomeText");
    if (welcomeEl) welcomeEl.textContent = `${name}님 환영합니다.`;

    // ✅ 로그아웃
    document.getElementById("logoutBtn")?.addEventListener("click", () => {
        localStorage.removeItem("ADMIN_TOKEN");
        localStorage.removeItem("ADMIN_NAME");
        localStorage.removeItem("ADMIN_ROLE");
        location.replace("/admin");
    });

    // ✅ 화면 요소
    const tbody = document.getElementById("companyTbody");
    const approvalSelect = document.getElementById("approvalSelect"); // all / Y / N
    const fieldSelect = document.getElementById("fieldSelect");       // all / company / manager / id
    const keywordInput = document.getElementById("keywordInput");
    const searchBtn = document.getElementById("searchBtn");

    if (!tbody) return;

    // XSS 방지용 간단 escape
    function esc(v) {
        return String(v ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function badge(approvalYn) {
        const yn = String(approvalYn ?? "").toUpperCase();
        if (yn === "Y" || yn === "승인") {
            return `<span class="status-approve">승인</span>`;
        }
        return `<span class="status-wait">미승인</span>`;
    }

    function render(list) {
        if (!Array.isArray(list) || list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">데이터가 없습니다.</td></tr>`;
            return;
        }

        tbody.innerHTML = list
            .map((r) => {
                // 백엔드가 내려주는 키가 케이스마다 달라질 수 있어 안전하게 대응
                const no = r.no ?? r.seqNoM200 ?? "";
                const company = r.company ?? "";
                const manager = r.manager ?? r.managerNm ?? "";
                const id = r.id ?? r.loginId ?? "";
                const date = r.date ?? r.insertDate ?? "";
                const approve = r.approve ?? r.approvalYn ?? "";


                const detailUrl = `/admin/company_detail?seq=${encodeURIComponent(no)}`;

                return `
          <tr>
            <td>${esc(no)}</td>
            <td>
                <a href="${detailUrl}" style="text-decoration:none; color:inherit;">
                   ${esc(company)}
                </a>
            </td>
            <td>${esc(manager)}</td>
             <td>${esc(id)}</td>
            <td>${esc(date)}</td>
            <td>${badge(approve)}</td>
          </tr>
        `;
            })
            .join("");
    }

    async function load() {
        const field = fieldSelect?.value || "all";
        const keyword = (keywordInput?.value || "").trim();
        const approvalVal = approvalSelect?.value || "all"; // all / Y / N

        // ✅ company-service 관리자 목록 API
        // GET /api/company/admin/companies?field=all&keyword=...&approvalYn=Y|N
        const params = new URLSearchParams();
        params.set("field", field);
        params.set("keyword", keyword);
        if (approvalVal !== "all") params.set("approvalYn", approvalVal);

        const url = `/api/company/admin/companies?${params.toString()}`;

        // 로딩 표시(선택)
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">로딩중...</td></tr>`;

        try {
            const res = await fetch(url, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    // 서버에서 JWT 검증하도록 되어 있으면 아래 사용
                    // "Authorization": `Bearer ${token}`,
                },
            });

            if (!res.ok) {
                tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">목록 조회 실패 (${res.status})</td></tr>`;
                return;
            }

            const json = await res.json();

            // ✅ 응답 형태 2가지 대응
            // 1) [{...},{...}]
            // 2) {success:true, data:[...]}
            const list = Array.isArray(json) ? json : (json.data || []);
            render(list);
        } catch (err) {
            console.error(err);
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">네트워크 오류</td></tr>`;
        }
    }

    // ✅ 이벤트 연결
    searchBtn?.addEventListener("click", load);
    keywordInput?.addEventListener("keydown", (e) => {
        if (e.key === "Enter") load();
    });
    approvalSelect?.addEventListener("change", load);
    fieldSelect?.addEventListener("change", load);

    // ✅ 첫 로딩
    load();
});
