package com.phonebook.service;

import com.phonebook.model.Contact;
import com.phonebook.repository.ContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ContactService {
    
    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);
    
    @Autowired
    private ContactRepository contactRepository;
    
    @Transactional
    public Contact createContact(Contact contact) {
        logger.info("💾 Сохранение нового контакта в БД: '{}'", contact.getFullName());
        
        try {
            Contact savedContact = contactRepository.save(contact);
            logger.info("✅ Контакт успешно сохранен в БД с ID: {}", savedContact.getId());
            logger.debug("📊 Данные в БД: {}", savedContact);
            return savedContact;
        } catch (Exception e) {
            logger.error("❌ Ошибка при сохранении контакта в БД: {}", e.getMessage());
            throw e;
        }
    }
    
    @Transactional
    public Contact updateContact(Long id, Contact contactDetails) {
        logger.info("🔄 Обновление контакта ID {} в БД", id);
        
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("❌ Контакт с ID {} не найден в БД", id);
                return new RuntimeException("Contact not found");
            });
        
        logger.debug("📊 Текущие данные в БД: имя='{}', телефон='{}'", 
            contact.getFullName(), contact.getPhoneNumber());
        
        contact.setFullName(contactDetails.getFullName());
        contact.setPhoneNumber(contactDetails.getPhoneNumber());
        contact.setNote(contactDetails.getNote());
        
        Contact updatedContact = contactRepository.save(contact);
        logger.info("✅ Контакт ID {} успешно обновлен в БД", id);
        logger.debug("📊 Новые данные в БД: имя='{}', телефон='{}'", 
            updatedContact.getFullName(), updatedContact.getPhoneNumber());
        
        return updatedContact;
    }
    
    @Transactional
    public void deleteContact(Long id) {
        logger.info("🗑️ Удаление контакта ID {} из БД", id);
        
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("❌ Контакт с ID {} не найден в БД", id);
                return new RuntimeException("Contact not found");
            });
        
        logger.info("Найден контакт для удаления: '{}'", contact.getFullName());
        contactRepository.delete(contact);
        logger.info("✅ Контакт ID {} удален из БД", id);
    }
    
    public List<Contact> getAllContacts() {
        logger.info("📊 Запрос всех контактов из БД");
        long startTime = System.currentTimeMillis();
        
        List<Contact> contacts = contactRepository.findAll();
        
        long endTime = System.currentTimeMillis();
        logger.info("✅ Получено {} контактов из БД (время запроса: {} мс)", 
            contacts.size(), (endTime - startTime));
        
        return contacts;
    }
    
    public Optional<Contact> getContactById(Long id) {
        logger.info("🔍 Поиск контакта в БД по ID: {}", id);
        
        Optional<Contact> contact = contactRepository.findById(id);
        
        if (contact.isPresent()) {
            logger.info("✅ Контакт найден в БД: '{}'", contact.get().getFullName());
        } else {
            logger.info("❌ Контакт с ID {} не найден в БД", id);
        }
        
        return contact;
    }
    
    public List<Contact> searchContacts(String searchTerm) {
        logger.info("🔎 Поиск в БД по запросу: '{}'", searchTerm);
        long startTime = System.currentTimeMillis();
        
        List<Contact> results = contactRepository.search(searchTerm);
        
        long endTime = System.currentTimeMillis();
        logger.info("✅ Поиск завершен. Найдено {} результатов (время: {} мс)", 
            results.size(), (endTime - startTime));
        
        if (!results.isEmpty()) {
            logger.debug("📊 Результаты поиска: {}", 
                results.stream().map(Contact::getFullName).toList());
        }
        
        return results;
    }
}
