document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("ADMIN_TOKEN");
    const name = localStorage.getItem("ADMIN_NAME") || "관리자";
    const role = localStorage.getItem("ADMIN_ROLE") || "";

    if (!token) {
        window.location.href = "/admin.html";
        return;
    }

    const welcomeEl = document.getElementById("welcomeText");
    if (welcomeEl) welcomeEl.textContent = `${name}님 환영합니다.`;

    document.getElementById("logoutBtn")?.addEventListener("click", () => {
        localStorage.removeItem("ADMIN_TOKEN");
        localStorage.removeItem("ADMIN_NAME");
        localStorage.removeItem("ADMIN_ROLE");
        window.location.href = "/admin.html";
    });

    const tbody = document.getElementById("adminTbody");
    let admins = [];

    function render(list) {
        if (!tbody) return;
        tbody.innerHTML = (list || []).map(a => `
            <tr>
              <td>${a.no ?? ""}</td>
              <td class="col-id">
                <a class="admin-id-link" href="/admin/admin_modify?id=${encodeURIComponent(a.id ?? "")}">
                    ${a.id ?? ""}
                </a>
              </td>
              <td>${a.name ?? ""}</td>
              <td>${a.roleName ?? ""}</td>
            </tr>
        `).join("");
    }

    async function loadAdmins(field = "all", keyword = "") {
        const params = new URLSearchParams();

        // ✅ keyword가 있으면 field/keyword 전달 (field=all도 포함)
        if (keyword && keyword.trim() !== "") {
            params.set("field", field);
            params.set("keyword", keyword.trim());
        }

        const url = `/api/admin/users${params.toString() ? `?${params.toString()}` : ""}`;

        const res = await fetch(url, {
            headers: {
                "Accept": "application/json",
                // "Authorization": `Bearer ${token}`
            }
        });

        if (!res.ok) {
            alert("관리자 목록을 불러오지 못했습니다.");
            return;
        }

        admins = await res.json();
        render(admins);
    }

    // 최초 로딩
    await loadAdmins();

    // =========================
    // 검색
    // =========================
    const searchType = document.getElementById("searchType");      // all | id | name
    const searchKeyword = document.getElementById("searchKeyword");
    const searchBtn = document.getElementById("searchBtn");

    async function doSearch() {
        const type = searchType?.value || "all";
        const kw = (searchKeyword?.value || "").trim();

        // ✅ 검색어 없으면 전체 목록
        if (!kw) {
            await loadAdmins();
            return;
        }

        // ✅ 핵심: type이 all이어도 서버 검색 호출!
        await loadAdmins(type, kw);
    }

    searchBtn?.addEventListener("click", doSearch);
    searchKeyword?.addEventListener("keydown", (e) => {
        if (e.key === "Enter") doSearch();
    });

    // (선택) 셀렉트 바뀌면 재검색
    searchType?.addEventListener("change", () => {
        const kw = (searchKeyword?.value || "").trim();
        if (kw) doSearch();
    });

    // (선택) 입력 지우면 전체로 복귀
    searchKeyword?.addEventListener("input", async () => {
        const kw = (searchKeyword?.value || "").trim();
        if (!kw) await loadAdmins();
    });

    // 등록 버튼: 슈퍼관리자만 표시
    const createBtn = document.getElementById("createBtn");
    const roleHint = document.getElementById("roleHint");
    const isSuper = role === "SUPER" || role === "슈퍼관리자" || role === "1";

    if (!isSuper) {
        if (createBtn) createBtn.style.display = "none";
        if (roleHint) {
            roleHint.style.display = "block";
            roleHint.textContent = "※ 관리자 계정 등록/수정/삭제는 슈퍼관리자만 가능합니다.";
        }
    }
});
