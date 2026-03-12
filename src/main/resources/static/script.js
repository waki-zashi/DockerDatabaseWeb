const API_URL = '/api/contacts';
const MAX_SEARCH_LENGTH = 100;
const MAX_FIELD_LENGTH = 500;
const REQUEST_TIMEOUT = 8000;

document.addEventListener('DOMContentLoaded', () => {
    loadAllContacts();

    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                searchContacts();
            }
        });
    }
});

const form = document.getElementById('contactForm');
if (form) {
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const contactId = document.getElementById('contactId').value.trim();
        const fullName = sanitizeInput(document.getElementById('fullName').value);
        const phoneNumber = sanitizeInput(document.getElementById('phoneNumber').value);
        const note = sanitizeInput(document.getElementById('note').value);

        if (!fullName || !phoneNumber) {
            showNotification('Full name and phone number are required', 'error');
            return;
        }

        if (fullName.length > MAX_FIELD_LENGTH || phoneNumber.length > MAX_FIELD_LENGTH || note.length > MAX_FIELD_LENGTH) {
            showNotification(`Fields cannot exceed ${MAX_FIELD_LENGTH} characters`, 'error');
            return;
        }

        const contact = { fullName, phoneNumber, note };

        showLoading(true);

        try {
            if (contactId) {
                await updateContact(contactId, contact);
                showNotification('Contact successfully updated', 'success');
            } else {
                await createContact(contact);
                showNotification('Contact successfully created', 'success');
            }

            clearForm();
            await loadAllContacts();

        } catch (error) {
            const msg = getUserFriendlyErrorMessage(error.message);
            showNotification(msg, 'error');
        } finally {
            showLoading(false);
        }
    });
}

async function safeFetch(url, options = {}) {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), REQUEST_TIMEOUT);

    try {
        const response = await fetch(url, {
            ...options,
            signal: controller.signal,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers,
            },
        });

        let body;
        const contentType = response.headers.get('content-type');

        if (contentType?.includes('application/json')) {
            body = await response.json().catch(() => ({}));
        } else {
            body = await response.text().catch(() => '');
        }

        if (!response.ok) {
            let errorMessage = 'Request failed';

            if (body && typeof body === 'object') {
                if (body.error) {
                    errorMessage = body.error;
                } else if (Object.keys(body).length > 0) {
                    errorMessage = Object.entries(body)
                        .map(([field, msg]) => `${msg}`)
                        .join(' • ');
                }
            } else if (typeof body === 'string' && body.trim()) {
                errorMessage = body.trim();
            }

            throw new Error(errorMessage || `HTTP ${response.status}`);
        }

        return body;

    } catch (err) {
        if (err.name === 'AbortError') {
            throw new Error('Request timed out');
        }
        throw err;
    } finally {
        clearTimeout(timeoutId);
    }
}

async function createContact(contact) {
    return safeFetch(API_URL, { method: 'POST', body: JSON.stringify(contact) });
}

async function updateContact(id, contact) {
    if (!Number.isInteger(Number(id)) || Number(id) <= 0) {
        throw new Error('Invalid contact ID');
    }
    return safeFetch(`${API_URL}/${id}`, { method: 'PUT', body: JSON.stringify(contact) });
}

async function deleteContact(id) {
    if (!Number.isInteger(Number(id)) || Number(id) <= 0) {
        showNotification('Invalid contact ID', 'error');
        return;
    }

    if (!confirm('Are you sure you want to delete this contact?')) return;

    showLoading(true);
    try {
        await safeFetch(`${API_URL}/${id}`, { method: 'DELETE' });
        showNotification('Contact successfully deleted', 'success');
        await loadAllContacts();
    } catch (err) {
        showNotification(getUserFriendlyErrorMessage(err.message), 'error');
    } finally {
        showLoading(false);
    }
}

async function loadAllContacts() {
    showLoading(true);
    try {
        const contacts = await safeFetch(API_URL);
        displayContacts(Array.isArray(contacts) ? contacts : []);
    } catch (err) {
        showNotification(getUserFriendlyErrorMessage(err.message, 'Failed to load contacts'), 'error');
        displayContacts([]);
    } finally {
        showLoading(false);
    }
}

async function searchContacts() {
    const input = document.getElementById('searchInput');
    const searchTerm = sanitizeInput(input?.value || '');

    if (!searchTerm) {
        loadAllContacts();
        return;
    }

    if (searchTerm.length > MAX_SEARCH_LENGTH) {
        showNotification(`Search query cannot exceed ${MAX_SEARCH_LENGTH} characters`, 'error');
        return;
    }

    showLoading(true);
    try {
        const contacts = await safeFetch(`${API_URL}/search?q=${encodeURIComponent(searchTerm)}`);
        displayContacts(Array.isArray(contacts) ? contacts : []);

        if (!contacts.length) {
            showNotification('No matching contacts found', 'info');
        }
    } catch (err) {
        showNotification(getUserFriendlyErrorMessage(err.message, 'Search failed'), 'error');
    } finally {
        showLoading(false);
    }
}

