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

function normalizePath(path) {
    if (!path) return '/';
    if (path.length > 1 && path.endsWith('/')) return path.slice(0, -1);
    return path;
}

function syncSidebarActiveState() {
    const sidebarRoot = document.getElementById('sidebar');
    if (!sidebarRoot) return null;

    const currentPath = normalizePath(window.location.pathname || '/');
    const links = Array.from(sidebarRoot.querySelectorAll('.nav-link[href]'))
        .filter(link => {
            const href = link.getAttribute('href') || '';
            return href.startsWith('/') && !href.startsWith('//') && href !== '/logout';
        });

    if (!links.length) return null;

    const byPath = links.map(link => {
        const rawHref = link.getAttribute('href') || '/';
        return {
            link,
            path: normalizePath(rawHref.split('?')[0] || '/'),
            isRoot: rawHref === '/'
        };
    });

    let bestMatch = byPath.find(item => item.path === currentPath) || null;

    if (!bestMatch) {
        bestMatch = byPath
            .filter(item => !item.isRoot && currentPath.startsWith(item.path + '/'))
            .sort((a, b) => b.path.length - a.path.length)[0] || null;
    }

    if (!bestMatch) return null;

    links.forEach(link => {
        link.classList.remove('active');
        link.removeAttribute('aria-current');
    });

    bestMatch.link.classList.add('active');
    bestMatch.link.setAttribute('aria-current', 'page');

    const parentGroup = bestMatch.link.closest('details.nav-group');
    if (parentGroup) parentGroup.open = true;

    return bestMatch.link;
}

