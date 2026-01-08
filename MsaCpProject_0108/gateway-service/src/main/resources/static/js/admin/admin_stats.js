// /js/admin_stats.js
(() => {
    let activeTab = "personal";

    const $ = (sel) => document.querySelector(sel);
    const $$ = (sel) => Array.from(document.querySelectorAll(sel));

    // ✅ 추가: 올해 1/1 ~ 12/31 자동 세팅
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

            // 비어있을 때만 세팅 (사용자가 입력한 값은 유지)
            if (fromEl && !fromEl.value) fromEl.value = start;
            if (toEl && !toEl.value) toEl.value = end;
        }
    }

    // -----------------------
    // 개인회원 월별 가입
    // -----------------------
    async function fetchPersonalMonthly() {
        const token = localStorage.getItem("ADMIN_TOKEN");
        const from = $("#p_from")?.value;
        const to = $("#p_to")?.value;

        const res = await fetch(
            `/api/personal/admin/stats/join-monthly?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`,
            {
                headers: {
                    "Content-Type": "application/json",
                    ...(token ? { Authorization: `Bearer ${token}` } : {}),
                },
            }
        );

        if (!res.ok) throw new Error("개인회원 통계 조회 실패");
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
            console.error(e);
            tbody.innerHTML = `<tr><td colspan="3" style="text-align:center;">데이터를 불러오지 못했습니다.</td></tr>`;
            $("#p_total").textContent = `* 전체 회원수 : 0`;
        }
    }

    // -----------------------
    // 기업회원 월별 가입
    // -----------------------
    async function fetchCompanyMonthly() {
        const token = localStorage.getItem("ADMIN_TOKEN");
        const from = $("#c_from")?.value;
        const to = $("#c_to")?.value;

        const res = await fetch(
            `/api/company/admin/stats/join-monthly?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`,
            {
                headers: {
                    "Content-Type": "application/json",
                    ...(token ? { Authorization: `Bearer ${token}` } : {}),
                },
            }
        );

        if (!res.ok) throw new Error("기업회원 통계 조회 실패");
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
        setDefaultYearRange(); // ✅ 여기! 추가
        render(activeTab);
    }

    document.addEventListener("DOMContentLoaded", init);
})();
