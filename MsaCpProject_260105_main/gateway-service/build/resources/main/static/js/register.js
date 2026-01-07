/**
 * 회원가입 페이지 JavaScript
 */

//---------------------------------------------------------------------
// 아이디 조회 버튼 클릭
//---------------------------------------------------------------------
async function userIdChk() {
    const form = document.rpForm;
    let idRegex;

    //-------------------------------------------------------------
    // 아이디 유효성 검사
    //-------------------------------------------------------------
    if ((form.ID.value).length == 0) {
        alert('아이디를 입력해주세요.');
        form.ID.value = "";
        form.ID.focus();
        return;
    }

    idRegex = /^[A-Za-z0-9]{6,}$/;
    if (idRegex.test(form.ID.value) == false) {
        alert('아이디는 6자 이상의 영문, 숫자를 포함해야 합니다.');
        form.ID.value = "";
        form.ID.focus();
        return;
    }

    // REST API로 ID 중복 체크
    try {
        const response = await fetch(`/api/personal/check-id/${form.ID.value}`);
        const data = await response.json();

        if (data.exists) {
            alert('이미 사용중인 ID입니다.');
            form.userIdIdx.value = "N";
        } else {
            alert('사용 가능한 ID입니다.');
            form.userIdIdx.value = "Y";
        }
    } catch (error) {
        alert('중복 확인 중 오류가 발생했습니다.');
    }
}

//---------------------------------------------------------------------
// 공백 입력 방지
//---------------------------------------------------------------------
function noSpaceForm(obj) {
    const str_space = /\s/;  // 공백체크
    if (str_space.exec(obj.value)) { //공백 체크
        obj.focus();
        obj.value = obj.value.replace(' ', ''); // 공백제거
        return false;
    }
}

//==================================================
// 인증번호 발송
//==================================================
async function sendMail() {
    const form = document.rpForm;

    // 이메일 입력 확인
    if ((form.EMAIL.value).length == 0) {
        alert('이메일을 입력해주세요.');
        form.EMAIL.focus();
        return;
    }

    // 이메일 유효성 검사
    if (form.EMAIL.value != '' && emailCheck(form.EMAIL.value) == false) {
        alert('올바른 이메일 주소를 입력해주세요.');
        form.EMAIL.value = "";
        form.EMAIL.focus();
        return;
    }

    try {
        // 인증번호 발송 API 호출
        const response = await fetch('/api/personal/send-verification', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: form.EMAIL.value
            })
        });

        const data = await response.json();

        if (data.success) {
            alert('인증번호가 발송되었습니다!\n\n[개발 모드]\n콘솔(IntelliJ)에서 인증번호를 확인하세요.\n\n인증번호: ' + data.code);
            form.SendCertiNumber.value = "Y"; // 발송 완료 표시
        } else {
            alert(data.message || '인증번호 발송에 실패했습니다.');
        }
    } catch (error) {
        alert('인증번호 발송 중 오류가 발생했습니다.');
        console.error(error);
    }
}

//---------------------------------------------------------------------
// 이메일 유효성 검사
//---------------------------------------------------------------------
function emailCheck(emailad) {
    const exclude = /[^@\-\.\w]|^[_@\.\-]|[\._\-]{2}|[@\.]{2}|(@)[^@]*\1/;
    const check = /@[\w\-]+\./;
    const checkend = /\.[a-zA-Z]{2,3}$/;

    if (((emailad.search(exclude) != -1) || (emailad.search(check)) == -1) || (emailad.search(checkend) == -1))
        return false;
    else
        return true;
}

