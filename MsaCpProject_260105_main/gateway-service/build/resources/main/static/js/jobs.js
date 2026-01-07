/**
 * 관리자 채용공고 관리 (통합) - 수정본
 */

// ===== 전역 변수 =====
let allJobs = [];
let currentJobId = null;
let isApplicantLoading = false;

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
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (!res.ok) throw new Error(`API 오류: ${res.status}`);
        return res.json();
    },

    async put(url, data) {
        const res = await fetch(url, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (!res.ok) throw new Error(`API 오류: ${res.status}`);
        return res.json();
    }
};

// ===== 목록 렌더링 =====
function renderJobList(jobs) {
    const ul = document.querySelector('.job_list');
    if (!ul) return;

    if (!jobs.length) {
        ul.innerHTML = '<li style="text-align:center; padding:40px;">등록된 채용공고가 없습니다.</li>';
        return;
    }

    ul.innerHTML = '';
    jobs.forEach(job => {
        const li = document.createElement('li');
        li.className = 'job_item';
        li.innerHTML = `
            <div class="company_logo">
                <img src="${job.logo || '/images/default_logo.png'}" alt="로고">
            </div>
            
            <div class="job_info">
                <div class="title">
                    <a href="/company/jobs/detail?id=${job.id}">${job.title}</a>
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
                <a href="javascript:void(0);" class="btn-common btn-blue" onclick="openApplicants('${job.id}')">지원자 보기</a>
                <span class="deadline_text">마감일 &nbsp; ${job.endDate || '상시채용'}</span>
                <a href="javascript:void(0);" class="btn-common btn-gray" onclick="closeJob('${job.id}')">공고마감</a>
            </div>
        `;
        ul.appendChild(li);
    });
}

// ===== 목록 로드 =====
async function loadJobList() {
    try {
        const companyId = 1; // TODO: 실제 로그인한 회사 ID로 변경
        allJobs = await api.get(`/api/jobs?companyId=${companyId}`);
        renderJobList(allJobs);
    } catch (err) {
        console.error('목록 로드 실패:', err);
        alert('목록을 불러오는데 실패했습니다.');
    }
}

// ===== 검색 및 필터링 =====
function goSearch(event) {
    if (event && typeof event.preventDefault === 'function') {
        event.preventDefault();
    }

    const form = document.rpForm || document.forms['rpForm'];
    if (!form) return;

    const searchField = form.searchfield ? form.searchfield.value : 'ALL';
    const searchWord = form.searchword ? form.searchword.value.trim().toLowerCase() : '';
    const searchType = form.searchtype ? form.searchtype.value : 'ALL';

    let filtered = [...allJobs];

    // 1. 검색어 필터링
    if (searchWord) {
        filtered = filtered.filter(job => {
            const title = (job.title || '').toLowerCase();
            const location = (job.workLocation || '').toLowerCase();
            switch(searchField) {
                case 'TITLE': return title.includes(searchWord);
                case 'JOB_LOCATION': return location.includes(searchWord);
                case 'ALL': return title.includes(searchWord) || location.includes(searchWord);
                default: return true;
            }
        });
    }

    // 2. 상태 필터링
    if (searchType !== 'ALL') {
        filtered = filtered.filter(job => {
            const postingYn = String(job.postingYn || '1');
            const closeYn = String(job.closeYn || 'N').toUpperCase();

            if (searchType === '1') {
                return postingYn === '1' && closeYn === 'N';
            }
            if (searchType === '2') {
                return postingYn === '0';
            }
            return true;
        });
    }

    renderJobList(filtered);
}

// ===== 상세 정보 렌더링 =====
function renderJobDetail(job) {
    const compName = job.companyName || '미등록 기업';
    setText('compName', compName);

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
    setText('employeeNum', job.employeeNum);
    setText('capital', job.capital);
    setText('revenue', job.revenue);
    setText('homepage', job.homepage);
}

// ===== 상세 정보 로드 =====
async function loadJobDetail(jobId) {
    try {
        const job = await api.get(`/api/jobs/${jobId}`);
        renderJobDetail(job);
    } catch (err) {
        console.error('상세 로드 실패:', err);
        alert('상세 정보를 불러오는데 실패했습니다.');
    }
}

/**
 * 임시저장 기능
 */