function getUserFriendlyErrorMessage(rawMessage, fallback = 'Request failed') {
    if (!rawMessage) return fallback;
    const lower = rawMessage.toLowerCase();

    if (lower.includes('between 5 and 20') || lower.includes('from 5 to 20') || lower.includes('must contain between 5')) {
        return 'Phone number must be between 5 and 20 characters long';
    }
    if (lower.includes('invalid phone format') || lower.includes('only contain digits') || lower.includes('can only contain digits')) {
        return 'Phone number can only contain digits, spaces, +, -, (, )';
    }

    if (lower.includes('from 2 to 100') || lower.includes('minimum 2')) {
        return 'Full name must be between 2 and 100 characters';
    }
    if (lower.includes('cannot be empty') || lower.includes('required')) {
        return 'Full name is required';
    }
    if (lower.includes('exceed 500')) {
        return 'Note cannot be longer than 500 characters';
    }
    if (lower.includes('not found with id')) {
        return 'Contact not found (it may have been deleted)';
    }
    if (lower.includes('invalid id') || lower.includes('invalid value for parameter')) {
        return 'Invalid ID format';
    }
    if (lower.includes('timed out')) {
        return 'Request timed out — please try again';
    }

    return rawMessage.length > 120 ? rawMessage.substring(0, 117) + '...' : rawMessage;
}

async function editContact(id) {
    showLoading(true);
    try {
        const contact = await safeFetch(`${API_URL}/${id}`);
        document.getElementById('contactId').value = contact.id || '';
        document.getElementById('fullName').value = contact.fullName || '';
        document.getElementById('phoneNumber').value = contact.phoneNumber || '';
        document.getElementById('note').value = contact.note || '';
        document.querySelector('.form-section').scrollIntoView({ behavior: 'smooth' });
    } catch (err) {
        showNotification(getUserFriendlyErrorMessage(err.message, 'Failed to load contact'), 'error');
    } finally {
        showLoading(false);
    }
}

function displayContacts(contacts) {
    const list = document.getElementById('contactsList');
    if (!list) return;

    if (!contacts?.length) {
        list.innerHTML = '<div class="empty-message">No contacts found</div>';
        return;
    }

    list.innerHTML = contacts.map(c => `
        <div class="contact-card" data-id="${c.id}">
            <h3>${escapeHtml(c.fullName)}</h3>
            <div class="phone">${escapeHtml(c.phoneNumber)}</div>
            ${c.note ? `<div class="note">${escapeHtml(c.note)}</div>` : ''}
            <div class="actions">
                <button onclick="editContact(${c.id})">Edit</button>
                <button onclick="deleteContact(${c.id})">Delete</button>
            </div>
            <small class="date">${formatDate(c.createdAt)}</small>
        </div>
    `).join('');
}

function clearForm() {
    document.getElementById('contactId').value = '';
    document.getElementById('contactForm')?.reset();
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    try {
        return new Date(dateStr).toLocaleString([], {
            year: 'numeric', month: 'short', day: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    } catch {
        return dateStr;
    }
}

function sanitizeInput(value) {
    if (!value) return '';
    return value.trim().substring(0, MAX_FIELD_LENGTH);
}

function escapeHtml(text) {
    if (!text) return '';
    const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
    return text.replace(/[&<>"']/g, m => map[m]);
}

function showNotification(message, type = 'info') {
    const old = document.querySelector('.notification');
    if (old) old.remove();

    const el = document.createElement('div');
    el.className = `notification ${type}`;
    el.textContent = message;

    Object.assign(el.style, {
        position: 'fixed',
        top: '20px',
        right: '20px',
        padding: '12px 20px',
        background: type === 'error' ? '#dc3545' : type === 'success' ? '#198754' : '#0d6efd',
        color: 'white',
        borderRadius: '8px',
        zIndex: '1000',
        boxShadow: '0 4px 12px rgba(0,0,0,0.25)',
        maxWidth: '350px',
        wordBreak: 'break-word'
    });

    document.body.appendChild(el);
    setTimeout(() => el.remove(), type === 'error' ? 5000 : 3000);
}

function showLoading(show) {
    let loader = document.querySelector('.loader');
    if (show) {
        if (!loader) {
            loader = document.createElement('div');
            loader.className = 'loader';
            Object.assign(loader.style, {
                position: 'fixed',
                top: '50%',
                left: '50%',
                width: '48px',
                height: '48px',
                border: '5px solid #e0e0e0',
                borderTop: '5px solid #667eea',
                borderRadius: '50%',
                transform: 'translate(-50%, -50%)',
                zIndex: '9999',
                animation: 'spin 1s linear infinite'
            });
            document.body.appendChild(loader);
        }
    } else if (loader) {
        loader.remove();
    }
}

const style = document.createElement('style');
style.textContent = `
@keyframes spin {
  0% { transform: translate(-50%, -50%) rotate(0deg); }
  100% { transform: translate(-50%, -50%) rotate(360deg); }
}
`;
document.head.appendChild(style);
