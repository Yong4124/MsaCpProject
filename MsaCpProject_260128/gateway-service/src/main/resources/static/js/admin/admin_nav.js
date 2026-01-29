// /js/admin_nav.js
document.addEventListener("DOMContentLoaded", () => {
    // 1) 햄버거 버튼을 헤더에 자동 삽입(각 HTML 수정 최소화)
    const topbarLeft = document.querySelector(".topbar-left");
    if (topbarLeft && !document.getElementById("navToggle")) {
        topbarLeft.insertAdjacentHTML(
            "afterbegin",
            `<button id="navToggle" class="nav-toggle" type="button" aria-label="메뉴"></button>`
        );
    }

    // 2) backdrop 자동 삽입
    if (!document.getElementById("sidebarBackdrop")) {
        document.body.insertAdjacentHTML(
            "beforeend",
            `<div id="sidebarBackdrop" class="sidebar-backdrop" aria-hidden="true"></div>`
        );
    }

    const btn = document.getElementById("navToggle");
    const backdrop = document.getElementById("sidebarBackdrop");
    const sidebar = document.querySelector(".sidebar");
    if (!btn || !backdrop || !sidebar) return;

    const close = () => document.body.classList.remove("nav-open");
    const toggle = () => document.body.classList.toggle("nav-open");

    btn.addEventListener("click", toggle);
    backdrop.addEventListener("click", close);

    // 메뉴 클릭하면 닫히게 (모바일 UX)
    sidebar.addEventListener("click", (e) => {
        if (e.target.closest("a.menu")) close();
    });

    // ESC로 닫기
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") close();
    });
});
