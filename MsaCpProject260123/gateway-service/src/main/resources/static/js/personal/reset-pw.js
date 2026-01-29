/**
 * 비밀번호 재설정 페이지 JavaScript
 */

//==================================================
// 페이지 로드 시 토큰 및 아이디 확인
//==================================================
window.onload = function() {
    // URL에서 파라미터 가져오기
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    const loginId = urlParams.get('id');

    if (!token || !loginId) {
        alert('잘못된 접근입니다.');
        window.location.href = '/search-pw';
        return;
    }

    document.rpForm.TOKEN.value = token;
    document.rpForm.LOGIN_ID.value = decodeURIComponent(loginId);
};

//==================================================
// Enter 키 처리
//==================================================
function handleEnter(field, event) {
    const keyCode = event.keyCode ? event.keyCode : event.which ? event.which : event.charCode;
    if (keyCode == 13) {
        goResetPassword();
    } else return true;
}

//==================================================
// 공백 입력 방지
//==================================================
function noSpaceForm(obj) {
    const str_space = /\s/;
    if (str_space.exec(obj.value)) {
        obj.focus();
        obj.value = obj.value.replace(' ', '');
        return false;
    }
}

//==================================================
// 비밀번호 재설정
//==================================================
async function goResetPassword() {
    const form = document.rpForm;
    const newPassword = form.NEW_PW.value;
    const confirmPassword = form.NEW_PW_CONFIRM.value;
    const passwordRegex = /^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*?_]).{8,16}$/;

    // 새 비밀번호 입력 확인
    if (newPassword.length == 0) {
        alert('새 비밀번호를 입력해주세요.');
        form.NEW_PW.focus();
        return;
    }

    // 비밀번호 유효성 검사
    if (!passwordRegex.test(newPassword)) {
        alert('비밀번호는 8~16자의 영문, 숫자, 특수기호를 포함해야 합니다.');
        form.NEW_PW.value = '';
        form.NEW_PW_CONFIRM.value = '';
        form.NEW_PW.focus();
        return;
    }

    // 비밀번호 확인 입력 확인
    if (confirmPassword.length == 0) {
        alert('비밀번호 확인을 입력해주세요.');
        form.NEW_PW_CONFIRM.focus();
        return;
    }

    // 비밀번호 일치 확인
    if (newPassword != confirmPassword) {
        alert('비밀번호가 일치하지 않습니다.');
        form.NEW_PW.value = '';
        form.NEW_PW_CONFIRM.value = '';
        form.NEW_PW.focus();
        return;
    }

    try {
        // 비밀번호 변경 API 호출
        const response = await fetch('/api/personal/reset-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                loginId: form.LOGIN_ID.value,
                token: form.TOKEN.value,
                newPassword: newPassword
            })
        });

        const data = await response.json();

        if (data.success) {
            alert('비밀번호가 성공적으로 변경되었습니다.\n새 비밀번호로 로그인해주세요.');
            window.location.href = '/login';
        } else {
            alert(data.message || '비밀번호 변경에 실패했습니다.');

            // 토큰 만료 시 비밀번호 찾기 페이지로
            if (data.message && data.message.includes('토큰')) {
                window.location.href = '/search-pw';
            }
        }

    } catch (error) {
        alert('비밀번호 변경 중 오류가 발생했습니다.');
        console.error(error);
    }
}