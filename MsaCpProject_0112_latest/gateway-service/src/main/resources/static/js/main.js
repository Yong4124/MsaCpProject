/* ============================
   Main Page Scripts
   ============================ */

const CONFIG = {
    jobsApi: "/api/public/jobs",
    noticesApi: "/api/main/notices",
    jidoJsonUrl: "/js/jido_company_info.json",
    logoBasePath: "/img/main/jido_logo/",
    defaultThumbImages: [
        "/img/main/mex_img1.png",
        "/img/main/mex_img2.png",
        "/img/main/mex_img3.png",
        "/img/main/mex_img4.png",
        "/img/main/mex_img5.png",
        "/img/main/mex_img6.png",
        "/img/main/mex_img7.png",
        "/img/main/mex_img8.png"
    ]
};

function getDefaultThumbImage(index) {
    return CONFIG.defaultThumbImages[index % CONFIG.defaultThumbImages.length];
}

/* ============================
   Swiper Initialization
   ============================ */
function initSwiper() {
    if (typeof Swiper === "undefined") return;

    new Swiper(".mySwiper", {
        spaceBetween: 30,
        centeredSlides: true,
        loop: true,
        autoplay: { delay: 3500, disableOnInteraction: false },
        speed: 2500,
        on: {
            init: function () {
                document.querySelector(".total").textContent = String(this.slides.length).padStart(2, "0");
                document.querySelector(".current").textContent = String(this.realIndex + 1).padStart(2, "0");
            },
            slideChangeTransitionStart: function () {
                const fill = document.querySelector(".bar .fill");
                if (fill) fill.style.width = "0%";
            },
            slideChange: function () {
                const cur = document.querySelector(".current");
                if (cur) cur.textContent = String(this.realIndex + 1).padStart(2, "0");
            },
            autoplayTimeLeft: function (_s, _time, progress) {
                const fill = document.querySelector(".bar .fill");
                if (fill) fill.style.width = ((1 - progress) * 100) + "%";
            }
        }
    });
}

/* ============================
   Search Function
   ============================ */
function goSearch() {
    const q = (document.getElementById("searchword")?.value || "").trim();
    window.location.href = q ? "/jobs?q=" + encodeURIComponent(q) : "/jobs";
}

/* ============================
   Job & Notice Card Rendering
   ============================ */
function escapeHtml(str) {
    return String(str).replace(/[&<>"']/g, function(m) {
        return {"&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;"}[m];
    });
}

function renderJobCard(job, index) {
    var col = document.createElement("div");
    col.className = "col-3";

    var a = document.createElement("a");
    a.className = "job_link";
    a.href = "/jobs/detail?id=" + (job.id || "");

    var noHover = document.createElement("div");
    noHover.className = "job_nohover";

    var box = document.createElement("div");
    box.className = "box";

    // 상단 이미지 (기업 전경)
    var topImg = document.createElement("div");
    topImg.className = "top_img";
    var thumbUrl = job.photoPath || getDefaultThumbImage(index);
    var img = document.createElement("img");
    img.src = thumbUrl;
    img.alt = job.companyName || "";
    img.onerror = function() {
        this.src = getDefaultThumbImage(index);
    };
    topImg.appendChild(img);

    // 기업 정보
    var boxIn = document.createElement("div");
    boxIn.className = "box_in";

    // 기업 로고
    var logo = document.createElement("div");
    logo.className = "logo";
    if (job.logoPath) {
        var logoImg = document.createElement("img");
        logoImg.src = job.logoPath;
        logoImg.alt = job.companyName || "";
        logoImg.onerror = function() {
            this.src = "/img/common/default_logo.png";
        };
        logo.appendChild(logoImg);
    }

    // 기업명
    var nm = document.createElement("div");
    nm.className = "nm";
    nm.textContent = job.companyName || "";

    // 직무 제목
    var txt = document.createElement("div");
    txt.className = "txt";
    txt.textContent = job.title || "";

    boxIn.appendChild(logo);
    boxIn.appendChild(nm);
    boxIn.appendChild(txt);

    box.appendChild(topImg);
    box.appendChild(boxIn);
    noHover.appendChild(box);

    // 화살표
    var arrowWrap = document.createElement("div");
    arrowWrap.className = "arrow_wrap";
    var arrow = document.createElement("span");
    arrow.className = "arrow";
    arrowWrap.appendChild(arrow);
    noHover.appendChild(arrowWrap);

    // 호버 효과
    var hover = document.createElement("div");
    hover.className = "job_hover_box";
    var hoverBgUrl = job.photoPath || getDefaultThumbImage(index);
    hover.innerHTML = '<div class="job_info">' +
        '<div class="back_img"><img src="' + hoverBgUrl + '" alt="" onerror="this.src=\'' + getDefaultThumbImage(index) + '\'"></div>' +
        '<div class="jhb_info_box">' +
        '<div class="jhb_logo">' + (job.logoPath ? '<img src="' + job.logoPath + '" alt="" onerror="this.src=\'/img/common/default_logo.png\'">' : '') + '</div>' +
        '<div class="jhb_ibox">' +
        '<div class="jhb_itit">' + escapeHtml(job.companyName || "") + '</div>' +
        '<div class="jhb_itxt">' + escapeHtml(job.title || "") + '</div>' +
        '</div></div></div>';

    a.appendChild(noHover);
    a.appendChild(hover);
    col.appendChild(a);
    return col;
}

