/**
 * 아이디 찾기 페이지 JavaScript
 */

//==================================================
// Enter 키 처리
//==================================================
function handleEnter(field, event) {
    const keyCode = event.keyCode ? event.keyCode : event.which ? event.which : event.charCode;
    if (keyCode == 13) {
        goSearchAct();
    } else return true;
}

//==================================================
// 아이디 찾기
//==================================================
async function goSearchAct() {
    const form = document.rpForm;

    // 이름 입력 확인
    if ((form.NAME.value).length == 0) {
        alert('이름을 입력해주세요.');
        form.NAME.focus();
        return;
    }

    // 이메일 입력 확인
    if ((form.EMAIL.value).length == 0) {
        alert('이메일을 입력해주세요.');
        form.EMAIL.focus();
        return;
    }

    try {
        // 아이디 찾기 API 호출
        const response = await fetch('/api/personal/find-id', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                name: form.NAME.value,
                email: form.EMAIL.value
            })
        });

        const data = await response.json();

        if (data.success) {
            // 찾은 아이디 표시
            alert('회원님의 아이디는 "' + data.loginId + '" 입니다.');

            // 로그인 페이지로 이동
            if (confirm('로그인 페이지로 이동하시겠습니까?')) {
                window.location.href = '/login.html';
            }
        } else {
            alert(data.message || '일치하는 회원 정보가 없습니다.');
        }

    } catch (error) {
        alert('아이디 찾기 중 오류가 발생했습니다.');
        console.error(error);
    }
}