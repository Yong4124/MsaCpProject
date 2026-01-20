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
    }
};

// 전역 변수
let allJobs = [];
let currentJobId = null;
let isApplicantLoading = false;
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

    // 페이지네이션 계산
    const totalPages = Math.ceil(jobs.length / itemsPerPage);
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const pageJobs = jobs.slice(startIndex, endIndex);

    ul.innerHTML = '';
    pageJobs.forEach(job => {
        const li = document.createElement('li');
        const logoSrc = job.logoPath || '/images/default_logo.png';

        li.innerHTML = `
            <div class="box">
                <div class="img">
                    <img src="${logoSrc}" alt="기업 로고" 
                         onerror="this.onerror=null; this.src='/images/default_logo.png';">
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

    // 페이지네이션 렌더링
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

    // 검색하지 않고 페이지만 이동하는 경우가 아니면 첫 페이지로
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
        li.innerHTML = `
            <div class="applicant_card">
                <div class="card_main">
                    <div class="info_grid_layout" style="border-left: 3px solid #000; padding-left: 30px;">
                        <div class="info_label">이름</div><div class="info_val">${app.name || '-'}</div>
                        <div class="info_label">성별</div><div class="info_val">${app.gender || '-'}</div>
                        <div class="info_label">생년월일</div><div class="info_val">${app.birthDate || '-'}</div>
                        <div class="info_label">전화번호</div><div class="info_val">${app.phone || '-'}</div>
                        <div class="info_label">학교</div><div class="info_val">${app.schoolName || '-'}</div>
                        <div class="info_label">전공명</div><div class="info_val">${app.major || '-'}</div>
                    </div>
                </div>
                <div class="btn-flex-center">
                    <button class="btn-common btn-blue" onclick="openResume(${currentJobId}, ${app.id})">이력서 상세보기</button>
                    <button type="button" class="btn-common btn_orange" onclick="updateStatus(${app.id}, '2', '${app.status}')">합격</button>
                </div>
            </div>
            <div class="dashed_line"></div>
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

// 이력서 렌더링
function renderResumeModal(app) {
    setText('r_name', app.name);
    setText('r_gender', app.gender);
    setText('r_birth', app.birthDate);
    setText('r_phone', app.phone);
    setText('r_email', app.email);
    setText('r_address', app.address);

    setText('r_school', app.schoolName);
    setText('r_major', app.major);
    setText('r_entrance', app.entranceDate);
    setText('r_grad', app.gradDate);
    setText('r_score', app.score);
    setText('r_status_edu', app.gradStatus);

    setText('r_company', app.company);
    setText('r_dept', app.dept);
    setText('r_join', app.joinDate);
    setText('r_leave', app.leaveDate);
    setText('r_position', app.position);
    setText('r_salary', app.salary);
    setHTML('r_task', app.task);
    setHTML('r_career_desc', app.careerDesc);

    setHTML('r_field', app.speciality);
    setHTML('r_intro', app.introduction);

    renderCertificates(app.certificates);
    renderServiceProofFiles(app.serviceProofFiles);
    renderResumeFiles(app.resumeFiles);
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
}

// 복무증명서 파일 렌더링
function renderServiceProofFiles(files) {
    const container = document.getElementById('r_service_proof_files');
    if (!container) return;

    if (!files || files.length === 0) {
        container.innerHTML = '<li style="text-align:center; color:#999; padding:20px; list-style:none;">첨부된 파일이 없습니다.</li>';
        return;
    }

    container.innerHTML = '';
    files.forEach(file => {
        const li = document.createElement('li');
        li.style.cssText = 'padding: 10px; border-bottom: 1px solid #eee; list-style: none;';
        li.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <span style="color: #333;">📎 ${file.fileName || '파일명 없음'}</span>
                <button class="btn-common btn-blue" 
                        style="padding: 5px 15px; font-size: 13px;"
                        onclick="downloadServiceProof(${file.id}, '${file.fileName}')">
                    다운로드
                </button>
            </div>
        `;
        container.appendChild(li);
    });
}

// 이력서 파일 렌더링
function renderResumeFiles(files) {
    const container = document.getElementById('r_resume_files');
    if (!container) return;

    if (!files || files.length === 0) {
        container.innerHTML = '<li style="text-align:center; color:#999; padding:20px; list-style:none;">첨부된 파일이 없습니다.</li>';
        return;
    }

    container.innerHTML = '';
    files.forEach(file => {
        const li = document.createElement('li');
        li.style.cssText = 'padding: 10px; border-bottom: 1px solid #eee; list-style: none;';
        li.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <span style="color: #333;">📎 ${file.fileName || '파일명 없음'}</span>
                <button class="btn-common btn-blue" 
                        style="padding: 5px 15px; font-size: 13px;"
                        onclick="downloadResumeFile(${file.id}, '${file.fileName}')">
                    다운로드
                </button>
            </div>
        `;
        container.appendChild(li);
    });
}

// 파일 다운로드 함수들
function downloadServiceProof(fileId, fileName) {
    const getTxt = (id) => document.getElementById(id)?.textContent?.trim() || '-';
    const name = getTxt('r_name');
    const birth = getTxt('r_birth');
    const phone = getTxt('r_phone');

    const content = `
        <div style="font-family: 'Malgun Gothic', sans-serif; line-height: 1.6;">
            <h1 style="text-align: center; border-bottom: 2px solid #333; padding-bottom: 10px;">서비스 증빙 자료 상세</h1>
            <table style="width: 100%; border-collapse: collapse; margin-top: 20px;">
                <tr>
                    <td style="background: #f4f4f4; padding: 10px; border: 1px solid #ddd; width: 25%;"><b>대상자 성명</b></td>
                    <td style="padding: 10px; border: 1px solid #ddd;">${name}</td>
                </tr>
                <tr>
                    <td style="background: #f4f4f4; padding: 10px; border: 1px solid #ddd;"><b>생년월일</b></td>
                    <td style="padding: 10px; border: 1px solid #ddd;">${birth}</td>
                </tr>
                <tr>
                    <td style="background: #f4f4f4; padding: 10px; border: 1px solid #ddd;"><b>연락처</b></td>
                    <td style="padding: 10px; border: 1px solid #ddd;">${phone}</td>
                </tr>
                <tr>
                    <td style="background: #f4f4f4; padding: 10px; border: 1px solid #ddd;"><b>증빙 파일명</b></td>
                    <td style="padding: 10px; border: 1px solid #ddd;">${fileName || '첨부파일 참조'}</td>
                </tr>
            </table>
            <p style="margin-top: 50px; text-align: center; color: #888;">본 문서는 ${name}님의 서비스 증빙을 확인하기 위해 자동 생성된 문서입니다.</p>
        </div>
    `;

    const cleanFileName = fileName ? fileName.replace(/\.docx$/i, '') : '';
    const converted = htmlDocx.asBlob(content);
    const finalFileName = cleanFileName
        ? `증빙자료_${name}_${cleanFileName}.docx`
        : `증빙자료_${name}.docx`;

    saveAs(converted, finalFileName);
}

function downloadResumeFile() {
    const getTxt = (id) => document.getElementById(id)?.textContent || '-';
    const name = getTxt('r_name');

    const certElements = document.querySelectorAll('#r_cert_list .info_grid');
    let certHtml = '';
    if (certElements.length > 0) {
        certElements.forEach(cert => {
            const spans = cert.querySelectorAll('.value_box span');
            if (spans.length >= 4) {
                certHtml += `<p>- ${spans[0].textContent} (${spans[1].textContent}) / ${spans[2].textContent}</p>`;
            }
        });
    } else {
        certHtml = '<p>등록된 자격증 없음</p>';
    }

    const content = `
        <div style="font-family: 'Malgun Gothic', sans-serif;">
            <h1 style="text-align: center; color: #333;">이력서 (${name})</h1>
            <h3 style="border-bottom: 1px solid #000; padding-bottom: 5px;">1. 기본 인적 사항</h3>
            <p><b>성별/생년월일:</b> ${getTxt('r_gender')} / ${getTxt('r_birth')}</p>
            <p><b>연락처:</b> ${getTxt('r_phone')}</p>
            <p><b>이메일:</b> ${getTxt('r_email')}</p>
            <p><b>주소:</b> ${getTxt('r_address')}</p>
            <h3 style="border-bottom: 1px solid #000; padding-bottom: 5px; margin-top: 20px;">2. 학력 사항</h3>
            <p><b>학교명:</b> ${getTxt('r_school')} (${getTxt('r_status_edu')})</p>
            <p><b>전공/학점:</b> ${getTxt('r_major')} / ${getTxt('r_score')}</p>
            <p><b>재학기간:</b> ${getTxt('r_entrance')} ~ ${getTxt('r_grad')}</p>
            <h3 style="border-bottom: 1px solid #000; padding-bottom: 5px; margin-top: 20px;">3. 경력 사항</h3>
            <p><b>회사명:</b> ${getTxt('r_company')} (${getTxt('r_position')})</p>
            <p><b>부서/연봉:</b> ${getTxt('r_dept')} / ${getTxt('r_salary')}</p>
            <p><b>근무기간:</b> ${getTxt('r_join')} ~ ${getTxt('r_leave')}</p>
            <p><b>주요업무:</b> ${getTxt('r_task')}</p>
            <h3 style="border-bottom: 1px solid #000; padding-bottom: 5px; margin-top: 20px;">4. 자격 사항</h3>
            ${certHtml}
            <h3 style="border-bottom: 1px solid #000; padding-bottom: 5px; margin-top: 20px;">5. 자기소개</h3>
            <div style="margin-top: 10px; white-space: pre-wrap;">${getTxt('r_intro')}</div>
        </div>
    `;

    const converted = htmlDocx.asBlob(content);
    saveAs(converted, `이력서_${name}.docx`);
}

function downloadExcel() {
    const resumeData = {
        "이름": document.getElementById('r_name').textContent,
        "성별": document.getElementById('r_gender').textContent,
        "생년월일": document.getElementById('r_birth').textContent,
        "연락처": document.getElementById('r_phone').textContent,
        "이메일": document.getElementById('r_email').textContent,
        "주소": document.getElementById('r_address').textContent,
        "학교명": document.getElementById('r_school').textContent,
        "전공": document.getElementById('r_major').textContent,
        "회사명": document.getElementById('r_company').textContent,
        "부서": document.getElementById('r_dept').textContent
    };

    const worksheet = XLSX.utils.json_to_sheet([resumeData]);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, "이력서_상세");
    XLSX.writeFile(workbook, `지원자_이력서_${resumeData["이름"]}.xlsx`);
}

// 지원자 상태 변경
async function updateStatus(applicantId, status, currentStatus) {
    if (currentStatus === status) {
        alert('이미 합격 처리된 지원자입니다.');
        return;
    }

    if (!confirm('해당 지원자를 합격 처리하시겠습니까?')) return;

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