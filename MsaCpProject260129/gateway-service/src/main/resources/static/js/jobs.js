// API 객체 - Cookie 기반
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
    },

    async put(url, data) {
        const res = await fetch(url, {
            method: 'PUT',
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

// 전역 변수
let allJobs = [];
let currentJobId = null;
let isApplicantLoading = false;
let currentPage = 1;
let itemsPerPage = 10;
let currentResumeData = null;
let currentJobData = null;

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

// 목록 렌더링
function renderJobList(jobs) {
    const ul = document.querySelector('.job_list');
    if (!ul) return;

    if (!jobs.length) {
        ul.innerHTML = '<li style="text-align:center; padding:40px;">등록된 채용공고가 없습니다.</li>';
        const pageWrap = document.querySelector('.page_wrap');
        if (pageWrap) pageWrap.style.display = 'none';
        return;
    }

    const totalPages = Math.ceil(jobs.length / itemsPerPage);
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const pageJobs = jobs.slice(startIndex, endIndex);

    ul.innerHTML = '';
    pageJobs.forEach(job => {
        const li = document.createElement('li');
        const logoSrc = job.logoPath || '/img/common/default_logo.png';

        li.innerHTML = `
            <div class="box">
                <div class="img">
                    <img src="${logoSrc}" alt="기업 로고" 
                         onerror="this.onerror=null; this.src='/img/common/default_logo.png';">
                </div>
                
                <div class="job_linfo">
                    <div class="ji_tit">
                        <a href="/company/jobs/detail?id=${job.id}" style="cursor:pointer; color:inherit;">
                            ${job.title}
                        </a>
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
                    <div class="link">
                        <button type="button" class="btn-submit mar" onclick="openApplicants(${job.id})">지원자 보기</button>
                    </div>
                    <div class="date" style="margin-top: 24px; margin-bottom: 23px;">
                        <div class="th">마감일</div>
                        <div class="td">${job.endDate || '상시채용'}</div>
                    </div>
                    <div class="link">
                        <button type="button" class="btn-keep mar" onclick="closeJob(${job.id})">공고마감</button>
                    </div>
                </div>
            </div>
        `;
        ul.appendChild(li);
    });

    const pageWrap = document.querySelector('.page_wrap');
    if (pageWrap) pageWrap.style.display = 'block';
    renderPagination(totalPages);
}

// 목록 로드
async function loadJobList() {
    try {
        allJobs = await api.get('/api/jobs');
        renderJobList(allJobs);
    } catch (err) {
        console.error('목록 로드 실패:', err);
        alert('목록을 불러오는데 실패했습니다.');
    }
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

// 검색 시 첫 페이지로 리셋
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

    if (searchWord) {
        filtered = filtered.filter(job => {
            const title = (job.title || '').toLowerCase();
            const location = (job.workLocation || '').toLowerCase();
            switch (searchField) {
                case 'TITLE':
                    return title.includes(searchWord);
                case 'JOB_LOCATION':
                    return location.includes(searchWord);
                case 'ALL':
                    return title.includes(searchWord) || location.includes(searchWord);
                default:
                    return true;
            }
        });
    }

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

    if (event) {
        currentPage = 1;
    }

    renderJobList(filtered);
}

function renderJobDetail(job) {
    const compName = job.companyName || '회사 정보 없음';

    // input 필드는 value로 설정
    const setInputValue = (id, value) => {
        const el = document.getElementById(id);
        if (el) el.value = value || '-';
    };

    setText('compName', compName);
    setInputValue('jobTitle', job.title);
    setInputValue('startDate', job.startDate);
    setInputValue('endDate', job.endDate);

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

    Object.keys(fields).forEach(key => setInputValue(key, fields[key]));

    // textarea는 value로 설정
    document.getElementById('companyIntro').value = job.companyIntro || '';
    document.getElementById('positionSummary').value = job.positionSummary || '';
    document.getElementById('skillQualification').value = job.skillQualification || '';
    document.getElementById('benefits').value = job.benefits || '';
    document.getElementById('notes').value = job.notes || '';

    setInputValue('companyType', job.companyType);
    setInputValue('establishedDate', job.establishedDate);
    setInputValue('ceoName', job.ceoName);
    setInputValue('employeeNum', job.employeeNum);
    setInputValue('capital', job.capital);
    setInputValue('revenue', job.revenue);
    setInputValue('homepage', job.homepage);
    setInputValue('companyAddress', job.companyAddress);

    updateDetailButtons(job);
}

// 상세페이지 버튼 업데이트
function updateDetailButtons(job) {
    const btnArea = document.querySelector('.btn_area.center');
    if (!btnArea) return;

    const isTemp = String(job.postingYn) === '0';

    if (isTemp) {
        // 임시저장 공고: 입력 필드 활성화, 수정/등록 버튼 표시
        enableInputs();
        btnArea.innerHTML = `
            <a href="javascript:void(0);" class="btn-submit" onclick="updateTempJob()">임시저장 수정</a>&nbsp;&nbsp;
            <a href="javascript:void(0);" class="btn-submit" style="background-color: #ed5904" onclick="submitTempJob()">공고등록</a>&nbsp;&nbsp;
            <a href="javascript:goList();" class="btn-cancel">목록가기</a>
        `;
    } else {
        // 정식 등록 공고: 입력 필드 비활성화, 기존 버튼 유지
        disableInputs();
        btnArea.innerHTML = `
            <a href="#" class="btn-cancel" onclick="closeJob(); return false;">공고마감</a>&nbsp;&nbsp;
            <a href="javascript:goList();" class="btn-cancel">목록가기</a>
        `;
    }
}

// 입력 필드 활성화
function enableInputs() {
    const form = document.applForm;
    if (!form) return;

    const inputs = form.querySelectorAll('input:not([readonly])');
    inputs.forEach(input => {
        input.disabled = false;
        input.style.backgroundColor = '';
    });

    const textareas = form.querySelectorAll('textarea');
    textareas.forEach(textarea => {
        textarea.disabled = false;
        textarea.style.backgroundColor = '';
    });
}

// 입력 필드 비활성화
function disableInputs() {
    const form = document.applForm;
    if (!form) return;

    const inputs = form.querySelectorAll('input:not([readonly])');
    inputs.forEach(input => {
        input.disabled = true;
        input.style.backgroundColor = '#f5f5f5';
    });

    const textareas = form.querySelectorAll('textarea');
    textareas.forEach(textarea => {
        textarea.disabled = true;
        textarea.style.backgroundColor = '#f5f5f5';
    });
}

// 임시저장 수정
async function updateTempJob() {
    const form = document.applForm;
    if (!form) return;

    if (!form.TITLE.value.trim()) {
        alert('공고명을 입력해주세요.');
        form.TITLE.focus();
        return;
    }

    const jobId = getUrlParam('id');
    if (!jobId) {
        alert('공고 ID를 찾을 수 없습니다.');
        return;
    }

    const tempData = buildJobDataFromDetail(form);
    tempData.postingYn = "0";

    try {
        await api.put(`/api/jobs/${jobId}`, tempData);
        alert('임시저장이 수정되었습니다.');
        location.href = '/company/jobs';
    } catch (err) {
        console.error('임시저장 수정 실패:', err);
        alert('수정에 실패했습니다.');
    }
}

// 임시저장 공고를 정식 등록
async function submitTempJob() {
    const form = document.applForm;
    if (!form) return;

    if (!form.TITLE.value.trim()) {
        alert('공고명을 입력해주세요.');
        form.TITLE.focus();
        return;
    }

    if (!form.START_DATE.value) {
        alert('접수 시작일을 입력해주세요.');
        form.START_DATE.focus();
        return;
    }

    if (!form.END_DATE.value) {
        alert('접수 마감일을 입력해주세요.');
        form.END_DATE.focus();
        return;
    }

    const jobId = getUrlParam('id');
    if (!jobId) {
        alert('공고 ID를 찾을 수 없습니다.');
        return;
    }

    const jobData = buildJobDataFromDetail(form);
    jobData.postingYn = "1";

    try {
        await api.put(`/api/jobs/${jobId}`, jobData);
        alert('공고가 등록되었습니다.');
        location.href = '/company/jobs';
    } catch (err) {
        console.error('공고 등록 실패:', err);
        alert('등록에 실패했습니다.');
    }
}

// Detail 페이지용 데이터 구성 (name 속성이 다름)
function buildJobDataFromDetail(form) {
    return {
        title: form.TITLE.value,
        startDate: form.START_DATE.value,
        endDate: form.END_DATE.value,
        jobForm: form.JOB_FORM.value,
        jobType: form.JOB_TYPE.value,
        jobCategory: form.JOB_CATEGORY.value,
        industry: form.INDUSTRY.value,
        roleLevel: form.ROLE_LEVEL.value,
        experience: form.EXPERIENCE.value,
        baseSalary: form.BASE_SALARY.value,
        workTime: form.WORK_TIME.value,
        workLocation: form.JOB_LOCATION.value,
        companyIntro: form.COMPANY_INTRO.value,
        positionSummary: form.POSITION_SUMMARY.value,
        skillQualification: form.SKILL_QUALIFICATION.value,
        benefits: form.BENEFITS.value,
        notes: form.NOTES.value,
        companyType: form.COMPANY_TYPE.value,
        establishedDate: form.ESTABLISHED_DATE.value,
        employeeNum: form.EMPLOYEE_NUM.value,
        capital: form.CAPITAL.value,
        revenue: form.REVENUE.value,
        homepage: form.HOMEPAGE.value,
        ceoName: form.PRESIDENT_NM.value,
        companyAddress: form.COMPANY_ADDRESS.value,
        logoPath: currentJobData?.logoPath || null
    };
}

// 상세 정보 로드
async function loadJobDetail(jobId) {
    try {
        const job = await api.get(`/api/jobs/${jobId}`);
        renderJobDetail(job);
    } catch (err) {
        console.error('상세 로드 실패:', err);
        alert('상세 정보를 불러오는데 실패했습니다.');
    }
}

// 임시저장
async function saveTemp() {
    const form = document.applForm;
    if (!form) return;

    if (!form.jobTitle.value.trim()) {
        alert('임시저장을 위해 공고명을 입력해주세요.');
        form.jobTitle.focus();
        return;
    }

    const tempData = buildJobData(form);
    tempData.postingYn = "0";

    try {
        await api.post('/api/jobs', tempData);
        alert('임시저장 되었습니다.');
        location.href = '/company/jobs';
    } catch (err) {
        console.error('임시저장 실패:', err);
        alert('서버 저장에 실패했습니다.');
    }
}

// 채용공고 등록
async function submitJob() {
    const form = document.applForm;

    if (!form.jobTitle.value.trim()) {
        alert('공고명을 입력해주세요.');
        form.jobTitle.focus();
        return;
    }

    const startDate = document.getElementById('START_DATE').value;
    const endDate = document.getElementById('END_DATE').value;

    if (!startDate) {
        alert('접수 시작일을 입력해주세요.');
        document.getElementById('START_DATE').focus();
        return;
    }
    if (!endDate) {
        alert('접수 마감일을 입력해주세요.');
        document.getElementById('END_DATE').focus();
        return;
    }

    const payload = buildJobData(form);
    payload.postingYn = "1";

    try {
        await api.post('/api/jobs', payload);
        alert('등록되었습니다.');
        location.href = '/company/jobs';
    } catch (err) {
        console.error('등록 실패:', err);
        alert('등록에 실패했습니다.');
    }
}

// Job 데이터 구성 헬퍼 함수
function buildJobData(form) {
    return {
        title: form.jobTitle.value,
        startDate: document.getElementById('START_DATE').value,
        endDate: document.getElementById('END_DATE').value,
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
        ceoName: form.ceoName.value,
        companyAddress: form.companyAddress.value,
        logoPath: form.logoPath?.value || null
    };
}

// 공고 마감
async function closeJob(id) {
    if (!id) {
        id = getUrlParam('id');
    }

    if (!id) {
        alert('공고 ID를 확인할 수 없습니다.');
        return;
    }

    // 해당 공고의 마감 상태 확인
    const job = allJobs.find(j => j.id == id);
    if (job && job.closeYn === 'Y') {
        alert('이미 마감된 공고입니다.');
        return;
    }

    if (!confirm('이 공고를 마감하시겠습니까?')) return;

    try {
        await api.post(`/api/jobs/${id}/close`);
        alert('공고가 마감되었습니다.');
        location.href = '/company/jobs';
    } catch (err) {
        console.error('마감 실패:', err);
        alert('마감 처리에 실패했습니다.');
    }
}

// 지원자 모달 열기
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

// 지원자 목록 로드
async function loadApplicants(e) {
    if (e) {
        e.preventDefault();
        e.stopPropagation();
    }

    if (isApplicantLoading || !currentJobId) return;

    isApplicantLoading = true;

    try {
        const status = document.getElementById('filterStatus')?.value || '';
        const url = status
            ? `/api/jobs/${currentJobId}/applicants?status=${status}`
            : `/api/jobs/${currentJobId}/applicants`;

        const applicants = await api.get(url);
        const searchWord = document.getElementById('applicantSearchWord')?.value.trim().toLowerCase() || '';

        let filtered = applicants;

        if (searchWord) {
            filtered = applicants.filter(app => {
                const name = (app.name || '').toLowerCase();
                const phone = (app.phone || '').toLowerCase();
                const email = (app.email || '').toLowerCase();
                return name.includes(searchWord) || phone.includes(searchWord) || email.includes(searchWord);
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

// 지원자 목록 렌더링
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

        // ✅✅✅ 사진 URL 처리
        const photoUrl = app.photoPath
            ? `http://localhost:8006${app.photoPath}`
            : '/img/common/default_logo.png';

        li.innerHTML = `
    <div class="item">
         <div class="img">
            <img src="${photoUrl}" 
                 alt="이력서 사진" onerror="this.onerror=null; this.src='/img/common/default_logo.png'">
        </div>
        <div class="info_wrap">
            <div class="info">
                <div class="row">
                    <div class="item item1">
                        <div class="field">
                            <div class="th">이름</div>
                            <div class="td">${app.name || '-'}</div>
                        </div>
                    </div>
                    <div class="item item2">
                        <div class="field">
                            <div class="th">성별</div>
                            <div class="td">${app.gender || '-'}</div>
                        </div>
                    </div>
                    <div class="item item1">
                        <div class="field">
                            <div class="th">생년월일</div>
                            <div class="td">${app.birthDate || '-'}</div>
                        </div>
                    </div>
                    <div class="item item2">
                        <div class="field">
                            <div class="th">전화번호</div>
                            <div class="td">${app.phone || '-'}</div>
                        </div>
                    </div>
                    <div class="item item1">
                        <div class="field">
                            <div class="th">학교</div>
                            <div class="td">${app.schoolName || '-'}</div>
                        </div>
                    </div>
                    <div class="item item2">
                        <div class="field">
                            <div class="th">전공명</div>
                            <div class="td">${app.major || '-'}</div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="btn_area left">
                <a href="javascript:void(0);" class="btn-submit" onclick="openResume(${currentJobId}, ${app.id})">이력서 상세보기</a>
                <button type="button" class="btn-complete mar" onclick="updateStatus(${app.id}, '2', '${app.status}')">합격</button>
            </div>
        </div>
    </div>
`;
        container.appendChild(li);
    });
}

// 이력서 모달 열기
async function openResume(jobId, applicantId) {
    try {
        const modal = document.getElementById('resumeModal');
        modal.style.display = 'block';

        await new Promise(resolve => setTimeout(resolve, 100));

        const data = await api.get(`/api/jobs/${jobId}/applicants/${applicantId}/resume`);
        renderResumeModal(data);

    } catch (e) {
        alert('이력서를 불러오지 못했습니다.');
        console.error(e);
        document.getElementById('resumeModal').style.display = 'none';
    }
}

// 이력서 렌더링 함수 수정
function renderResumeModal(app) {
    // ✨ 전역 변수에 저장
    currentResumeData = app;

    // input 필드는 value로 설정하는 함수 추가
    const setInputValue = (id, value) => {
        const el = document.getElementById(id);
        if (el) el.value = value || '-';
    };

    // 인적사항 - input 필드
    setInputValue('r_name', app.name);
    setInputValue('r_gender', app.gender);
    setInputValue('r_birth', app.birthDate);
    setInputValue('r_phone', app.phone);
    setInputValue('r_email', app.email);
    setInputValue('r_address', app.address);

    // 최종학력 - input 필드
    setInputValue('r_school', app.schoolName);
    setInputValue('r_major', app.major);
    setInputValue('r_entrance', app.entranceDate);
    setInputValue('r_grad', app.gradDate);
    setInputValue('r_score', app.score);
    setInputValue('r_status_edu', app.gradStatus);

    // ✨ 경력 이력 렌더링 (다중)
    renderCareerHistories(app.careerHistories);

    // textarea 필드 - value로 설정
    const setTextareaValue = (id, value) => {
        const el = document.getElementById(id);
        if (el) el.value = value || '';
    };

    setTextareaValue('r_field', app.speciality);
    setTextareaValue('r_intro', app.introduction);

    // ✅✅✅ 사진 URL 처리
    const photoEl = document.getElementById('r_photo');
    if (photoEl && app.photoPath) {
        photoEl.src = `http://localhost:8006${app.photoPath}`;
    }

    renderCertificates(app.certificates);
    renderServiceProofFiles(app.serviceProofFiles);
    renderResumeFiles(app.resumeFiles);
}

// ✨ 경력 이력 렌더링 함수 추가
function renderCareerHistories(careerHistories) {
    const careerList = document.getElementById('r_career_list');
    if (!careerList) return;

    if (!careerHistories || careerHistories.length === 0) {
        careerList.innerHTML = '<div class="text_box" style="text-align:center; color:#999;">등록된 경력 정보가 없습니다.</div>';
        return;
    }

    careerList.innerHTML = '';
    careerHistories.forEach((career, index) => {
        const careerDiv = document.createElement('div');
        careerDiv.className = 'pop_box mb60';
        careerDiv.innerHTML = `
        <div class="man_info nopad">
            <div class="info">
                <div class="row">
                    <div class="item item3">
                        <div class="field">
                            <div class="th">회사명</div>
                            <div class="td"><input type="text" class="input mw250" value="${career.company || '-'}" readonly></div>
                        </div>
                    </div>
                    <div class="item item3">
                        <div class="field">
                            <div class="th">부서명</div>
                            <div class="td"><input type="text" class="input mw250" value="${career.department || '-'}" readonly></div>
                        </div>
                    </div>
                    <div class="item item3">
                        <div class="field">
                            <div class="th">입사년월</div>
                            <div class="td"><input type="text" class="input mw250" value="${career.joinDate || '-'}" readonly></div>
                        </div>
                    </div>
                    <div class="item item3">
                        <div class="field">
                            <div class="th">퇴사년월</div>
                            <div class="td"><input type="text" class="input mw250" value="${career.retireDate || '-'}" readonly></div>
                        </div>
                    </div>
                    <div class="item item3">
                        <div class="field">
                            <div class="th">직급/직책</div>
                            <div class="td"><input type="text" class="input mw250" value="${career.position || '-'}" readonly></div>
                        </div>
                    </div>
                    <div class="item item3">
                        <div class="field">
                            <div class="th">연봉</div>
                            <div class="td"><input type="text" class="input mw250" value="${career.salary || '-'}" readonly></div>
                        </div>
                    </div>
                    <div class="item">
                        <div class="field xsblock">
                            <div class="th">담당업무</div>
                            <div class="td"><textarea class="textarea h100" readonly>${career.positionSummary || '-'}</textarea></div>
                        </div>
                    </div>
                    <div class="item">
                        <div class="field xsblock">
                            <div class="th">경력기술서</div>
                            <div class="td"><textarea class="textarea h100" readonly>${career.experience || '-'}</textarea></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
        careerList.appendChild(careerDiv);
    });
}

// 자격증 렌더링
function renderCertificates(certificates) {
    const certList = document.getElementById('r_cert_list');
    if (!certList) return;

    if (!certificates || certificates.length === 0) {
        certList.innerHTML = '<div class="text_box" style="text-align:center; color:#999;">등록된 자격증 정보가 없습니다.</div>';
        return;
    }

    certList.innerHTML = '';
    certificates.forEach(cert => {
        const certDiv = document.createElement('div');
        certDiv.className = 'pop_box mb60';
        certDiv.innerHTML = `
        <div class="man_info nopad">
            <div class="info">
                <div class="row">
                    <div class="item item3">
                        <div class="field">
                            <div class="th">자격/기술명</div>
                            <div class="td"><input type="text" class="input mw250" value="${cert.certificateNm || '-'}" readonly></div>
                        </div>
                    </div>
                    <div class="item item3">
                        <div class="field">
                            <div class="th">취득년월</div>
                            <div class="td"><input type="text" class="input mw250" value="${cert.obtainDate || '-'}" readonly></div>
                        </div>
                    </div>
                    <div class="item item3">
                        <div class="field">
                            <div class="th">발급기관</div>
                            <div class="td"><input type="text" class="input mw250" value="${cert.agency || '-'}" readonly></div>
                        </div>
                    </div>
                    <div class="item item3">
                        <div class="field">
                            <div class="th">자격증 번호</div>
                            <div class="td"><input type="text" class="input mw250" value="${cert.certificateNum || '-'}" readonly></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
        certList.appendChild(certDiv);
    });
}

// 이력서 파일 리스트 렌더링
function renderResumeFiles(files) {
    const container = document.getElementById('r_resume_files');
    if (!container) return;

    if (!files || files.length === 0) {
        container.innerHTML = '<li style="text-align:center; color:#999; padding:20px; list-style:none;">첨부된 이력서가 없습니다.</li>';
        return;
    }

    container.innerHTML = '';
    files.forEach(file => {
        // DB에 저장된 경로 (예: /upload/resume/abc.pdf)
        const filePath = file.fileName;
        // 화면에 표시할 파일명만 추출
        const displayName = filePath ? filePath.split('/').pop() : '이력서_다운로드';

        const fileDiv = document.createElement('div');
        fileDiv.className = 'file_item';
        fileDiv.innerHTML = `
            <span class="fnm">${displayName}</span>
            <a href="${filePath}" download="${displayName}" class="file_down">다운로드</a>
        `;
        container.appendChild(fileDiv);
    });
}

// 복무증명서 리스트 렌더링 (이력서와 동일한 로직 적용)
function renderServiceProofFiles(files) {
    const container = document.getElementById('r_service_proof_files');
    if (!container) return;

    if (!files || files.length === 0) {
        container.innerHTML = '<li style="text-align:center; color:#999; padding:20px; list-style:none;">첨부된 증빙자료가 없습니다.</li>';
        return;
    }

    container.innerHTML = '';
    files.forEach(file => {
        const filePath = file.fileName;
        const displayName = filePath ? filePath.split('/').pop() : '증빙자료_다운로드';

        const fileDiv = document.createElement('div');
        fileDiv.className = 'file_item';
        fileDiv.innerHTML = `
            <span class="fnm">${displayName}</span>
            <a href="${filePath}" download="${displayName}" class="file_down">다운로드</a>
        `;
        container.appendChild(fileDiv);
    });
}

function downloadExcel() {
    // 1. 데이터 가져오기 (input의 value를 가져오도록 수정)
    const getValue = (id) => document.getElementById(id)?.value || '-';

// 1. 경력사항 처리 (이미지에 표시된 모든 필드 반영)
    const careers = currentResumeData?.careerHistories?.map(c =>
        `[${c.company || '-'}] 
        부서: ${c.department || '-'} 
        직급: ${c.position || '-'} 
        기간: ${c.joinDate || '-'} ~ ${c.retireDate || '-'} 
        연봉: ${c.salary || '-'} 
        담당업무: ${c.positionSummary || '-'} 
        경력기술서: ${c.experience || '-'}`
    ).join('\n' + '-'.repeat(10) + '\n') || '-';

// 2. 자격증 기술 처리 (이미지에 표시된 모든 필드 반영)
    const certificates = currentResumeData?.certificates?.map(cert =>
        `[${cert.certificateNm || '-'}] 
        발급기관: ${cert.agency || '-'}
        취득년월: ${cert.obtainDate || '-'}
        자격증 번호: ${cert.certificateNum || '-'}`
    ).join('\n' + '-'.repeat(10) + '\n') || '-';

    // 3. 전체 필드 매핑
    const resumeData = {
        "이름": getValue('r_name'),
        "성별": getValue('r_gender'),
        "생년월일": getValue('r_birth'),
        "연락처": getValue('r_phone'),
        "이메일": getValue('r_email'),
        "주소": getValue('r_address'),
        "학교명": getValue('r_school'),
        "전공": getValue('r_major'),
        "입학년월": getValue('r_entrance'),
        "졸업년월": getValue('r_grad'),
        "학점": getValue('r_score'),
        "졸업상태": getValue('r_status_edu'),
        "전문분야": getValue('r_field'),
        "경력사항": careers,
        "자격증/기술": certificates,
        "자기소개서": getValue('r_intro')
    };

    // 4. 엑셀 파일 생성
    try {
        const worksheet = XLSX.utils.json_to_sheet([resumeData]);

        // 셀 너비 자동 조절 (내용이 긴 필드 대응)
        worksheet['!cols'] = [
            {wch: 10}, {wch: 8},  {wch: 12}, {wch: 15}, {wch: 20}, {wch: 30}, // 1~6
            {wch: 15}, {wch: 15}, {wch: 12}, {wch: 12}, {wch: 8},  {wch: 10}, // 7~12
            {wch: 30}, {wch: 100}, {wch: 100}, {wch: 60}                        // 13~16
        ];

        const workbook = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(workbook, worksheet, "이력서_상세_전체");

        const fileName = `이력서_${resumeData["이름"]}_${new Date().toISOString().slice(0, 10)}.xlsx`;
        XLSX.writeFile(workbook, fileName);
    } catch (error) {
        console.error("엑셀 다운로드 중 오류 발생:", error);
        alert("데이터를 추출하는 중 오류가 발생했습니다.");
    }
}

// 지원자 상태 변경
async function updateStatus(applicantId, status, currentStatus) {
    if (currentStatus === status) {
        alert('이미 합격 처리된 지원자입니다.');
        return;
    }

    if (!confirm('해당 지원자를 합격 처리하시겠습니까?')) return;

    try {
        await api.post(`/api/jobs/applicants/${applicantId}/status`, {status});
        alert('상태가 변경되었습니다.');
        loadApplicants();
    } catch (err) {
        console.error('상태 변경 실패:', err);
        alert('상태 변경에 실패했습니다.');
    }
}

function goList() {
    location.href = '/company/jobs';
}

// 회사 정보 자동 채우기
async function loadCompanyInfo() {
    try {
        const response = await api.get('/api/company/myinfo');

        if (response.success && response.data) {
            const company = response.data;

            const ceoInput = document.querySelector('input[name="ceoName"]');
            if (ceoInput && company.presidentNm) {
                ceoInput.value = company.presidentNm;
                ceoInput.readOnly = true;
                ceoInput.style.backgroundColor = '#f5f5f5';
            }

            const addressInput = document.querySelector('input[name="companyAddress"]');
            if (addressInput && company.companyAddress) {
                addressInput.value = company.companyAddress;
                addressInput.readOnly = true;
                addressInput.style.backgroundColor = '#f5f5f5';
            }

            if (company.logoPath) {
                let logoInput = document.querySelector('input[name="logoPath"]');
                if (!logoInput) {
                    logoInput = document.createElement('input');
                    logoInput.type = 'hidden';
                    logoInput.name = 'logoPath';
                    document.applForm.appendChild(logoInput);
                }
                logoInput.value = company.logoPath;
            }
        }
    } catch (err) {
        console.error('회사 정보 로드 실패:', err);
    }
}

// 기존 datePickerSet 함수를 완전히 교체
function datePickerSet(sDate, eDate, flag) {
    // 시작 ~ 종료 2개 짜리 달력 datepicker
    if (!isValidStr(sDate) && !isValidStr(eDate) && sDate.length > 0 && eDate.length > 0) {
        var sDay = sDate.val();
        var eDay = eDate.val();

        if (flag && !isValidStr(sDay) && !isValidStr(eDay)) { // 처음 입력 날짜 설정, update...
            var sdp = sDate.datepicker().data("datepicker");
            sdp.selectDate(new Date(sDay.replace(/-/g, "/"))); // 익스에서는 그냥 new Date하면 -를 인식못함 replace필요

            var edp = eDate.datepicker().data("datepicker");
            edp.selectDate(new Date(eDay.replace(/-/g, "/"))); // 익스에서는 그냥 new Date하면 -를 인식못함 replace필요
        }

        // 시작일자 세팅하기 날짜가 없는경우엔 제한을 걸지 않음
        if (!isValidStr(eDay)) {
            sDate.datepicker({
                maxDate: new Date(eDay.replace(/-/g, "/"))
            });
        }
        sDate.datepicker({
            language: 'ko',
            autoClose: true,
            dateFormat: 'yyyy-mm-dd',
            onSelect: function () {
                datePickerSet(sDate, eDate);
            }
        });

        // 종료일자 세팅하기 날짜가 없는경우엔 제한을 걸지 않음
        if (!isValidStr(sDay)) {
            eDate.datepicker({
                minDate: new Date(sDay.replace(/-/g, "/"))
            });
        }
        eDate.datepicker({
            language: 'ko',
            autoClose: true,
            dateFormat: 'yyyy-mm-dd',
            onSelect: function () {
                datePickerSet(sDate, eDate);
            }
        });

        // 한개짜리 달력 datepicker
    } else if (!isValidStr(sDate)) {
        var sDay = sDate.val();
        if (flag && !isValidStr(sDay)) { // 처음 입력 날짜 설정, update...
            var sdp = sDate.datepicker().data("datepicker");
            sdp.selectDate(new Date(sDay.replace(/-/g, "/"))); // 익스에서는 그냥 new Date하면 -를 인식못함 replace필요
        }

        sDate.datepicker({
            language: 'ko',
            autoClose: true,
            dateFormat: 'yyyy-mm-dd'
        });
    }
}

/**
 * 문자열 유효성 체크 헬퍼
 */
function isValidStr(str) {
    if (str == null || str == undefined || str == "")
        return true;
    else
        return false;
}


// 페이지 초기화
window.addEventListener('load', () => {
    const jobId = getUrlParam('id');

    if (document.querySelector('.job_list')) {
        loadJobList();
    }

    if (jobId && document.getElementById('compName')) {
        loadJobDetail(jobId);
    }

    if (document.applForm && document.querySelector('input[name="ceoName"]')) {
        loadCompanyInfo();
    }

    // 데이트피커 초기화 - jQuery 객체로 전달
    const $startDate = $("#START_DATE");
    const $endDate = $("#END_DATE");

    if ($startDate.length > 0 && $endDate.length > 0) {
        datePickerSet($startDate, $endDate, true);
    }
});