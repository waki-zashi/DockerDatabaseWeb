package com.phonebook.repository;

import com.phonebook.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    
    List<Contact> findByFullNameContainingIgnoreCase(String fullName);
    
    List<Contact> findByPhoneNumberContaining(String phoneNumber);
    
    @Query("SELECT c FROM Contact c WHERE " +
           "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "c.phoneNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
           "LOWER(c.note) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Contact> search(@Param("searchTerm") String searchTerm);
}