async function saveTemp() {
    const form = document.applForm;
    if (!form) return;

    const tempData = {
        title: form.jobTitle.value,
        startDate: form.startDate.value,
        endDate: form.endDate.value,
        jobForm: form.jobType.value,
        jobType: form.employType.value,
        jobCategory: form.jobCategory.value,
        industry: form.industry.value,
        roleLevel: form.jobLevel.value,
        experience: form.career.value,
        baseSalary: form.salary.value,
        workTime: form.workTime.value,
        workLocation: form.workLocation.value,
        companyIntro: form.companyIntro.value,
        positionSummary: form.jobDescription.value,
        skillQualification: form.requirements.value,
        benefits: form.benefits.value,
        notes: form.notes.value,
        companyType: form.compType.value,
        establishedDate: form.foundingDate.value,
        employeeNum: form.employeeCount.value,
        capital: form.capital.value,
        revenue: form.sales.value,
        homepage: form.homepage.value,
        postingYn: "0",
        closeYn: "N"
    };

    try {
        await api.post('/api/jobs', tempData);
        alert('임시저장 되었습니다.');
        location.href = '/company/jobs';
    } catch (err) {
        console.error('임시저장 실패:', err);
        alert('서버 저장에 실패했습니다.');
    }
}

// ===== 채용공고 등록 =====
async function submitJob() {
    const form = document.applForm;

    if (!form.jobTitle.value.trim()) {
        alert('공고명을 입력해주세요.');
        form.jobTitle.focus();
        return;
    }
    if (!form.startDate.value) {
        alert('접수 시작일을 입력해주세요.');
        form.startDate.focus();
        return;
    }
    if (!form.endDate.value) {
        alert('접수 마감일을 입력해주세요.');
        form.endDate.focus();
        return;
    }

    const payload = {
        title: form.jobTitle.value,
        startDate: form.startDate.value,
        endDate: form.endDate.value,
        jobForm: form.jobType.value,
        jobType: form.employType.value,
        jobCategory: form.jobCategory.value,
        industry: form.industry.value,
        roleLevel: form.jobLevel.value,
        experience: form.career.value,
        baseSalary: form.salary.value,
        workTime: form.workTime.value,
        workLocation: form.workLocation.value,
        companyIntro: form.companyIntro.value,
        positionSummary: form.jobDescription.value,
        skillQualification: form.requirements.value,
        benefits: form.benefits.value,
        notes: form.notes.value,
        companyType: form.compType.value,
        establishedDate: form.foundingDate.value,
        employeeNum: form.employeeCount.value,
        capital: form.capital.value,
        revenue: form.sales.value,
        homepage: form.homepage.value,
        postingYn: "1"
    };

    try {
        await api.post('/api/jobs', payload);
        alert('등록되었습니다.');
        location.href = '/company/jobs';
    } catch (err) {
        console.error('등록 실패:', err);
        alert('등록에 실패했습니다.');
    }
}

// ===== 공고 마감 =====
async function closeJob(id) {
    if (!confirm('이 공고를 마감하시겠습니까?')) return;

    try {
        await api.post(`/api/jobs/${id}/close`);
        alert('공고가 마감되었습니다.');
        location.reload();
    } catch (err) {
        console.error('마감 실패:', err);
        alert('마감 처리에 실패했습니다.');
    }
}

// ===== 지원자 모달 =====
function openApplicants(jobId) {
    currentJobId = jobId;

    const job = allJobs.find(j => j.id == jobId);
    if (job) {
        setText('modalJobTitle', job.title);
    }

    document.getElementById('applicantModal').style.display = 'block';
    loadApplicants();
}

function closeApplicantModal() {
    document.getElementById('applicantModal').style.display = 'none';
}

function closeResumeModal() {
    document.getElementById('resumeModal').style.display = 'none';
}

// ===== 지원자 목록 로드 =====
async function loadApplicants(e) {
    if (e) {
        e.preventDefault();
        e.stopPropagation();
    }

    if (isApplicantLoading || !currentJobId) return;

    isApplicantLoading = true;

    try {
        const status = document.getElementById('filterStatus')?.value || '';
        const url = status ? `/api/jobs/${currentJobId}/applicants?status=${status}` : `/api/jobs/${currentJobId}/applicants`;

        const applicants = await api.get(url);

        // 검색어 필터링 (이름 필드 제거됨)
        const searchWord = document.getElementById('applicantSearchWord')?.value.trim().toLowerCase() || '';

        let filtered = applicants;

        if (searchWord) {
            filtered = applicants.filter(app => {
                const phone = (app.phone || '').toLowerCase();
                const email = (app.email || '').toLowerCase();
                return phone.includes(searchWord) || email.includes(searchWord);
            });
        }

        renderApplicants(filtered);
    } catch (err) {
        console.error('지원자 로드 실패:', err);
        alert('지원자 목록을 불러오는데 실패했습니다.');
    } finally {
        isApplicantLoading = false;
    }
}