function renderNoticeCard(n) {
    var col = document.createElement("div");
    col.className = "col-3";

    var a = document.createElement("a");
    a.className = "notice_link";
    a.href = n.detailUrl || "/notices";

    var box = document.createElement("div");
    box.className = "box";

    var txt = document.createElement("div");
    txt.className = "txt";
    txt.textContent = n.title || "";

    var date = document.createElement("div");
    date.className = "date";
    date.textContent = n.date || "";

    box.appendChild(txt);
    box.appendChild(date);
    a.appendChild(box);
    col.appendChild(a);
    return col;
}

async function loadMainCards() {
    var jobWrap = document.getElementById("jobCards");
    var noticeWrap = document.getElementById("noticeCards");

    try {
        var results = await Promise.all([
            fetch(CONFIG.jobsApi, { cache: "no-store" }).catch(function() { return null; }),

        ]);
        var jobsRes = results[0];
        var noticesRes = results[1];

        if (jobWrap && jobsRes && jobsRes.ok) {
            var jobs = await jobsRes.json();
            jobWrap.innerHTML = "";
            var jobList = Array.isArray(jobs) ? jobs : [];

            // 최대 8개의 채용공고 표시
            jobList.slice(0, 8).forEach(function(j, index) {
                jobWrap.appendChild(renderJobCard(j, index));
            });

            // 데이터가 없으면 안내 메시지
            if (jobList.length === 0) {
                jobWrap.innerHTML = '<div class="col-12" style="text-align:center;padding:40px;color:#666;">등록된 채용공고가 없습니다.</div>';
            }
        }

        if (noticeWrap && noticesRes && noticesRes.ok) {
            var notices = await noticesRes.json();
            noticeWrap.innerHTML = "";
            (Array.isArray(notices) ? notices : []).slice(0, 4).forEach(function(n) {
                noticeWrap.appendChild(renderNoticeCard(n));
            });
        }
    } catch (e) {
        console.log('메인 카드 로드 실패:', e);
    }
}

function initJobHover() {
    var links = document.querySelectorAll(".job_link");
    links.forEach(function(a) {
        a.addEventListener("mouseenter", function() {
            if (window.innerWidth >= 50) this.classList.add("active");
        });
        a.addEventListener("mouseleave", function() {
            if (window.innerWidth >= 50) this.classList.remove("active");
        });
    });
}

/* ============================
   USA Map (Jido) - Company Info
   ============================ */

var __jidoCache = null;

function isEmpty(val) {
    if (val === null || val === undefined) return true;
    if (typeof val === 'string') {
        var noTags = val.replace(/<[^>]*>/g, '').trim();
        return noTags.length === 0;
    }
    if (Array.isArray(val)) return val.length === 0;
    if (typeof val === 'object') return Object.keys(val).length === 0;
    return false;
}

function elCreate(tag, className, attrs) {
    attrs = attrs || {};
    var elem = document.createElement(tag);
    if (className) elem.className = className;
    for (var k in attrs) {
        if (attrs.hasOwnProperty(k)) {
            var v = attrs[k];
            if (k === 'text') elem.textContent = v;
            else if (k === 'html') elem.innerHTML = v;
            else elem.setAttribute(k, v);
        }
    }
    return elem;
}

function addRow(ul, label, value, opts) {
    opts = opts || {};
    if (isEmpty(value)) return;
    var li = elCreate('li', '');
    var th = elCreate('div', 'th', { text: label });
    var td = elCreate('div', 'td', opts.html ? { html: value } : { text: value });
    li.appendChild(th);
    li.appendChild(td);
    ul.appendChild(li);
}

