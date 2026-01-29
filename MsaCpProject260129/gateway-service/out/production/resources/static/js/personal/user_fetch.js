// /js/user/user_fetch.js
(function () {
    function userForceLogout(message) {
        if (message) alert(message);
        localStorage.removeItem("USER_TOKEN");
        localStorage.removeItem("USER_NAME");
        location.replace("/login"); // 너 프로젝트 로그인 경로에 맞게 변경
    }

    window.userFetch = async function userFetch(url, options = {}) {
        const token = localStorage.getItem("USER_TOKEN");
        const headers = { ...(options.headers || {}) };

        if (options.json === true) {
            headers["Content-Type"] = "application/json";
        }

        if (token) headers["Authorization"] = `Bearer ${token}`;

        const res = await fetch(url, { ...options, headers });

        if (res.status === 401) {
            userForceLogout("로그인이 만료되었습니다. 다시 로그인 해주세요.");
            throw new Error("UNAUTHORIZED");
        }

        return res;
    };

    window.userForceLogout = userForceLogout;
})();
