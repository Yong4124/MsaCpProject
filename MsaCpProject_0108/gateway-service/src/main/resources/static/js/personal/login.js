/**
 * 로그인 페이지 JavaScript
 */

//==================================================
// Enter 키 처리
//==================================================
function handleEnter(field, event) {
    const keyCode = event.keyCode ? event.keyCode : event.which ? event.which : event.charCode;
    if (keyCode == 13) {
        goLoginAct();
    } else return true;
}

//==================================================
// 로그인 처리
//==================================================
async function goLoginAct() {
    const form = document.rpForm;

    // 아이디 검증
    if ((form.ID.value).length == 0) {
        alert('아이디를 입력해주세요.');
        form.ID.value = "";
        form.ID.focus();
        return;
    }

    // 비밀번호 검증
    if ((form.PW.value).length == 0) {
        alert('비밀번호를 입력해주세요.');
        form.PW.value = "";
        form.PW.focus();
        return;
    }

    // 로그인 요청
    const loginData = {
        loginId: form.ID.value,
        password: form.PW.value
    };

    try {
        const response = await fetch('/api/personal/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginData)
        });

        const data = await response.json();

        if (data.success) {
            // 메인 페이지로 이동
            window.location.href = '/';
        } else {
            alert(data.message || '로그인에 실패했습니다.');
            form.PW.value = "";
            form.PW.focus();
        }
    } catch (error) {
        alert('로그인 중 오류가 발생했습니다.');
        console.error(error);
    }
}