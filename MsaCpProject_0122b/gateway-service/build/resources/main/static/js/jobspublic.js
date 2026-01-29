// 전역 변수
let allJobs = [];
let currentPage = 1;
let itemsPerPage = 10;

// 공통 유틸리티
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
    now.setHours(0, 0, 0, 0);

    if (endDate < now) return '마감';

    const diff = Math.ceil((endDate - now) / (1000 * 60 * 60 * 24));
    return `D-${diff}`;
};

// API 호출 함수 (JWT 쿠키 포함)
const api = {
    async get(url) {
        const res = await fetch(url, {
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        if (!res.ok) throw new Error(`API 오류: ${res.status}`);
        return res.json();
    },

    async post(url, data) {
        const res = await fetch(url, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        if (!res.ok) throw new Error(`API 오류: ${res.status}`);
        return res.json();
    }
};

// 목록 렌더링
function renderPublicJobs(jobs) {
    const ul = document.querySelector('.job_list');
    if (!ul) return;

    if (!jobs.length) {
        ul.innerHTML = '<li style="text-align:center; padding:100px 0; width:100%;">검색 결과가 없습니다.</li>';
        const pageWrap = document.querySelector('.page_wrap');
        if (pageWrap) pageWrap.style.display = 'none';
        return;
    }

    // 페이지네이션 계산
    const totalPages = Math.ceil(jobs.length / itemsPerPage);
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const pageJobs = jobs.slice(startIndex, endIndex);

    ul.innerHTML = '';
    pageJobs.forEach(job => {
        const title = job.title || '제목 없음';
        const endDate = job.endDate;
        const dDayText = getDday(endDate);

        const li = document.createElement('li');
        const logoSrc = job.logoPath || '/img/common/default_logo.png';

        const isClosedByAdmin = (job.closeYn === 'Y' || job.closeYn === 'y');
        const isClosedByDate = dDayText === '마감';
        const isClosed = isClosedByAdmin || isClosedByDate;

        li.innerHTML = `
            <div class="box">
                <div class="img">
                    <img src="${logoSrc}" alt="기업 로고" 
                         onerror="this.onerror=null; this.src='/img/common/default_logo.png';">
                </div>
            
                <div class="job_linfo">
                    <div class="ji_tit">
                        <a href="/jobs/detail?id=${job.id}" style="cursor:pointer; color:inherit;">${title}</a>
                    </div>
                    
                    <div class="ji_linfo">
                        <div class="item">
                            <div class="th">직업유형</div>
                            <div class="td">${job.jobForm || '-'}</div>
                        </div>
                        <div class="item">
                            <div class="th">고용형태</div>
                            <div class="td">${job.jobType || '-'}</div>
                        </div>
                        <div class="item">
                            <div class="th">직종</div>
                            <div class="td">${job.jobCategory || '-'}</div>
                        </div>
                        <div class="item">
                            <div class="th">업계</div>
                            <div class="td">${job.industry || '-'}</div>
                        </div>
                        <div class="item">
                            <div class="th">직급</div>
                            <div class="td">${job.roleLevel || '-'}</div>
                        </div>
                        <div class="item">
                            <div class="th">경력</div>
                            <div class="td">${job.experience || '-'}</div>
                        </div>
                        <div class="item">
                            <div class="th">기본급</div>
                            <div class="td">${job.baseSalary || '-'}</div>
                        </div>
                        <div class="item">
                            <div class="th">근무시간</div>
                            <div class="td">${job.workTime || '-'}</div>
                        </div>
                        <div class="item full">
                            <div class="th">근무처</div>
                            <div class="td">${job.workLocation || '-'}</div>
                        </div>
                    </div>
                </div>
                
                <div class="job_link">
                    ${isClosed ?
                    `<div class="link">
                            <button type="button" class="btn-cancel">공고마감</button>
                    </div>` :
                    `<div class="link">
                            <button type="button" class="btn-submit mar" onclick="applyJob(${job.id})">지원하기</button>
                        </div>
                        <div class="date">
                            <div class="th">마감일</div>
                            <div class="td">${dDayText}</div>
                        </div>`
                    }
                </div>
            </div>`;
        ul.appendChild(li);
    });

    // 페이지네이션 렌더링
    const pageWrap = document.querySelector('.page_wrap');
    if (pageWrap) pageWrap.style.display = 'block';
    renderPagination(totalPages);
}

// 페이징 렌더링 함수
function renderPagination(totalPages) {
    const pagination = document.getElementById('pagination');
    if (!pagination) return;

    if (totalPages <= 1) {
        pagination.innerHTML = '<a href="#none" class="active">1</a>';
        return;
    }

    let html = '';

    for (let i = 1; i <= totalPages; i++) {
        if (i === currentPage) {
            html += `<a href="#none" class="active">${i}</a>`;
        } else {
            html += `<a href="#none" onclick="goToPage(${i}); return false;">${i}</a>`;
        }
    }

    pagination.innerHTML = html;
}

// 페이지 이동 함수
function goToPage(page) {
    currentPage = page;
    goSearch();
}

// 초기 데이터 로드
async function loadInitialData() {
    try {
        allJobs = await api.get('/api/public/jobs');
        goSearch();
    } catch (err) {
        console.error('데이터 로드 실패:', err);
        alert('공고 목록을 불러오는데 실패했습니다.');
    }
}

// 검색 및 필터링
function goSearch(event) {
    if (event) event.preventDefault();

    const categoryField = document.getElementById('jobCategorySelect')?.value || 'ALL';
    const searchWord = document.getElementById('publicSearchWord')?.value.trim().toLowerCase() || '';
    const sortOrder = document.getElementById('sortOrder')?.value || 'START_DATE';

    let filtered = [...allJobs];

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

// 상세 정보 렌더링
function renderJobDetail(job) {
    const compName = job.companyName || '미등록 기업';
    setText('compName', compName);
    setText('displayCompName', compName);
    setText('jobTitle', job.title);
    setText('startDate', job.startDate);
    setText('endDate', job.endDate);

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

    setHTML('companyIntro', job.companyIntro);
    setHTML('positionSummary', job.positionSummary);
    setHTML('skillQualification', job.skillQualification);
    setHTML('benefits', job.benefits);
    setHTML('notes', job.notes);

    setText('companyType', job.companyType);
    setText('establishedDate', job.establishedDate);
    setText('ceoName', job.ceoName);
    setText('employeeNum', job.employeeNum);
    setText('capital', job.capital);
    setText('revenue', job.revenue);
    setText('companyAddress', job.companyAddress);

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

// 상세 정보 로드
async function loadJobDetail(jobId) {
    try {
        const job = await api.get(`/api/public/jobs/${jobId}`);
        renderJobDetail(job);
    } catch (err) {
        console.error('상세 로드 실패:', err);
        alert('상세 정보를 불러오는데 실패했습니다.');
    }
}

// 지원하기
async function applyJob(jobId) {
    if (!jobId || jobId === 'undefined') {
        jobId = getUrlParam('id');
    }

    if (!jobId) {
        alert('공고 ID를 찾을 수 없습니다.');
        return;
    }

    try {
        // ✅ 1) 세션 체크: 개인 → 기업 fallback
        let memberCheck = await api.get('/api/personal/check-session');

        if (!memberCheck?.loggedIn) {
            // ✅ 기업 서비스 쪽 check-session (너 프로젝트에 맞는 걸로 하나 살아있으면 됨)
            try {
                memberCheck = await api.get('/api/company/check-session');
            } catch (e1) {
                try {
                    memberCheck = await api.get('/api/company-member/check-session');
                } catch (e2) {
                    // 여기까지 왔으면 memberCheck는 loggedIn false로 유지
                }
            }
        }

        if (memberCheck.memberType === 'COMPANY') {
            alert('개인회원만 지원 가능합니다.');
            return;
        }

        const resumeId = document.getElementById('r_resumeId')?.value ||
            document.querySelector('[name="seq_no_m110"]')?.value;

        if (!resumeId) {
            if (confirm('이력서를 먼저 등록해주세요. 이력서 등록 페이지로 이동하시겠습니까?')) {
                location.href = `/apply?jobId=${jobId}`;
            }
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
        const errorMsg = err.message || '지원 처리 중 오류가 발생했습니다.';

        // ⛔️ 이 부분 그대로 유지
        if (errorMsg.includes('이미')) {
            alert('이미 해당 공고에 지원하셨습니다.');
        } else if (errorMsg.includes('마감')) {
            alert('마감된 공고입니다.');
        } else {
            alert(errorMsg);
        }
    }
}


// 페이지 초기화
document.addEventListener('DOMContentLoaded', () => {
    const jobId = getUrlParam('id');

    if (document.querySelector('.job_list') && !document.getElementById('jobTitle')) {
        loadInitialData();
    }

    if (jobId && document.getElementById('jobTitle')) {
        loadJobDetail(jobId);
    }
});