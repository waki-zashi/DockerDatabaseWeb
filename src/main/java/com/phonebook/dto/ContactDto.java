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
    @Size(min = 5, max = 20, message = "Phone number must be between 5 and 20 characters long")
    @Pattern(regexp = "^[0-9+() \\-]+$", message = "Phone number can only contain digits, spaces, +, -, ( and )")
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