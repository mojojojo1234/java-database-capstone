package com.project.back_end.DTO;

import java.util.List;

public class DoctorDTO {
    private Long id;
    private String name;
    private String specialty;
    private String email;
    private String phone;
    private List<String> availableTimes;

    public DoctorDTO(Long id, String name, String specialty, String email, String phone, List<String> availableTimes) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.email = email;
        this.phone = phone;
        this.availableTimes = availableTimes;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public List<String> getAvailableTimes() {
        return availableTimes;
    }
}

