package com.phonebook.controller;

import com.phonebook.dto.ContactDto;
import com.phonebook.model.Contact;
import com.phonebook.service.ContactService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ResponseEntity<List<Contact>> getAllContacts() {
        logger.info("Request received: get all contacts");
        List<Contact> contacts = contactService.getAllContacts();
        return ResponseEntity.ok(contacts);
    }

    @PostMapping
    public ResponseEntity<Contact> createContact(@Valid @RequestBody ContactDto contactDto) {
        logger.info("Creating new contact");
        Contact saved = contactService.createContact(contactDto);
        logger.info("Contact created with id {}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contact> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactDto contactDto) {
        logger.info("Updating contact {}", id);
        Contact updated = contactService.updateContact(id, contactDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        logger.info("Deleting contact {}", id);
        contactService.deleteContact(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Contact>> searchContacts(@RequestParam String q) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        String trimmed = q.trim();
        if (trimmed.length() > 100) {
            logger.warn("Search query too long: {}", trimmed.length());
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        logger.info("Searching for: {}", trimmed);
        List<Contact> results = contactService.searchContacts(trimmed);
        return ResponseEntity.ok(results);
    }
}