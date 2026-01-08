/* ============================================
   공통 JavaScript - common.js
   JWT 기반 로그인 상태 확인 및 UI 제어
   ============================================ */

/**
 * 로그아웃 처리
 */
window.logout = async function() {
    if (!confirm('로그아웃 하시겠습니까?')) {
        return;
    }

    try {
        // Personal 로그아웃 시도
        await fetch('/api/personal/logout', {
            method: 'POST',
            credentials: 'include'
        }).catch(() => {});

        // Company 로그아웃 시도
        await fetch('/api/company/logout', {
            method: 'POST',
            credentials: 'include'
        }).catch(() => {});

        alert('로그아웃 되었습니다.');
        window.location.href = '/';

    } catch (error) {
        console.error('로그아웃 오류:', error);
        window.location.href = '/';
    }
};

/**
 * 로그인 상태 확인 및 UI 업데이트
 * - JWT 토큰을 이용한 세션 체크
 * - Personal/Company 각각 확인
 */
async function checkLoginStatus() {
    try {
        // 1. Personal 세션 체크
        const personalRes = await fetch('/api/personal/check-session', {
            method: 'GET',
            credentials: 'include'
        });

        if (personalRes.ok) {
            const personalData = await personalRes.json();
            console.log('Personal 체크:', personalData);

            if (personalData.loggedIn && personalData.memberType === 'PERSONAL') {
                updateLoginUI(personalData.loginId, '개인회원');
                return;
            }
        }

        // 2. Company 세션 체크
        const companyRes = await fetch('/api/company/check-session', {
            method: 'GET',
            credentials: 'include'
        });

        if (companyRes.ok) {
            const companyData = await companyRes.json();
            console.log('Company 체크:', companyData);

            if (companyData.loggedIn && companyData.memberType === 'COMPANY') {
                updateLoginUI(companyData.loginId, '기업회원');
                return;
            }
        }

    } catch (error) {
        console.log('로그인 상태 체크 오류:', error);
    }
}

/**
 * 로그인 UI 업데이트 (PC + 모바일)
 * @param {string} userName - 사용자 이름(ID)
 * @param {string} memberType - 회원 유형 (개인회원/기업회원)
 */
function updateLoginUI(userName, memberType) {
    // ========== PC 헤더 업데이트 ==========
    const loginLink = document.getElementById('loginLink');
    const registerLink = document.getElementById('registerLink');

    if (loginLink) loginLink.style.display = 'none';
    if (registerLink) registerLink.style.display = 'none';

    const userInfo = document.getElementById('userInfo');
    const logoutBtn = document.getElementById('logoutBtn');

    if (userInfo) {
        userInfo.textContent = `${userName}님 (${memberType})`;
        userInfo.style.display = 'inline';
    }

    if (logoutBtn) {
        logoutBtn.style.display = 'inline';
    }

    // ========== 모바일 메뉴 업데이트 ==========
    const mLoginLink = document.getElementById('mLoginLink');
    const mRegisterLink = document.getElementById('mRegisterLink');

    if (mLoginLink) mLoginLink.style.display = 'none';
    if (mRegisterLink) mRegisterLink.style.display = 'none';

    const mUserInfo = document.getElementById('mUserInfo');
    const mLogoutBtn = document.getElementById('mLogoutBtn');

    if (mUserInfo) {
        mUserInfo.textContent = `${userName}님 (${memberType})`;
        mUserInfo.style.display = 'inline';
        mUserInfo.style.marginRight = '20px';
    }

    if (mLogoutBtn) {
        mLogoutBtn.style.display = 'inline';
    }
}

/**
 * 마이페이지 이동 (개인/기업 분기 처리)
 * - Personal 로그인 → /mypage.html
 * - Company 로그인 → /company_mypage.html
 * - 미로그인 → /login.html
 */
window.goToMyPage = async function() {
    console.log('🔍 마이페이지 세션 체크 시작...');

    // 1. Personal 세션 체크
    try {
        const personalRes = await fetch('/api/personal/check-session', {
            method: 'GET',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' }
        });

        if (personalRes.ok) {
            const personalData = await personalRes.json();
            console.log('✅ Personal 세션:', personalData);

            if (personalData.loggedIn && personalData.memberType === 'PERSONAL') {
                console.log('➡️ Personal 마이페이지로 이동');
                window.location.href = '/mypage';
                return;
            }
        }
    } catch (e) {
        console.log('⚠️ Personal 세션 체크 오류:', e.message);
    }

    // 2. Company 세션 체크
    try {
        const companyRes = await fetch('/api/company/check-session', {
            method: 'GET',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' }
        });

        if (companyRes.ok) {
            const companyData = await companyRes.json();
            console.log('✅ Company 세션:', companyData);

            if (companyData.loggedIn && companyData.memberType === 'COMPANY') {
                console.log('➡️ Company 마이페이지로 이동');
                window.location.href = '/company/mypage';
                return;
            }
        }
    } catch (e) {
        console.log('⚠️ Company 세션 체크 오류:', e.message);
    }

    // 3. 둘 다 로그인 안 됨
    console.log('⛔ 로그인 필요 → login.html로 이동');
    alert('로그인이 필요합니다.');
    window.location.href = '/login';
};

/**
 * 페이지 로드 시 초기화
 */
document.addEventListener('DOMContentLoaded', function() {
    // 로그인 상태 확인
    checkLoginStatus();

    // 모바일 메뉴 토글
    const menuBtn = document.querySelector('.ac-allmenu');
    const totalMenu = document.querySelector('.total_menu');

    if (menuBtn) {
        menuBtn.addEventListener('click', function() {
            document.body.classList.toggle('is-nav');
            console.log('모바일 메뉴 토글!');
        });
    }

    // 모바일 메뉴 배경 클릭 시 닫기
    if (totalMenu) {
        totalMenu.addEventListener('click', function(e) {
            if (e.target === totalMenu) {
                document.body.classList.remove('is-nav');
            }
        });
    }
});