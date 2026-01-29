const API_BASE = ""; // 같은 도메인이면 ""
const LOGIN_URL = `${API_BASE}/api/admin/login`;

// ✅ 로그인 성공 후 3번째 이미지 스타일 화면으로 이동
const REDIRECT_URL = "/admin/user_list";

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("loginForm");
    const userIdEl = document.getElementById("userId");
    const passwordEl = document.getElementById("password");
    const btn = document.getElementById("loginBtn");
    const msg = document.getElementById("message");

    const setMessage = (text, ok = false) => {
        msg.textContent = text || "";
        msg.classList.toggle("ok", !!ok);
    };

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        setMessage("");

        const userId = userIdEl.value.trim();
        const password = passwordEl.value;

        if (!userId) return setMessage("아이디를 입력하세요.");
        if (!password) return setMessage("비밀번호를 입력하세요.");

        btn.disabled = true;

        try {
            const res = await fetch(LOGIN_URL, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ id: userId, password }),
            });

            const data = await res.json().catch(() => ({}));

            if (!res.ok) {
                setMessage(data?.message || `로그인 실패 (HTTP ${res.status})`);
                return;
            }

            // ✅ 토큰/권한 저장
            localStorage.setItem("ADMIN_TOKEN", data.token);
            localStorage.setItem("ADMIN_NAME", data.name || "관리자");
            localStorage.setItem("ADMIN_ROLE", data.role || "");

            setMessage("로그인 성공", true);
            window.location.href = REDIRECT_URL;
        } catch (err) {
            console.error(err);
            setMessage("네트워크 오류가 발생했습니다.");
        } finally {
            btn.disabled = false;
        }
    });
});
