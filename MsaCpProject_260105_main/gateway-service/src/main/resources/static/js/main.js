const CONFIG = {
    jobsApi: "/api/main/jobs",
    noticesApi: "/api/main/notices",
    companyMapApi: "/api/main/companies-map"
};

/* =========================
   마이페이지 이동 (로그인 체크) - 즉시 정의
   ========================= */
window.goToMyPage = async function() {
    try {
        const response = await fetch('/api/personal/check-login', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.status === 401 || response.status === 403) {
            alert('로그인이 필요합니다.');
            window.location.href = '/login.html';
            return;
        }

        const data = await response.json();

        if (data.isLoggedIn) {
            window.location.href = '/mypage.html';
        } else {
            alert('로그인이 필요합니다.');
            window.location.href = '/login.html';
        }
    } catch (error) {
        console.error('로그인 체크 실패:', error);
        alert('로그인이 필요합니다.');
        window.location.href = '/login.html';
    }
};

function initSwiper() {
    if (typeof Swiper === "undefined") return;

    new Swiper(".mySwiper", {
        spaceBetween: 30,
        centeredSlides: true,
        loop: true,
        autoplay: { delay: 3500, disableOnInteraction: false },
        speed: 900,
        on: {
            init: function () {
                const totalSlides = this.slides.length - this.loopedSlides * 2;
                const total = Math.max(1, totalSlides);
                document.querySelector(".total").textContent = String(total).padStart(2, "0");
                document.querySelector(".current").textContent = String(this.realIndex + 1).padStart(2, "0");
            },
            slideChangeTransitionStart: function () {
                const fill = document.querySelector(".bar .fill");
                if (fill) fill.style.width = "0%";
            },
            slideChange: function () {
                const cur = document.querySelector(".current");
                if (cur) cur.textContent = String(this.realIndex + 1).padStart(2, "0");
            },
            autoplayTimeLeft: function (_s, _time, progress) {
                const fill = document.querySelector(".bar .fill");
                if (fill) fill.style.width = `${(1 - progress) * 100}%`;
            }
        }
    });
}

function goSearch() {
    const q = (document.getElementById("searchword")?.value || "").trim();
    window.location.href = q ? `/jobs?q=${encodeURIComponent(q)}` : "/jobs";
}

function renderJobCard(job) {
    const col = document.createElement("div");
    col.className = "col-3";

    const a = document.createElement("a");
    a.className = "job_link";
    a.href = job.detailUrl || "/jobs";

    const noHover = document.createElement("div");
    noHover.className = "job_nohover";

    const box = document.createElement("div");
    box.className = "box";

    const topImg = document.createElement("div");
    topImg.className = "top_img";
    if (job.thumbUrl) {
        const img = document.createElement("img");
        img.src = job.thumbUrl;
        img.alt = "";
        topImg.appendChild(img);
    }

    const boxIn = document.createElement("div");
    boxIn.className = "box_in";

    const logo = document.createElement("div");
    logo.className = "logo";
    if (job.logoUrl) {
        const img = document.createElement("img");
        img.src = job.logoUrl;
        img.alt = "";
        logo.appendChild(img);
    }

    const nm = document.createElement("div");
    nm.className = "nm";
    nm.textContent = job.companyName || "";

    const txt = document.createElement("div");
    txt.className = "txt";
    txt.textContent = job.summary || "";

    boxIn.appendChild(logo);
    boxIn.appendChild(nm);
    boxIn.appendChild(txt);

    box.appendChild(topImg);
    box.appendChild(boxIn);
    noHover.appendChild(box);

    const arrowWrap = document.createElement("div");
    arrowWrap.className = "arrow_wrap";
    const arrow = document.createElement("span");
    arrow.className = "arrow";
    arrowWrap.appendChild(arrow);
    noHover.appendChild(arrowWrap);

    const hover = document.createElement("div");
    hover.className = "job_hover_box";
    hover.innerHTML = `
    <div class="job_info">
      <div class="back_img">${job.hoverBgUrl ? `<img src="${job.hoverBgUrl}" alt="">` : ""}</div>
      <div class="jhb_info_box">
        <div class="jhb_logo">${job.logoUrl ? `<img src="${job.logoUrl}" alt="">` : ""}</div>
        <div class="jhb_ibox">
          <div class="jhb_itit">${escapeHtml(job.companyName || "")}</div>
          <div class="jhb_itxt">${escapeHtml(job.summary || "")}</div>
        </div>
      </div>
    </div>
  `;

    a.appendChild(noHover);
    a.appendChild(hover);

    col.appendChild(a);
    return col;
}

