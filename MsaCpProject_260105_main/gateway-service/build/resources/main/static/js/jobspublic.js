// ===== 전역 변수 =====
let allJobs = [];
let currentPage = 1;
const itemsPerPage = 5;

// ===== 공통 유틸리티 =====
const getUrlParam = (key) => new URLSearchParams(window.location.search).get(key);

const setText = (id, value) => {
    const el = document.getElementById(id);
    if (el) el.textContent = value || '-';
};

const setHTML = (id, value) => {
    const el = document.getElementById(id);
    if (el) el.innerHTML = (value || '내용 없음').replace(/\n/g, '<br>');
};

// D-Day 계산
const getDday = (dateString) => {
    if (!dateString) return '상시채용';

    const endDate = new Date(dateString);
    const now = new Date();

    if (endDate < now) return '마감';

    const diff = Math.ceil((endDate - now) / (1000 * 60 * 60 * 24));
    return `D-${diff}`;
};

// ===== API 호출 함수 =====
const api = {
    async get(url) {
        const res = await fetch(url);
        if (!res.ok) throw new Error(`API 오류: ${res.status}`);
        return res.json();
    },

    async post(url, data) {
        const res = await fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        });
        if (!res.ok) throw new Error(`API 오류: ${res.status}`);
        return res.json();
    }
};

// ===== 목록 렌더링 =====
function renderPublicJobs(jobs) {
    const ul = document.querySelector('.job_list');
    if (!ul) return;

    if (!jobs.length) {
        ul.innerHTML = '<li style="text-align:center; padding:100px 0; width:100%;">검색 결과가 없습니다.</li>';
        renderPagination(0);
        return;
    }

    // 페이지네이션 계산
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const paginatedJobs = jobs.slice(startIndex, endIndex);

    ul.innerHTML = '';
    paginatedJobs.forEach(job => {
        const title = job.title || '제목 없음';
        const firstWord = title.split(' ')[0];
        const endDate = job.endDate;
        const dDayText = getDday(endDate);

        // ✅ 관리자가 마감한 공고 체크 추가
        const isClosedByAdmin = (job.closeYn === 'Y' || job.closeYn === 'y');
        const isClosedByDate = dDayText === '마감';
        const isClosed = isClosedByAdmin || isClosedByDate;

        const li = document.createElement('li');
        li.className = 'job_item';
        li.innerHTML = `
            <div class="company_logo">
                <img src="/images/logos/${firstWord}.png" 
                     onerror="this.onerror=null; this.src='/images/default_logo.png';"
                     alt="로고">
            </div>
            
            <div class="job_info">
                <div class="title">
                    <a href="/jobs/detail?id=${job.id}">${title}</a>
                </div>
                
                <div class="info_grid_layout">
                    <span class="info_label">직업유형</span><span class="info_val">${job.jobForm || '-'}</span>
                    <span class="info_label">고용형태</span><span class="info_val">${job.jobType || '-'}</span>
                    <span class="info_label">직종</span><span class="info_val">${job.jobCategory || '-'}</span>
                    <span class="info_label">업계</span><span class="info_val">${job.industry || '-'}</span>
                    <span class="info_label">직급</span><span class="info_val">${job.roleLevel || '-'}</span>
                    <span class="info_label">경력</span><span class="info_val">${job.experience || '-'}</span>
                    <span class="info_label">기본급</span><span class="info_val">${job.baseSalary || '-'}</span>
                    <span class="info_label">근무시간</span><span class="info_val">${job.workTime || '-'}</span>
                    <span class="info_label">근무처</span>
                    <span class="info_val" style="grid-column: span 3;">${job.workLocation || '-'}</span>
                </div>
            </div>

            <div class="btn-flex-right column">
                ${isClosed ?
            '<button class="btn-common btn-gray" disabled>공고마감</button>' :
            `<button class="btn-common btn-blue" onclick="applyJob('${job.id}')">지원하기</button>
                     <div class="deadline_text"><span>마감일</span> <strong>${dDayText}</strong></div>`
        }
            </div>
        `;
        ul.appendChild(li);
    });
    renderPagination(jobs.length);
}

// ===== 페이지네이션 렌더링 =====
function renderPagination(totalItems) {
    const pagination = document.getElementById('pagination');
    if (!pagination) return;

    const totalPages = Math.ceil(totalItems / itemsPerPage);

    if (totalPages <= 1) {
        pagination.style.display = 'none';
        return;
    }

    pagination.style.display = 'flex';
    let html = '';

    // 이전 버튼
    if (currentPage > 1) {
        html += `<a href="javascript:goToPage(${currentPage - 1})">‹</a>`;
    }

    // 페이지 번호 (최대 10개만 표시)
    const maxVisible = 10;
    let startPage = Math.max(1, currentPage - Math.floor(maxVisible / 2));
    let endPage = Math.min(totalPages, startPage + maxVisible - 1);

    if (endPage - startPage + 1 < maxVisible) {
        startPage = Math.max(1, endPage - maxVisible + 1);
    }

    // 첫 페이지
    if (startPage > 1) {
        html += `<a href="javascript:goToPage(1)">1</a>`;
        if (startPage > 2) {
            html += `<span>...</span>`;
        }
    }

    // 페이지 번호
    for (let i = startPage; i <= endPage; i++) {
        if (i === currentPage) {
            html += `<span class="active">${i}</span>`;
        } else {
            html += `<a href="javascript:goToPage(${i})">${i}</a>`;
        }
    }

    // 마지막 페이지
    if (endPage < totalPages) {
        if (endPage < totalPages - 1) {
            html += `<span>...</span>`;
        }
        html += `<a href="javascript:goToPage(${totalPages})">${totalPages}</a>`;
    }

    // 다음 버튼
    if (currentPage < totalPages) {
        html += `<a href="javascript:goToPage(${currentPage + 1})">›</a>`;
    }

    pagination.innerHTML = html;
}

