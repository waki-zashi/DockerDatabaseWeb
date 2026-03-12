import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    @Size(min = 2, max = 100, message = "Name can contane from 2 to 100 simbols")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @NotBlank(message = "Phone number is necessary")
    @Pattern(
        regexp = "^[0-9+\\-() ]{5,20}$",
        message = "Wrong format"
    )
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Size(max = 500, message = "Note cannot contane more that 500 simbols")
    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { 
        return id; 
    }
    
    public String getFullName() { 
        return fullName; 
    }
    
    public String getPhoneNumber() { 
        return phoneNumber; 
    }
    
    public String getNote() { 
        return note; 
    }
    
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }

    // Setters
    public void setId(Long id) { 
        this.id = id; 
    }

    public void setFullName(String fullName) {
        if (fullName != null) {
            this.fullName = fullName.trim();
        }
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber.trim();
        }
    }

    public void setNote(String note) {
        if (note != null) {
            this.note = note.trim();
        }
    }
}
