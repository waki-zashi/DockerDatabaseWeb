package com.phonebook.service;

import com.phonebook.model.Contact;
import com.phonebook.repository.ContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Contact createContact(Contact contact) {

        if (contact == null) {
            logger.warn("Attempt to save null contact");
            throw new IllegalArgumentException("Contact cannot be null");
        }

        try {

            Contact savedContact = contactRepository.save(contact);

            logger.info("Contact saved with id {}", savedContact.getId());

            return savedContact;

        } catch (Exception e) {

            logger.error("Database error while saving contact: {}", e.getMessage());

            throw new RuntimeException("Database error");
        }
    }

    @Transactional
    public Contact updateContact(Long id, Contact contactDetails) {

        if (id == null || id <= 0) {
            logger.warn("Invalid contact id {}", id);
            throw new IllegalArgumentException("Invalid id");
        }

        if (contactDetails == null) {
            logger.warn("Attempt to update contact with null data");
            throw new IllegalArgumentException("Contact data cannot be null");
        }

        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Contact not found {}", id);
                    return new RuntimeException("Contact not found");
                });

        contact.setFullName(contactDetails.getFullName());
        contact.setPhoneNumber(contactDetails.getPhoneNumber());
        contact.setNote(contactDetails.getNote());

        try {

            Contact updatedContact = contactRepository.save(contact);

            logger.info("Contact updated {}", id);

            return updatedContact;

        } catch (Exception e) {

            logger.error("Database error while updating contact {}: {}", id, e.getMessage());

            throw new RuntimeException("Database error");
        }
    }

    @Transactional
    public void deleteContact(Long id) {

        if (id == null || id <= 0) {
            logger.warn("Invalid contact id {}", id);
            throw new IllegalArgumentException("Invalid id");
        }

        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Contact not found {}", id);
                    return new RuntimeException("Contact not found");
                });

        try {

            contactRepository.delete(contact);

            logger.info("Contact deleted {}", id);

        } catch (Exception e) {

            logger.error("Database error while deleting contact {}: {}", id, e.getMessage());

            throw new RuntimeException("Database error");
        }
    }

    public List<Contact> getAllContacts() {

        try {

            List<Contact> contacts = contactRepository.findAll();

            logger.info("Contacts retrieved: {}", contacts.size());

            return contacts;

        } catch (Exception e) {

            logger.error("Database error while retrieving contacts: {}", e.getMessage());

            return Collections.emptyList();
        }
    }

    public Optional<Contact> getContactById(Long id) {

        if (id == null || id <= 0) {
            logger.warn("Invalid contact id {}", id);
            return Optional.empty();
        }

        try {

            Optional<Contact> contact = contactRepository.findById(id);

            if (contact.isPresent()) {
                logger.info("Contact found {}", id);
            } else {
                logger.warn("Contact not found {}", id);
            }

            return contact;

        } catch (Exception e) {

            logger.error("Database error while retrieving contact {}: {}", id, e.getMessage());

            return Optional.empty();
        }
    }

    public List<Contact> searchContacts(String searchTerm) {

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Collections.emptyList();
        }

        if (searchTerm.length() > 100) {
            logger.warn("Search query too long");
            return Collections.emptyList();
        }

        try {

            List<Contact> results = contactRepository.search(searchTerm.trim());

            logger.info("Search results: {}", results.size());

            return results;

        } catch (Exception e) {

            logger.error("Database error during search: {}", e.getMessage());

            return Collections.emptyList();
        }
    }
}