// ===== 페이지 이동 =====
function goToPage(page) {
    currentPage = page;
    goSearch();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// ===== 초기 데이터 로드 =====
async function loadInitialData() {
    try {
        allJobs = await api.get('/api/public/jobs');
        goSearch();
    } catch (err) {
        console.error('데이터 로드 실패:', err);
        alert('공고 목록을 불러오는데 실패했습니다.');
    }
}

// ===== 검색 및 필터링 =====
function goSearch(event) {
    if (event) event.preventDefault();

    const categoryField = document.getElementById('jobCategorySelect')?.value || 'ALL';
    const searchWord = document.getElementById('publicSearchWord')?.value.trim().toLowerCase() || '';
    const sortOrder = document.getElementById('sortOrder')?.value || 'START_DATE';

    let filtered = [...allJobs];

    // 검색어 필터
    if (searchWord) {
        filtered = filtered.filter(job => {
            const title = (job.title || '').toLowerCase();
            const company = (job.companyName || '').toLowerCase();
            const location = (job.workLocation || '').toLowerCase();

            switch (categoryField) {
                case 'TITLE':
                    return title.includes(searchWord);
                case 'COMPANY':
                    return company.includes(searchWord);
                case 'JOB_LOCATION':
                    return location.includes(searchWord);
                case 'ALL':
                    return title.includes(searchWord) ||
                        company.includes(searchWord) ||
                        location.includes(searchWord);
                default:
                    return true;
            }
        });
        currentPage = 1;
    }

    // 정렬
    filtered.sort((a, b) => {
        if (sortOrder === 'START_DATE') {
            const dateA = new Date(a.startDate || 0);
            const dateB = new Date(b.startDate || 0);
            return dateB - dateA;
        } else if (sortOrder === 'END_DATE') {
            const dateA = new Date(a.endDate || 0);
            const dateB = new Date(b.endDate || 0);
            return dateA - dateB;
        }
        return 0;
    });

    renderPublicJobs(filtered);
}

// ===== 상세 정보 렌더링 =====
function renderJobDetail(job) {
    const compName = job.companyName || '미등록 기업';
    setText('compName', compName);
    setText('displayCompName', compName);

    setText('jobTitle', job.title);
    setText('startDate', job.startDate);
    setText('endDate', job.endDate);

    // 기본 정보
    const fields = {
        jobForm: job.jobForm,
        jobType: job.jobType,
        jobCategory: job.jobCategory,
        industry: job.industry,
        roleLevel: job.roleLevel,
        experience: job.experience,
        baseSalary: job.baseSalary,
        workTime: job.workTime,
        workLocation: job.workLocation
    };

    Object.keys(fields).forEach(key => setText(key, fields[key]));

    // 상세 내용
    setHTML('companyIntro', job.companyIntro);
    setHTML('positionSummary', job.positionSummary);
    setHTML('skillQualification', job.skillQualification);
    setHTML('benefits', job.benefits);
    setHTML('notes', job.notes);

    // 회사 정보
    setText('companyType', job.companyType);
    setText('establishedDate', job.establishedDate);
    setText('ceoName', job.ceoName);
    setText('employeeNum', job.employeeNum);
    setText('capital', job.capital);
    setText('revenue', job.revenue);
    setText('companyAddress', job.companyAddress);

    // 홈페이지 링크 처리
    const homepageEl = document.getElementById('homepage');
    if (homepageEl && job.homepage) {
        const url = job.homepage.startsWith('http')
            ? job.homepage
            : `https://${job.homepage}`;
        homepageEl.textContent = job.homepage;
        if (homepageEl.tagName === 'A') {
            homepageEl.href = url;
        }
    }
}

// ===== 상세 정보 로드 =====
async function loadJobDetail(jobId) {
    try {
        const job = await api.get(`/api/public/jobs/${jobId}`);
        renderJobDetail(job);
    } catch (err) {
        console.error('상세 로드 실패:', err);
        alert('상세 정보를 불러오는데 실패했습니다.');
    }
}

// ===== 지원하기 =====
async function applyJob(jobId) {
    if (!jobId || jobId === 'undefined') {
        jobId = getUrlParam('id');
    }

    try {
        const resumeId = document.getElementById('r_resumeId')?.value ||
            document.querySelector('[name="seq_no_m110"]')?.value;

        if (!resumeId) {
            alert('이력서 정보를 찾을 수 없습니다. 로그인이 필요하거나 이력서를 먼저 등록해주세요.');
            return;
        }

        const applicationData = {
            resumeId: parseInt(resumeId),
            jobId: parseInt(jobId)
        };

        if (!confirm("이 이력서로 지원하시겠습니까?")) return;

        const response = await api.post(`/api/public/jobs/${jobId}/apply`, applicationData);

        if (response.success) {
            alert('지원이 완료되었습니다!');
            location.reload();
        } else {
            alert(response.message || '지원 처리 중 오류가 발생했습니다.');
        }
    } catch (err) {
        console.error('지원 실패:', err);
        alert('지원 처리 중 오류가 발생했습니다. (로그인 여부나 이력서 존재를 확인하세요)');
    }
}

// ===== 페이지 초기화 =====
document.addEventListener('DOMContentLoaded', () => {
    const jobId = getUrlParam('id');

    // 목록 페이지
    if (document.querySelector('.job_list') && !document.getElementById('jobTitle')) {
        loadInitialData();
    }

    // 상세 페이지
    if (jobId && document.getElementById('jobTitle')) {
        loadJobDetail(jobId);
    }
});