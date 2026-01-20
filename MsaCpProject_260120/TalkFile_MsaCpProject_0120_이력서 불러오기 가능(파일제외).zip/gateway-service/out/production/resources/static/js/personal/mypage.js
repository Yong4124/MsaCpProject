//==================================================
// Enum 매핑 (DB 값 <-> HTML value)
//==================================================
const GENDER_MAP = { 'M': '1', 'F': '2', 'O': '3', 'N': '4' };
const GENDER_REVERSE = { '1': 'M', '2': 'F', '3': 'O', '4': 'N' };

const RESIDENCE_MAP = { 'O': '1', 'K': '2', 'U': '3' };
const RESIDENCE_REVERSE = { '1': 'O', '2': 'K', '3': 'U' };

const SERVICE_CATEGORY_MAP = {
    'USFK': '1',
    'KATUSA': '2',
    'CFC_ROK': '3',
    'RETIRED_MND_JCS_SERVICES_ROK': '4'
};
const SERVICE_CATEGORY_REVERSE = {
    '1': 'USFK',
    '2': 'KATUSA',
    '3': 'CFC_ROK',
    '4': 'RETIRED_MND_JCS_SERVICES_ROK'
};

const SERVICE_BRANCH_MAP = {
    'KATUSA': '1',
    'ROK_AIR_FORCE': '2',
    'ROK_ARMY': '3',
    'ROK_MARINE_CORPS': '4',
    'ROK_NAVY': '5',
    'ROKA_SUPPORT_GROUP': '6',
    'US_AIR_FORCE': '7',
    'US_ARMY': '8',
    'US_DOD_PERSONNEL': '9',
    'US_MARINE_CORPS': '10',
    'US_NAVY': '11'
};
const SERVICE_BRANCH_REVERSE = {
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

// 주둔지 이름 배열 (value는 index + 1)
const STATION_NAMES = [
    'Camp Ames', 'Camp Carroll', 'Camp Castle',
    'Camp Chinhae', 'Camp Colbern', 'Camp Eagle',
    'Camp Essayons', 'Camp Garry Owen', 'Camp Giant',
    'Camp Greaves', 'Camp Henry', 'Camp Hialeah',
    'Camp Hovey', 'Camp Howze', 'Camp Jackson',
    'Camp Kitty Hawk', 'Camp Kyle', 'Camp La Guardia',
    'Camp Libby', 'Camp Liberty Bell', 'Camp Long',
    'Camp Market', 'Camp Mobile', 'Camp Mujuk',
    'Camp Nimble', 'Camp Page', 'Camp Pelham',
    'Camp Sears', 'Camp Stanley', 'Camp Stanton',
    'Camp Yongin', 'Chinhae Navy Base', 'CP TANGO',
    'Kunsan Air Base', 'Kangiu(Gwangju) Air Base', 'MEC-Pohang',
    'Osan Air Base', 'Pusan(Busan) Air Base', 'Suwon Air Base',
    'Taegu(daegu) Air Base', 'USAG Casey(Camp Casey)', 'USAG Daegu(Camp Walker)',
    'USAG Humphreys(Camp Humphreys)', 'USAG Red Cloud(Camp Red Cloud)',
    'USAG Yongsan(Includes: Camp Kim & Coiner)', 'Yechon(Yecheon) Air Base',
    'Sachon Air Base', 'Other'
];

//==================================================
// 복무 연도 체크박스 생성 (1950~)
//==================================================
function createServiceYearCheckboxes() {
    const grid = document.getElementById('service-year-grid');
    const currentYear = new Date().getFullYear();
    for (let year = 1950; year <= currentYear; year++) {
        const div = document.createElement('div');
        div.className = 'icheck';

        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.name = 'P_SERVICE_YEAR';
        checkbox.id = 'P_SERVICE_YEAR' + (year - 1949);
        checkbox.value = year;

        const label = document.createElement('label');
        label.htmlFor = 'P_SERVICE_YEAR' + (year - 1949);
        label.textContent = year;

        div.appendChild(checkbox);
        div.appendChild(label);
        grid.appendChild(div);
    }
}

//==================================================
// 복무 주둔지 체크박스 생성
//==================================================
function createServiceStationCheckboxes() {
    const grid = document.getElementById('service-station-grid');
    STATION_NAMES.forEach((station, index) => {
        const div = document.createElement('div');
        div.className = 'icheck';

        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.name = 'P_SERVICE_STATION';
        checkbox.id = 'P_SERVICE_STATION' + (index + 1);
        checkbox.value = (index + 1);  // value는 숫자 (1~49)

        const label = document.createElement('label');
        label.htmlFor = 'P_SERVICE_STATION' + (index + 1);
        label.textContent = station;

        div.appendChild(checkbox);
        div.appendChild(label);
        grid.appendChild(div);
    });
}

//==================================================
// 페이지 로드 시 체크박스 생성 및 회원 정보 조회
//==================================================
window.onload = async function() {
    // 체크박스 생성
    createServiceYearCheckboxes();
    createServiceStationCheckboxes();

    // 회원 정보 로드
    await loadMemberInfo();
};

//==================================================
// 회원 정보 조회
//==================================================
async function loadMemberInfo() {
    try {
        const response = await fetch('/api/personal/my-info', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        // 로그인 안 되어 있으면 로그인 페이지로
        if (response.status === 401 || response.status === 403) {
            alert('로그인이 필요합니다.');
            window.location.href = '/login';
            return;
        }

        const data = await response.json();

        if (data.success) {
            const member = data.member;

            // 기본 정보
            document.getElementById('ID').value = member.loginId || '';
            document.getElementById('NAME').value = member.name || '';
            document.getElementById('BIRTH_DATE').value = member.birthDate || '';

            // 성별 (Enum → HTML value 변환)
            if (member.gender && GENDER_MAP[member.gender]) {
                const genderValue = GENDER_MAP[member.gender];
                const genderRadio = document.querySelector(`input[name="P_GENDER"][value="${genderValue}"]`);
                if (genderRadio) {
                    genderRadio.checked = true;
                }
            }

            // 거주지 (Enum → HTML value 변환)
            if (member.residence && RESIDENCE_MAP[member.residence]) {
                const residenceValue = RESIDENCE_MAP[member.residence];
                const residenceRadio = document.querySelector(`input[name="P_RESIDENCE"][value="${residenceValue}"]`);
                if (residenceRadio) {
                    residenceRadio.checked = true;
                }
            }

            // 복무 정보
            document.getElementById('LAST_RANK').value = member.lastRank || '';

            // 복무 형태 (Enum → HTML value 변환)
            if (member.serviceCategory && SERVICE_CATEGORY_MAP[member.serviceCategory]) {
                document.getElementById('P_SERVICE_CATEGORY').value = SERVICE_CATEGORY_MAP[member.serviceCategory];
            }

            // 소속 군 (Enum → HTML value 변환)
            if (member.serviceBranch && SERVICE_BRANCH_MAP[member.serviceBranch]) {
                document.getElementById('P_SERVICE_BRANCH').value = SERVICE_BRANCH_MAP[member.serviceBranch];
            }

            // 복무 연도 체크박스 선택
            if (member.serviceYear) {
                const years = member.serviceYear.split('|');
                years.forEach(year => {
                    const checkbox = document.querySelector(`input[name="P_SERVICE_YEAR"][value="${year.trim()}"]`);
                    if (checkbox) {
                        checkbox.checked = true;
                    }
                });
            }

            // 복무 주둔지 체크박스 선택
            if (member.serviceStation) {
                console.log('DB 주둔지 데이터:', member.serviceStation);
                const stations = member.serviceStation.split('|');
                stations.forEach(stationValue => {
                    const trimmedValue = stationValue.trim();
                    console.log('주둔지 값:', trimmedValue);

                    // 숫자인지 확인 (DB에 숫자로 저장된 경우)
                    if (!isNaN(trimmedValue) && trimmedValue !== '') {
                        // 숫자로 저장된 경우 - 바로 체크
                        const checkbox = document.querySelector(`input[name="P_SERVICE_STATION"][value="${trimmedValue}"]`);
                        if (checkbox) {
                            checkbox.checked = true;
                            console.log('체크 완료 (숫자):', trimmedValue, STATION_NAMES[parseInt(trimmedValue) - 1]);
                        } else {
                            console.warn('체크박스를 찾을 수 없음:', trimmedValue);
                        }
                    } else {
                        // 텍스트로 저장된 경우 - 이름으로 찾기
                        const stationIndex = STATION_NAMES.findIndex(name =>
                            name.toLowerCase() === trimmedValue.toLowerCase()
                        );

                        if (stationIndex !== -1) {
                            const stationNumValue = stationIndex + 1;
                            const checkbox = document.querySelector(`input[name="P_SERVICE_STATION"][value="${stationNumValue}"]`);
                            if (checkbox) {
                                checkbox.checked = true;
                                console.log('체크 완료 (텍스트):', STATION_NAMES[stationIndex]);
                            }
                        } else {
                            console.warn('주둔지를 찾을 수 없음:', trimmedValue);
                        }
                    }
                });
            }

            document.getElementById('UNIT_POSITON').value = member.unitPosition || '';
            document.getElementById('EMAIL').value = member.email || '';

            document.rpForm.SendCertiNumber.value = "";

        } else {
            alert('회원 정보를 불러오는데 실패했습니다.');
        }

    } catch (error) {
        console.error('Error loading member info:', error);
        alert('로그인이 필요합니다.');
        window.location.href = '/login';
    }
}

//==================================================
// 공백 입력 방지
//==================================================
function noSpaceForm(obj) {
    var str_space = /\s/;
    if (str_space.exec(obj.value)) {
        obj.focus();
        obj.value = obj.value.replace(' ', '');
        return false;
    }
}

//==================================================
// 이메일 인증번호 발송
//==================================================
async function sendMail() {
    var form = document.rpForm;

    if ((form.EMAIL.value).length == 0) {
        alert('이메일을 입력해주세요.');
        form.EMAIL.focus();
        return;
    }

    try {
        const response = await fetch('/api/personal/send-email-verification', {
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
            alert('인증번호가 발송되었습니다!\n\n[개발 모드]\n콘솔에서 인증번호를 확인하세요.\n\n인증번호: ' + data.code);
            form.SendCertiNumber.value = data.code;
            form.CERTI_NUM.focus();
        } else {
            alert(data.message || '인증번호 발송에 실패했습니다.');
        }

    } catch (error) {
        alert('인증번호 발송 중 오류가 발생했습니다.');
        console.error(error);
    }
}

//==================================================
// 회원정보 수정
//==================================================
async function goWriteAct() {
    var form = document.rpForm;
    var i;
    var passwordRegex = /^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*?_]).{8,16}$/;

    // 이름 확인
    if ((form.NAME.value).length == 0) {
        alert('이름을 입력해주세요.');
        form.NAME.focus();
        return;
    }

    // 비밀번호 변경에 원하는 경우
    if (document.getElementsByName('PW_AGREE')[0].checked) {
        const newPassword = form.PW1.value;
        const confirmPassword = form.PW2.value;

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
            if (newPassword == confirmPassword) {
                form.PW.value = newPassword;
            } else {
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
    }

    // 생년월일 확인
    if ((form.BIRTH_DATE.value).length == 0) {
        alert('생년월일 입력해주세요.');
        form.BIRTH_DATE.focus();
        return;
    }

    // 성별 확인 (HTML value → Enum 변환)
    for (i = 0; i < document.getElementsByName('P_GENDER').length; i++) {
        if (document.getElementsByName('P_GENDER')[i].checked) {
            const htmlValue = document.getElementsByName('P_GENDER')[i].value;
            form.GENDER.value = GENDER_REVERSE[htmlValue];
        }
    }
    if ((form.GENDER.value).length == 0) {
        alert('성별을 입력해주세요.');
        return;
    }

    // 거주지 확인 (HTML value → Enum 변환)
    for (i = 0; i < document.getElementsByName('P_RESIDENCE').length; i++) {
        if (document.getElementsByName('P_RESIDENCE')[i].checked) {
            const htmlValue = document.getElementsByName('P_RESIDENCE')[i].value;
            form.RESIDENCE.value = RESIDENCE_REVERSE[htmlValue];
        }
    }
    if ((form.RESIDENCE.value).length == 0) {
        alert('현재 거주지를 입력해주세요.');
        return;
    }

    // 계급/직급
    if ((form.LAST_RANK.value).length == 0) {
        alert('현재 또는 전역 당시 계급/직급/등급을 입력해주세요.');
        form.LAST_RANK.focus();
        return;
    }

    // 복무 형태 (HTML value → Enum 변환)
    var ServiceCategory = document.getElementById('P_SERVICE_CATEGORY');
    const categoryHtmlValue = ServiceCategory.options[ServiceCategory.selectedIndex].value;
    form.SERVICE_CATEGORY.value = SERVICE_CATEGORY_REVERSE[categoryHtmlValue];
    if ((form.SERVICE_CATEGORY.value).length == 0) {
        alert('한국 복무 당시 복무 형태를 입력해주세요.');
        return;
    }

    // 소속 군 (HTML value → Enum 변환)
    var ServiceBranch = document.getElementById('P_SERVICE_BRANCH');
    const branchHtmlValue = ServiceBranch.options[ServiceBranch.selectedIndex].value;
    form.SERVICE_BRANCH.value = SERVICE_BRANCH_REVERSE[branchHtmlValue];
    if ((form.SERVICE_BRANCH.value).length == 0) {
        alert('한국 복무 당시 소속 군을 입력해주세요.');
        return;
    }

    // 복무 연도
    var ServiceYear = document.getElementsByName('P_SERVICE_YEAR');
    form.SERVICE_YEAR.value = ""; // 초기화
    for (i = 0; i < document.getElementsByName('P_SERVICE_YEAR').length; i++) {
        if (ServiceYear[i].checked) {
            if ((form.SERVICE_YEAR.value).length == 0)
                form.SERVICE_YEAR.value += ServiceYear[i].value;
            else
                form.SERVICE_YEAR.value = form.SERVICE_YEAR.value + "|" + ServiceYear[i].value;
        }
    }
    if ((form.SERVICE_YEAR.value).length == 0) {
        alert('한국에서 복무한 연도를 입력해주세요.');
        return;
    }

    // 복무 주둔지 (숫자로 저장)
    var ServiceStation = document.getElementsByName('P_SERVICE_STATION');
    form.SERVICE_STATION.value = ""; // 초기화
    for (i = 0; i < document.getElementsByName('P_SERVICE_STATION').length; i++) {
        if (ServiceStation[i].checked) {
            const stationValue = ServiceStation[i].value;  // 숫자 그대로
            if ((form.SERVICE_STATION.value).length == 0)
                form.SERVICE_STATION.value += stationValue;
            else
                form.SERVICE_STATION.value = form.SERVICE_STATION.value + "|" + stationValue;
        }
    }
    if ((form.SERVICE_STATION.value).length == 0) {
        alert('한국에서 복무한 주둔지를 입력해주세요.');
        return;
    }

    // 부대 및 직책
    if ((form.UNIT_POSITON.value).length == 0) {
        alert('부대 및 직책을 입력해주세요.');
        form.UNIT_POSITON.focus();
        return;
    }

    // 이메일
    if (form.EMAIL.value == "") {
        alert("이메일을 입력해주세요.");
        form.EMAIL.focus();
        return;
    }

    // 인증번호 확인
    if (form.CERTI_NUM.value == "") {
        alert("인증번호를 입력해주세요.");
        form.CERTI_NUM.focus();
        return;
    }
    if (form.CERTI_NUM.value != form.SendCertiNumber.value) {
        alert("인증번호가 일치하지 않습니다. 다시 입력해주세요.");
        form.CERTI_NUM.focus();
        return;
    }

    if (!confirm('회원정보를 수정하시겠습니까?')) {
        return;
    }

    try {
        const updateData = {
            gender: form.GENDER.value,
            residence: form.RESIDENCE.value,
            lastRank: form.LAST_RANK.value,
            serviceCategory: form.SERVICE_CATEGORY.value,
            serviceBranch: form.SERVICE_BRANCH.value,
            serviceYear: form.SERVICE_YEAR.value,
            serviceStation: form.SERVICE_STATION.value,
            unitPosition: form.UNIT_POSITON.value,
            email: form.EMAIL.value
        };

        // 비밀번호 변경 시 추가
        if (form.PW.value.length > 0) {
            updateData.newPassword = form.PW.value;
        }

        const response = await fetch('/api/personal/update-info', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updateData)
        });

        const data = await response.json();

        if (data.success) {
            alert('회원정보가 성공적으로 수정되었습니다.');
            window.location.reload();
        } else {
            alert(data.message || '회원정보 수정에 실패했습니다.');
        }

    } catch (error) {
        alert('회원정보 수정 중 오류가 발생했습니다.');
        console.error(error);
    }
}

//==================================================
// 회원 탈퇴
//==================================================
async function goDeleteAct() {
    if (!confirm('회원을 탈퇴 하시겠습니까?')) {
        return;
    }

    const password = prompt('회원 탈퇴를 위해 비밀번호를 입력해주세요:');
    if (!password) {
        return;
    }

    try {
        const response = await fetch('/api/personal/delete-account', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                password: password
            })
        });

        const data = await response.json();

        if (data.success) {
            alert('회원 탈퇴가 완료되었습니다.');
            window.location.href = '/';
        } else {
            alert(data.message || '회원 탈퇴에 실패했습니다.');
        }

    } catch (error) {
        alert('회원 탈퇴 중 오류가 발생했습니다.');
        console.error(error);
    }
}