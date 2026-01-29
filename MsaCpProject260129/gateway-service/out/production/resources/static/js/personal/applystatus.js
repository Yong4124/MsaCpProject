(() => {
    "use strict";

    const ENDPOINT_MY = (page, size) => `/api/apply/my?page=${page}&size=${size}`;
    const DEFAULT_LOGO = "/img/default_logo.png";
    const RESUME_VIEW_URL = (jobId, resumeId) => `/apply?jobId=${encodeURIComponent(jobId)}&resumeId=${encodeURIComponent(resumeId)}`;
    const JOB_DETAIL_URL = (jobId) => `/jobs/detail?id=${encodeURIComponent(jobId)}`;
    const $ = (id) => document.getElementById(id);

    let state = {
        page: 0, size: 10, totalPages: 0, totalElements: 0, items: [], filter: "ALL", q: ""
    };

    function esc(s) {
        return String(s ?? "").replace(/[&<>"']/g, m => ({
            "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
        }[m]));
    }

    function safeText(v, fallback = "-") {
        if (v === null || v === undefined) return fallback;
        const s = String(v).trim();
        return s.length ? s : fallback;
    }

    function pickStatusKey(item) {
        if (String(item?.cancelStatus ?? "").toUpperCase() === "Y") return "CANCELED";
        if (String(item?.reviewStatus ?? "").toUpperCase() === "SUBMITTED") return "SUBMITTED";
        return "TEMP";
    }

    function getStatusButton(item, resumeId, jobId, canOpenResume) {
        const statusKey = pickStatusKey(item);

        // 제출완료된 경우 - 주황색 버튼으로 표시
        if (statusKey === "SUBMITTED") {
            return `
                <div class="link">
                    <button type="button" 
                            class="btn-complete mar" 
                            style="background: #ff6a00; color: #fff; border-color: #ff6a00; cursor: default;">
                        제출완료
                    </button>
                </div>
            `;
        }

        // 임시저장 또는 기타 상태 - 이력서 보기 버튼 (수정 가능)
        return `
            <div class="link">
                <button type="button" 
                        class="btn-submit mar" 
                        ${canOpenResume ? '' : 'disabled style="opacity:0.5; cursor:not-allowed;"'} 
                        data-action="resume" 
                        data-resume-id="${esc(resumeId ?? '')}" 
                        data-job-id="${esc(jobId ?? '')}">
                    이력서 보기
                </button>
            </div>
        `;
    }

    function render() {
        const list = $("applyList");
        const pager = $("pager");
        $("totalText").textContent = state.totalElements ? `총 ${state.totalElements}건` : "";

        if (!state.items.length) {
            list.innerHTML = '<li style="text-align:center; padding:40px;">조회된 지원 내역이 없습니다.</li>';
            pager.style.display = "none";
            return;
        }

        list.innerHTML = '';
        state.items.forEach(item => {
            const li = document.createElement('li');
            const logoSrc = item?.logoPath || DEFAULT_LOGO;
            const resumeId = item?.resumeId;
            const jobId = item?.jobId;
            const canOpenResume = !!resumeId;

            li.innerHTML = `
                <div class="box">
                    <div class="img">
                        <img src="${esc(logoSrc)}" alt="기업 로고" 
                             onerror="this.onerror=null; this.src='${esc(DEFAULT_LOGO)}';">
                    </div>
                    
                    <div class="job_linfo">
                        <div class="ji_tit">
                            <a href="${esc(JOB_DETAIL_URL(jobId))}" style="cursor:pointer; color:inherit;">
                                ${esc(safeText(item?.title, '공고 제목'))}
                            </a>
                        </div>
                        
                        <div class="ji_linfo">
                            <div class="item">
                                <div class="th">직업유형</div>
                                <div class="td">${esc(safeText(item?.workType))}</div>
                            </div>
                            <div class="item">
                                <div class="th">고용형태</div>
                                <div class="td">${esc(safeText(item?.employmentType))}</div>
                            </div>
                            <div class="item">
                                <div class="th">업계</div>
                                <div class="td">${esc(safeText(item?.industry))}</div>
                            </div>
                            <div class="item">
                                <div class="th">직급</div>
                                <div class="td">${esc(safeText(item?.level))}</div>
                            </div>
                            <div class="item">
                                <div class="th">경력</div>
                                <div class="td">${esc(safeText(item?.experience))}</div>
                            </div>
                            <div class="item">
                                <div class="th">기본급</div>
                                <div class="td">${esc(safeText(item?.salaryText))}</div>
                            </div>
                            <div class="item">
                                <div class="th">근무시간</div>
                                <div class="td">${esc(safeText(item?.workingHours))}</div>
                            </div>
                            <div class="item full">
                                <div class="th">근무처</div>
                                <div class="td">${esc(safeText(item?.location))}</div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="job_link">
                        ${getStatusButton(item, resumeId, jobId, canOpenResume)}
                        <div class="date" style="margin-top: 24px; margin-bottom: 23px;">
                            <div class="th">마감일</div>
                            <div class="td">${esc(safeText(item?.ddayText, '진행중'))}</div>
                        </div>
                        <div class="link">
                            <button type="button" 
                                    class="btn-keep mar" 
                                    onclick="window.location.href='${esc(JOB_DETAIL_URL(jobId))}'">
                                공고 보기
                            </button>
                        </div>
                    </div>
                </div>
            `;
            list.appendChild(li);
        });

        if (state.totalPages > 1) {
            pager.style.display = "block";
            $("pagerInfo").textContent = `${state.page + 1} / ${state.totalPages}`;
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
            if (q && !`${it?.title} ${it?.companyName}`.toLowerCase().includes(q)) return false;
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

        if (res.status === 401) {
            alert("로그인 후 이용하세요.");
            location.href = "/login";
            return;
        }

        if (!res.ok) throw new Error(`API 오류: ${res.status}`);

        const json = await res.json();
        const data = json?.data ?? json;
        const rawItems = data?.items ?? data?.list ?? [];

        // 중복 제거 필터링
        const submittedJobIds = new Set();
        rawItems.forEach(it => {
            if (it.reviewStatus === 'SUBMITTED' && it.jobId > 0) {
                submittedJobIds.add(it.jobId);
            }
        });

        const deduplicatedItems = rawItems.filter(it => {
            const isTemp = (it.reviewStatus === 'TEMP' || !it.reviewStatus);
            if (isTemp && it.jobId > 0 && submittedJobIds.has(it.jobId)) {
                return false;
            }
            return true;
        });

        state.totalPages = Number(data?.totalPages ?? 0);
        state.totalElements = Number(data?.totalElements ?? deduplicatedItems.length);
        state.items = applyClientFilter(deduplicatedItems);
        render();
    }

    function bindEvents() {
        $("btnPrev").addEventListener("click", () => {
            if (state.page > 0) load(state.page - 1);
        });

        $("btnNext").addEventListener("click", () => {
            if (state.page < state.totalPages - 1) load(state.page + 1);
        });

        $("statusFilter").addEventListener("change", (e) => {
            state.filter = e.target.value;
            load(0);
        });

        $("btnSearch").addEventListener("click", () => {
            state.q = $("q").value || "";
            load(0);
        });

        $("q").addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                state.q = $("q").value || "";
                load(0);
            }
        });

        $("btnReset").addEventListener("click", () => {
            state.filter = "ALL";
            state.q = "";
            $("statusFilter").value = "ALL";
            $("q").value = "";
            load(0);
        });

        $("applyList").addEventListener("click", (e) => {
            const btn = e.target.closest("button[data-action]");
            if (!btn) return;
            if (btn.getAttribute("data-action") === "resume") {
                const rId = btn.getAttribute("data-resume-id");
                const jId = btn.getAttribute("data-job-id");
                if (rId && jId) location.href = RESUME_VIEW_URL(jId, rId);
            }
        });
    }

    async function init() {
        bindEvents();
        await load(0);
    }

    init();
})();