document.addEventListener("DOMContentLoaded", async () => {
    // ✅ 로그인 토큰 체크 + 로그아웃
    const token = localStorage.getItem("ADMIN_TOKEN");
    const adminName = localStorage.getItem("ADMIN_NAME") || "관리자";
    const myRole = localStorage.getItem("ADMIN_ROLE") || "";

    // ✅ 슈퍼관리자 판별
    const isSuper =
        myRole === "SUPER_ADMIN" || myRole === "SUPER" || myRole === "슈퍼관리자" || myRole === "1";

    if (!token) {
        location.replace("/admin");
        return;
    }

    const welcomeEl = document.getElementById("welcomeText");
    if (welcomeEl) welcomeEl.textContent = `${adminName}님 환영합니다.`;

    document.getElementById("logoutBtn")?.addEventListener("click", () => {
        localStorage.removeItem("ADMIN_TOKEN");
        localStorage.removeItem("ADMIN_NAME");
        localStorage.removeItem("ADMIN_ROLE");
        location.replace("/admin");
    });

    // ===========================
    // 관리자 상세/수정/삭제
    // ===========================
    const params = new URLSearchParams(location.search);
    const loginId = params.get("id");

    if (!loginId) {
        alert("잘못된 접근입니다.");
        location.href = "/admin/admin_list";
        return;
    }

    const $viewId = document.getElementById("viewId");
    const $pw = document.getElementById("pw");
    const $pw2 = document.getElementById("pw2");
    const $name = document.getElementById("name");
    const $email = document.getElementById("email");
    const $dept = document.getElementById("dept");
    const $phone = document.getElementById("phone");

    const $btnList = document.getElementById("btnList");
    const $btnUpdate = document.getElementById("btnUpdate");
    const $btnDelete = document.getElementById("btnDelete");

    // ✅ 슈퍼관리자 아니면 수정/삭제 UI 차단 (UX)
    if (!isSuper) {
        if ($btnUpdate) $btnUpdate.style.display = "none";
        if ($btnDelete) $btnDelete.style.display = "none";

        document.querySelectorAll('input[name="role"]').forEach((el) => {
            el.disabled = true;
        });
    }

    async function loadDetail() {
        // ✅ fetch → adminFetch
        const res = await adminFetch(`/api/admin/users/${encodeURIComponent(loginId)}`, {
            method: "GET",
            headers: { Accept: "application/json" },
        });

        if (!res.ok) {
            // 401은 adminFetch에서 이미 처리(로그아웃)됨
            alert("상세 정보를 불러오지 못했습니다.");
            location.href = "/admin/admin_list";
            return;
        }

        const data = await res.json();

        $viewId.textContent = data.id;
        $name.value = data.name ?? "";
        $email.value = data.email ?? "";
        $dept.value = data.department ?? "";
        $phone.value = data.phone ?? "";

        const radio = document.querySelector(`input[name="role"][value="${data.role}"]`);
        if (radio) radio.checked = true;
    }

    await loadDetail();

    $btnList?.addEventListener("click", () => {
        location.href = "/admin/admin_list";
    });

    // ===========================
    // 수정 (SUPER만)
    // ===========================
    $btnUpdate?.addEventListener("click", async () => {
        if (!isSuper) {
            alert("슈퍼관리자만 수정할 수 있습니다.");
            return;
        }

        const pw = $pw.value;
        const pw2 = $pw2.value;

        if ((pw || pw2) && pw !== pw2) {
            alert("비밀번호가 일치하지 않습니다.");
            return;
        }

        const selectedRole = document.querySelector('input[name="role"]:checked')?.value;
        if (!selectedRole) return alert("권한을 선택하세요.");

        const body = {
            password: pw ? pw : null,
            name: $name.value.trim(),
            email: $email.value.trim(),
            department: $dept.value.trim(),
            phone: $phone.value.trim(),
            role: selectedRole,
        };

        // ✅ fetch → adminFetch
        const res = await adminFetch(`/api/admin/users/${encodeURIComponent(loginId)}`, {
            method: "PUT",
            headers: {
                Accept: "application/json",
                "Content-Type": "application/json",
            },
            body: JSON.stringify(body),
        });

        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            alert(err.message || "수정 실패");
            return;
        }

        alert("수정 완료");
        location.href = "/admin/admin_list";
    });

    // ===========================
    // 삭제 (SUPER만)
    // ===========================
    $btnDelete?.addEventListener("click", async () => {
        if (!isSuper) {
            alert("슈퍼관리자만 삭제할 수 있습니다.");
            return;
        }

        if (!confirm("정말 삭제하시겠습니까?")) return;

        // ✅ fetch → adminFetch
        const res = await adminFetch(`/api/admin/users/${encodeURIComponent(loginId)}`, {
            method: "DELETE",
            headers: { Accept: "application/json" },
        });

        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            alert(err.message || "삭제 실패");
            return;
        }

        alert("삭제 완료");
        location.href = "/admin/admin_list";
    });
});
