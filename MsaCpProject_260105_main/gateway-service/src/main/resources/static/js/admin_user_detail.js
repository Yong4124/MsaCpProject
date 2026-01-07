document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("ADMIN_TOKEN");

    // ✅ 토큰 없으면 /admin(로그인 화면)으로
    if (!token) {
        location.replace("/admin");
        return;
    }

    // ✅ 로그아웃 버튼
    document.getElementById("logoutBtn")?.addEventListener("click", () => {
        localStorage.removeItem("ADMIN_TOKEN");
        localStorage.removeItem("ADMIN_NAME");
        localStorage.removeItem("ADMIN_ROLE");
        location.replace("/admin");
    });

    const params = new URLSearchParams(location.search);
    const seq = params.get("seq");
    if (!seq) {
        alert("잘못된 접근입니다.");
        location.href = "/admin/user_list";
        return;
    }

    // 목록 버튼
    document.getElementById("btnBack")?.addEventListener("click", () => {
        location.href = "/admin/user_list";
    });

    // 코드값 -> 라벨
    const genderLabel = (g) => ({ M:"남성", F:"여성", O:"기타", N:"무응답" }[g] || g);
    const residenceLabel = (r) => ({ K:"대한민국", U:"미국", O:"기타" }[r] || r);

    // 1건 조회
    const res = await fetch(`/api/personal/admin/users/${seq}`, {
        headers: { "Authorization": `Bearer ${token}` }
    });

    if (!res.ok) {
        alert("회원 정보를 불러오지 못했습니다.");
        return;
    }

    const d = await res.json();

    // 화면 바인딩
    document.getElementById("approvalYn").checked = (d.approvalYn === "Y");
    document.getElementById("loginId").value = d.loginId ?? "";
    document.getElementById("name").value = d.name ?? "";
    document.getElementById("birthDate").value = (d.birthDate ?? "").toString().slice(0,10);
    document.getElementById("gender").value = genderLabel(d.gender);
    document.getElementById("email").value = d.email ?? "";
    document.getElementById("residence").value = residenceLabel(d.residence);
    document.getElementById("lastRank").value = d.lastRank ?? "";

    document.getElementById("serviceBranch").value = d.serviceBranch ?? "";
    document.getElementById("serviceCategory").value = d.serviceCategory ?? "";
    document.getElementById("serviceStation").value = d.serviceStation ?? "";
    document.getElementById("serviceYear").value = d.serviceYear ?? "";
    document.getElementById("unitPosition").value = d.unitPosition ?? "";

    // 저장(승인여부만 변경)
    document.getElementById("btnSave")?.addEventListener("click", async () => {
        const approvalYn = document.getElementById("approvalYn").checked ? "Y" : "N";

        const saveRes = await fetch(`/api/personal/admin/users/${seq}/approval`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ approvalYn })
        });

        if (!saveRes.ok) {
            alert("저장 실패");
            return;
        }
        alert("저장 완료");
    });
});
