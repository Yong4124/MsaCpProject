document.addEventListener("DOMContentLoaded", () => {
    // ✅ 로그인 토큰 체크 + 로그아웃
    const token = localStorage.getItem("ADMIN_TOKEN");
    const adminName = localStorage.getItem("ADMIN_NAME") || "관리자";

    // ✅ ADMIN_ROLE 정규화 (요구사항: "1"이면 SUPER_ADMIN으로 취급)
    const rawRole = (localStorage.getItem("ADMIN_ROLE") || "").trim(); // "1", "SUPER_ADMIN", "ROLE_SUPER_ADMIN"
    const normalizedRole = (() => {
        let r = rawRole.replace(/^ROLE_/, "");
        if (r === "1") return "SUPER_ADMIN";
        return r;
    })();

    if (!token) {
        location.replace("/admin");
        return;
    }

    // 상단 환영문구(있을 때만)
    const welcomeEl = document.getElementById("welcomeText");
    if (welcomeEl) welcomeEl.textContent = `${adminName}님 환영합니다.`;

    // 로그아웃 버튼(있을 때만)
    document.getElementById("logoutBtn")?.addEventListener("click", () => {
        localStorage.removeItem("ADMIN_TOKEN");
        localStorage.removeItem("ADMIN_NAME");
        localStorage.removeItem("ADMIN_ROLE");
        location.replace("/admin");
    });

    // ===========================
    // (기존 관리자 등록 로직)
    // ===========================
    const form = document.getElementById("adminWriteForm");
    const dupBtn = document.getElementById("dupBtn");

    let dupChecked = false;
    let lastCheckedId = "";

    const $id = document.getElementById("adminId");
    const $pw = document.getElementById("pw");
    const $pw2 = document.getElementById("pw2");
    const $name = document.getElementById("name");
    const $email = document.getElementById("email");
    const $dept = document.getElementById("dept");
    const $phone = document.getElementById("phone");

    $id?.addEventListener("input", () => {
        dupChecked = false;
        lastCheckedId = "";
    });

    // ✅ 중복확인: fetch → adminFetch
    dupBtn?.addEventListener("click", async () => {
        const id = $id.value.trim();
        if (!id) return alert("아이디를 입력하세요.");

        try {
            const res = await adminFetch(`/api/admin/users/check?id=${encodeURIComponent(id)}`, {
                method: "GET",
                headers: { Accept: "application/json" },
            });

            if (!res.ok) {
                const errText = await res.text().catch(() => "");
                try {
                    const err = JSON.parse(errText || "{}");
                    return alert(err.message || `중복확인 실패 (${res.status})`);
                } catch {
                    return alert(errText || `중복확인 실패 (${res.status})`);
                }
            }

            const data = await res.json(); // {available, message}
            alert(data.message);

            dupChecked = !!data.available;
            lastCheckedId = data.available ? id : "";
        } catch (e) {
            // 401이면 adminFetch가 이미 로그아웃/이동 처리했을 수 있음
            console.error(e);
        }
    });

    // ✅ 등록: fetch → adminFetch
    form?.addEventListener("submit", async (e) => {
        e.preventDefault();

        const id = $id.value.trim();
        const pw = $pw.value;
        const pw2 = $pw2.value;

        const name = $name.value.trim();
        const email = $email.value.trim();
        const department = $dept.value.trim();
        const phone = $phone.value.trim();
        const roleValue = document.querySelector('input[name="role"]:checked')?.value;

        if (!id || !pw || !pw2 || !name || !email || !department || !roleValue)
            return alert("필수값을 입력하세요.");
        if (pw !== pw2) return alert("비밀번호가 일치하지 않습니다.");
        if (!phone) return alert("전화번호를 입력하세요.");
        if (!department) return alert("부서는 필수입니다.");
        if (!dupChecked || lastCheckedId !== id) return alert("아이디 중복확인을 해주세요.");

        const body = { id, password: pw, name, email, department, phone, role: roleValue };

        try {
            const res = await adminFetch("/api/admin/users", {
                method: "POST",
                headers: {
                    Accept: "application/json",
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(body),
            });

            if (!res.ok) {
                const errText = await res.text().catch(() => "");
                try {
                    const err = JSON.parse(errText || "{}");
                    return alert(err.message || `등록 실패 (${res.status})`);
                } catch {
                    return alert(errText || `등록 실패 (${res.status})`);
                }
            }

            alert("등록 완료");
            window.location.href = "/admin/admin_list";
        } catch (e2) {
            console.error(e2);
        }
    });
});
