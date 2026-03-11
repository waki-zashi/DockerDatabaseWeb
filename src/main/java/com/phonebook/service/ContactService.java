package com.phonebook.service;

import com.phonebook.model.Contact;
import com.phonebook.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ContactService {
    
    @Autowired
    private ContactRepository contactRepository;
    
    @Transactional
    public Contact createContact(Contact contact) {
        return contactRepository.save(contact);
    }
    
    @Transactional
    public Contact updateContact(Long id, Contact contactDetails) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Contact not found"));
        
        contact.setFullName(contactDetails.getFullName());
        contact.setPhoneNumber(contactDetails.getPhoneNumber());
        contact.setNote(contactDetails.getNote());
        
        return contactRepository.save(contact);
    }
    
    @Transactional
    public void deleteContact(Long id) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Contact not found"));
        contactRepository.delete(contact);
    }
    
    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }
    
    public Optional<Contact> getContactById(Long id) {
        return contactRepository.findById(id);
    }
    
    public List<Contact> searchContacts(String searchTerm) {
        return contactRepository.search(searchTerm);
    }
}
