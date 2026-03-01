/* ---- Global Notification / Modal Helpers ---- */
const Toast = Swal.mixin({
    toast: true,
    position: 'top-end',
    showConfirmButton: false,
    timer: 3000,
    timerProgressBar: true,
    background: 'var(--bg-primary)',
    color: 'var(--text-primary)',
    didOpen: (toast) => {
        toast.addEventListener('mouseenter', Swal.stopTimer)
        toast.addEventListener('mouseleave', Swal.resumeTimer)
    }
});

// Use this worldwide inline via `showToast('success', 'Saved!')`
window.showToast = function (icon, title) {
    Toast.fire({ icon, title });
};

// Replaces native confirm() with Promise-based SweetAlert2
window.confirmAction = function (title, text, confirmBtnText = 'Yes, do it!', icon = 'warning') {
    return Swal.fire({
        title: title,
        text: text,
        icon: icon,
        showCancelButton: true,
        confirmButtonColor: '#ef4444',
        cancelButtonColor: '#6b7280',
        confirmButtonText: confirmBtnText,
        background: 'var(--bg-primary)',
        color: 'var(--text-primary)'
    });
};

// Helper for anchor tags: onclick="return confirmLink(event, this, 'Delete item?')"
window.confirmLink = function (event, element, text, confirmBtnText = 'Delete', title = 'Are you sure?') {
    event.preventDefault();
    confirmAction(title, text, confirmBtnText).then((result) => {
        if (result.isConfirmed) window.location.href = element.href;
    });
    return false;
};

/* ---- Check Flash Messages ---- */
document.addEventListener('DOMContentLoaded', () => {
    const successAlert = document.querySelector('.alert-success');
    const errorAlert = document.querySelector('.alert-danger');
    const activeNavLink = document.querySelector('.sidebar .nav-link.active');

    if (successAlert) {
        successAlert.style.display = 'none'; // hide native alert
        showToast('success', successAlert.textContent.trim());
    }
    if (errorAlert) {
        errorAlert.style.display = 'none';
        showToast('error', errorAlert.textContent.trim());
    }

    if (activeNavLink) {
        activeNavLink.scrollIntoView({ block: 'nearest' });
    }

    const savedQuery = localStorage.getItem('globalSearchQuery');
    const globalSearchInput = document.getElementById('globalSearch');
    if (globalSearchInput && savedQuery) {
        globalSearchInput.value = savedQuery;
    }
});

/* ---- Mobile Sidebar Logic ---- */
const mobileToggle = document.getElementById('mobileToggle');
const sidebar = document.getElementById('sidebar');
const sidebarOverlay = document.getElementById('sidebarOverlay');
const sidebarCloseBtn = document.getElementById('sidebarCloseBtn');
const toggleIcon = document.getElementById('toggleIcon');

const isMobile = () => window.innerWidth <= 992;

/* ---- Mobile helpers ---- */
function openSidebarMobile() {
    sidebar.classList.add('active');
    sidebarOverlay.classList.add('active');
    mobileToggle.classList.add('open');
    toggleIcon.className = 'bi bi-x-lg';
    mobileToggle.setAttribute('aria-expanded', 'true');
}

function closeSidebarMobile() {
    sidebar.classList.remove('active');
    sidebarOverlay.classList.remove('active');
    mobileToggle.classList.remove('open');
    toggleIcon.className = 'bi bi-list';
    mobileToggle.setAttribute('aria-expanded', 'false');
}

function toggleMobile() {
    sidebar.classList.contains('active') ? closeSidebarMobile() : openSidebarMobile();
}

/* ---- Desktop mini-collapse ---- */
function toggleDesktopMini() {
    const mini = sidebar.classList.toggle('mini');
    localStorage.setItem('sidebarMini', mini ? '1' : '0');
    /* Force-open all <details> in mini mode so icons are visible */
    const groups = sidebar.querySelectorAll('details.nav-group');
    if (mini) {
        groups.forEach(d => { d._wasOpen = d.open; d.open = true; });
    } else {
        groups.forEach(d => { if (d._wasOpen !== undefined) d.open = d._wasOpen; });
    }
}

/* Restore desktop preference */
if (!isMobile() && localStorage.getItem('sidebarMini') === '1') {
    sidebar.classList.add('mini');
    sidebar.querySelectorAll('details.nav-group').forEach(d => { d._wasOpen = d.open; d.open = true; });
}

/* ---- Event listeners ---- */
if (mobileToggle) {
    mobileToggle.addEventListener('click', () => {
        isMobile() ? toggleMobile() : toggleDesktopMini();
    });
}

if (sidebarCloseBtn) {
    sidebarCloseBtn.addEventListener('click', closeSidebarMobile);
}

if (sidebarOverlay) {
    sidebarOverlay.addEventListener('click', closeSidebarMobile);
}

/* Close on nav-link click (mobile) */
document.querySelectorAll('.sidebar .nav-link').forEach(link => {
    link.addEventListener('click', () => {
        if (isMobile()) closeSidebarMobile();
    });
});

/* Reset mobile state on resize to desktop */
window.addEventListener('resize', () => {
    if (!isMobile()) closeSidebarMobile();
});

/* ---- Live clock ---- */
function updateClock() {
    const now = new Date();
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    const ss = String(now.getSeconds()).padStart(2, '0');
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    const timeEl = document.getElementById('liveClock');
    const dateEl = document.getElementById('liveDate');
    if (timeEl) timeEl.textContent = hh + ':' + mm + ':' + ss;
    if (dateEl) dateEl.textContent = days[now.getDay()] + ', ' + now.getDate() + ' ' + months[now.getMonth()] + ' ' + now.getFullYear();
}
updateClock();
setInterval(updateClock, 1000);

/* ---- Notification Dropdown ---- */
const notifBtn = document.getElementById('notifBtn');
const notifDropdown = document.getElementById('notifDropdown');
const notifContainer = document.getElementById('notifContainer');

if (notifBtn && notifDropdown) {
    notifBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        const isOpen = notifDropdown.classList.toggle('open');
        notifBtn.setAttribute('aria-expanded', isOpen);
    });
    document.addEventListener('click', (e) => {
        if (notifContainer && !notifContainer.contains(e.target)) {
            notifDropdown.classList.remove('open');
            notifBtn.setAttribute('aria-expanded', 'false');
        }
    });
}

/* ---- Global Search (Ctrl+K) ---- */
const globalSearchInput = document.getElementById('globalSearch');
if (globalSearchInput) {
    document.addEventListener('keydown', (e) => {
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            globalSearchInput.focus();
        }
    });
    globalSearchInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && globalSearchInput.value.trim()) {
            const query = globalSearchInput.value.trim();
            localStorage.setItem('globalSearchQuery', query);
            window.location.href = '/medicines?search=' + encodeURIComponent(query);
        }
        if (e.key === 'Escape') {
            globalSearchInput.blur();
        }
    });

    globalSearchInput.addEventListener('input', () => {
        localStorage.setItem('globalSearchQuery', globalSearchInput.value);
    });
}

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        if (isMobile() && sidebar.classList.contains('active')) {
            closeSidebarMobile();
        }
        if (notifDropdown && notifDropdown.classList.contains('open')) {
            notifDropdown.classList.remove('open');
            notifBtn?.setAttribute('aria-expanded', 'false');
        }
    }
});

