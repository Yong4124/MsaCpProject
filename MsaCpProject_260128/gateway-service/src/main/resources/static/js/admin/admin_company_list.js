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
    const fieldSelect = document.getElementById("fieldSelect"); // all / company / manager / id
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
        if (yn === "Y" || yn === "승인") return `<span class="status-approve">승인</span>`;
        return `<span class="status-wait">미승인</span>`;
    }

    function render(list) {
        if (!Array.isArray(list) || list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">데이터가 없습니다.</td></tr>`;
            return;
        }

        tbody.innerHTML = list
            .map((r) => {
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

    // ✅ (중요) company-service가 토큰 검증을 안 하면 401이 안 떨어짐
    // 그래서 admin-service의 "보호된 API"를 한번 호출해서 만료를 확실히 감지한다.
    // (너 SecurityConfig에서 GET /api/admin/users/** 는 authenticated()라서 체크용으로 사용 가능)
    let lastSessionCheckAt = 0;
    async function ensureAdminSession() {
        const now = Date.now();

        // 너무 자주 치면 불필요하니 20초 쿨다운(원하면 조절)
        if (now - lastSessionCheckAt < 20000) return;
        lastSessionCheckAt = now;

        // ✅ 이 요청에서 401이면 adminFetch가 자동 로그아웃/이동 처리
        // 성공하면 아무것도 하지 않음
        const res = await adminFetch("/api/admin/users", {
            method: "GET",
            headers: { Accept: "application/json" },
        });

        // adminFetch는 401이면 throw 하므로 여기까지 오면 정상/403/기타
        if (res.status === 403) {
            // 로그인은 됐는데 권한 문제가 있으면(보통은 없겠지만) 안내만
            alert("권한이 없습니다.");
        }
    }

    async function load() {
        const field = fieldSelect?.value || "all";
        const keyword = (keywordInput?.value || "").trim();
        const approvalVal = approvalSelect?.value || "all"; // all / Y / N

        const params = new URLSearchParams();
        params.set("field", field);
        params.set("keyword", keyword);
        if (approvalVal !== "all") params.set("approvalYn", approvalVal);

        const url = `/api/company/admin/companies?${params.toString()}`;

        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">로딩중...</td></tr>`;

        try {
            // ✅ 1) 먼저 admin-service로 세션(토큰 만료) 체크 → 만료면 여기서 바로 튕김
            await ensureAdminSession();

            // ✅ 2) 실제 데이터 호출
            const res = await adminFetch(url, {
                method: "GET",
                headers: { Accept: "application/json" },
            });

            if (res.status === 403) {
                tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">권한이 없습니다. (403)</td></tr>`;
                return;
            }

            if (!res.ok) {
                tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">목록 조회 실패 (${res.status})</td></tr>`;
                return;
            }

            const json = await res.json();
            const list = Array.isArray(json) ? json : (json.data || []);
            render(list);
        } catch (err) {
            // adminFetch가 401 처리하면서 throw("UNAUTHORIZED") 했을 수 있음 → 그 경우 화면 메시지 띄우지 않음
            if (String(err?.message || "") === "UNAUTHORIZED") return;

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
