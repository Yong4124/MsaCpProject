document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("ADMIN_TOKEN");
    const name = localStorage.getItem("ADMIN_NAME") || "관리자";

    // ✅ 권한은 roleCode로 통일해서 저장하는 걸 권장: "SUPER_ADMIN" | "KCCI_ADMIN" | "KUSAF_ADMIN"
    const roleCode = localStorage.getItem("ADMIN_ROLE") || "";

    if (!token) {
        window.location.href = "/admin";
        return;
    }

    const welcomeEl = document.getElementById("welcomeText");
    if (welcomeEl) welcomeEl.textContent = `${name}님 환영합니다.`;

    document.getElementById("logoutBtn")?.addEventListener("click", () => {
        localStorage.removeItem("ADMIN_TOKEN");
        localStorage.removeItem("ADMIN_NAME");
        localStorage.removeItem("ADMIN_ROLE");
        window.location.href = "/admin";
    });

    const tbody = document.getElementById("adminTbody");
    let admins = [];

    // ✅ SUPER_ADMIN만 수정 페이지 링크 노출
    // (roleCode를 통일했으면 아래 한 줄로 끝)
    const isSuper =
        roleCode === "SUPER_ADMIN" ||
        roleCode === "SUPER" ||
        roleCode === "1" ||
        roleCode === "슈퍼관리자";

    function render(list) {
        if (!tbody) return;

        tbody.innerHTML = (list || [])
            .map(
                (a) => `
          <tr>
            <td>${a.no ?? ""}</td>
            <td class="col-id">
              ${
                    isSuper
                        ? `<a class="admin-id-link" href="/admin/admin_modify?id=${encodeURIComponent(
                            a.id ?? ""
                        )}">${a.id ?? ""}</a>`
                        : `<span class="admin-id-text">${a.id ?? ""}</span>`
                }
            </td>
            <td>${a.name ?? ""}</td>
            <td>${a.roleName ?? ""}</td>
          </tr>
        `
            )
            .join("");
    }

    async function loadAdmins(field = "all", keyword = "") {
        const params = new URLSearchParams();

        if (keyword && keyword.trim() !== "") {
            params.set("field", field);
            params.set("keyword", keyword.trim());
        }

        const url = `/api/admin/users${params.toString() ? `?${params.toString()}` : ""}`;

        const res = await adminFetch(url, {
            method: "GET",
            headers: {
                "Accept": "application/json",
            },
        });

        // ✅ 권한 부족 대응
        if (res.status === 403) {
            alert("권한이 없습니다.");
            return;
        }

        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            alert(err.message || "관리자 목록을 불러오지 못했습니다.");
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
    const searchType = document.getElementById("searchType"); // all | id | name
    const searchKeyword = document.getElementById("searchKeyword");
    const searchBtn = document.getElementById("searchBtn");

    async function doSearch() {
        const type = searchType?.value || "all";
        const kw = (searchKeyword?.value || "").trim();

        if (!kw) {
            await loadAdmins();
            return;
        }

        await loadAdmins(type, kw);
    }

    searchBtn?.addEventListener("click", doSearch);
    searchKeyword?.addEventListener("keydown", (e) => {
        if (e.key === "Enter") doSearch();
    });

    searchType?.addEventListener("change", () => {
        const kw = (searchKeyword?.value || "").trim();
        if (kw) doSearch();
    });

    searchKeyword?.addEventListener("input", async () => {
        const kw = (searchKeyword?.value || "").trim();
        if (!kw) await loadAdmins();
    });

    // 등록 버튼: 슈퍼관리자만 표시
    const createBtn = document.getElementById("createBtn");
    const roleHint = document.getElementById("roleHint");

    if (!isSuper) {
        if (createBtn) createBtn.style.display = "none";
        if (roleHint) {
            roleHint.style.display = "block";
            roleHint.textContent = "※ 관리자 계정 등록/수정/삭제는 슈퍼관리자만 가능합니다.";
        }
    }
});
