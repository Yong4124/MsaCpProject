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
            ["cm_from", "cm_to"],
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

        // ✅ 토큰 만료 체크(확실하게)
        await ensureAdminSession();

        const res = await adminFetch(
            `/api/personal/admin/stats/join-monthly?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`,
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
            // adminFetch가 401 처리하면서 throw("UNAUTHORIZED") 했을 수 있음
            if (String(e?.message || "") === "UNAUTHORIZED") return;

            console.error(e);
            tbody.innerHTML = `<tr><td colspan="3" style="text-align:center;">데이터를 불러오지 못했습니다.</td></tr>`;
            $("#p_total").textContent = `* 전체 회원수 : 0`;
        }
    }

    // -----------------------
    // 기업회원 월별 가입
    // -----------------------
    async function fetchCompanyMonthly() {
        const from = $("#c_from")?.value || "";
        const to = $("#c_to")?.value || "";

        // ✅ 토큰 만료 체크(확실하게)
        await ensureAdminSession();

        // ✅ fetch → adminFetch
        const res = await adminFetch(
            `/api/company/admin/stats/join-monthly?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`,
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

    function render(tabKey) {
        if (tabKey === "personal") return renderPersonal();
        if (tabKey === "company") return renderCompany();
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
    }

    function init() {
        bindEvents();
        setDefaultYearRange();
        render(activeTab);
    }

    document.addEventListener("DOMContentLoaded", init);
})();
