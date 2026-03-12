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
@CrossOrigin(origins = "*")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ResponseEntity<List<Contact>> getAllContacts() {

        try {

            logger.info("Request received: get all contacts");

            List<Contact> contacts = contactService.getAllContacts();

            return ResponseEntity.ok(contacts);

        } catch (Exception e) {

            logger.error("Error retrieving contacts: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContactById(@PathVariable Long id) {

        if (id == null || id <= 0) {
            logger.warn("Invalid contact ID: {}", id);
            return ResponseEntity.badRequest().build();
        }

        try {

            return contactService.getContactById(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("Contact not found: {}", id);
                        return ResponseEntity.notFound().build();
                    });

        } catch (Exception e) {

            logger.error("Error retrieving contact {}: {}", id, e.getMessage());

            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createContact(@Valid @RequestBody Contact contact) {

        if (contact == null) {
            logger.warn("Attempt to create null contact");
            return ResponseEntity.badRequest().body("Contact cannot be null");
        }

        try {

            Contact saved = contactService.createContact(contact);

            logger.info("Contact created with id {}", saved.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {

            logger.error("Error creating contact: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating contact");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateContact(@PathVariable Long id,
                                           @Valid @RequestBody Contact contact) {

        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("Invalid contact id");
        }

        if (contact == null) {
            return ResponseEntity.badRequest().body("Contact cannot be null");
        }

        try {

            Contact updated = contactService.updateContact(id, contact);

            logger.info("Contact updated: {}", id);

            return ResponseEntity.ok(updated);

        } catch (RuntimeException e) {

            logger.warn("Contact not found: {}", id);

            return ResponseEntity.notFound().build();

        } catch (Exception e) {

            logger.error("Error updating contact {}: {}", id, e.getMessage());

            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContact(@PathVariable Long id) {

        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("Invalid contact id");
        }

        try {

            contactService.deleteContact(id);

            logger.info("Contact deleted: {}", id);

            return ResponseEntity.ok().build();

        } catch (RuntimeException e) {

            logger.warn("Contact not found: {}", id);

            return ResponseEntity.notFound().build();

        } catch (Exception e) {

            logger.error("Error deleting contact {}: {}", id, e.getMessage());

            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Contact>> searchContacts(@RequestParam String q) {

        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        if (q.length() > 100) {
            logger.warn("Search query too long");
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        try {

            List<Contact> results = contactService.searchContacts(q.trim());

            return ResponseEntity.ok(results);

        } catch (Exception e) {

            logger.error("Search error: {}", e.getMessage());

            return ResponseEntity.internalServerError()
                    .body(Collections.emptyList());
        }
    }
}