function makeLogoSrc(logo) {
    if (isEmpty(logo)) return null;
    if (/^https?:\/\//i.test(logo) || logo.startsWith('/')) return logo;
    return CONFIG.logoBasePath + logo;
}

function buildHeaderBlock(data) {
    var wrap = elCreate('div', 'cnp_logo');
    var src = makeLogoSrc(data.logo);
    if (src) {
        var logoArea = elCreate('div', 'logo_area');
        var img = elCreate('img', '', { src: src, alt: '' });
        logoArea.appendChild(img);
        wrap.appendChild(logoArea);
    }
    if (!isEmpty(data.title)) wrap.appendChild(elCreate('div', 'cnp_tit', { text: data.title }));
    if (!isEmpty(data.website)) {
        var site = elCreate('div', 'cnp_website');
        var a = elCreate('a', 'cpb_link', { href: data.website, target: '_blank', rel: 'noopener', text: 'Website' });
        site.appendChild(a);
        wrap.appendChild(site);
    }
    return wrap;
}

function buildInfoBlock(titleText) {
    var info = elCreate('div', 'cnp_info');
    if (!isEmpty(titleText)) info.appendChild(elCreate('div', 'tit', { text: titleText }));
    var ul = elCreate('ul', 'cplist');
    info.appendChild(ul);
    return { info: info, ul: ul };
}

function renderCorporateItem(item) {
    var box = elCreate('div', 'cnp_logo_item');
    var header = buildHeaderBlock({
        logo: item.logo,
        title: item.companyName,
        website: item.website
    });
    box.appendChild(header);

    var infoObj = buildInfoBlock('About the Company');
    var ul = infoObj.ul;
    addRow(ul, 'Company Name', item.companyName);
    addRow(ul, 'Industry', item.industry);
    addRow(ul, 'Founding Year', item.foundedYear);
    addRow(ul, 'Revenue', item.revenue);
    addRow(ul, 'Locations', item.locations, { html: true });
    addRow(ul, 'Notes', item.notes);
    box.appendChild(infoObj.info);

    if (box.children.length === 1 && ul.children.length === 0) return null;
    return box;
}

function renderGroupItem(group) {
    var box = elCreate('div', 'cnp_logo_item groupcom');
    var header = buildHeaderBlock({
        logo: group.logo,
        title: group.groupName,
        website: group.website
    });
    box.appendChild(header);

    var infoObj = buildInfoBlock('About the Korea Headquarters');
    var ul = infoObj.ul;
    addRow(ul, 'Company Name', group.groupName);
    addRow(ul, 'Industry', group.industry);
    addRow(ul, 'Founded Year', group.foundedYear);
    addRow(ul, 'Revenue', group.revenue);
    addRow(ul, 'Headquarters', group.headquarters);
    addRow(ul, 'Notes', group.notes);
    box.appendChild(infoObj.info);

    if (box.children.length === 1 && ul.children.length === 0) return null;
    return box;
}

async function getJidoData() {
    if (!__jidoCache) {
        var res = await fetch(CONFIG.jidoJsonUrl, { cache: 'no-store' });
        if (!res.ok) throw new Error('jido_company_info.json 로드 실패');
        __jidoCache = await res.json();
    }
    return __jidoCache;
}

async function jidoView(event, key) {
    if (event && event.preventDefault) event.preventDefault();

    var groupBox = document.querySelector('.cpb_box_group');
    var wrap = groupBox ? groupBox.querySelector('.company_pop_wrap_box') : null;
    var container = wrap ? wrap.querySelector('.company_agent') : null;
    if (!container) {
        console.error('company_agent 컨테이너를 찾을 수 없습니다.');
        return;
    }

    try {
        var data = await getJidoData();
        var entry = data[key];
        container.innerHTML = '';

        if (!entry) {
            container.innerHTML = '<p style="padding:16px;">데이터가 없습니다.</p>';
            groupBox.style.display = 'block';
            return;
        }

        if (Array.isArray(entry.corporateList)) {
            entry.corporateList.forEach(function(item) {
                var node = renderCorporateItem(item);
                if (node) container.appendChild(node);
            });
        }

        if (entry.groupInfo && !isEmpty(entry.groupInfo)) {
            var gnode = renderGroupItem(entry.groupInfo);
            if (gnode) container.appendChild(gnode);
        }

        if (!container.children.length) {
            container.innerHTML = '<p style="padding:16px;">표시할 항목이 없습니다.</p>';
        }

        groupBox.style.display = 'block';

    } catch (err) {
        console.error(err);
        container.innerHTML = '<p style="padding:16px;color:#d00">데이터 로드 중 오류 발생</p>';
        groupBox.style.display = 'block';
    }
}

function jidoClose(event) {
    if (event && event.preventDefault) event.preventDefault();
    var box = event && event.currentTarget ? event.currentTarget.closest('.cpb_box_group') : null;
    if (box) {
        box.style.display = 'none';
    } else {
        var popup = document.getElementById('jidoPopup');
        if (popup) popup.style.display = 'none';
    }
}

function initJidoHover() {
    document.querySelectorAll('.jido_slink').forEach(function(el) {
        el.addEventListener('mouseenter', function() {
            el.classList.add('on');
        });
        el.addEventListener('mouseleave', function() {
            el.classList.remove('on');
        });
    });
}

/* ============================
   DOM Ready
   ============================ */
document.addEventListener("DOMContentLoaded", function() {
    initSwiper();
    loadMainCards();
    initJidoHover();

    var input = document.getElementById("searchword");
    if (input) {
        input.addEventListener("keydown", function(e) {
            if (e.key === "Enter") {
                e.preventDefault();
                goSearch();
            }
        });
    }

    var observer = new MutationObserver(function() { initJobHover(); });
    var jobWrap = document.getElementById("jobCards");
    if (jobWrap) observer.observe(jobWrap, { childList: true });
});

/* ============================
   Global Exports
   ============================ */
window.goSearch = goSearch;
window.jidoView = jidoView;
window.jidoClose = jidoClose;