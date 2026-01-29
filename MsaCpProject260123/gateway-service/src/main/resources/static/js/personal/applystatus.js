(() => {
    "use strict";

    const ENDPOINT_MY = (page, size) => `/api/apply/my?page=${page}&size=${size}`;

    // ✅ 너네 프로젝트에 기본 로고가 있으면 그 경로로 바꿔
    const DEFAULT_LOGO = "/img/default_logo.png";

    // ✅ "이력서 보기" 버튼 이동 경로: 네 프로젝트 라우팅에 맞게 수정
    // 예: /resume/view?m110=123  또는 /resume/detail/123
    // ✅ apply 페이지는 jobId도 필요하니까 같이 넘기는 게 안전함
    const RESUME_VIEW_URL = (jobId, resumeId) =>
        `/apply?jobId=${encodeURIComponent(jobId)}&resumeId=${encodeURIComponent(resumeId)}`;


    // ✅ 공고 상세 페이지가 있으면 연결 (없으면 주석처리)
    const JOB_DETAIL_URL = (jobId) => `/jobs/detail?id=${encodeURIComponent(jobId)}`;

    const $ = (id) => document.getElementById(id);

    let state = {
        page: 0,
        size: 10,
        totalPages: 0,
        totalElements: 0,
        items: [],
        filter: "ALL",
        q: ""
    };

    function esc(s) {
        return String(s ?? "").replace(/[&<>"']/g, (m) => ({
            "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
        }[m]));
    }

    function safeText(v, fallback = "-") {
        if (v === null || v === undefined) return fallback;
        const s = String(v).trim();
        return s.length ? s : fallback;
    }

    function pickStatusKey(item) {
        // cancelStatus = "Y"면 CANCELED
        if (String(item?.cancelStatus ?? "").toUpperCase() === "Y") return "CANCELED";
        const rs = String(item?.reviewStatus ?? "").toUpperCase();
        if (rs === "SUBMITTED") return "SUBMITTED";
        // TEMP 또는 비어있으면 임시로 처리
        return "TEMP";
    }

    function statusBadge(item) {
        const key = pickStatusKey(item);
        const text = safeText(item?.statusText, key);

        if (key === "SUBMITTED") return `<span class="badge green">${esc(text)}</span>`;
        if (key === "CANCELED") return `<span class="badge red">${esc(text)}</span>`;
        return `<span class="badge blue">${esc(text)}</span>`;
    }

    function ddayBadge(item) {
        // 우선 ddayText가 있으면 사용, 없으면 closed 기준
        const ddayText = (item?.ddayText ?? "").trim();
        const closed = !!item?.closed;

        if (ddayText) {
            // D-xx / 마감 / 채용완료 / 채용마감 등
            const t = ddayText;
            if (t.startsWith("D-")) return `<span class="badge dark">${esc(t)}</span>`;
            if (t.includes("마감") || t.includes("완료")) return `<span class="badge red">${esc(t)}</span>`;
            return `<span class="badge dark">${esc(t)}</span>`;
        }

        if (closed) return `<span class="badge red">채용마감</span>`;
        return `<span class="badge dark">진행중</span>`;
    }

    function buildMetaGrid(item) {
        // 화면에 맞게 키/값 구성
        const rows = [
            ["근무형태", item?.workType],
            ["고용형태", item?.employmentType],
            ["업계", item?.industry],
            ["레벨", item?.level],
            ["경력", item?.experience],
            ["급여", item?.salaryText],
            ["근무시간", item?.workingHours],
            ["근무지", item?.location],
        ];

        return rows.map(([k, v]) => `
      <div class="meta_item">
        <div class="meta_k">${esc(k)}</div>
        <div class="meta_v">${esc(safeText(v))}</div>
      </div>
    `).join("");
    }

    function render() {
        const list = $("applyList");
        const pager = $("pager");
        const totalText = $("totalText");

        // total
        totalText.textContent = state.totalElements ? `총 ${state.totalElements}건` : "";

        // empty
        if (!state.items.length) {
            list.innerHTML = `
        <div class="empty_box">
          조회된 지원 내역이 없습니다.
        </div>
      `;
            pager.style.display = "none";
            return;
        }

        // render cards
        list.innerHTML = state.items.map((item) => {
            const logo = item?.logoPath ? item.logoPath : DEFAULT_LOGO;
            const company = safeText(item?.companyName);
            const title = safeText(item?.title);

            const resumeId = item?.resumeId;
            const jobId = item?.jobId;

            const canOpenResume = !!resumeId;
            const resumeBtnDisabled = canOpenResume ? "" : "disabled";

            return `
        <div class="apply_card">
          <div class="apply_card_top">
            <div class="apply_left">
              <div class="apply_logo">
                <img src="${esc(logo)}" alt="logo" onerror="this.src='${esc(DEFAULT_LOGO)}'">
              </div>

              <div class="apply_title_wrap">
                <div class="apply_company">${esc(company)}</div>
                <div class="apply_title">
                  <a href="${esc(JOB_DETAIL_URL(jobId))}" style="color:inherit; text-decoration:none;">
                    ${esc(title)}
                  </a>
                </div>

                <div class="badge_row">
                  ${statusBadge(item)}
                  ${ddayBadge(item)}
                </div>
              </div>
            </div>

            <div class="apply_right">
              <div class="apply_actions">
                <button type="button" class="apply_btn" ${resumeBtnDisabled}
                        data-action="resume"
                        data-resume-id="${esc(resumeId ?? "")}"
                        data-job-id="${esc(jobId ?? "")}">
                  이력서 보기
                </button>
              </div>
            </div>
          </div>

          <div class="apply_meta_grid">
            ${buildMetaGrid(item)}
          </div>
        </div>
      `;
        }).join("");

        // paging
        if (state.totalPages > 1) {
            pager.style.display = "flex";
            $("pagerInfo").textContent = `${state.page + 1} / ${state.totalPages}`;
            $("btnPrev").disabled = state.page <= 0;
            $("btnNext").disabled = state.page >= state.totalPages - 1;
        } else {
            pager.style.display = "none";
        }
    }

    function applyClientFilter(items) {
        const q = state.q.trim().toLowerCase();
        const filter = state.filter;

        return items.filter((it) => {
            const key = pickStatusKey(it);
            if (filter !== "ALL" && key !== filter) return false;

            if (q) {
                const hay = `${it?.title ?? ""} ${it?.companyName ?? ""}`.toLowerCase();
                if (!hay.includes(q)) return false;
            }
            return true;
        });
    }

    async function load(page) {
        state.page = page;

        const res = await fetch(ENDPOINT_MY(state.page, state.size), {
            method: "GET",
            credentials: "include",
            headers: { "Content-Type": "application/json" }
        });

        // 401이면 기존 패턴대로 alert
        if (res.status === 401) {
            alert("로그인 후 이용하세요.");
            location.href = "/login";
            return;
        }
        if (!res.ok) {
            const t = await res.text().catch(() => "");
            throw new Error(`API 오류: ${res.status} ${t}`);
        }

        const json = await res.json();

        // ApiResponse 래핑/비래핑 둘 다 대응
        const data = json?.data ?? json;

        const items = data?.items ?? data?.list ?? [];
        state.totalPages = Number(data?.totalPages ?? 0);
        state.totalElements = Number(data?.totalElements ?? items.length);

        // ✅ 서버 페이징 + 클라 필터(검색/상태)를 동시에 쓰면 페이지/총건수와 약간 안 맞을 수 있음
        // 일단은 "페이지 단위로 받아온 items"에서만 필터링해서 보여주도록 함 (간단/안전)
        state.items = applyClientFilter(items);

        render();
    }

    function bindEvents() {
        $("btnPrev").addEventListener("click", () => load(Math.max(0, state.page - 1)));
        $("btnNext").addEventListener("click", () => load(Math.min(state.totalPages - 1, state.page + 1)));

        $("statusFilter").addEventListener("change", (e) => {
            state.filter = e.target.value;
            load(state.page);
        });

        $("btnSearch").addEventListener("click", () => {
            state.q = $("q").value || "";
            load(state.page);
        });

        $("q").addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                state.q = $("q").value || "";
                load(state.page);
            }
        });

        $("btnReset").addEventListener("click", () => {
            state.filter = "ALL";
            state.q = "";
            $("statusFilter").value = "ALL";
            $("q").value = "";
            load(0);
        });

        // 카드 내부 버튼 이벤트(이력서 보기)
        $("applyList").addEventListener("click", (e) => {
            const btn = e.target.closest("button[data-action]");
            if (!btn) return;

            const action = btn.getAttribute("data-action");
            if (action === "resume") {
                const resumeId = btn.getAttribute("data-resume-id");
                const jobId = btn.getAttribute("data-job-id");

                if (!resumeId || !jobId) return;

                location.href = RESUME_VIEW_URL(jobId, resumeId);
            }

        });
    }

    async function init() {
        bindEvents();
        await load(0);
    }

    init().catch((err) => {
        console.error(err);
        $("applyList").innerHTML = `
      <div class="empty_box">
        지원현황을 불러오지 못했습니다.<br/>
        <small style="color:#999;">${esc(err.message)}</small>
      </div>
    `;
    });
})();
