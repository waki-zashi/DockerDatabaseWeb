// API URL для запросов к серверу
const API_URL = '/api/contacts';

// Загружаем все контакты при загрузке страницы
document.addEventListener('DOMContentLoaded', () => {
    loadAllContacts();
    
    // Добавляем обработчик для поиска при нажатии Enter
    document.getElementById('searchInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            searchContacts();
        }
    });
});

// Обработка отправки формы
document.getElementById('contactForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const contactId = document.getElementById('contactId').value;
    const contact = {
        fullName: document.getElementById('fullName').value.trim(),
        phoneNumber: document.getElementById('phoneNumber').value.trim(),
        note: document.getElementById('note').value.trim()
    };
    
    // Валидация
    if (!contact.fullName || !contact.phoneNumber) {
        showNotification('Заполните обязательные поля!', 'error');
        return;
    }
    
    // Показываем индикатор загрузки
    showLoading(true);
    
    try {
        if (contactId) {
            await updateContact(contactId, contact);
            showNotification('Контакт успешно обновлен!', 'success');
        } else {
            await createContact(contact);
            showNotification('Контакт успешно добавлен!', 'success');
        }
        clearForm();
        await loadAllContacts();
    } catch (error) {
        console.error('Error:', error);
        showNotification('Ошибка при сохранении контакта', 'error');
    } finally {
        showLoading(false);
    }
});

// Создание нового контакта
async function createContact(contact) {
    const response = await fetch(API_URL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(contact)
    });
    
    if (!response.ok) {
        throw new Error('Failed to create contact');
    }
    
    return response.json();
}

// Обновление существующего контакта
async function updateContact(id, contact) {
    const response = await fetch(`${API_URL}/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(contact)
    });
    
    if (!response.ok) {
        throw new Error('Failed to update contact');
    }
    
    return response.json();
}

// Удаление контакта
async function deleteContact(id) {
    if (!confirm('Вы уверены, что хотите удалить этот контакт?')) {
        return;
    }
    
    showLoading(true);
    
    try {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) {
            throw new Error('Failed to delete contact');
        }
        
        showNotification('Контакт удален', 'success');
        await loadAllContacts();
    } catch (error) {
        console.error('Error deleting contact:', error);
        showNotification('Ошибка при удалении контакта', 'error');
    } finally {
        showLoading(false);
    }
}

// Загрузка всех контактов
async function loadAllContacts() {
    showLoading(true);
    
    try {
        const response = await fetch(API_URL);
        if (!response.ok) {
            throw new Error('Failed to load contacts');
        }
        const contacts = await response.json();
        displayContacts(contacts);
    } catch (error) {
        console.error('Error loading contacts:', error);
        showNotification('Ошибка при загрузке контактов', 'error');
        displayContacts([]);
    } finally {
        showLoading(false);
    }
}

// Поиск контактов
async function searchContacts() {
    const searchTerm = document.getElementById('searchInput').value.trim();
    
    if (!searchTerm) {
        loadAllContacts();
        return;
    }
    
    showLoading(true);
    
    try {
        const response = await fetch(`${API_URL}/search?q=${encodeURIComponent(searchTerm)}`);
        if (!response.ok) {
            throw new Error('Failed to search contacts');
        }
        const contacts = await response.json();
        displayContacts(contacts);
        
        if (contacts.length === 0) {
            showNotification('Ничего не найдено', 'info');
        }
    } catch (error) {
        console.error('Error searching contacts:', error);
        showNotification('Ошибка при поиске', 'error');
    } finally {
        showLoading(false);
    }
}

// Отображение контактов на странице
function displayContacts(contacts) {
    const contactsList = document.getElementById('contactsList');
    
    if (!contacts || contacts.length === 0) {
        contactsList.innerHTML = '<div class="empty-message">📭 Контакты не найдены</div>';
        return;
    }
    
    contactsList.innerHTML = contacts.map(contact => `
        <div class="contact-card" data-id="${contact.id}">
            <h3>${escapeHtml(contact.fullName)}</h3>
            <div class="phone">${escapeHtml(contact.phoneNumber)}</div>
            ${contact.note ? `<div class="note">${escapeHtml(contact.note)}</div>` : ''}
            <div class="actions">
                <button class="edit-btn" onclick="editContact(${contact.id})">
                    <span>✏️</span> Редактировать
                </button>
                <button class="delete-btn" onclick="deleteContact(${contact.id})">
                    <span>🗑️</span> Удалить
                </button>
            </div>
            <small class="date">
                ${formatDate(contact.createdAt)}
            </small>
        </div>
    `).join('');
}

// Редактирование контакта (загрузка данных в форму)
async function editContact(id) {
    showLoading(true);
    
    try {
        const response = await fetch(`${API_URL}/${id}`);
        if (!response.ok) {
            throw new Error('Failed to load contact');
        }
        const contact = await response.json();
        
        document.getElementById('contactId').value = contact.id;
        document.getElementById('fullName').value = contact.fullName;
        document.getElementById('phoneNumber').value = contact.phoneNumber;
        document.getElementById('note').value = contact.note || '';
        
        // Плавная прокрутка к форме
        document.querySelector('.form-section').scrollIntoView({ 
            behavior: 'smooth' 
        });
        
        // Подсветка формы
        document.getElementById('contactForm').style.animation = 'highlight 1s';
        setTimeout(() => {
            document.getElementById('contactForm').style.animation = '';
        }, 1000);
        
    } catch (error) {
        console.error('Error loading contact:', error);
        showNotification('Ошибка при загрузке контакта', 'error');
    } finally {
        showLoading(false);
    }
}

// Очистка формы
function clearForm() {
    document.getElementById('contactId').value = '';
    document.getElementById('contactForm').reset();
    
    // Убираем подсветку с полей
    document.querySelectorAll('input, textarea').forEach(field => {
        field.style.borderColor = '';
    });
}

// Форматирование даты
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Защита от XSS атак
function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// Показ уведомлений
function showNotification(message, type = 'info') {
    // Удаляем старое уведомление, если есть
    const oldNotification = document.querySelector('.notification');
    if (oldNotification) {
        oldNotification.remove();
    }
    
    // Создаем новое уведомление
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    
    // Стили для уведомления
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 25px;
        border-radius: 10px;
        color: white;
        font-weight: 600;
        z-index: 1000;
        animation: slideIn 0.3s ease-out;
        box-shadow: 0 5px 15px rgba(0,0,0,0.3);
    `;
    
    // Цвет в зависимости от типа
    if (type === 'success') {
        notification.style.background = 'linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)';
    } else if (type === 'error') {
        notification.style.background = 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)';
    } else {
        notification.style.background = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
    }
    
    document.body.appendChild(notification);
    
    // Удаляем через 3 секунды
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 3000);
}

