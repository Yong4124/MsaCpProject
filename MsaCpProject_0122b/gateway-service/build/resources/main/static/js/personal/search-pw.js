/**
 * 비밀번호 찾기 페이지 JavaScript
 */

//==================================================
// 인증번호 발송
//==================================================
async function sendMail() {
    const form = document.rpForm;

    // 아이디 확인
    if ((form.ID.value).length == 0) {
        alert('아이디를 입력해주세요.');
        form.ID.focus();
        return;
    }

    // 이름 확인
    if ((form.NAME.value).length == 0) {
        alert('이름을 입력해주세요.');
        form.NAME.focus();
        return;
    }

    // 이메일 확인
    if ((form.EMAIL.value).length == 0) {
        alert('이메일을 입력해주세요.');
        form.EMAIL.focus();
        return;
    }

    try {
        // 회원 정보 확인 + 인증번호 발송
        const response = await fetch('/api/personal/send-password-reset', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                loginId: form.ID.value,
                name: form.NAME.value,
                email: form.EMAIL.value
            })
        });

        const data = await response.json();

        if (data.success) {
            alert('인증번호가 발송되었습니다!\n\n[개발 모드]\n콘솔(IntelliJ)에서 인증번호를 확인하세요.\n\n인증번호: ' + data.code);
            form.SendCertiNumber.value = "Y";

            // 인증번호 입력란에 포커스
            form.CERTI_NUM.focus();
        } else {
            alert(data.message || '회원 정보가 일치하지 않습니다.');
        }

    } catch (error) {
        alert('인증번호 발송 중 오류가 발생했습니다.');
        console.error(error);
    }
}

//==================================================
// 인증 확인 후 비밀번호 재설정 페이지로 이동
//==================================================
async function goVerifyAndNext() {
    const form = document.rpForm;

    // 인증번호 발송 확인
    if (form.SendCertiNumber.value != "Y") {
        alert('먼저 인증번호를 발송해주세요.');
        form.EMAIL.focus();
        return;
    }

    // 인증번호 입력 확인
    if ((form.CERTI_NUM.value).length == 0) {
        alert('인증번호를 입력해주세요.');
        form.CERTI_NUM.focus();
        return;
    }

    // 인증번호 자릿수 확인
    if ((form.CERTI_NUM.value).length != 6) {
        alert('인증번호 6자리를 모두 입력해주세요.');
        form.CERTI_NUM.focus();
        return;
    }

    try {
        // 인증번호 확인
        const response = await fetch('/api/personal/verify-password-reset', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                loginId: form.ID.value,
                email: form.EMAIL.value,
                code: form.CERTI_NUM.value
            })
        });

        const data = await response.json();

        if (data.success) {
            // 인증 성공 - 비밀번호 재설정 페이지로 이동
            // 토큰을 URL 파라미터로 전달
            window.location.href = '/reset-pw?token=' + data.token + '&id=' + encodeURIComponent(form.ID.value);
        } else {
            alert(data.message || '인증번호가 일치하지 않습니다.');
            form.CERTI_NUM.value = '';
            form.CERTI_NUM.focus();
        }

    } catch (error) {
        alert('인증 확인 중 오류가 발생했습니다.');
        console.error(error);
    }
}