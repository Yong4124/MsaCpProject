document.addEventListener("DOMContentLoaded", async () => {
    // ✅ 로그인 토큰 체크 + 로그아웃
    const token = localStorage.getItem("ADMIN_TOKEN");
    const adminName = localStorage.getItem("ADMIN_NAME") || "관리자";

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
    // (기존 관리자 상세/수정/삭제 로직)
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

    async function loadDetail() {
        const res = await fetch(`/api/admin/users/${encodeURIComponent(loginId)}`, {
            headers: {
                "Accept": "application/json",
                // ✅ 백엔드가 토큰 검증하면 필요
                "Authorization": `Bearer ${token}`
            }
        });

        if (!res.ok) {
            alert("상세 정보를 불러오지 못했습니다.");
            location.href = "/admin/admin_list";
            return;
        }

        const data = await res.json(); // { id,name,email,department,phone,role }

        $viewId.textContent = data.id;
        $name.value = data.name ?? "";
        $email.value = data.email ?? "";
        $dept.value = data.department ?? "";
        $phone.value = data.phone ?? "";

        const radio = document.querySelector(`input[name="role"][value="${data.role}"]`);
        if (radio) radio.checked = true;
    }

    await loadDetail();

    document.getElementById("btnList")?.addEventListener("click", () => {
        location.href = "/admin/admin_list";
    });

    document.getElementById("btnUpdate")?.addEventListener("click", async () => {
        const pw = $pw.value;
        const pw2 = $pw2.value;

        if ((pw || pw2) && pw !== pw2) {
            alert("비밀번호가 일치하지 않습니다.");
            return;
        }

        const role = document.querySelector('input[name="role"]:checked')?.value;
        if (!role) return alert("권한을 선택하세요.");

        const body = {
            password: pw ? pw : null,
            name: $name.value.trim(),
            email: $email.value.trim(),
            department: $dept.value.trim(),
            phone: $phone.value.trim(),
            role
        };

        const res = await fetch(`/api/admin/users/${encodeURIComponent(loginId)}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json",
                // ✅ 백엔드가 토큰 검증하면 필요
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(body)
        });

        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            alert(err.message || "수정 실패");
            return;
        }

        alert("수정 완료");
        location.href = "/admin/admin_list";
    });

    document.getElementById("btnDelete")?.addEventListener("click", async () => {
        if (!confirm("정말 삭제하시겠습니까?")) return;

        const res = await fetch(`/api/admin/users/${encodeURIComponent(loginId)}`, {
            method: "DELETE",
            headers: {
                "Accept": "application/json",
                // ✅ 백엔드가 토큰 검증하면 필요
                "Authorization": `Bearer ${token}`
            }
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
