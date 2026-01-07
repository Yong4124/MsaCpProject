// /js/admin_stats.js
(() => {
    let activeTab = "personal";

    const $ = (sel) => document.querySelector(sel);
    const $$ = (sel) => Array.from(document.querySelectorAll(sel));

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
        return await res.json(); // { total, rows:[{ym,count}] }
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
    // ✅ 기업회원 월별 가입
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
        return await res.json(); // { total, rows:[{ym,count}] }
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

    // -----------------------
    // 공통: 탭 렌더링
    // -----------------------
    function render(tabKey) {
        if (tabKey === "personal") return renderPersonal();
        if (tabKey === "company") return renderCompany();
        // 나머지 탭(recruit-month, job-name, ...)은 필요 시 같은 패턴으로 확장
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
        // 탭 클릭
        $$("#statTabs .tab").forEach((btn) =>
            btn.addEventListener("click", () => setActiveTab(btn.dataset.tab))
        );

        // 개인회원 검색 버튼
        document
            .querySelector('button[data-search="personal"]')
            ?.addEventListener("click", renderPersonal);

        // ✅ 기업회원 검색 버튼
        document
            .querySelector('button[data-search="company"]')
            ?.addEventListener("click", renderCompany);
    }

    function init() {
        bindEvents();
        render(activeTab);
    }

    document.addEventListener("DOMContentLoaded", init);
})();