// Показ/скрытие индикатора загрузки
function showLoading(show) {
    let loader = document.querySelector('.loader');
    
    if (show) {
        if (!loader) {
            loader = document.createElement('div');
            loader.className = 'loader';
            loader.style.cssText = `
                position: fixed;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                width: 50px;
                height: 50px;
                border: 5px solid #f3f3f3;
                border-top: 5px solid #667eea;
                border-radius: 50%;
                animation: spin 1s linear infinite;
                z-index: 1001;
            `;
            document.body.appendChild(loader);
            
            // Затемняющий фон
            const overlay = document.createElement('div');
            overlay.className = 'loader-overlay';
            overlay.style.cssText = `
                position: fixed;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: rgba(255,255,255,0.8);
                z-index: 1000;
            `;
            document.body.appendChild(overlay);
        }
    } else {
        if (loader) {
            loader.remove();
            document.querySelector('.loader-overlay')?.remove();
        }
    }
}

// Добавляем CSS анимации
const style = document.createElement('style');
style.textContent = `
    @keyframes spin {
        0% { transform: translate(-50%, -50%) rotate(0deg); }
        100% { transform: translate(-50%, -50%) rotate(360deg); }
    }
    
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
    
    @keyframes highlight {
        0% { background-color: #fff; }
        50% { background-color: #e3f2fd; }
        100% { background-color: #fff; }
    }
    
    .contact-card {
        position: relative;
        padding-bottom: 35px;
    }
    
    .date {
        position: absolute;
        bottom: 10px;
        right: 15px;
        color: #999;
        font-size: 11px;
    }
`;
document.head.appendChild(style);