// ===== 지원자 목록 렌더링 =====
function renderApplicants(applicants) {
    const container = document.getElementById('applicantList');
    if (!container) return;

    setText('applicantCount', applicants.length);

    if (!applicants.length) {
        container.innerHTML = '<li style="text-align:center; padding:40px; list-style:none;">지원자가 없습니다.</li>';
        return;
    }

    container.innerHTML = '';
    applicants.forEach(app => {
        const li = document.createElement('li');
        li.style.listStyle = 'none';
        li.innerHTML = `
            <div class="applicant_card">
                <div class="card_main">
                    <div class="info_grid_layout" style="border-left: 3px solid #000; padding-left: 30px;">
                        <div class="info_label">전화번호</div><div class="info_val">${app.phone || '-'}</div>
                        <div class="info_label">이메일</div><div class="info_val">${app.email || '-'}</div>
                        <div class="info_label">학교</div><div class="info_val">${app.schoolName || '-'}</div>
                        <div class="info_label">전공명</div><div class="info_val">${app.major || '-'}</div>
                    </div>
                </div>
                <div class="btn-flex-center">
                    <button class="btn-common btn-blue" onclick="openResume('${currentJobId}', '${app.id}')">이력서 상세보기</button>
                    <button type="button" class="btn-common btn_orange" onclick="updateStatus('${app.id}', '2', '${app.status}')">합격</button>
                </div>
            </div>
            <div class="dashed_line"></div>
        `;
        container.appendChild(li);
    });
}

async function openResume(jobId, applicantId) {
    try {
        const data = await api.get(`/api/jobs/${jobId}/applicants/${applicantId}/resume`);
        renderResumeModal(data);
        document.getElementById('resumeModal').style.display = 'block';
    } catch (e) {
        alert('이력서를 불러오지 못했습니다.');
        console.error(e);
    }
}

function renderResumeModal(app) {
    setText('r_phone', app.phone);
    setText('r_email', app.email);
    setText('r_address', app.address);

    setText('r_school', app.schoolName);
    setText('r_major', app.major);
    setText('r_entrance', app.entranceDate);
    setText('r_grad', app.gradDate);
    setText('r_score', app.score);
    setText('r_status_edu', app.gradStatus);

    setHTML('r_field', app.speciality);
    setHTML('r_intro', app.introduction);

    // 자격증 렌더링
    const certList = document.getElementById('r_cert_list');
    if (certList && app.certificates && app.certificates.length > 0) {
        certList.innerHTML = '';
        app.certificates.forEach(cert => {
            const certDiv = document.createElement('div');
            certDiv.className = 'grid_layout info_grid';
            certDiv.style.marginBottom = '20px';
            certDiv.innerHTML = `
                <label class="label">자격/기술명</label>
                <div class="value_box"><span>${cert.certificateNm || '-'}</span></div>
                <label class="label">취득년월</label>
                <div class="value_box"><span>${cert.obtainDate || '-'}</span></div>
                <label class="label">발급기관</label>
                <div class="value_box"><span>${cert.agency || '-'}</span></div>
                <label class="label">자격증 번호</label>
                <div class="value_box"><span>${cert.certificateNum || '-'}</span></div>
            `;
            certList.appendChild(certDiv);
        });
    } else if (certList) {
        certList.innerHTML = '<div class="text_box" style="text-align:center; color:#999;">등록된 자격증 정보가 없습니다.</div>';
    }
}

function downloadExcel() {
    const resumeData = {
        "연락처": document.getElementById('r_phone').textContent,
        "이메일": document.getElementById('r_email').textContent,
        "학교": document.getElementById('r_school').textContent,
        "전공": document.getElementById('r_major').textContent,
        "전문분야": document.getElementById('r_field').textContent,
        "자기소개": document.getElementById('r_intro').textContent
    };

    const worksheet = XLSX.utils.json_to_sheet([resumeData]);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, "이력서_상세");

    const fileName = `지원자_이력서.xlsx`;
    XLSX.writeFile(workbook, fileName);
}

async function updateStatus(applicantId, status, currentStatus) {
    if (currentStatus === status) {
        alert(`이미 합격 처리된 지원자입니다.`);
        return;
    }

    if (!confirm(`해당 지원자를 합격 처리하시겠습니까?`)) return;

    try {
        await api.post(`/api/jobs/applicants/${applicantId}/status`, { status });
        alert('상태가 변경되었습니다.');
        loadApplicants();
    } catch (err) {
        console.error('상태 변경 실패:', err);
        alert('상태 변경에 실패했습니다.');
    }
}

function goList() {
    location.href = '/jobs';
}

// ===== 페이지 초기화 =====
window.addEventListener('load', () => {
    const jobId = getUrlParam('id');

    if (document.querySelector('.job_list')) {
        loadJobList();
    }

    if (jobId && document.getElementById('jobTitle')) {
        loadJobDetail(jobId);
    }
});