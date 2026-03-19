package com.project.back_end.services;


import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import jakarta.transaction.Transactional;
import com.project.back_end.DTO.DoctorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {


    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository, TokenService tokenService) {
        this.appointmentRepository=appointmentRepository;
        this.doctorRepository=doctorRepository;
        this.tokenService=tokenService;
    }

    @Transactional
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
            List<String> slots=new ArrayList<>();

        LocalDateTime start=date.atStartOfDay();
        LocalDateTime end=date.atTime(23,59);
        List<Appointment> appointments=appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);

        Set<LocalTime> bookedSlots=appointments.stream().map(a->a.getAppointmentTime().toLocalTime().withMinute(0)).collect(Collectors.toSet());

        LocalTime current=LocalTime.of(0,0);
        while(current.isBefore(LocalTime.of(23,59))) {
            if(!bookedSlots.contains(current)){
                slots.add(current.toString());
            }
            current=current.plusHours(1);
        }
        return slots;
    }

    public List<Doctor> filterDoctorsByNameAndSpecialty(String name, String specialty) {

        List<Doctor> doctors =
                doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
                        name, specialty
                );

        return doctors;
    }
    public List<Doctor> filterDoctorsByNameSpecialtyandTime(String name, String specialty, String time) {

        List<Doctor> doctors =
                doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
                        name, specialty
                );

        if (time == null || time.isBlank()) {
            return doctors;
        }

        return doctors.stream()
                .filter(doctor ->
                        doctor.getAvailableTimes().stream()
                                .anyMatch(slot -> slot.toUpperCase().contains(time.toUpperCase()))
                )
                .toList();
    }

    @Transactional
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Transactional
    public List<DoctorDTO> getAllDoctorDtos() {
        return doctorRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public List<DoctorDTO> filterDoctorDtos(String name, String specialty, String time) {
        // Reuse existing filtering methods so behavior stays the same, but convert to DTOs
        List<Doctor> doctors;
        if (name != null && specialty != null && time != null) {
            doctors = filterDoctorsByNameSpecialtyandTime(name, specialty, time);
        } else if (name != null && specialty != null) {
            doctors = filterDoctorsByNameAndSpecialty(name, specialty);
        } else if (name != null && time != null) {
            doctors = filterDoctorsByNameAndTime(name, time);
        } else if (specialty != null && time != null) {
            doctors = filterDoctorsBySpecialtyAndTime(specialty, time);
        } else if (name != null) {
            doctors = filterDoctorsByName(name);
        } else if (specialty != null) {
            doctors = filterDoctorsBySpecility(specialty);
        } else if (time != null) {
            doctors = filterDoctorsByTime(time);
        } else {
            doctors = getAllDoctors();
        }

        return doctors.stream().map(this::toDto).toList();
    }

    private DoctorDTO toDto(Doctor d) {
        // Touch availableTimes inside transaction to avoid lazy-init issues at JSON serialization time
        List<String> availableTimes = d.getAvailableTimes() == null ? List.of() : List.copyOf(d.getAvailableTimes());
        return new DoctorDTO(
                d.getId(),
                d.getName(),
                d.getSpecialty(),
                d.getEmail(),
                d.getPhone(),
                availableTimes
        );
    }


    @Transactional
    public List<Doctor> filterDoctorsByName(String name) {

        return doctorRepository.findAll()
                .stream()
                .filter(d -> d.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    @Transactional
    public List<Doctor> filterDoctorsBySpecility(String specialty) {

        return doctorRepository.findAll()
                .stream()
                .filter(d -> d.getSpecialty().toLowerCase().contains(specialty.toLowerCase()))
                .toList();
    }

    @Transactional
    public List<Doctor> filterDoctorsByTime(String time) {

        return doctorRepository.findAll()
                .stream()
                .filter(d ->
                        d.getAvailableTimes().stream()
                                .anyMatch(slot -> slot.toUpperCase().contains(time.toUpperCase()))
                )
                .toList();
    }

    @Transactional
    public List<Doctor> filterDoctorsByNameAndTime(String name, String time) {

        return doctorRepository.findAll()
                .stream()
                .filter(d -> d.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(d ->
                        d.getAvailableTimes().stream()
                                .anyMatch(slot -> slot.toUpperCase().contains(time.toUpperCase()))
                )
                .toList();
    }

    @Transactional
    public List<Doctor> filterDoctorsBySpecialtyAndTime(String specialty, String time) {

        return doctorRepository.findAll()
                .stream()
                .filter(d -> d.getSpecialty().toLowerCase().contains(specialty.toLowerCase()))
                .filter(d ->
                        d.getAvailableTimes().stream()
                                .anyMatch(slot -> slot.toUpperCase().contains(time.toUpperCase()))
                )
                .toList();
    }


    public int saveDoctor(Doctor doctor) {
        try {
        Optional<Doctor> doctorOptional=doctorRepository.findByEmail(doctor.getEmail());
        if(doctorOptional.isPresent()) return -1;
        doctorRepository.save(doctor);
        return 1;
        }
        catch(Exception e) {
            return 0;
        }
    }

    public int updateDoctor(Doctor doctor) {
        try {
            Optional<Doctor> doctorOptional=doctorRepository.findById(doctor.getId());
            if(doctorOptional.isEmpty()) return -1;
            doctorRepository.save(doctor);
            return 1;
        }
        catch(Exception e) {
            return 0;
        }
    }
//    @Transactional
//    public List<Doctor> getDoctors() {
//        List<Doctor> doctors=doctorRepository.findAll();
//        return doctors;
//    }

    public int deleteDoctor(Long id) {
        try {
            Optional<Doctor> doctorOptional=doctorRepository.findById(id);
            if(doctorOptional.isEmpty()) {
                return -1;
            }
//            Long doctorId=doctorOptional.get().getId();
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        }
        catch(Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> validateDoctor(String username, String password) {
        Map<String, String> response=new HashMap<>();
        try {
            Optional<Doctor> optionalDoctor=doctorRepository.findByEmail(username);
            if(optionalDoctor.isEmpty()) {
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            Doctor doctor=optionalDoctor.get();
            if(!doctor.getPassword().equals(password)) {
                response.put("message","Invalid password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            String token=tokenService.generateToken(doctor.getEmail());
            response.put("token",token);
            return ResponseEntity.ok(response);
        }
        catch(Exception e) {
            response.put("error","Internal server error occured");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}




// 11. **filterDoctorsByNameSpecilityandTime Method**:
//    - Filters doctors based on their name, specialty, and availability during a specific time (AM/PM).
//    - The method fetches doctors matching the name and specialty criteria, then filters them based on their availability during the specified time period.
//    - Instruction: Ensure proper filtering based on both the name and specialty as well as the specified time period.

// 12. **filterDoctorByTime Method**:
//    - Filters a list of doctors based on whether their available times match the specified time period (AM/PM).
//    - This method processes a list of doctors and their available times to return those that fit the time criteria.
//    - Instruction: Ensure that the time filtering logic correctly handles both AM and PM time slots and edge cases.


// 13. **filterDoctorByNameAndTime Method**:
//    - Filters doctors based on their name and the specified time period (AM/PM).
//    - Fetches doctors based on partial name matching and filters the results to include only those available during the specified time period.
//    - Instruction: Ensure that the method correctly filters doctors based on the given name and time of day (AM/PM).

// 14. **filterDoctorByNameAndSpecialty Method**:
//    - Filters doctors by name and specialty.
//    - It ensures that the resulting list of doctors matches both the name (case-insensitive) and the specified specialty.
//    - Instruction: Ensure that both name and specialty are considered when filtering doctors.


// 15. **filterDoctorByTimeAndSpecialty Method**:
//    - Filters doctors based on their specialty and availability during a specific time period (AM/PM).
//    - Fetches doctors based on the specified specialty and filters them based on their available time slots for AM/PM.
//    - Instruction: Ensure the time filtering is accurately applied based on the given specialty and time period (AM/PM).

// 16. **filterDoctorBySpecialty Method**:
//    - Filters doctors based on their specialty.
//    - This method fetches all doctors matching the specified specialty and returns them.
//    - Instruction: Make sure the filtering logic works for case-insensitive specialty matching.

// 17. **filterDoctorsByTime Method**:
//    - Filters all doctors based on their availability during a specific time period (AM/PM).
//    - The method checks all doctors' available times and returns those available during the specified time period.
//    - Instruction: Ensure proper filtering logic to handle AM/PM time periods.


// 1. **Add @Service Annotation**:
//    - This class should be annotated with `@Service` to indicate that it is a service layer class.
//    - The `@Service` annotation marks this class as a Spring-managed bean for business logic.
//    - Instruction: Add `@Service` above the class declaration.

// 2. **Constructor Injection for Dependencies**:
//    - The `DoctorService` class depends on `DoctorRepository`, `AppointmentRepository`, and `TokenService`.
//    - These dependencies should be injected via the constructor for proper dependency management.
//    - Instruction: Ensure constructor injection is used for injecting dependencies into the service.

// 3. **Add @Transactional Annotation for Methods that Modify or Fetch Database Data**:
//    - Methods like `getDoctorAvailability`, `getDoctors`, `findDoctorByName`, `filterDoctorsBy*` should be annotated with `@Transactional`.
//    - The `@Transactional` annotation ensures that database operations are consistent and wrapped in a single transaction.
//    - Instruction: Add the `@Transactional` annotation above the methods that perform database operations or queries.
// 4. **getDoctorAvailability Method**:
//    - Retrieves the available time slots for a specific doctor on a particular date and filters out already booked slots.
//    - The method fetches all appointments for the doctor on the given date and calculates the availability by comparing against booked slots.
//    - Instruction: Ensure that the time slots are properly formatted and the available slots are correctly filtered.
// 5. **saveDoctor Method**:
//    - Used to save a new doctor record in the database after checking if a doctor with the same email already exists.
//    - If a doctor with the same email is found, it returns `-1` to indicate conflict; `1` for success, and `0` for internal errors.
//    - Instruction: Ensure that the method correctly handles conflicts and exceptions when saving a doctor.
// 6. **updateDoctor Method**:
//    - Updates an existing doctor's details in the database. If the doctor doesn't exist, it returns `-1`.
//    - Instruction: Make sure that the doctor exists before attempting to save the updated record and handle any errors properly.
// 7. **getDoctors Method**:
//    - Fetches all doctors from the database. It is marked with `@Transactional` to ensure that the collection is properly loaded.
//    - Instruction: Ensure that the collection is eagerly loaded, especially if dealing with lazy-loaded relationships (e.g., available times).
// 8. **deleteDoctor Method**:
//    - Deletes a doctor from the system along with all appointments associated with that doctor.
//    - It first checks if the doctor exists. If not, it returns `-1`; otherwise, it deletes the doctor and their appointments.
//    - Instruction: Ensure the doctor and their appointments are deleted properly, with error handling for internal issues.

// 9. **validateDoctor Method**:
//    - Validates a doctor's login by checking if the email and password match an existing doctor record.
//    - It generates a token for the doctor if the login is successful, otherwise returns an error message.
//    - Instruction: Make sure to handle invalid login attempts and password mismatches properly with error responses.

