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

const onDomReady = (callback) => {
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', callback);
    } else {
        callback();
    }
};

// Replaces native confirm() with Promise-based SweetAlert2
window.confirmAction = function (title, text, confirmBtnText = 'Yes, do it!', icon = 'warning') {
    return Swal.fire({
        title: title,
        text: text,
        icon: icon,
        showCancelButton: true,
        confirmButtonColor: 'var(--danger-color)',
        cancelButtonColor: 'var(--text-secondary)',
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
onDomReady(() => {
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
    if (!sidebar || !sidebarOverlay || !mobileToggle || !toggleIcon) return;
    sidebar.classList.add('active');
    sidebarOverlay.classList.add('active');
    mobileToggle.classList.add('open');
    toggleIcon.className = 'bi bi-x-lg';
    mobileToggle.setAttribute('aria-expanded', 'true');
}

function closeSidebarMobile() {
    if (!sidebar || !sidebarOverlay || !mobileToggle || !toggleIcon) return;
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
    if (!sidebar) return;
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
if (sidebar && !isMobile() && localStorage.getItem('sidebarMini') === '1') {
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
        if (isMobile() && sidebar && sidebar.classList.contains('active')) {
            closeSidebarMobile();
        }
        if (notifDropdown && notifDropdown.classList.contains('open')) {
            notifDropdown.classList.remove('open');
            notifBtn?.setAttribute('aria-expanded', 'false');
        }
    }
});

/* ══════════════════════════════════════════════════════════════════
   ENTERPRISE UTILITY LIBRARY
   All helpers are on window.* so page templates can call them inline.
══════════════════════════════════════════════════════════════════ */

/* ─────────────────────────────────────────────────
   1. CSV TABLE EXPORT
   Usage: entExportCSV('myTableId', 'filename.csv')
───────────────────────────────────────────────── */
window.entExportCSV = function (tableId, filename) {
    const table = document.getElementById(tableId);
    if (!table) return;
    filename = filename || document.title.replace(/\s+/g, '_') + '.csv';

    const rows = Array.from(table.querySelectorAll('tr'));
    const csv  = rows.map(row => {
        const cells = Array.from(row.querySelectorAll('th, td'));
        return cells
            .filter(c => !c.classList.contains('no-export') && !c.closest('.no-export'))
            .map(c => {
                // Prefer data-export attribute for custom values (e.g. numeric without badges)
                const raw = c.hasAttribute('data-export') ? c.getAttribute('data-export') : c.innerText;
                const clean = raw.replace(/\r?\n|\r/g, ' ').trim();
                return '"' + clean.replace(/"/g, '""') + '"';
            })
            .join(',');
    }).join('\r\n');

    const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' }); // BOM for Excel
    const url  = URL.createObjectURL(blob);
    const a    = Object.assign(document.createElement('a'), { href: url, download: filename });
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
    showToast('success', 'Exported to ' + filename);
};

/* ─────────────────────────────────────────────────
   2. EXPORT DROPDOWN TOGGLE
   Called by ent-action-row fragment's Export button.
───────────────────────────────────────────────── */
window.entExportMenu = function (btn) {
    const menu = btn.nextElementSibling;
    if (!menu) return;
    const isOpen = menu.classList.toggle('open');
    btn.setAttribute('aria-expanded', isOpen);
    if (isOpen) {
        const close = (e) => {
            if (!btn.parentElement.contains(e.target)) {
                menu.classList.remove('open');
                btn.setAttribute('aria-expanded', 'false');
                document.removeEventListener('click', close);
            }
        };
        setTimeout(() => document.addEventListener('click', close), 10);
    }
};

/* ─────────────────────────────────────────────────
   3. CLIENT-SIDE TABLE SORT
   Add class "ent-sortable" and data-col="N" to <th> elements, then:
   entInitTableSort('myTableId')
───────────────────────────────────────────────── */
window.entInitTableSort = function (tableId) {
    const table = document.getElementById(tableId);
    if (!table) return;
    const headers = table.querySelectorAll('th.ent-sortable');
    headers.forEach((th, idx) => {
        th.addEventListener('click', () => {
            const col   = th.dataset.col !== undefined ? parseInt(th.dataset.col, 10) : idx;
            const tbody = table.querySelector('tbody');
            if (!tbody) return;
            const asc = th.classList.contains('asc');
            // Reset all headers
            headers.forEach(h => h.classList.remove('asc', 'desc'));
            th.classList.add(asc ? 'desc' : 'asc');
            const dir = asc ? -1 : 1;
            const rows = Array.from(tbody.querySelectorAll('tr:not(.ent-skeleton--row)'));
            rows.sort((a, b) => {
                const aText = (a.cells[col]?.getAttribute('data-sort') || a.cells[col]?.innerText || '').trim().toLowerCase();
                const bText = (b.cells[col]?.getAttribute('data-sort') || b.cells[col]?.innerText || '').trim().toLowerCase();
                const aNum = parseFloat(aText.replace(/[₹,]/g, ''));
                const bNum = parseFloat(bText.replace(/[₹,]/g, ''));
                if (!isNaN(aNum) && !isNaN(bNum)) return (aNum - bNum) * dir;
                return aText.localeCompare(bText) * dir;
            });
            rows.forEach(r => tbody.appendChild(r));
        });
    });
};

/* ─────────────────────────────────────────────────
   4. BULK ROW SELECT
   Call entInitBulkSelect('tableId') after page load.
   Requires: table has a checkbox in col 0 with class "ent-row-check",
             and #selectAll checkbox in the header.
   Requires: ent-bulk-bar fragment on page.
───────────────────────────────────────────────── */
window.entInitBulkSelect = function (tableId) {
    const table   = document.getElementById(tableId);
    const bulkBar = document.getElementById('entBulkBar');
    const countEl = document.getElementById('entBulkCount');
    if (!table) return;

    const selectAll = table.querySelector('#selectAll');
    const getChecked = () => Array.from(table.querySelectorAll('tbody .ent-row-check:checked'));

    const updateBar = () => {
        const checked = getChecked();
        if (bulkBar) bulkBar.classList.toggle('active', checked.length > 0);
        if (countEl) countEl.textContent = checked.length;
    };

    if (selectAll) {
        selectAll.addEventListener('change', () => {
            table.querySelectorAll('tbody .ent-row-check').forEach(cb => {
                cb.checked = selectAll.checked;
            });
            updateBar();
        });
    }

    table.addEventListener('change', (e) => {
        if (e.target.classList.contains('ent-row-check')) updateBar();
    });
};

/* Gets selected row IDs (from data-id on checkbox) */
window.entGetSelectedIds = function (tableId) {
    const scope = tableId ? document.getElementById(tableId) : document;
    if (!scope) return [];
    return Array.from(scope.querySelectorAll('tbody .ent-row-check:checked, .ent-row-check:checked'))
                .map(cb => cb.dataset.id).filter(Boolean);
};

window.entClearBulkSelect = function () {
    document.querySelectorAll('.ent-row-check, #selectAll').forEach(cb => { cb.checked = false; });
    const bulkBar = document.getElementById('entBulkBar');
    if (bulkBar) bulkBar.classList.remove('active');
};

window.entBulkDelete = function (url, tableId = null) {
    const ids = entGetSelectedIds(tableId);
    if (!ids.length) { showToast('warning', 'No rows selected.'); return; }
    confirmAction('Delete selected items?', ids.length + ' item(s) will be permanently removed.', 'Yes, delete all!').then(r => {
        if (r.isConfirmed) {
            const form = Object.assign(document.createElement('form'), { method: 'POST', action: url });
            ids.forEach(id => {
                const inp = Object.assign(document.createElement('input'), { type: 'hidden', name: 'ids', value: id });
                form.appendChild(inp);
            });
            document.body.appendChild(form);
            form.submit();
        }
    });
};

/* ─────────────────────────────────────────────────
   5. FORM DIRTY STATE DETECTION
   Call entInitFormDirty('formId') on pages with long forms.
   Shows a visual indicator and warns before navigating away.
───────────────────────────────────────────────── */
window.entInitFormDirty = function (formId) {
    const form = document.getElementById(formId);
    if (!form) return;
    let dirty = false;
    const indicatorEl = form.querySelector('.ent-dirty-indicator');

    const markDirty = () => {
        dirty = true;
        if (indicatorEl) indicatorEl.classList.add('visible');
    };

    form.addEventListener('input',  markDirty);
    form.addEventListener('change', markDirty);

    form.addEventListener('submit', () => { dirty = false; });

    window.addEventListener('beforeunload', (e) => {
        if (dirty) {
            e.preventDefault();
            e.returnValue = 'You have unsaved changes. Are you sure you want to leave?';
        }
    });
};

/* ─────────────────────────────────────────────────
   6. AUTO-DISMISS FLASH ALERTS
   Replaces the auto-hide that was baked into layout.html inline script.
   Alerts with class "ent-auto-dismiss" or Bootstrap ".alert" will fade.
───────────────────────────────────────────────── */
onDomReady(() => {
    const AUTO_DISMISS_MS = 5000;
    document.querySelectorAll('.alert-success.alert-dismissible, .alert-info.alert-dismissible, .ent-auto-dismiss').forEach(el => {
        setTimeout(() => {
            el.style.transition = 'opacity 0.5s ease';
            el.style.opacity = '0';
            setTimeout(() => el.remove(), 500);
        }, AUTO_DISMISS_MS);
    });
});

/* ─────────────────────────────────────────────────
   7. KEYBOARD SHORTCUT HELP  (press '?' in app)
   Shortcuts shown when user is not typing in an input.
───────────────────────────────────────────────── */
(function () {
    const SHORTCUTS = [
        { keys: ['?'],          label: 'Show keyboard shortcuts' },
        { keys: ['Ctrl', 'K'],  label: 'Focus global search' },
        { keys: ['Alt', 'N'],   label: 'New sale' },
        { keys: ['Alt', 'M'],   label: 'Go to medicines' },
        { keys: ['Alt', 'S'],   label: 'Go to sales' },
        { keys: ['Alt', 'D'],   label: 'Go to dashboard' },
        { keys: ['Esc'],        label: 'Close dialogs / collapse menu' },
    ];

    const dialog = document.createElement('div');
    dialog.className = 'ent-shortcuts-dialog';
    dialog.setAttribute('role', 'dialog');
    dialog.setAttribute('aria-modal', 'true');
    dialog.setAttribute('aria-label', 'Keyboard shortcuts');
    dialog.innerHTML = `
        <div class="ent-shortcuts-dialog__box">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <span class="ent-shortcuts-dialog__title">⌨ Keyboard Shortcuts</span>
                <button type="button" class="btn-close" aria-label="Close shortcuts"></button>
            </div>
            ${SHORTCUTS.map(s => `
                <div class="ent-shortcut-row">
                    <span>${s.label}</span>
                    <span class="ent-kbd">${s.keys.map(k => `<kbd>${k}</kbd>`).join('')}</span>
                </div>
            `).join('')}
        </div>`;
    document.body.appendChild(dialog);

    const openDialog  = () => dialog.classList.add('open');
    const closeDialog = () => dialog.classList.remove('open');

    dialog.querySelector('.btn-close').addEventListener('click', closeDialog);
    dialog.addEventListener('click', (e) => { if (e.target === dialog) closeDialog(); });

    document.addEventListener('keydown', (e) => {
        const active = document.activeElement;
        const inInput = active && (active.tagName === 'INPUT' || active.tagName === 'TEXTAREA' || active.tagName === 'SELECT' || active.isContentEditable);
        if (e.key === '?' && !inInput) { e.preventDefault(); openDialog(); }
        if (e.key === 'Escape' && dialog.classList.contains('open')) closeDialog();

        // Alt+N = new sale
        if (e.altKey && e.key === 'n') { e.preventDefault(); window.location.href = '/sales/new'; }
        // Alt+M = medicines
        if (e.altKey && e.key === 'm') { e.preventDefault(); window.location.href = '/medicines'; }
        // Alt+S = sales
        if (e.altKey && e.key === 's') { e.preventDefault(); window.location.href = '/sales'; }
        // Alt+D = dashboard
        if (e.altKey && e.key === 'd') { e.preventDefault(); window.location.href = '/'; }
    });
})();

/* ─────────────────────────────────────────────────
   8. CONFIRM MODAL HELPER  (uses ent-confirm-modal fragment)
   entConfirmModal({ title, message, okLabel, href, onConfirm })
───────────────────────────────────────────────── */
window.entConfirmModal = function ({ title = 'Confirm', message = 'Are you sure?', okLabel = 'Confirm', href = null, onConfirm = null } = {}) {
    const modal  = document.getElementById('entConfirmModal');
    if (!modal) { return confirmAction(title, message, okLabel); }

    document.getElementById('entConfirmModalLabel').textContent = title;
    document.getElementById('entConfirmModalBody').textContent  = message;
    const okBtn = document.getElementById('entConfirmModalOk');
    okBtn.textContent = okLabel;
    okBtn.href = href || '#';

    if (onConfirm) {
        const handler = () => { onConfirm(); okBtn.removeEventListener('click', handler); };
        okBtn.addEventListener('click', handler);
    }

    const bsModal = new bootstrap.Modal(modal);
    bsModal.show();
};

/* ─────────────────────────────────────────────────
   9. CHARACTER COUNTER FOR TEXTAREAS
   Call entCharCounter('fieldId', maxLength) or
   include data-max="200" on a <textarea class="ent-char-counter">.
───────────────────────────────────────────────── */
window.entCharCounter = function (fieldId, max) {
    const ta = document.getElementById(fieldId);
    if (!ta) return;
    const counter = document.createElement('small');
    counter.className = 'ent-field-hint d-block text-end';
    ta.parentNode.insertBefore(counter, ta.nextSibling);
    const update = () => {
        const len = ta.value.length;
        counter.textContent = len + ' / ' + max;
        counter.style.color = len > max * 0.9 ? 'var(--danger-color)' : 'var(--text-secondary)';
    };
    ta.addEventListener('input', update);
    update();
};

// Auto-init any textarea[data-max]
onDomReady(() => {
    document.querySelectorAll('textarea[data-max]').forEach(ta => {
        if (!ta.id) ta.id = 'ent_ta_' + Math.random().toString(36).slice(2);
        entCharCounter(ta.id, parseInt(ta.dataset.max, 10));
    });
});

/* ─────────────────────────────────────────────────
   10. SEARCH CLEAR BUTTON  (ent-search component)
   Auto-shows/hides the × button as user types.
───────────────────────────────────────────────── */
onDomReady(() => {
    document.querySelectorAll('.ent-search input').forEach(input => {
        const clearBtn = input.parentElement.querySelector('.ent-search__clear');
        if (!clearBtn) return;
        const toggle = () => { clearBtn.style.display = input.value ? 'block' : 'none'; };
        input.addEventListener('input', toggle);
        toggle();
    });
});

/* ─────────────────────────────────────────────────
   11. FILTER CHIP REMOVE  (auto-close on chip × click)
───────────────────────────────────────────────── */
document.addEventListener('click', (e) => {
    const chip = e.target.closest('.ent-filter-chip a');
    if (chip) { chip.closest('.ent-filter-chip')?.classList.add('removing'); }
});

/* ─────────────────────────────────────────────────
   12. DARK MODE TOGGLE  (already wired in layout.html; expose toggle globally)
───────────────────────────────────────────────── */
window.entToggleDark = function () {
    const current = document.documentElement.getAttribute('data-theme');
    const next = current === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('theme', next);
};

// Restore on load
(function () {
    const saved = localStorage.getItem('theme');
    if (saved) document.documentElement.setAttribute('data-theme', saved);
})();


