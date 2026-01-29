(function () {
    const API = "/api/jobs"; // gateway에서 /api → jobs-service 라우팅되어야 함

    // static/img/default_logo.png, static/img/sub/icon_fav.png 가 있어야 함
    const DEFAULT_LOGO = "/img/default_logo.png";
    const FAV_ICON = "/img/sub/icon_fav.png";

    function escapeHtml(v) {
        return String(v ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    // 백엔드 파라미터명(JobService 시그니처)에 맞춤
    function buildQuery() {
        const page = Math.max((Number(document.getElementById("pageno")?.value) || 1) - 1, 0);
        const size = 10;

        const markOption = document.getElementById("markoption")?.value || "";
        const groupCompany = document.getElementById("searchgwfield")?.value || "";
        const searchField = document.getElementById("searchfield")?.value || "ALL";
        const searchWord = document.getElementById("searchword")?.value || "";
        const sortOption = document.getElementById("sortoption")?.value || "START_DATE";

        const sp = new URLSearchParams();
        sp.set("page", String(page));
        sp.set("size", String(size));
        if (markOption) sp.set("markOption", markOption);
        if (groupCompany) sp.set("groupCompany", groupCompany);
        if (searchField) sp.set("searchField", searchField);
        if (searchWord) sp.set("searchWord", searchWord);
        if (sortOption) sp.set("sortOption", sortOption);

        return sp.toString();
    }

    async function loadJobs() {
        const listEl = document.getElementById("job_list");
        const pageEl = document.getElementById("page_nation");
        if (!listEl || !pageEl) return;

        listEl.innerHTML = "";
        pageEl.innerHTML = "";

        const url = `${API}?${buildQuery()}`;

        try {
            const res = await fetch(url, { headers: { "Accept": "application/json" } });
            if (!res.ok) {
                listEl.innerHTML = `<li><div style="padding:20px;color:#666;">불러오기 실패 (HTTP ${res.status})</div></li>`;
                return;
            }

            const data = await res.json();
            const jobs = data.content || [];
            renderList(jobs);
            renderPaging((data.number ?? 0) + 1, data.totalPages ?? 1);
        } catch (e) {
            listEl.innerHTML = `<li><div style="padding:20px;color:#c00;">ERROR: ${escapeHtml(e.message)}</div></li>`;
        }
    }

    // “원본 카드 구조(디자인)” 그대로 생성
    function renderList(jobs) {
        const listEl = document.getElementById("job_list");
        if (!jobs.length) {
            listEl.innerHTML = `<li><div style="padding:30px;color:#666;">표시할 채용공고가 없습니다.</div></li>`;
            return;
        }

        const html = jobs.map(job => {
            const id = job.id ?? job.nKey ?? job.seqNo ?? job.seq_no ?? "";
            const isMark = (job.marked === true || job.isMark === "Y" || job.IS_MARK === "Y") ? "Y" : "N";

            const title = job.title ?? job.jobTitle ?? job.noticeTitle ?? "";
            const rawLogo = job.logoUrl ?? job.logo ?? job.companyLogo ?? job.logo_path ?? "";
            const logoSrc = rawLogo ? rawLogo : DEFAULT_LOGO;

            const closed =
                job.closed === true ||
                job.isClosed === true ||
                job.status === "CLOSED" ||
                job.status === "마감";

            const workType = job.workType ?? job.work_style ?? "";
            const empType = job.employmentType ?? job.hireType ?? "";
            const jobType = job.jobCategory ?? job.position ?? job.jobType ?? "";
            const industry = job.industry ?? job.bizType ?? "";
            const level = job.level ?? job.rank ?? job.jobLevel ?? "";
            const exp = job.experience ?? job.career ?? "";
            const salary = job.salaryText ?? job.salary ?? job.basePay ?? "";
            const hours = job.workingHours ?? job.workHours ?? job.workTime ?? "";
            const location = job.location ?? job.jobLocation ?? job.workPlace ?? "";

            const rawDeadline = job.deadlineText ?? job.dDayText ?? job.dday ?? job.dDay ?? "";
            let deadlineText = "";
            if (rawDeadline !== "" && rawDeadline != null) {
                const s = String(rawDeadline).trim();
                deadlineText = s.startsWith("D-") ? s : `D-${s}`;
            }

            const favHtml = (isMark === "Y")
                ? `<div class="fav"><img src="${FAV_ICON}" alt="관심기업"></div>`
                : ``;

            const btnHtml = closed
                ? `<button type="button" class="btn-cancel">공고마감</button>`
                : `<button type="button" class="btn-submit mar" onclick="goApply('${escapeHtml(id)}');">지원하기</button>`;

            const deadlineHtml = deadlineText
                ? `<div class="date"><div class="th">마감일</div><div class="td">${escapeHtml(deadlineText)}</div></div>`
                : ``;

            return `
<li>
  <div class="box">
    <div class="img">
      ${favHtml}
      <img src="${escapeHtml(logoSrc)}" onerror="this.src='${DEFAULT_LOGO}'" alt="기업 로고">
    </div>

    <div class="job_linfo">
      <div class="ji_tit">
        <a href="javascript:goView('${escapeHtml(id)}','${escapeHtml(isMark)}');" style="cursor:pointer; color:inherit;">
          ${escapeHtml(title)}
        </a>
      </div>

      <div class="ji_linfo">
        ${item("직업유형", workType)}
        ${item("고용형태", empType)}
        ${item("직종", jobType)}
        ${item("업계", industry)}
        ${item("직급", level)}
        ${item("경력", exp)}
        ${item("기본급", salary)}
        ${item("근무시간", hours)}
        ${itemFull("근무처", location)}
      </div>
    </div>

    <div class="job_link">
      <div class="link">${btnHtml}</div>
      ${deadlineHtml}
    </div>
  </div>
</li>`;
        }).join("");

        listEl.innerHTML = html;
    }

    function item(th, td) {
        return `
<div class="item">
  <div class="th">${escapeHtml(th)}</div>
  <div class="td">${escapeHtml(td)}</div>
</div>`;
    }

    function itemFull(th, td) {
        return `
<div class="item full">
  <div class="th">${escapeHtml(th)}</div>
  <div class="td">${escapeHtml(td)}</div>
</div>`;
    }

    function renderPaging(current, total) {
        const pageEl = document.getElementById("page_nation");
        if (total <= 1) {
            pageEl.innerHTML = `<a href="#none" class="active">1</a>`;
            return;
        }

        let html = "";
        for (let i = 1; i <= total; i++) {
            html += (i === current)
                ? `<a href="#none" class="active">${i}</a>`
                : `<a href="javascript:page('${i}');">${i}</a>`;
        }
        pageEl.innerHTML = html;
    }

    // HTML이 이 이름을 쓰고 있음
    window.goSearch = function () {
        document.getElementById("pageno").value = "1";
        loadJobs();
    };

    window.page = function (num) {
        document.getElementById("pageno").value = String(num);
        loadJobs();
    };

    window.goApply = async function (id) {
        try {
            const res = await fetch("/api/personal/check-login", { credentials: "include" });

            if (res.status === 401) {
                alert("로그인 후 이용하세요.");
                location.href = "/login";
                return;
            }

            // 로그인 OK면 지원서 페이지 이동
            location.href = "/apply?jobId=" + encodeURIComponent(id);
        } catch (e) {
            // 서버 확인 실패 시에도 안전하게 로그인으로 보냄(선택)
            alert("로그인 후 이용하세요.");
            location.href = "/login";
        }
    };

    window.goView = function (nKey, isMark) {
        alert("상세보기: " + nKey + " (IS_MARK=" + isMark + ")");
    };

    window.addEventListener("load", () => loadJobs());
})();
