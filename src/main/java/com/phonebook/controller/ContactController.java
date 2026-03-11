package com.phonebook.controller;

import com.phonebook.model.Contact;
import com.phonebook.service.ContactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "*")
public class ContactController {
    
    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);
    
    @Autowired
    private ContactService contactService;
    
    @GetMapping
    public List<Contact> getAllContacts() {
        logger.info("📋 Получен запрос на список ВСЕХ контактов");
        long startTime = System.currentTimeMillis();
        
        List<Contact> contacts = contactService.getAllContacts();
        
        long endTime = System.currentTimeMillis();
        logger.info("✅ УСПЕШНО: найдено {} контактов (время выполнения: {} мс)", 
            contacts.size(), (endTime - startTime));
        
        if (contacts.isEmpty()) {
            logger.warn("⚠️ Внимание: в телефонной книге нет контактов");
        }
        
        return contacts;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContactById(@PathVariable Long id) {
        logger.info("🔍 Поиск контакта по ID: {}", id);
        
        return contactService.getContactById(id)
            .map(contact -> {
                logger.info("✅ Найден контакт: '{}' с телефоном {}", 
                    contact.getFullName(), contact.getPhoneNumber());
                return ResponseEntity.ok(contact);
            })
            .orElseGet(() -> {
                logger.error("❌ ОШИБКА: контакт с ID {} не найден", id);
                return ResponseEntity.notFound().build();
            });
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Contact createContact(@RequestBody Contact contact) {
        logger.info("➕ СОЗДАНИЕ нового контакта: '{}'", contact.getFullName());
        logger.debug("📝 Детали: телефон='{}', заметка='{}'", 
            contact.getPhoneNumber(), 
            contact.getNote() != null ? contact.getNote() : "пусто");
        
        try {
            Contact savedContact = contactService.createContact(contact);
            logger.info("✅ УСПЕШНО: контакт '{}' создан с ID: {}", 
                savedContact.getFullName(), savedContact.getId());
            return savedContact;
        } catch (Exception e) {
            logger.error("❌ ОШИБКА при создании контакта: {}", e.getMessage());
            throw e;
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Contact> updateContact(@PathVariable Long id, @RequestBody Contact contact) {
        logger.info("✏️ ОБНОВЛЕНИЕ контакта ID: {}", id);
        logger.debug("📝 Новые данные: имя='{}', телефон='{}'", 
            contact.getFullName(), contact.getPhoneNumber());
        
        try {
            Contact updatedContact = contactService.updateContact(id, contact);
            logger.info("✅ УСПЕШНО: контакт ID {} обновлен", id);
            logger.debug("📊 После обновления: имя='{}', телефон='{}'", 
                updatedContact.getFullName(), updatedContact.getPhoneNumber());
            return ResponseEntity.ok(updatedContact);
        } catch (RuntimeException e) {
            logger.error("❌ ОШИБКА обновления контакта ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        logger.info("🗑️ УДАЛЕНИЕ контакта ID: {}", id);
        
        try {
            // Сначала проверим, существует ли контакт (для информативного логирования)
            contactService.getContactById(id).ifPresentOrElse(
                contact -> logger.info("Найден контакт для удаления: '{}'", contact.getFullName()),
                () -> logger.warn("Контакт с ID {} не найден, возможно уже удален", id)
            );
            
            contactService.deleteContact(id);
            logger.info("✅ УСПЕШНО: контакт ID {} удален", id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            logger.error("❌ ОШИБКА при удалении контакта ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/search")
    public List<Contact> searchContacts(@RequestParam String q) {
        logger.info("🔎 ПОИСК контактов по запросу: '{}'", q);
        long startTime = System.currentTimeMillis();
        
        List<Contact> results = contactService.searchContacts(q);
        
        long endTime = System.currentTimeMillis();
        logger.info("✅ Найдено {} контактов по запросу '{}' (время: {} мс)", 
            results.size(), q, (endTime - startTime));
        
        if (results.isEmpty()) {
            logger.info("📭 Ничего не найдено по запросу: '{}'", q);
        } else {
            logger.debug("🔍 Результаты поиска: {}", 
                results.stream().map(Contact::getFullName).toList());
        }
        
        return results;
    }
}
