// /js/admin/admin_fetch.js
(function () {
    // ✅ 관리자 페이지에서만 사용하도록 강제 (실수 방지)
    const isAdminPage = location.pathname.startsWith("/admin");
    if (!isAdminPage) {
        // admin_fetch.js가 메인 페이지에 실수로 포함되면 바로 막음
        window.adminFetch = async () => {
            throw new Error("adminFetch는 /admin 경로에서만 사용할 수 있습니다.");
        };
        return;
    }

    function adminForceLogout(message) {
        if (message) alert(message);
        localStorage.removeItem("ADMIN_TOKEN");
        localStorage.removeItem("ADMIN_NAME");
        localStorage.removeItem("ADMIN_ROLE");
        location.replace("/admin");
    }

    window.adminFetch = async function adminFetch(url, options = {}) {
        const token = localStorage.getItem("ADMIN_TOKEN");
        const headers = { ...(options.headers || {}) };

        // Content-Type은 GET에서 불필요할 수 있어 옵션으로만 처리
        if (options.json === true) {
            headers["Content-Type"] = "application/json";
        }

        if (token) headers["Authorization"] = `Bearer ${token}`;

        const res = await fetch(url, { ...options, headers });

        // ✅ 만료/무효
        if (res.status === 401) {
            adminForceLogout("로그인이 만료되었습니다. 다시 로그인 해주세요.");
            throw new Error("UNAUTHORIZED");
        }

        return res;
    };

    // 필요하면 외부에서도 쓰게 노출
    window.adminForceLogout = adminForceLogout;
})();
