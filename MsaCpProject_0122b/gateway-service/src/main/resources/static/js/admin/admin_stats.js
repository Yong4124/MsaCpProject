// /js/admin_stats.js
(() => {
    let activeTab = "personal";

    const $ = (sel) => document.querySelector(sel);
    const $$ = (sel) => Array.from(document.querySelectorAll(sel));

    // ✅ 올해 1/1 ~ 12/31 자동 세팅
    function setDefaultYearRange() {
        const pad = (n) => String(n).padStart(2, "0");
        const ymd = (yy, mm, dd) => `${yy}-${pad(mm)}-${pad(dd)}`;

        const y = new Date().getFullYear();
        const start = ymd(y, 1, 1);
        const end = ymd(y, 12, 31);

        const pairs = [
            ["p_from", "p_to"],
            ["c_from", "c_to"],
            ["rm_from", "rm_to"],
            ["jn_from", "jn_to"],
            ["ct_from", "ct_to"],
            ["cm_from", "cm_to"], // ✅ 회사별공고(월별)
        ];

        for (const [fromId, toId] of pairs) {
            const fromEl = document.getElementById(fromId);
            const toEl = document.getElementById(toId);

            if (fromEl && !fromEl.value) fromEl.value = start;
            if (toEl && !toEl.value) toEl.value = end;
        }
    }

    // ✅ (중요) personal/company 서비스가 토큰 검증을 안 하면 401이 안 떨어질 수 있음
    // 그래서 admin-service의 보호된 API를 한번 호출해서 만료를 확실히 감지한다.
    let lastSessionCheckAt = 0;
    async function ensureAdminSession() {
        const now = Date.now();
        if (now - lastSessionCheckAt < 20000) return; // 20초 쿨다운 (원하면 조절)
        lastSessionCheckAt = now;

        // 이 요청에서 401이면 adminFetch가 자동 로그아웃/이동 처리
        await adminFetch("/api/admin/users", {
            method: "GET",
            headers: { Accept: "application/json" },
        });
    }

    // -----------------------
    // 개인회원 월별 가입
    // -----------------------
    async function fetchPersonalMonthly() {
        const from = $("#p_from")?.value || "";
        const to = $("#p_to")?.value || "";

        await ensureAdminSession();

        const res = await adminFetch(
            `/api/personal/admin/stats/join-monthly?from=${encodeURIComponent(
                from
            )}&to=${encodeURIComponent(to)}`,
            { method: "GET", headers: { Accept: "application/json" } }
        );

        if (!res.ok) throw new Error(`개인회원 통계 조회 실패 (${res.status})`);
        return await res.json();
    }

    async function renderPersonal() {
        const tbody = $("#p_tbody");
        try {
            const { total, rows } = await fetchPersonalMonthly();
            $("#p_total").textContent = `* 전체 회원수 : ${total}`;

            if (!rows || rows.length === 0) {
                tbody.innerHTML = `<tr><td colspan="3" style="text-align:center;">조회 결과가 없습니다.</td></tr>`;
                return;
            }

            tbody.innerHTML = rows
                .map(
                    (r, idx) => `
          <tr>
            <td>${idx + 1}</td>
            <td>${r.ym}</td>
            <td>${r.count}</td>
          </tr>
        `
                )
                .join("");
        } catch (e) {
            if (String(e?.message || "") === "UNAUTHORIZED") return;

            console.error(e);
            tbody.innerHTML = `<tr><td colspan="3" style="text-align:center;">데이터를 불러오지 못했습니다.</td></tr>`;
            $("#p_total").textContent = `* 전체 회원수 : 0`;
        }
    }

    // -----------------------
    // 채용공고(월별)
    // -----------------------
    async function fetchRecruitMonthly() {
        const from = $("#rm_from")?.value || "";
        const to = $("#rm_to")?.value || "";

        await ensureAdminSession();

        const res = await adminFetch(
            `/api/jobs/admin/stats/recruit-monthly?from=${encodeURIComponent(
                from
            )}&to=${encodeURIComponent(to)}`,
            { method: "GET", headers: { Accept: "application/json" } }
        );

        if (!res.ok) throw new Error(`채용공고 통계 조회 실패 (${res.status})`);
        return await res.json();
    }

    async function renderRecruitMonthly() {
        const tbody = $("#rm_tbody");
        try {
            const { total, totalApplicants, rows } = await fetchRecruitMonthly();

            $("#rm_total").textContent = `* 총공고수 : ${total} / 총지원자수 : ${totalApplicants}`;

            if (!rows || rows.length === 0) {
                tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;">조회 결과가 없습니다.</td></tr>`;
                return;
            }

            tbody.innerHTML = rows
                .map(
                    (r, idx) => `
          <tr>
            <td>${idx + 1}</td>
            <td>${r.ym}</td>
            <td>${r.jobCount}</td>
            <td>${r.applicantCount}</td>
          </tr>
        `
                )
                .join("");
        } catch (e) {
            if (String(e?.message || "") === "UNAUTHORIZED") return;

            console.error(e);
            tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;">데이터를 불러오지 못했습니다.</td></tr>`;
            $("#rm_total").textContent = `* 총공고수 : 0 / 총지원자수 : 0`;
        }
    }

    // -----------------------
    // 기업회원 월별 가입
    // -----------------------
    async function fetchCompanyMonthly() {
        const from = $("#c_from")?.value || "";
        const to = $("#c_to")?.value || "";

        await ensureAdminSession();

        const res = await adminFetch(
            `/api/company/admin/stats/join-monthly?from=${encodeURIComponent(
                from
            )}&to=${encodeURIComponent(to)}`,
            { method: "GET", headers: { Accept: "application/json" } }
        );

        if (!res.ok) throw new Error(`기업회원 통계 조회 실패 (${res.status})`);
        return await res.json();
    }

    async function renderCompany() {
        const tbody = $("#c_tbody");
        try {
            const { total, rows } = await fetchCompanyMonthly();
            $("#c_total").textContent = `* 전체 회원수 : ${total}`;

            if (!rows || rows.length === 0) {
                tbody.innerHTML = `<tr><td colspan="3" style="text-align:center;">조회 결과가 없습니다.</td></tr>`;
                return;
            }

            tbody.innerHTML = rows
                .map(
                    (r, idx) => `
          <tr>
            <td>${idx + 1}</td>
            <td>${r.ym}</td>
            <td>${r.count}</td>
          </tr>
        `
                )
                .join("");
        } catch (e) {
            if (String(e?.message || "") === "UNAUTHORIZED") return;

            console.error(e);
            tbody.innerHTML = `<tr><td colspan="3" style="text-align:center;">데이터를 불러오지 못했습니다.</td></tr>`;
            $("#c_total").textContent = `* 전체 회원수 : 0`;
        }
    }

    // -----------------------
    // 공고명별 통계 + ✅ 클라이언트 페이징
    // -----------------------
    const JN_PAGE_SIZE = 10; // ✅ 페이지당 row 수
    let jnRowsCache = [];
    let jnPage = 1;

    const esc = (v) =>
        String(v ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");

    const formatCompany = (groupName, companyName) => {
        const g = (groupName ?? "").trim();
        const c = (companyName ?? "").trim();
        if (g && c) return `[${g}] ${c}`;
        if (c) return c;
        if (g) return `[${g}]`;
        return "";
    };

    // 현재 페이지에 해당하는 목록(테이블 tbody)을 그리는 함수
    function renderJobNamePage(page) {
        const tbody = $("#jn_tbody");

        const total = jnRowsCache.length;
        const totalPages = Math.max(1, Math.ceil(total / JN_PAGE_SIZE));

        jnPage = Math.min(Math.max(1, page), totalPages);

        const start = (jnPage - 1) * JN_PAGE_SIZE;
        const slice = jnRowsCache.slice(start, start + JN_PAGE_SIZE);

        if (slice.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">조회 결과가 없습니다.</td></tr>`;
            return;
        }

        tbody.innerHTML = slice
            .map((r, idx) => {
                const period = `${r.startDate ?? ""} ~ ${r.endDate ?? ""}`;
                const no = start + idx + 1;
                return `
          <tr>
            <td>${no}</td>
            <td>${esc(r.jobName)}</td>
            <td>${esc(formatCompany(r.groupName, r.companyName))}</td>
            <td>${esc(period)}</td>
            <td>${r.jobCount ?? 0}</td>
            <td>${r.applicantCount ?? 0}</td>
          </tr>
        `;
            })
            .join("");
    }

    // 페이지버튼을 만드는 함수
    function renderJobNamePager() {
        const pager = $("#jn_pager");
        if (!pager) return;

        const total = jnRowsCache.length;
        const totalPages = Math.max(1, Math.ceil(total / JN_PAGE_SIZE));

        pager.innerHTML = "";

        for (let p = 1; p <= totalPages; p++) {
            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = "pager-btn" + (p === jnPage ? " active" : "");
            btn.textContent = String(p);

            btn.addEventListener("click", () => {
                renderJobNamePage(p);
                renderJobNamePager();
            });

            pager.appendChild(btn);
        }
    }

    async function fetchJobNameStats() {
        const from = $("#jn_from")?.value || "";
        const to = $("#jn_to")?.value || "";
        const job = $("#jn_job")?.value || "";
        const company = $("#jn_company")?.value || "";

        await ensureAdminSession();

        const qs = new URLSearchParams({ from, to, job, company });

        const res = await adminFetch(
            `/api/jobs/admin/stats/job-name?${qs.toString()}`,
            { method: "GET", headers: { Accept: "application/json" } }
        );

        if (!res.ok) throw new Error(`공고명별 통계 조회 실패 (${res.status})`);
        return await res.json();
    }

    async function renderJobNameStats() {
        const tbody = $("#jn_tbody");

        try {
            const { total, totalApplicants, rows } = await fetchJobNameStats();

            $("#jn_total").textContent = `* 총공고수 : ${total} / 총지원자수 : ${totalApplicants}`;

            jnRowsCache = Array.isArray(rows) ? rows : [];
            jnPage = 1;

            renderJobNamePage(1);
            renderJobNamePager();
        } catch (e) {
            if (String(e?.message || "") === "UNAUTHORIZED") return;

            console.error(e);
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">데이터를 불러오지 못했습니다.</td></tr>`;
            $("#jn_total").textContent = `* 총공고수 : 0 / 총지원자수 : 0`;

            jnRowsCache = [];
            jnPage = 1;
            renderJobNamePager();
        }
    }

    // -----------------------
    // ✅ 회사별공고 통계 + 클라이언트 페이징(10개 단위)
    //  - HTML: ct_from, ct_to, ct_company, ct_total, ct_tbody, ct_pager
    //  - API : GET /api/jobs/admin/stats/company
    // -----------------------
    const CT_PAGE_SIZE = 10;
    let ctRowsCache = [];
    let ctPage = 1;

    async function fetchCompanyTotalStats() {
        const from = $("#ct_from")?.value || "";
        const to = $("#ct_to")?.value || "";
        const company = $("#ct_company")?.value || "";

        await ensureAdminSession();

        const qs = new URLSearchParams({ from, to, company });

        const res = await adminFetch(
            `/api/jobs/admin/stats/company?${qs.toString()}`,
            { method: "GET", headers: { Accept: "application/json" } }
        );

        if (!res.ok) throw new Error(`회사별공고 통계 조회 실패 (${res.status})`);
        return await res.json();
    }

    function renderCompanyTotalPage(page) {
        const tbody = $("#ct_tbody");

        const total = ctRowsCache.length;
        const totalPages = Math.max(1, Math.ceil(total / CT_PAGE_SIZE));

        ctPage = Math.min(Math.max(1, page), totalPages);

        const start = (ctPage - 1) * CT_PAGE_SIZE;
        const slice = ctRowsCache.slice(start, start + CT_PAGE_SIZE);

        if (slice.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;">조회 결과가 없습니다.</td></tr>`;
            return;
        }

        tbody.innerHTML = slice
            .map((r, idx) => {
                const no = start + idx + 1;
                return `
          <tr>
            <td>${no}</td>
            <td>${esc(r.companyName)}</td>
            <td>${esc(r.groupName)}</td>
            <td>${r.jobCount ?? 0}</td>
            <td>${r.applicantCount ?? 0}</td>
          </tr>
        `;
            })
            .join("");
    }

    function renderCompanyTotalPager() {
        const pager = $("#ct_pager");
        if (!pager) return;

        const total = ctRowsCache.length;
        const totalPages = Math.max(1, Math.ceil(total / CT_PAGE_SIZE));

        pager.innerHTML = "";

        for (let p = 1; p <= totalPages; p++) {
            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = "pager-btn" + (p === ctPage ? " active" : "");
            btn.textContent = String(p);

            btn.addEventListener("click", () => {
                renderCompanyTotalPage(p);
                renderCompanyTotalPager();
            });

            pager.appendChild(btn);
        }
    }

    async function renderCompanyTotalStats() {
        const tbody = $("#ct_tbody");

        try {
            const { total, totalApplicants, rows } = await fetchCompanyTotalStats();

            $("#ct_total").textContent = `* 총공고수 : ${total} / 총지원자수 : ${totalApplicants}`;

            ctRowsCache = Array.isArray(rows) ? rows : [];
            ctPage = 1;

            renderCompanyTotalPage(1);
            renderCompanyTotalPager();
        } catch (e) {
            if (String(e?.message || "") === "UNAUTHORIZED") return;

            console.error(e);
            tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;">데이터를 불러오지 못했습니다.</td></tr>`;
            $("#ct_total").textContent = `* 총공고수 : 0 / 총지원자수 : 0`;

            ctRowsCache = [];
            ctPage = 1;
            renderCompanyTotalPager();
        }
    }

    // -----------------------
    // ✅ (추가) 회사별공고(월별) 통계 + 클라이언트 페이징(10개 단위)
    //  - HTML: cm_from, cm_to, cm_company, cm_total, cm_tbody, cm_pager
    //  - API : GET /api/jobs/admin/stats/company-monthly
    // -----------------------
    const CM_PAGE_SIZE = 10;
    let cmRowsCache = [];
    let cmPage = 1;

    async function fetchCompanyMonthlyStats() {
        const from = $("#cm_from")?.value || "";
        const to = $("#cm_to")?.value || "";
        const company = $("#cm_company")?.value || "";

        await ensureAdminSession();

        const qs = new URLSearchParams({ from, to, company });

        const res = await adminFetch(
            `/api/jobs/admin/stats/company-monthly?${qs.toString()}`,
            { method: "GET", headers: { Accept: "application/json" } }
        );

        if (!res.ok) throw new Error(`회사별공고(월별) 통계 조회 실패 (${res.status})`);
        return await res.json();
    }

    function renderCompanyMonthlyPage(page) {
        const tbody = $("#cm_tbody");

        const total = cmRowsCache.length;
        const totalPages = Math.max(1, Math.ceil(total / CM_PAGE_SIZE));

        cmPage = Math.min(Math.max(1, page), totalPages);

        const start = (cmPage - 1) * CM_PAGE_SIZE;
        const slice = cmRowsCache.slice(start, start + CM_PAGE_SIZE);

        // ✅ 테이블 컬럼: 번호 / 년-월 / 회사명 / 그룹명 / 공고수 / 지원자수
        if (slice.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">조회 결과가 없습니다.</td></tr>`;
            return;
        }

        tbody.innerHTML = slice
            .map((r, idx) => {
                const no = start + idx + 1;
                return `
          <tr>
            <td>${no}</td>
            <td>${esc(r.ym)}</td>
            <td>${esc(r.companyName)}</td>
            <td>${esc(r.groupName)}</td>
            <td>${r.jobCount ?? 0}</td>
            <td>${r.applicantCount ?? 0}</td>
          </tr>
        `;
            })
            .join("");
    }

    function renderCompanyMonthlyPager() {
        const pager = $("#cm_pager");
        if (!pager) return;

        const total = cmRowsCache.length;
        const totalPages = Math.max(1, Math.ceil(total / CM_PAGE_SIZE));

        pager.innerHTML = "";

        for (let p = 1; p <= totalPages; p++) {
            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = "pager-btn" + (p === cmPage ? " active" : "");
            btn.textContent = String(p);

            btn.addEventListener("click", () => {
                renderCompanyMonthlyPage(p);
                renderCompanyMonthlyPager();
            });

            pager.appendChild(btn);
        }
    }

    async function renderCompanyMonthlyStats() {
        const tbody = $("#cm_tbody");

        try {
            const { total, totalApplicants, rows } = await fetchCompanyMonthlyStats();

            $("#cm_total").textContent = `* 총공고수 : ${total} / 총지원자수 : ${totalApplicants}`;

            cmRowsCache = Array.isArray(rows) ? rows : [];
            cmPage = 1;

            renderCompanyMonthlyPage(1);
            renderCompanyMonthlyPager();
        } catch (e) {
            if (String(e?.message || "") === "UNAUTHORIZED") return;

            console.error(e);
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">데이터를 불러오지 못했습니다.</td></tr>`;
            $("#cm_total").textContent = `* 총공고수 : 0 / 총지원자수 : 0`;

            cmRowsCache = [];
            cmPage = 1;
            renderCompanyMonthlyPager();
        }
    }

    // -----------------------
    // 탭 렌더 분기
    // -----------------------
    function render(tabKey) {
        if (tabKey === "personal") return renderPersonal();
        if (tabKey === "company") return renderCompany();
        if (tabKey === "recruit-month") return renderRecruitMonthly();
        if (tabKey === "job-name") return renderJobNameStats();
        if (tabKey === "company-total") return renderCompanyTotalStats();
        if (tabKey === "company-month") return renderCompanyMonthlyStats(); // ✅ 여기만 변경
    }

    function setActiveTab(tabKey) {
        activeTab = tabKey;

        $$("#statTabs .tab").forEach((btn) =>
            btn.classList.toggle("active", btn.dataset.tab === tabKey)
        );

        $$(".tab-panel").forEach((panel) => (panel.style.display = "none"));

        const panel = $(`#panel-${tabKey}`);
        if (panel) panel.style.display = "";

        render(tabKey);
    }

    function bindEvents() {
        $$("#statTabs .tab").forEach((btn) =>
            btn.addEventListener("click", () => setActiveTab(btn.dataset.tab))
        );

        document
            .querySelector('button[data-search="personal"]')
            ?.addEventListener("click", renderPersonal);

        document
            .querySelector('button[data-search="company"]')
            ?.addEventListener("click", renderCompany);

        document
            .querySelector('button[data-search="recruit-month"]')
            ?.addEventListener("click", renderRecruitMonthly);

        document
            .querySelector('button[data-search="job-name"]')
            ?.addEventListener("click", renderJobNameStats);

        // ✅ 회사별공고 검색 버튼 연결
        document
            .querySelector('button[data-search="company-total"]')
            ?.addEventListener("click", renderCompanyTotalStats);

        // ✅ (추가) 회사별공고(월별) 검색 버튼 연결
        document
            .querySelector('button[data-search="company-month"]')
            ?.addEventListener("click", renderCompanyMonthlyStats);
    }

    function init() {
        bindEvents();
        setDefaultYearRange();
        render(activeTab);
    }

    document.addEventListener("DOMContentLoaded", init);
})();