function renderNoticeCard(n) {
    const col = document.createElement("div");
    col.className = "col-3";

    const a = document.createElement("a");
    a.className = "notice_link";
    a.href = n.detailUrl || "/notices";

    const box = document.createElement("div");
    box.className = "box";

    const txt = document.createElement("div");
    txt.className = "txt";
    txt.textContent = n.title || "";

    const date = document.createElement("div");
    date.className = "date";
    date.textContent = n.date || "";

    box.appendChild(txt);
    box.appendChild(date);
    a.appendChild(box);
    col.appendChild(a);
    return col;
}

async function loadMainCards() {
    const jobWrap = document.getElementById("jobCards");
    const noticeWrap = document.getElementById("noticeCards");

    try {
        const [jobsRes, noticesRes] = await Promise.all([
            fetch(CONFIG.jobsApi, { cache: "no-store" }),
            fetch(CONFIG.noticesApi, { cache: "no-store" })
        ]);

        if (jobWrap && jobsRes.ok) {
            const jobs = await jobsRes.json();
            jobWrap.innerHTML = "";
            (Array.isArray(jobs) ? jobs : []).slice(0, 8).forEach((j) => jobWrap.appendChild(renderJobCard(j)));
        }

        if (noticeWrap && noticesRes.ok) {
            const notices = await noticesRes.json();
            noticeWrap.innerHTML = "";
            (Array.isArray(notices) ? notices : []).slice(0, 4).forEach((n) => noticeWrap.appendChild(renderNoticeCard(n)));
        }
    } catch (e) {
        console.error(e);
    }
}

function initJobHover() {
    const links = document.querySelectorAll(".job_link");
    links.forEach((a) => {
        a.addEventListener("mouseenter", function () {
            if (window.innerWidth >= 992) this.classList.add("active");
        });
        a.addEventListener("mouseleave", function () {
            if (window.innerWidth >= 992) this.classList.remove("active");
        });
    });
}

let __companyMapCache = null;

async function getCompanyMap() {
    if (__companyMapCache) return __companyMapCache;
    const res = await fetch(CONFIG.companyMapApi, { cache: "no-store" });
    if (!res.ok) throw new Error("companies-map 로드 실패");
    __companyMapCache = await res.json();
    return __companyMapCache;
}

async function jidoView(event, key) {
    event?.preventDefault?.();

    const popup = document.getElementById("jidoPopup");
    const agent = document.getElementById("companyAgent");
    if (!popup || !agent) return;

    popup.style.display = "block";
    agent.innerHTML = "<p style='padding:16px;'>로딩중...</p>";

    try {
        const map = await getCompanyMap();
        const item = map?.[key];

        if (!item) {
            agent.innerHTML = "<p style='padding:16px;'>데이터가 없습니다.</p>";
            return;
        }

        agent.innerHTML = `
      <div class="cnp_tit">${escapeHtml(item.name || "기업정보")}</div>
      <ul class="cplist" style="list-style:none;padding:0;margin:0;">
        ${item.address ? `<li><div class="th">주소</div><div class="td">${escapeHtml(item.address)}</div></li>` : ""}
        ${item.state ? `<li><div class="th">주</div><div class="td">${escapeHtml(item.state)}</div></li>` : ""}
        ${item.site ? `<li><div class="th">웹사이트</div><div class="td"><a href="${item.site}" target="_blank" rel="noreferrer">${escapeHtml(item.site)}</a></div></li>` : ""}
      </ul>
    `;
    } catch (err) {
        console.error(err);
        agent.innerHTML = "<p style='padding:16px;color:#d00;'>데이터 로드 중 오류가 발생했습니다.</p>";
    }
}

function jidoClose(event) {
    event?.preventDefault?.();
    const popup = document.getElementById("jidoPopup");
    if (popup) popup.style.display = "none";
}

function escapeHtml(str) {
    return String(str).replace(/[&<>"']/g, (m) => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#039;"
    }[m]));
}

document.addEventListener("DOMContentLoaded", () => {
    initSwiper();
    loadMainCards();

    const input = document.getElementById("searchword");
    if (input) {
        input.addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                goSearch();
            }
        });
    }

    const observer = new MutationObserver(() => initJobHover());
    const jobWrap = document.getElementById("jobCards");
    if (jobWrap) observer.observe(jobWrap, { childList: true });
});

window.goSearch = goSearch;
window.jidoView = jidoView;
window.jidoClose = jidoClose;