package com.phonebook.service;

import com.phonebook.dto.ContactDto;
import com.phonebook.exception.ContactNotFoundException;
import com.phonebook.model.Contact;
import com.phonebook.repository.ContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ContactService {

    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);
    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Transactional
    public Contact createContact(ContactDto dto) {
        Contact contact = new Contact();
        mapDtoToEntity(dto, contact);
        Contact saved = contactRepository.save(contact);
        logger.info("Contact saved with id {}", saved.getId());
        return saved;
    }

    @Transactional
    public Contact updateContact(Long id, ContactDto dto) {
        validateId(id);
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException(id));
        mapDtoToEntity(dto, contact);
        Contact updated = contactRepository.save(contact);
        logger.info("Contact updated {}", id);
        return updated;
    }

    @Transactional
    public void deleteContact(Long id) {
        validateId(id);
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException(id));
        contactRepository.delete(contact);
        logger.info("Contact deleted {}", id);
    }

    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }

    public Optional<Contact> getContactById(Long id) {
        validateId(id);
        return contactRepository.findById(id);
    }

    public List<Contact> searchContacts(String searchTerm) {
        if (searchTerm.isEmpty()) {
            return Collections.emptyList();
        }
        String safeTerm = searchTerm
                .replace("%", "\\%")
                .replace("_", "\\_");
        return contactRepository.search(safeTerm);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            logger.warn("Invalid contact id: {}", id);
            throw new IllegalArgumentException("Invalid contact id");
        }
    }

    private void mapDtoToEntity(ContactDto dto, Contact contact) {
        contact.setFullName(HtmlUtils.htmlEscape(dto.getFullName()));
        contact.setPhoneNumber(HtmlUtils.htmlEscape(dto.getPhoneNumber()));
        contact.setNote(dto.getNote() != null ? HtmlUtils.htmlEscape(dto.getNote()) : null);
    }
}