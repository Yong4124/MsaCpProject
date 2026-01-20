document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("ADMIN_TOKEN");
    const name = localStorage.getItem("ADMIN_NAME") || "관리자";

    // 토큰 없으면 로그인 화면으로
    if (!token) {
        location.replace("/admin");
        return;
    }

    const welcomeEl = document.getElementById("welcomeText");
    if (welcomeEl) welcomeEl.textContent = `${name}님 환영합니다.`;

    // 로그아웃
    document.getElementById("logoutBtn")?.addEventListener("click", () => {
        localStorage.removeItem("ADMIN_TOKEN");
        localStorage.removeItem("ADMIN_NAME");
        localStorage.removeItem("ADMIN_ROLE");
        location.replace("/admin");
    });

    const tbody = document.getElementById("memberTbody");
    const approvalSelect = document.getElementById("approvalFilter");

    // ✅ 이름 검색용 DOM
    const keywordInput = document.getElementById("nameKeyword");
    const searchBtn = document.getElementById("searchBtn");

    // ✅ 기간 검색용 DOM
    const fromDateEl = document.getElementById("fromDate");
    const toDateEl = document.getElementById("toDate");

    if (!tbody) return;

    // ✅ 올해 1/1 ~ 12/31 자동 세팅 (값이 비어있을 때만)
    const pad = (n) => String(n).padStart(2, "0");
    const ymd = (yy, mm, dd) => `${yy}-${pad(mm)}-${pad(dd)}`;

    const now = new Date();
    const y = now.getFullYear();

    if (fromDateEl && !fromDateEl.value) fromDateEl.value = ymd(y, 1, 1);
    if (toDateEl && !toDateEl.value) toDateEl.value = ymd(y, 12, 31);

    // 코드값 → 한글 라벨 변환
    const genderLabel = (g) => ({ M: "남성", F: "여성", O: "기타", N: "무응답" }[g] || g);
    const residenceLabel = (r) => ({ O: "기타", K: "대한민국", U: "미국" }[r] || r);
    const approvalLabel = (a) => (a === "Y" ? "승인" : "미승인");

    // ✅ 전체 데이터 저장용
    let allRows = [];

    // ✅ 테이블 렌더링 함수
    function render(rows) {
        if (!rows || rows.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;">조회 결과가 없습니다.</td></tr>`;
            return;
        }

        tbody.innerHTML = rows
            .map((r) => {
                const isApproved = r.approve === "Y";
                const statusClass = isApproved ? "status-approve" : "status-pending";

                return `
          <tr>
            <td>${r.no ?? ""}</td>
            <td>
              <a class="link-name" href="/admin/user_detail?seq=${encodeURIComponent(r.no)}">
                ${r.name ?? ""}
              </a>
            </td>
            <td>${r.id ?? ""}</td>
            <td>${genderLabel(r.gender)}</td>
            <td>${residenceLabel(r.residence)}</td>
            <td>${(r.date ?? "").toString().slice(0, 10)}</td>
            <td><span class="${statusClass}">${approvalLabel(r.approve)}</span></td>
          </tr>
        `;
            })
            .join("");
    }

    // ✅ 승인여부 + 이름검색 + 기간검색 필터 적용
    function applyFilter() {
        const approval = approvalSelect?.value || ""; // "" | "Y" | "N"
        const keyword = (keywordInput?.value || "").trim().toLowerCase();

        // 기간(YYYY-MM-DD)
        const from = (fromDateEl?.value || "").trim();
        const to = (toDateEl?.value || "").trim();

        let filtered = allRows;

        // 1) 승인여부 필터
        if (approval === "Y") filtered = filtered.filter((r) => r.approve === "Y");
        else if (approval === "N") filtered = filtered.filter((r) => r.approve !== "Y");

        // 2) 이름 검색(부분일치)
        if (keyword) {
            filtered = filtered.filter((r) => (r.name || "").toLowerCase().includes(keyword));
        }

        // 3) 기간(등록일) 검색
        if (from || to) {
            filtered = filtered.filter((r) => {
                const d = (r.date || "").toString().slice(0, 10); // "YYYY-MM-DD"
                if (!d) return false;

                if (from && d < from) return false;
                if (to && d > to) return false;

                return true;
            });
        }

        render(filtered);
    }

    // ✅ 이벤트
    approvalSelect?.addEventListener("change", applyFilter);
    searchBtn?.addEventListener("click", applyFilter);
    keywordInput?.addEventListener("keydown", (e) => {
        if (e.key === "Enter") applyFilter();
    });
    fromDateEl?.addEventListener("change", applyFilter);
    toDateEl?.addEventListener("change", applyFilter);

    // ✅ (중요) personal-service가 토큰 검증을 안 하면 401이 안 떨어질 수 있음
    // 그래서 admin-service(보호된 API)로 한번 호출해서 만료를 확실히 감지한다.
    let lastSessionCheckAt = 0;
    async function ensureAdminSession() {
        const nowMs = Date.now();
        if (nowMs - lastSessionCheckAt < 20000) return; // 20초 쿨다운(원하면 조절)
        lastSessionCheckAt = nowMs;

        // 여기서 401이면 adminFetch가 자동 로그아웃/이동 처리
        await adminFetch("/api/admin/users", {
            method: "GET",
            headers: { Accept: "application/json" },
        });
    }

    async function loadUsers() {
        // 로딩 표시(선택)
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;">로딩중...</td></tr>`;

        // ✅ 먼저 만료 체크 (확실하게)
        await ensureAdminSession();

        const res = await adminFetch("/api/personal/admin/users", {
            method: "GET",
            headers: { Accept: "application/json" },
        });

        if (!res.ok) throw new Error(`목록 조회 실패 (${res.status})`);
        return await res.json();
    }

    try {
        allRows = await loadUsers();
        applyFilter(); // 최초 전체 렌더
    } catch (e) {
        // adminFetch가 401 처리하면서 throw("UNAUTHORIZED") 했으면 여기서 메시지 덮어쓰지 않음
        if (String(e?.message || "") === "UNAUTHORIZED") return;

        console.error(e);
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;">데이터를 불러오지 못했습니다.</td></tr>`;
    }
});