/* ---- Check Flash Messages ---- */
onDomReady(() => {
    const successAlert = document.querySelector('.alert-success');
    const errorAlert = document.querySelector('.alert-danger');
    const activeNavLink = syncSidebarActiveState() || document.querySelector('.sidebar .nav-link.active');

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

/* Close on nav-link click (mobile) — use event delegation instead of per-link listeners */
if (sidebar) {
    sidebar.addEventListener('click', (e) => {
        if (isMobile() && e.target.closest('.nav-link')) closeSidebarMobile();
    });
}

/* Reset mobile state on resize to desktop (debounced to avoid thrashing) */
let _resizeTimer = null;
window.addEventListener('resize', () => {
    if (_resizeTimer) return;
    _resizeTimer = setTimeout(() => {
        _resizeTimer = null;
        if (!isMobile()) closeSidebarMobile();
    }, 150);
});

/* ---- Live clock (DOM elements cached once to avoid repeated getElementById) ---- */
const _clockTimeEl = document.getElementById('liveClock');
const _clockDateEl = document.getElementById('liveDate');
const _clockMonths  = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
const _clockDays    = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'];
function updateClock() {
    const now = new Date();
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    const ss = String(now.getSeconds()).padStart(2, '0');
    if (_clockTimeEl) _clockTimeEl.textContent = hh + ':' + mm + ':' + ss;
    if (_clockDateEl) _clockDateEl.textContent = _clockDays[now.getDay()] + ', ' + now.getDate() + ' ' + _clockMonths[now.getMonth()] + ' ' + now.getFullYear();
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

    /* Debounce localStorage writes — no need to persist on every keystroke */
    let _searchSaveTimer = null;
    globalSearchInput.addEventListener('input', () => {
        clearTimeout(_searchSaveTimer);
        _searchSaveTimer = setTimeout(() => {
            localStorage.setItem('globalSearchQuery', globalSearchInput.value);
        }, 300);
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
   4a. ROW ANIMATE  (staggered fade-in on load)
   Usage:  entInitTableAnimateRows('myTableId')
   Or auto-trigger by adding class "table-animate-rows" to <table>.
   CSS keyframe lives in components.css.
───────────────────────────────────────────────── */
window.entInitTableAnimateRows = function (tableId) {
    const table = tableId ? document.getElementById(tableId) : null;
    const target = table || document.querySelectorAll('.table-animate-rows');
    const apply = (t) => {
        t.classList.add('table-animate-rows');
        // Re-trigger animation after a sort/filter
        t._entAnimRows = () => {
            const rows = t.querySelectorAll('tbody tr');
            rows.forEach((r, i) => {
                r.style.animation = 'none';
                void r.offsetHeight; // reflow
                r.style.animation = '';
            });
        };
    };
    if (table) { apply(table); }
    else { document.querySelectorAll('.table-animate-rows').forEach(apply); }
};

/* Auto-init on DOMContentLoaded for any table that already has the class */
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('table.table-animate-rows').forEach(t => {
        window.entInitTableAnimateRows && window.entInitTableAnimateRows(t.id || null);
    });
});

/* ─────────────────────────────────────────────────
   4b. ROW CLICK HIGHLIGHT
   Usage: entInitRowHighlight('myTableId')
   Clicking a body row toggles .table-active on it and fires a
   custom 'ent:rowselect' event with { row, id }.
───────────────────────────────────────────────── */
window.entInitRowHighlight = function (tableId, opts) {
    const table = document.getElementById(tableId);
    if (!table) return;
    const o = Object.assign({ single: true, activeClass: 'table-active' }, opts);
    table.addEventListener('click', (e) => {
        const row = e.target.closest('tbody tr');
        if (!row) return;
        if (o.single) {
            table.querySelectorAll(`tbody tr.${o.activeClass}`)
                 .forEach(r => r !== row && r.classList.remove(o.activeClass));
        }
        row.classList.toggle(o.activeClass);
        row.dispatchEvent(new CustomEvent('ent:rowselect', {
            bubbles: true,
            detail: { row, id: row.dataset.id }
        }));
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
   12. DATA-CONFIRM-DELETE  (auto-wire delete forms)
   Add data-confirm-delete="Message here" to any <form>
   to replace the native confirm() with SweetAlert2.
   Optimistic UI: the table row collapses immediately on confirm,
   then the fetch request fires in the background.
   On failure, the row is restored and an error toast is shown.
───────────────────────────────────────────────── */
document.addEventListener('submit', function (e) {
    const form = e.target;
    if (!form.hasAttribute('data-confirm-delete')) return;
    e.preventDefault();

    const msg = form.getAttribute('data-confirm-delete');

    confirmAction('Are you sure?', msg, 'Delete').then(result => {
        if (!result.isConfirmed) return;

        const row       = form.closest('tr');
        const actionUrl = form.action;
        const csrfInput = form.querySelector('[name="_csrf"], [name^="_csrf"]');
        const csrfVal   = csrfInput ? csrfInput.value : null;
        const csrfName  = csrfInput ? csrfInput.name  : '_csrf';

        // ── Optimistic: dim the row & swap trash icon for spinner ──
        const deleteBtn = form.querySelector('button[type="submit"]');
        const originalBtnHtml = deleteBtn ? deleteBtn.innerHTML : null;
        if (deleteBtn) {
            deleteBtn.innerHTML = '<span class="ent-del-spinner"></span>';
        }
        if (row) {
            row.classList.add('ent-row-deleting');
        }

        // ── Build fetch body ──
        const body = new URLSearchParams();
        if (csrfVal) body.append(csrfName, csrfVal);

        NProgress.start();
        fetch(actionUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: body.toString(),
            redirect: 'manual'   // don't follow the redirect — we handle it
        })
        .then(res => {
            // Spring returns 3xx redirect on success, OR 2xx if it's a REST endpoint
            const ok = res.ok || res.type === 'opaqueredirect' || res.status === 302 || res.status === 303;
            if (!ok) throw new Error('Server returned ' + res.status);

            NProgress.done();
            // ── Collapse the row ──
            if (row) {
                row.classList.remove('ent-row-deleting');
                row.classList.add('ent-row-removed');
                setTimeout(() => row.remove(), 420); // matches animation duration
            }

            // ── Bump the live row-count badge (if present on the page) ──
            _entBumpRowCount(-1);

            showToast('success', 'Deleted successfully');
        })
        .catch(() => {
            NProgress.done();
            // Restore row to original state
            if (row) {
                row.classList.remove('ent-row-deleting');
            }
            if (deleteBtn && originalBtnHtml !== null) {
                deleteBtn.innerHTML = originalBtnHtml;
            }
            showToast('error', 'Delete failed — please try again.');
        });
    });
});

/** Update any .ent-row-count badge on the page by delta (+1 / -1). */
function _entBumpRowCount(delta) {
    document.querySelectorAll('.ent-row-count').forEach(el => {
        const current = parseInt(el.textContent.replace(/\D/g, ''), 10);
        if (isNaN(current)) return;
        const next = Math.max(0, current + delta);
        el.textContent = el.textContent.replace(/\d+/, next);
        el.classList.add('bump');
        setTimeout(() => el.classList.remove('bump'), 400);
    });
}

/* ═══════════════════════════════════════════════════════════════
   N-PROGRESS  –  lightweight top-bar progress indicator
   • Fires on every <a href> navigation (click)
   • Fires on every <form> submit (non-delete) 
   • Completes on window load (new page arrival)
   • window.NProgress exposed so inline scripts can call it
══════════════════════════════════════════════════════════════ */
(function () {
    /* ── DOM setup ── */
    const el = document.createElement('div');
    el.id = 'nprogress';
    el.innerHTML = '<div class="bar"></div>';
    document.body.appendChild(el);
    const bar = el.querySelector('.bar');
    /* Hint to the compositor: this element's width/opacity will change frequently */
    bar.style.willChange = 'width, opacity';

    let _current = 0;
    let _timer    = null;
    let _trickling = null;

    function _set(n) {
        n = Math.min(Math.max(n, 0), 1);
        _current = n;
        bar.style.width = (n * 100).toFixed(1) + '%';
        bar.style.opacity = '1';
        el.classList.remove('nprogress-done');
    }

    function _trickle() {
        if (_current >= 0.94) return;
        /* slow logarithmic trickle — feels natural */
        const inc = (_current < 0.2) ? 0.1
                  : (_current < 0.5) ? 0.04
                  : (_current < 0.8) ? 0.02
                  : (_current < 0.9) ? 0.005
                  : 0;
        _set(_current + inc);
    }

    function start() {
        if (_trickling) return;   // already running
        _set(0.08);
        _trickling = setInterval(_trickle, 320);
    }

    function done() {
        clearInterval(_trickling);
        _trickling = null;
        _set(1);
        clearTimeout(_timer);
        _timer = setTimeout(() => {
            el.classList.add('nprogress-done');   // CSS fades the bar out
            setTimeout(() => {
                _current = 0;
                bar.style.width = '0%';
            }, 600);
        }, 120);
    }

    window.NProgress = { start, done, set: _set };

    /* ── Intercept navigation links ── */
    document.addEventListener('click', function (e) {
        /* only plain left-clicks with no modifier */
        if (e.button !== 0 || e.ctrlKey || e.metaKey || e.shiftKey || e.altKey) return;

        const anchor = e.target.closest('a[href]');
        if (!anchor) return;

        const href = anchor.getAttribute('href') || '';
        /* skip: hash links, javascript:, target=_blank, downloads, external */
        if (!href
            || href.startsWith('#')
            || href.startsWith('javascript')
            || anchor.target === '_blank'
            || anchor.hasAttribute('download')
            || (anchor.origin && anchor.origin !== location.origin)
        ) return;

        NProgress.start();
    });

    /* ── Intercept form submissions (non-delete — those handled above) ── */
    document.addEventListener('submit', function (e) {
        const form = e.target;
        if (form.hasAttribute('data-confirm-delete')) return; // handled separately
        if (form.dataset.noProgress) return;                  // opt-out escape hatch
        NProgress.start();
    });

    /* ── Finish on page-ready (handles full-page navigations) ── */
    if (document.readyState === 'complete') {
        done();
    } else {
        window.addEventListener('load', done);
    }

    /* ── Back/forward navigation ── */
    window.addEventListener('pageshow', function (e) {
        if (e.persisted) done();
    });
})();

/*
 * Bootstrap Modal Stacking-Context Fix
 * ─────────────────────────────────────
 * .app-container (and any ancestor with contain:paint / will-change:transform /
 * transform / opacity<1) creates a new CSS stacking context. Bootstrap 5 appends
 * .modal-backdrop directly to <body>, so it lives in the root stacking context
 * at z-index:1040. Any modal whose DOM node is inside a stacking-context ancestor
 * with z-index:auto is considered BELOW the backdrop, making the form unclickable
 * (visible through the 50%-opacity overlay but pointer events captured by the
 * backdrop). Fix: move every Bootstrap modal to a direct child of <body> so it
 * participates in the root stacking context alongside the backdrop.
 */
onDomReady(function () {
    document.querySelectorAll('.modal').forEach(function (modal) {
        document.body.appendChild(modal);
    });
});

