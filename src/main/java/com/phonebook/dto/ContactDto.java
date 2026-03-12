package com.phonebook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ContactDto {

    @NotBlank(message = "Name cannot be empty")
    @Size(min = 2, max = 100, message = "Name must contain from 2 to 100 characters")
    @Pattern(regexp = "^[A-Za-zА-Яа-яЁё\\s-]+$", message = "Name must contain only letters")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9+() -]{5,20}$", message = "Invalid phone format")
    private String phoneNumber;

    @Size(max = 500, message = "Note cannot exceed 500 characters")
    private String note;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}