//---------------------------------------------------------------------
// 개인회원 정보 등록
//---------------------------------------------------------------------
async function goWriteAct() {
    const form = document.rpForm;
    let i;
    let ServiceCategory, ServiceBranch, ServiceYear, ServiceStation;
    let idRegex;
    let newPassword, confirmPassword, passwordRegex;

    //-------------------------------------------------------------
    // 아이디 유효성 검사
    //-------------------------------------------------------------
    if (form.userIdIdx.value == "N") {
        alert("아이디를 중복확인해 주세요.");
        form.ID.focus();
        return;
    }

    //-------------------------------------------------------------
    // 이름 검사 (통합)
    //-------------------------------------------------------------
    if ((form.NAME.value).length == 0) {
        alert('이름을 입력해주세요.');
        form.NAME.focus();
        return;
    }

    //-------------------------------------------------------------
    // 비밀번호 유효성 검사
    //-------------------------------------------------------------
    newPassword = form.PW1.value;
    confirmPassword = form.PW2.value;
    passwordRegex = /^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*?_]).{8,16}$/;

    if (newPassword.length == 0) {
        alert('비밀번호를 입력해주세요.');
        form.PW1.focus();
        return;
    }

    if (confirmPassword.length == 0) {
        alert('비밀번호 확인을 입력해주세요.');
        form.PW2.focus();
        return;
    }

    if (passwordRegex.test(newPassword)) {
        if (newPassword != confirmPassword) {
            alert("비밀번호가 일치하지 않습니다.");
            form.PW1.value = "";
            form.PW2.value = "";
            form.PW1.focus();
            return;
        }
    } else {
        alert("비밀번호는 8~16자의 영문, 숫자, 특수기호를 포함해야 합니다.");
        form.PW1.value = "";
        form.PW2.value = "";
        form.PW1.focus();
        return;
    }

    form.PW.value = newPassword;

    //-------------------------------------------------------------
    // 생년월일 검사
    //-------------------------------------------------------------
    if ((form.BIRTH_DATE.value).length == 0) {
        alert('생년월일을 입력해주세요.');
        form.BIRTH_DATE.focus();
        return;
    }

    //-------------------------------------------------------------
    // 성별 검사
    //-------------------------------------------------------------
    let genderChk = 0;
    for (i = 0; i < document.getElementsByName('P_GENDER').length; i++) {
        if (document.getElementsByName('P_GENDER')[i].checked) {
            form.GENDER.value = document.getElementsByName('P_GENDER')[i].value;
            genderChk = 1;
        }
    }
    if (genderChk == 0) {
        alert('성별을 선택해주세요.');
        return;
    }

    //-------------------------------------------------------------
    // 현재 거주지 검사
    //-------------------------------------------------------------
    let residenceChk = 0;
    for (i = 0; i < document.getElementsByName('P_RESIDENCE').length; i++) {
        if (document.getElementsByName('P_RESIDENCE')[i].checked) {
            form.RESIDENCE.value = document.getElementsByName('P_RESIDENCE')[i].value;
            residenceChk = 1;
        }
    }
    if (residenceChk == 0) {
        alert('현재 거주지를 선택해주세요.');
        return;
    }

    //-------------------------------------------------------------
    // 복무형태 검사
    //-------------------------------------------------------------
    if (form.P_SERVICE_CATEGORY.value == "") {
        alert('한국 복무 형태를 선택해주세요.');
        form.P_SERVICE_CATEGORY.focus();
        return;
    }
    form.SERVICE_CATEGORY.value = form.P_SERVICE_CATEGORY.value;

    //-------------------------------------------------------------
    // 소속 군 검사
    //-------------------------------------------------------------
    if (form.P_SERVICE_BRANCH.value == "") {
        alert('한국 복무 당시 소속 군을 선택해주세요.');
        form.P_SERVICE_BRANCH.focus();
        return;
    }
    form.SERVICE_BRANCH.value = form.P_SERVICE_BRANCH.value;

    //-------------------------------------------------------------
    // 복무 연도 검사
    //-------------------------------------------------------------
    ServiceYear = "";
    let serviceYearChk = 0;
    for (i = 0; i < document.getElementsByName('P_SERVICE_YEAR').length; i++) {
        if (document.getElementsByName('P_SERVICE_YEAR')[i].checked) {
            if (ServiceYear == "") {
                ServiceYear += document.getElementsByName('P_SERVICE_YEAR')[i].value;
            } else {
                ServiceYear += "|" + document.getElementsByName('P_SERVICE_YEAR')[i].value;
            }
            serviceYearChk = 1;
        }
    }
    if (serviceYearChk == 0) {
        alert('한국에서 복무한 연도를 선택해주세요.');
        return;
    }
    form.SERVICE_YEAR.value = ServiceYear;

    //-------------------------------------------------------------
    // 주둔지 검사
    //-------------------------------------------------------------
    ServiceStation = "";
    let serviceStationChk = 0;
    for (i = 0; i < document.getElementsByName('P_SERVICE_STATION').length; i++) {
        if (document.getElementsByName('P_SERVICE_STATION')[i].checked) {
            if (ServiceStation == "") {
                ServiceStation += document.getElementsByName('P_SERVICE_STATION')[i].value;
            } else {
                ServiceStation += "|" + document.getElementsByName('P_SERVICE_STATION')[i].value;
            }
            serviceStationChk = 1;
        }
    }
    if (serviceStationChk == 0) {
        alert('한국에서 복무한 주둔지를 선택해주세요.');
        return;
    }
    form.SERVICE_STATION.value = ServiceStation;

    //-------------------------------------------------------------
    // 개인정보 동의 검사
    //-------------------------------------------------------------
    let agreeChk = 0;
    for (i = 0; i < document.getElementsByName('AGREE').length; i++) {
        if (document.getElementsByName('AGREE')[i].checked) {
            if (document.getElementsByName('AGREE')[i].value == "1") {
                agreeChk = 1;
            }
        }
    }
    if (agreeChk == 0) {
        alert('개인정보 수집, 이용을 동의해 주세요.');
        return;
    }

    //-------------------------------------------------------------
    // 이메일 검사
    //-------------------------------------------------------------
    if ((form.EMAIL.value).length == 0) {
        alert('이메일을 입력해주세요.');
        form.EMAIL.focus();
        return;
    } else if (form.EMAIL.value != '' && emailCheck(form.EMAIL.value) == false) {
        alert('올바른 이메일 주소를 입력해주세요.');
        form.EMAIL.value = "";
        form.EMAIL.focus();
        return;
    }

    //-------------------------------------------------------------
    //  이메일 인증번호 발송 확인
    //-------------------------------------------------------------
    if (form.SendCertiNumber.value != "Y") {
        alert('이메일 인증번호를 발송해주세요.');
        form.EMAIL.focus();
        return;
    }

    //-------------------------------------------------------------
    // 인증번호 입력 확인
    //-------------------------------------------------------------
    if ((form.CERTI_NUM.value).length == 0) {
        alert('인증번호를 입력해주세요.');
        form.CERTI_NUM.focus();
        return;
    }

    //-------------------------------------------------------------
    // 인증번호 확인
    //-------------------------------------------------------------
    try {
        const verifyResponse = await fetch('/api/personal/verify-code', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: form.EMAIL.value,
                code: form.CERTI_NUM.value
            })
        });

        const verifyData = await verifyResponse.json();

        if (!verifyData.success) {
            alert(verifyData.message || '인증번호가 일치하지 않습니다.');
            form.CERTI_NUM.focus();
            return;
        }

        // 인증 성공 - 계속 진행
        console.log('✅ 이메일 인증 성공!');

    } catch (error) {
        alert('인증번호 확인 중 오류가 발생했습니다.');
        console.error(error);
        return;
    }

    //-------------------------------------------------------------
    // 회원가입 처리
    //-------------------------------------------------------------
    if (confirm('회원정보를 등록하시겠습니까?')) {
        // 성별 값 변환 (1,2,3,4 → M, F, O, N)
        const genderMap = {
            '1': 'M',
            '2': 'F',
            '3': 'O',
            '4': 'N'
        };
        const genderValue = genderMap[form.GENDER.value] || form.GENDER.value;

        // 거주지 값 변환 (1,2,3 → O, K, U)
        const residenceMap = {
            '1': 'O',
            '2': 'K',
            '3': 'U'
        };
        const residenceValue = residenceMap[form.RESIDENCE.value] || form.RESIDENCE.value;

        // 복무형태 값 변환
        const serviceCategoryMap = {
            '1': 'USFK',
            '2': 'KATUSA',
            '3': 'CFC_ROK',
            '4': 'RETIRED_MND_JCS_SERVICES_ROK'
        };
        const serviceCategoryValue = serviceCategoryMap[form.SERVICE_CATEGORY.value] || form.SERVICE_CATEGORY.value;

        // 소속군 값 변환
        const serviceBranchMap = {
            '1': 'KATUSA',
            '2': 'ROK_AIR_FORCE',
            '3': 'ROK_ARMY',
            '4': 'ROK_MARINE_CORPS',
            '5': 'ROK_NAVY',
            '6': 'ROKA_SUPPORT_GROUP',
            '7': 'US_AIR_FORCE',
            '8': 'US_ARMY',
            '9': 'US_DOD_PERSONNEL',
            '10': 'US_MARINE_CORPS',
            '11': 'US_NAVY'
        };
        const serviceBranchValue = serviceBranchMap[form.SERVICE_BRANCH.value] || form.SERVICE_BRANCH.value;

        const formData = {
            loginId: form.ID.value,
            pw: form.PW.value,
            name: form.NAME.value,
            birthDate: form.BIRTH_DATE.value,
            gender: genderValue,
            email: form.EMAIL.value,
            residence: residenceValue,
            lastRank: form.LAST_RANK.value,
            serviceCategory: serviceCategoryValue,
            serviceBranch: serviceBranchValue,
            serviceYear: form.SERVICE_YEAR.value,
            serviceStation: form.SERVICE_STATION.value,
            unitPosition: form.UNIT_POSITON.value
        };

        try {
            const response = await fetch('/api/personal/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            const data = await response.json();

            if (data.success) {
                alert('회원가입이 완료되었습니다!');
                window.location.href = '/login.html';
            } else {
                alert(data.message || '회원가입에 실패했습니다.');
            }
        } catch (error) {
            alert('회원가입 중 오류가 발생했습니다.');
            console.error(error);
        }
    }
    return;
}