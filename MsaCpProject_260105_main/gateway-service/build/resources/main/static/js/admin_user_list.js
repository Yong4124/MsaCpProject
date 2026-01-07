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

    // ✅ [방법2] 올해 1/1 ~ 12/31 자동 세팅 (값이 비어있을 때만)
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
              <a class="link-name" href="/admin_user_detail.html?seq=${encodeURIComponent(r.no)}">
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

    // ✅ 승인여부 + 이름검색 + 기간검색 필터 적용 (전부 같이 적용)
    function applyFilter() {
        const approval = approvalSelect?.value || ""; // "" | "Y" | "N"
        const keyword = (keywordInput?.value || "").trim().toLowerCase();

        // ✅ 기간(YYYY-MM-DD)
        const from = (fromDateEl?.value || "").trim();
        const to = (toDateEl?.value || "").trim();

        let filtered = allRows;

        // 1) 승인여부 필터
        if (approval === "Y") filtered = filtered.filter((r) => r.approve === "Y");
        else if (approval === "N") filtered = filtered.filter((r) => r.approve !== "Y"); // N/null 대비

        // 2) 이름 검색(부분일치)
        if (keyword) {
            filtered = filtered.filter((r) => (r.name || "").toLowerCase().includes(keyword));
        }

        // 3) 기간(등록일) 검색
        if (from || to) {
            filtered = filtered.filter((r) => {
                const d = (r.date || "").toString().slice(0, 10); // "YYYY-MM-DD"
                if (!d) return false;

                // 문자열 비교 가능(YYYY-MM-DD)
                if (from && d < from) return false;
                if (to && d > to) return false;

                return true;
            });
        }

        render(filtered);
    }

    // ✅ 승인여부 바꾸면 즉시 필터 적용
    approvalSelect?.addEventListener("change", applyFilter);

    // ✅ 검색 버튼 클릭 시 필터 적용
    searchBtn?.addEventListener("click", applyFilter);

    // ✅ 엔터로도 검색되게
    keywordInput?.addEventListener("keydown", (e) => {
        if (e.key === "Enter") applyFilter();
    });

    // ✅ 기간 바꾸면 즉시 반영 (원치 않으면 이 2줄 삭제)
    fromDateEl?.addEventListener("change", applyFilter);
    toDateEl?.addEventListener("change", applyFilter);

    try {
        const res = await fetch("/api/personal/admin/users", {
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
        });

        if (!res.ok) throw new Error("목록 조회 실패");
        allRows = await res.json();

        // ✅ 최초 전체 렌더 (자동 세팅된 기간 포함해서 필터 적용)
        applyFilter();
    } catch (e) {
        console.error(e);
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;">데이터를 불러오지 못했습니다.</td></tr>`;
    }
});
