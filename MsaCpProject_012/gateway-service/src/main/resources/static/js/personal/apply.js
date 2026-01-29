(() => {
    "use strict";

    const $ = (sel) => document.querySelector(sel);

    const ENDPOINT_TEMP = "/api/apply/temp";
    const ENDPOINT_SUBMIT = "/api/apply/submit";
    const ENDPOINT_JOB_DETAIL = (jobId) => `/api/public/jobs/${jobId}`;

    const ENDPOINT_RESUME_LATEST = "/api/resume/me";
    const ENDPOINT_RESUME_LIST = (page = 0, size = 5) => `/api/resume/list?page=${page}&size=${size}`;
    const ENDPOINT_RESUME_ONE = (m110) => `/api/resume/${m110}`;

    function qs(name) {
        return new URLSearchParams(location.search).get(name);
    }

    async function requireLoginOrRedirect() {
        try {
            const r = await fetch("/api/personal/check-login", {
                method: "GET",
                credentials: "include",
                cache: "no-store",
            });

            const ct = (r.headers.get("content-type") || "").toLowerCase();
            const isJson = ct.includes("application/json");

            if (!r.ok || !isJson) {
                alert("로그인 후 이용하세요.");
                location.href = "/login";
                return false;
            }
            return true;
        } catch (e) {
            alert("로그인 후 이용하세요.");
            location.href = "/login";
            return false;
        }
    }

    async function fetchJsonAuth(url) {
        const res = await fetch(url, {
            method: "GET",
            credentials: "include",
            cache: "no-store",
        });

        const ct = (res.headers.get("content-type") || "").toLowerCase();
        if (res.status === 401) throw new Error("401");
        if (!res.ok) throw new Error("HTTP " + res.status);
        if (!ct.includes("application/json")) throw new Error("Not JSON");

        return await res.json();
    }

    async function autoFillBasicProfile() {
        try {
            const raw = await fetchJsonAuth("/api/personal/my-info");
            const member = raw?.member ?? raw?.data?.member ?? raw?.data ?? raw;

            const name = member?.name ?? member?.nm ?? "";
            const gender = member?.gender ?? "";
            const birth =
                member?.birth ??
                member?.birthDate ??
                member?.birth_date ??
                member?.birthDay ??
                "";

            if ($("#NAME_VIEW")) $("#NAME_VIEW").value = name;
            if ($("#GENDER_VIEW")) $("#GENDER_VIEW").value = gender;
            if ($("#BIRTH_VIEW")) $("#BIRTH_VIEW").value = birth;
        } catch (e) {
            console.warn("[apply] autoFillBasicProfile failed:", e);
        }
    }

    function careerCard() {
        const el = document.createElement("div");
        el.className = "apply_card";
        el.dataset.type = "career";
        el.innerHTML = `
      <button type="button" class="apply_btn_remove">-</button>
      <div class="apply_grid">
        <div class="apply_label">회사명</div>
        <div class="apply_field"><input class="c_company" type="text" name="CAREER_COMPANY[]"></div>

        <div class="apply_label">부서명</div>
        <div class="apply_field"><input class="c_dept" type="text" name="CAREER_DEPARTMENT[]"></div>

        <div class="apply_label">입사년월</div>
        <div class="apply_field"><input class="c_join" type="text" placeholder="YYYY-MM" name="CAREER_JOIN_DATE[]"></div>

        <div class="apply_label">퇴사년월</div>
        <div class="apply_field"><input class="c_retire" type="text" placeholder="YYYY-MM" name="CAREER_RETIRE_DATE[]"></div>

        <div class="apply_label">직급/직책</div>
        <div class="apply_field"><input class="c_position" type="text" name="CAREER_POSITION[]"></div>

        <div class="apply_label">연봉</div>
        <div class="apply_field"><input class="c_salary" type="text" name="CAREER_SALARY[]"></div>

        <div class="apply_row_wide">
          <div class="apply_label">담당업무</div>
          <div class="apply_field"><textarea class="c_duty" rows="3" name="CAREER_POSITION_SUMMARY[]"></textarea></div>
        </div>

        <div class="apply_row_wide">
          <div class="apply_label">경력기술서</div>
          <div class="apply_field"><textarea class="c_exp" rows="3" name="CAREER_EXPERIENCE[]"></textarea></div>
        </div>
      </div>
    `;
        el
            .querySelector(".apply_btn_remove")
            .addEventListener("click", () => el.remove());
        return el;
    }

    function licenseCard() {
        const el = document.createElement("div");
        el.className = "apply_card";
        el.dataset.type = "license";
        el.innerHTML = `
      <button type="button" class="apply_btn_remove">-</button>
      <div class="apply_grid">
        <div class="apply_label">자격/기술명</div>
        <div class="apply_field"><input class="l_name" type="text" name="LICENSE_NAME[]"></div>

        <div class="apply_label">취득년월</div>
        <div class="apply_field"><input class="l_date" type="text" placeholder="YYYY-MM" name="LICENSE_DATE[]"></div>

        <div class="apply_label">발급기관</div>
        <div class="apply_field"><input class="l_agency" type="text" name="LICENSE_AGENCY[]"></div>

        <div class="apply_label">자격증 번호</div>
        <div class="apply_field"><input class="l_num" type="text" name="LICENSE_NUM[]"></div>
      </div>
    `;
        el
            .querySelector(".apply_btn_remove")
            .addEventListener("click", () => el.remove());
        return el;
    }

    async function fetchJson(url) {
        const res = await fetch(url, {
            method: "GET",
            credentials: "include",
            cache: "no-store",
        });

        if (res.status === 401) {
            alert("로그인 후 이용하세요.");
            location.href = "/login";
            throw new Error("Unauthorized");
        }

        const ct = (res.headers.get("content-type") || "").toLowerCase();
        if (!res.ok) throw new Error(`HTTP ${res.status} @ ${url}`);
        if (!ct.includes("application/json")) throw new Error("Not JSON response");

        return await res.json();
    }

    async function postForm(endpoint, extra = {}) {
        const form = $("#applyForm");
        const rawFd = new FormData(form);

        const KEY_MAP = {
            NAME_VIEW: "name",
            GENDER_VIEW: "gender",
            BIRTH_VIEW: "birthDate",
            PHONE: "phone",
            EMAIL: "email",
            ADDRESS: "address",
            SCHOOL: "school",
            MAJOR: "major",
            SELF_INTRO: "selfIntro",
            SEQ_NO_M210: "SEQ_NO_M210",

            ENROLL_DATE: "enrollDate",
            GRADUATE_DATE: "graduateDate",
            GPA: "gpa",
            GRADUATE_STATUS: "graduateStatus",
            SKILL: "skill",

            CAREER_COMPANY: "careerCompany",
            CAREER_DEPARTMENT: "careerDepartment",
            CAREER_JOIN_DATE: "careerJoinDate",
            CAREER_RETIRE_DATE: "careerRetireDate",
            CAREER_POSITION: "careerPosition",
            CAREER_SALARY: "careerSalary",
            CAREER_POSITION_SUMMARY: "careerPositionSummary",
            CAREER_EXPERIENCE: "careerExperience",

            LICENSE_NAME: "licenseName",
            LICENSE_DATE: "licenseDate",
            LICENSE_AGENCY: "licenseAgency",
            LICENSE_NUM: "licenseNum",

            PHOTO_PATH: "photoPath",
            PROOF_PATH: "serviceProofPath",
            RESUME_FILE_PATH: "resumeFilePath",
        };

        const fd = new FormData();
        for (const [k, v] of rawFd.entries()) {
            let key = k;
            if (key && key.endsWith("[]")) key = key.slice(0, -2);
            if (KEY_MAP[key]) key = KEY_MAP[key];
            fd.append(key, v);
        }

        if ($("#PHOTO_PATH")) fd.set("photoPath", $("#PHOTO_PATH").value || "");
        if ($("#PROOF_PATH")) fd.set("serviceProofPath", $("#PROOF_PATH").value || "");
        if ($("#RESUME_FILE_PATH")) fd.set("resumeFilePath", $("#RESUME_FILE_PATH").value || "");

        Object.entries(extra).forEach(([k, v]) => fd.set(k, v));

        const res = await fetch(endpoint, {
            method: "POST",
            body: fd,
            credentials: "include",
            cache: "no-store",
        });

        if (res.status === 401) {
            alert("로그인 후 이용하세요.");
            location.href = "/login";
            throw new Error("Unauthorized");
        }

        // ✅ 에러 응답 처리 개선
        if (!res.ok) {
            const contentType = res.headers.get("content-type");
            if (contentType && contentType.includes("application/json")) {
                try {
                    const errorData = await res.json();
                    const errorMessage = errorData?.message || errorData?.error || "요청을 처리할 수 없습니다.";
                    throw new Error(errorMessage);
                } catch (e) {
                    if (e.message) throw e;
                    throw new Error("HTTP " + res.status);
                }
            }
            throw new Error("HTTP " + res.status);
        }

        return res;
    }

    function bindJob(jobRaw) {
        const job = jobRaw?.data ?? jobRaw;
        const jobId = job?.id ?? job?.seqNoM210 ?? job?.SEQ_NO_M210 ?? "";

        $("#jobTitle").textContent = job?.title ?? "지원서 작성";
        $("#SEQ_NO_M210").value = jobId;
    }

    function showExistingAttachments(model) {
        if ($("#PHOTO_PATH")) $("#PHOTO_PATH").value = model?.photoPath ?? "";
        if ($("#PROOF_PATH")) $("#PROOF_PATH").value = model?.serviceProofPath ?? "";
        if ($("#RESUME_FILE_PATH")) $("#RESUME_FILE_PATH").value = model?.resumeFilePath ?? "";

        const photo = model?.photoPath ?? "";
        const photoHint = $("#PHOTO_HINT");
        const photoThumb = $("#PHOTO_THUMB");
        if (photo) {
            if (photoHint) {
                photoHint.style.display = "";
                photoHint.innerHTML = `기존 사진: <a href="${photo}" target="_blank" rel="noopener noreferrer">${photo}</a>`;
            }
            if (photoThumb) {
                photoThumb.style.display = "";
                photoThumb.src = photo;
            }
        } else {
            if (photoHint) { photoHint.style.display = "none"; photoHint.innerHTML = ""; }
            if (photoThumb) { photoThumb.style.display = "none"; photoThumb.src = ""; }
        }

        const proof = model?.serviceProofPath ?? "";
        const proofHint = $("#PROOF_HINT");
        if (proof) {
            if (proofHint) {
                proofHint.style.display = "";
                proofHint.innerHTML = `기존 증빙자료: <a href="${proof}" target="_blank" rel="noopener noreferrer">${proof}</a>`;
            }
        } else {
            if (proofHint) { proofHint.style.display = "none"; proofHint.innerHTML = ""; }
        }

        const resumeFile = model?.resumeFilePath ?? "";
        const resumeHint = $("#RESUME_HINT");
        if (resumeFile) {
            if (resumeHint) {
                resumeHint.style.display = "";
                resumeHint.innerHTML = `기존 이력서: <a href="${resumeFile}" target="_blank" rel="noopener noreferrer">${resumeFile}</a>`;
            }
        } else {
            if (resumeHint) { resumeHint.style.display = "none"; resumeHint.innerHTML = ""; }
        }
    }

    function toResumeModel(raw) {
        const data = raw?.data ?? raw;

        const careers =
            (Array.isArray(data?.careers) && data.careers) ||
            (Array.isArray(data?.careerList) && data.careerList) ||
            (Array.isArray(data?.m111List) && data.m111List) ||
            [];

        const licenses =
            (Array.isArray(data?.licenses) && data.licenses) ||
            (Array.isArray(data?.licenseList) && data.licenseList) ||
            (Array.isArray(data?.m112List) && data.m112List) ||
            [];

        return {
            name: data?.name ?? data?.nm ?? "",
            gender: data?.gender ?? "",
            birthDate: data?.birthDate ?? data?.birth_date ?? "",

            email: data?.email ?? "",
            phone: data?.phone ?? "",
            address: data?.address ?? "",

            school: data?.school ?? data?.schoolName ?? "",
            major: data?.major ?? "",

            enrollDate: data?.enrollDate ?? data?.entranceDate ?? data?.enroll_date ?? "",
            graduateDate: data?.graduateDate ?? data?.gradDate ?? data?.graduate_date ?? "",
            gpa: data?.gpa ?? data?.score ?? "",
            graduateStatus: data?.graduateStatus ?? data?.gradStatus ?? data?.graduate_status ?? "",

            skill: data?.skill ?? data?.speciality ?? "",
            selfIntro: data?.selfIntro ?? data?.introduction ?? data?.self_intro ?? "",

            photoPath: data?.photoPath ?? data?.PHOTO_PATH ?? data?.photo_path ?? "",
            serviceProofPath: data?.serviceProofPath ?? data?.PROOF_PATH ?? data?.service_proof_path ?? "",
            resumeFilePath: data?.resumeFilePath ?? data?.RESUME_FILE_PATH ?? data?.resume_file_path ?? "",

            careers: careers.map((c) => ({
                company: c?.company ?? "",
                department: c?.department ?? c?.dept ?? "",
                joinDate: c?.joinDate ?? c?.join_date ?? "",
                retireDate: c?.retireDate ?? c?.leaveDate ?? c?.retire_date ?? "",
                position: c?.position ?? "",
                salary: c?.salary ?? "",
                positionSummary: c?.positionSummary ?? c?.task ?? c?.position_summary ?? "",
                experience: c?.experience ?? c?.careerDesc ?? "",
            })),

            licenses: licenses.map((l) => ({
                name: l?.certificateNm ?? l?.certificateName ?? l?.name ?? "",
                obtainDate: l?.obtainDate ?? l?.obtain_date ?? "",
                agency: l?.agency ?? "",
                certificateNum: l?.certificateNum ?? l?.certificate_num ?? "",
            })),
        };
    }

    function bindResume(resume) {
        if ($("#NAME_VIEW")) $("#NAME_VIEW").value = resume?.name ?? "";
        if ($("#GENDER_VIEW")) $("#GENDER_VIEW").value = resume?.gender ?? "";
        if ($("#BIRTH_VIEW")) $("#BIRTH_VIEW").value = resume?.birthDate ?? "";

        if ($("#EMAIL")) $("#EMAIL").value = resume?.email ?? "";
        if ($("#PHONE")) $("#PHONE").value = resume?.phone ?? "";
        if ($("#ADDRESS")) $("#ADDRESS").value = resume?.address ?? "";

        if ($("#SCHOOL")) $("#SCHOOL").value = resume?.school ?? "";
        if ($("#MAJOR")) $("#MAJOR").value = resume?.major ?? "";
        if ($("#ENROLL_DATE")) $("#ENROLL_DATE").value = resume?.enrollDate ?? "";
        if ($("#GRADUATE_DATE")) $("#GRADUATE_DATE").value = resume?.graduateDate ?? "";
        if ($("#GPA")) $("#GPA").value = resume?.gpa ?? "";
        if ($("#GRADUATE_STATUS")) $("#GRADUATE_STATUS").value = resume?.graduateStatus ?? "";

        if ($("#SKILL")) $("#SKILL").value = resume?.skill ?? "";
        if ($("#SELF_INTRO")) $("#SELF_INTRO").value = resume?.selfIntro ?? "";

        if (Array.isArray(resume?.careers) && resume.careers.length) {
            $("#careerList").innerHTML = "";
            resume.careers.forEach((c) => {
                const card = careerCard();
                card.querySelector(".c_company").value = c.company ?? "";
                card.querySelector(".c_dept").value = c.department ?? "";
                card.querySelector(".c_join").value = c.joinDate ?? "";
                card.querySelector(".c_retire").value = c.retireDate ?? "";
                card.querySelector(".c_position").value = c.position ?? "";
                card.querySelector(".c_salary").value = c.salary ?? "";
                card.querySelector(".c_duty").value = c.positionSummary ?? "";
                card.querySelector(".c_exp").value = c.experience ?? "";
                $("#careerList").appendChild(card);
            });
        }

        if (Array.isArray(resume?.licenses) && resume.licenses.length) {
            $("#licenseList").innerHTML = "";
            resume.licenses.forEach((l) => {
                const card = licenseCard();
                card.querySelector(".l_name").value = l.name ?? "";
                card.querySelector(".l_date").value = l.obtainDate ?? "";
                card.querySelector(".l_agency").value = l.agency ?? "";
                card.querySelector(".l_num").value = l.certificateNum ?? "";
                $("#licenseList").appendChild(card);
            });
        }

        showExistingAttachments(resume);
    }

    async function init() {
        const ok = await requireLoginOrRedirect();
        if (!ok) return;

        await autoFillBasicProfile();

        const jobId = qs("jobId") || qs("nKey") || "";
        const resumeId = qs("resumeId") || "";

        $("#careerList")?.appendChild(careerCard());
        $("#licenseList")?.appendChild(licenseCard());

        $("#careerAddBtn")?.addEventListener("click", () => $("#careerList").appendChild(careerCard()));
        $("#licenseAddBtn")?.addEventListener("click", () => $("#licenseList").appendChild(licenseCard()));
        $("#listBtn")?.addEventListener("click", () => (location.href = "/jobs"));

        try {
            if (!jobId) throw new Error("jobId missing");
            const job = await fetchJson(ENDPOINT_JOB_DETAIL(jobId));
            bindJob(job);
        } catch (e) {
            console.error("[apply] job load failed:", e);
            $("#jobTitle").textContent = "지원서 작성";
            $("#SEQ_NO_M210").value = jobId;
        }

        if (resumeId) {
            try {
                const raw = await fetchJson(ENDPOINT_RESUME_ONE(resumeId));
                const resume = toResumeModel(raw);
                bindResume(resume);
                await autoFillBasicProfile();
            } catch (err) {
                console.error("[apply] resume auto load failed:", err);
                alert("이력서를 불러올 수 없습니다.");
            }
        }

        $("#loadResumeBtn")?.addEventListener("click", () => {
            openResumePopup(jobId);
        });

        $("#tempSaveBtn")?.addEventListener("click", async () => {
            try {
                await postForm(ENDPOINT_TEMP, { SEQ_NO_M210: $("#SEQ_NO_M210").value });
                alert("임시 저장되었습니다.");
            } catch (e) {
                console.error("[apply] temp save failed:", e);
                alert("저장할 수 없습니다.");
            }
        });

        $("#applyForm")?.addEventListener("submit", async (e) => {
            e.preventDefault();
            try {
                await postForm(ENDPOINT_SUBMIT, { SEQ_NO_M210: $("#SEQ_NO_M210").value });
                alert("제출 완료되었습니다.");
                location.href = "/jobs";
            } catch (err) {
                console.error("[apply] submit failed:", err);

                // ✅ 백엔드에서 보낸 에러 메시지 표시
                const errorMessage = err.message || "이미 해당 공고에 지원하셨습니다.";

                if (errorMessage.includes("이미 해당 공고에 지원")) {
                    alert("이미 해당 공고에 지원하셨습니다.");
                } else {
                    alert(errorMessage);
                }
            }
        });
    }

    function openResumePopup(jobId) {
        const w = 720;
        const h = 640;
        const left = Math.max(0, (window.screen.width - w) / 2);
        const top = Math.max(0, (window.screen.height - h) / 2);

        const url = `/resume-popup?jobId=${encodeURIComponent(jobId || "")}`;
        const win = window.open(url, "resumePopup", `width=${w},height=${h},left=${left},top=${top},resizable=yes,scrollbars=yes`);

        if (!win) alert("팝업이 차단되었습니다. 팝업 허용해주세요.");
    }

    window.addEventListener("message", async (e) => {
        if (e.origin !== window.location.origin) return;

        const msg = e.data;
        if (!msg || msg.type !== "RESUME_PICKED") return;

        const m110 = msg.m110;
        if (!m110) return;

        try {
            const raw = await fetchJson(`/api/resume/${m110}`);
            const resume = toResumeModel(raw);
            bindResume(resume);
            await autoFillBasicProfile();
            alert("이력서 불러오기 완료");
        } catch (err) {
            console.error(err);
            alert("이력서를 불러올 수 없습니다.");
        }
    });

    document.addEventListener("DOMContentLoaded", init);
})();