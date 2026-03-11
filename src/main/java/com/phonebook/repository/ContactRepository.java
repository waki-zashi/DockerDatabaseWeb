package com.phonebook.repository;

import com.phonebook.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    @Query("""
        SELECT c FROM Contact c
        WHERE LOWER(c.fullName) LIKE LOWER(CONCAT('%', :term, '%'))
        OR c.phoneNumber LIKE CONCAT('%', :term, '%')
        OR LOWER(COALESCE(c.note, '')) LIKE LOWER(CONCAT('%', :term, '%'))
    """)
    List<Contact> search(@Param("term") String term);
}