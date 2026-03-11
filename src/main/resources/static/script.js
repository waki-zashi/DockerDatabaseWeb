const API_URL = '/api/contacts';
const MAX_LENGTH = 255;
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

        const contactId = document.getElementById('contactId').value;

        const fullName = sanitizeInput(document.getElementById('fullName').value);
        const phoneNumber = sanitizeInput(document.getElementById('phoneNumber').value);
        const note = sanitizeInput(document.getElementById('note').value);

        if (!fullName || !phoneNumber) {
            showNotification('Required fields are missing', 'error');
            return;
        }

        if (fullName.length > MAX_LENGTH || phoneNumber.length > MAX_LENGTH || note.length > MAX_LENGTH) {
            showNotification('Input too long', 'error');
            return;
        }

        const contact = { fullName, phoneNumber, note };

        showLoading(true);

        try {
            if (contactId) {
                await updateContact(contactId, contact);
                showNotification('Contact updated', 'success');
            } else {
                await createContact(contact);
                showNotification('Contact created', 'success');
            }

            clearForm();
            await loadAllContacts();

        } catch (error) {
            showNotification('Request failed', 'error');
        } finally {
            showLoading(false);
        }
    });
}

async function safeFetch(url, options = {}) {

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), REQUEST_TIMEOUT);

    try {
        const response = await fetch(url, {
            ...options,
            signal: controller.signal
        });

        if (!response.ok) {
            throw new Error('HTTP error');
        }

        const text = await response.text();

        try {
            return text ? JSON.parse(text) : {};
        } catch {
            throw new Error('Invalid JSON');
        }

    } finally {
        clearTimeout(timeout);
    }
}

async function createContact(contact) {

    return safeFetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(contact)
    });
}

async function updateContact(id, contact) {

    if (!Number.isInteger(Number(id))) {
        throw new Error('Invalid id');
    }

    return safeFetch(`${API_URL}/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(contact)
    });
}

async function deleteContact(id) {

    if (!Number.isInteger(Number(id))) {
        showNotification('Invalid id', 'error');
        return;
    }

    if (!confirm('Delete this contact?')) {
        return;
    }

    showLoading(true);

    try {

        await safeFetch(`${API_URL}/${id}`, {
            method: 'DELETE'
        });

        showNotification('Contact deleted', 'success');
        await loadAllContacts();

    } catch {
        showNotification('Delete failed', 'error');
    } finally {
        showLoading(false);
    }
}

async function loadAllContacts() {

    showLoading(true);

    try {

        const contacts = await safeFetch(API_URL);
        displayContacts(Array.isArray(contacts) ? contacts : []);

    } catch {

        showNotification('Failed to load contacts', 'error');
        displayContacts([]);

    } finally {
        showLoading(false);
    }
}

async function searchContacts() {

    const searchTerm = sanitizeInput(document.getElementById('searchInput').value);

    if (!searchTerm) {
        loadAllContacts();
        return;
    }

    showLoading(true);

    try {

        const contacts = await safeFetch(`${API_URL}/search?q=${encodeURIComponent(searchTerm)}`);

        displayContacts(Array.isArray(contacts) ? contacts : []);

        if (!contacts.length) {
            showNotification('No results', 'info');
        }

    } catch {

        showNotification('Search failed', 'error');

    } finally {
        showLoading(false);
    }
}

function displayContacts(contacts) {

    const contactsList = document.getElementById('contactsList');

    if (!contacts || contacts.length === 0) {
        contactsList.innerHTML = '<div class="empty-message">No contacts found</div>';
        return;
    }

    contactsList.innerHTML = contacts.map(contact => `
        <div class="contact-card" data-id="${contact.id}">
            <h3>${escapeHtml(contact.fullName)}</h3>
            <div class="phone">${escapeHtml(contact.phoneNumber)}</div>
            ${contact.note ? `<div class="note">${escapeHtml(contact.note)}</div>` : ''}
            <div class="actions">
                <button onclick="editContact(${contact.id})">Edit</button>
                <button onclick="deleteContact(${contact.id})">Delete</button>
            </div>
            <small class="date">${formatDate(contact.createdAt)}</small>
        </div>
    `).join('');
}

async function editContact(id) {

    showLoading(true);

    try {

        const contact = await safeFetch(`${API_URL}/${id}`);

        document.getElementById('contactId').value = contact.id;
        document.getElementById('fullName').value = contact.fullName || '';
        document.getElementById('phoneNumber').value = contact.phoneNumber || '';
        document.getElementById('note').value = contact.note || '';

        document.querySelector('.form-section').scrollIntoView({ behavior: 'smooth' });

    } catch {

        showNotification('Load failed', 'error');

    } finally {
        showLoading(false);
    }
}

function clearForm() {

    document.getElementById('contactId').value = '';
    document.getElementById('contactForm').reset();
}

function formatDate(dateString) {

    if (!dateString) return '';

    const date = new Date(dateString);

    return date.toLocaleString();
}

function sanitizeInput(value) {

    if (!value) return '';

    return value.trim().substring(0, MAX_LENGTH);
}

function escapeHtml(text) {

    if (!text) return '';

    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function showNotification(message, type = 'info') {

    const old = document.querySelector('.notification');
    if (old) old.remove();

    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;

    notification.style.position = 'fixed';
    notification.style.top = '20px';
    notification.style.right = '20px';
    notification.style.padding = '12px 20px';
    notification.style.background = '#333';
    notification.style.color = '#fff';
    notification.style.borderRadius = '8px';
    notification.style.zIndex = '1000';

    document.body.appendChild(notification);

    setTimeout(() => notification.remove(), 3000);
}

function showLoading(show) {

    let loader = document.querySelector('.loader');

    if (show) {

        if (!loader) {

            loader = document.createElement('div');
            loader.className = 'loader';

            loader.style.position = 'fixed';
            loader.style.top = '50%';
            loader.style.left = '50%';
            loader.style.width = '40px';
            loader.style.height = '40px';
            loader.style.border = '4px solid #ccc';
            loader.style.borderTop = '4px solid #333';
            loader.style.borderRadius = '50%';
            loader.style.animation = 'spin 1s linear infinite';

            document.body.appendChild(loader);
        }

    } else {

        if (loader) loader.remove();

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