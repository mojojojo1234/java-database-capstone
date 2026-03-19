package com.project.back_end.services;


import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.DTO.DoctorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@org.springframework.stereotype.Service
public class SharedService {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public SharedService(TokenService tokenService, AdminRepository adminRepository, DoctorRepository doctorRepository, PatientRepository patientRepository, DoctorService doctorService, PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }


    public Map<String, Object> validateToken(String token, String role) {
        Map<String, Object> errors = new HashMap<>();

        try {
            // Validate the token using TokenService
            boolean isValid = tokenService.validateToken(token, role);

            if (!isValid) {
                errors.put("error", "Invalid or expired token");
            }

        } catch (Exception e) {
            errors.put("error", "Unauthorized access");
        }

        return errors;
    }

    public ResponseEntity<Map<String, String>> validateAdmin(Admin recievedAdmin) {
        Map<String, String> response = new HashMap<>();

        try {
            Optional<Admin> adminOptional = adminRepository.findByUsername(recievedAdmin.getUsername());


            // If admin not found
            if (adminOptional.isEmpty()) {
                response.put("message", "Invalid username or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Admin admin = adminOptional.get();

            // Check password
            if (!admin.getPassword().equals(recievedAdmin.getPassword())) {
                response.put("message", "Invalid username or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Generate JWT token
            String token = tokenService.generateToken(admin.getUsername());

            response.put("token", token);
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public Map<String, Object> filterDoctor(String name, String specialty, String time) {

        Map<String, Object> response = new HashMap<>();
        List<DoctorDTO> doctors = doctorService.filterDoctorDtos(name, specialty, time);
        response.put("doctors", doctors);

        return response;
    }

    public int validateAppointment(Appointment appointment) {
        Optional<Doctor> doctorOptional = doctorRepository.findById(appointment.getDoctor().getId());
        if(doctorOptional.isEmpty()) {
            return -1;
        }
        LocalDate date=appointment.getAppointmentTime().toLocalDate();

        List<String> availableSlots=doctorService.getDoctorAvailability(appointment.getDoctor().getId(), date);

        String requestedTime=appointment.getAppointmentTime().toLocalTime().toString();

        for(String slot: availableSlots) {
            if(slot.contains(requestedTime)) return 1;
        }
        return 0;
    }

    public boolean validatePatient(Patient patient) {
        Optional<Patient> existingPatient= Optional.ofNullable(patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone()));
        if(existingPatient.isPresent()) return false;
        return true;
    }

    public ResponseEntity<Map<String, String>> validatePatientLogin(String email, String password) {
        Map<String, String> response=new HashMap<>();
        try {
//            Optional<Patient> patientOptional= Optional.ofNullable(patientRepository.findByEmail(email));
            Optional<Patient> patientOptional=patientRepository.findByEmail(email);
            if(patientOptional.isEmpty()) {
                response.put("error", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            Patient patient=patientOptional.get();
            if(!patient.getPassword().equals(password)) {
                response.put("error", "Invalid password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            String token=tokenService.generateToken(email);
            response.put("token", token);
            return ResponseEntity.ok(response);
        }
        catch(Exception e) {
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String dname, String token) {

        Map<String, Object> response = new HashMap<>();

        if (!tokenService.validateToken(token, "patient")) {
            response.put("message", "Invalid token");
        }
        String email = tokenService.extractEmail(token);
        Optional<Patient> optionalPatient = patientRepository.findByEmail(email);
        if (optionalPatient.isEmpty()) {
            response.put("message", "Patient not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        Long patientId = optionalPatient.get().getId();
        if (condition != null && !condition.isEmpty() && dname != null && !dname.isEmpty()) {
            return patientService.filterByDoctorAndCondition(condition, dname, patientId);
        } else if (condition != null && !condition.isEmpty()) {
            return patientService.filterByCondition(condition, patientId);
        } else if (dname != null && !dname.isEmpty()) {
            return patientService.filterByDoctor(dname, patientId);
        } else {
            return patientService.getPatientAppointment(patientId, token);
        }
    }
// 1. **@Service Annotation**
// The @Service annotation marks this class as a service component in Spring. This allows Spring to automatically detect it through component scanning
// and manage its lifecycle, enabling it to be injected into controllers or other services using @Autowired or constructor injection.

// 2. **Constructor Injection for Dependencies**
// The constructor injects all required dependencies (TokenService, Repositories, and other Services). This approach promotes loose coupling, improves testability,
// and ensures that all required dependencies are provided at object creation time.
// 3. **validateToken Method**
// This method checks if the provided JWT token is valid for a specific user. It uses the TokenService to perform the validation.
// If the token is invalid or expired, it returns a 401 Unauthorized response with an appropriate error message. This ensures security by preventing
// unauthorized access to protected resources.

// 4. **validateAdmin Method**
// This method validates the login credentials for an admin user.
// - It first searches the admin repository using the provided username.
// - If an admin is found, it checks if the password matches.
// - If the password is correct, it generates and returns a JWT token (using the admin’s username) with a 200 OK status.
// - If the password is incorrect, it returns a 401 Unauthorized status with an error message.
// - If no admin is found, it also returns a 401 Unauthorized.
// - If any unexpected error occurs during the process, a 500 Internal Server Error response is returned.
// This method ensures that only valid admin users can access secured parts of the system.

// 5. **filterDoctor Method**
// This method provides filtering functionality for doctors based on name, specialty, and available time slots.
// - It supports various combinations of the three filters.
// - If none of the filters are provided, it returns all available doctors.
// This flexible filtering mechanism allows the frontend or consumers of the API to search and narrow down doctors based on user criteria.

// 6. **validateAppointment Method**
// This method validates if the requested appointment time for a doctor is available.
// - It first checks if the doctor exists in the repository.
// - Then, it retrieves the list of available time slots for the doctor on the specified date.
// - It compares the requested appointment time with the start times of these slots.
// - If a match is found, it returns 1 (valid appointment time).
// - If no matching time slot is found, it returns 0 (invalid).
// - If the doctor doesn’t exist, it returns -1.
// This logic prevents overlapping or invalid appointment bookings.

// 7. **validatePatient Method**
// This method checks whether a patient with the same email or phone number already exists in the system.
// - If a match is found, it returns false (indicating the patient is not valid for new registration).
// - If no match is found, it returns true.
// This helps enforce uniqueness constraints on patient records and prevent duplicate entries.

// 8. **validatePatientLogin Method**
// This method handles login validation for patient users.
// - It looks up the patient by email.
// - If found, it checks whether the provided password matches the stored one.
// - On successful validation, it generates a JWT token and returns it with a 200 OK status.
// - If the password is incorrect or the patient doesn't exist, it returns a 401 Unauthorized with a relevant error.
// - If an exception occurs, it returns a 500 Internal Server Error.
// This method ensures only legitimate patients can log in and access their data securely.

// 9. **filterPatient Method**
// This method filters a patient's appointment history based on condition and doctor name.
// - It extracts the email from the JWT token to identify the patient.
// - Depending on which filters (condition, doctor name) are provided, it delegates the filtering logic to PatientService.
// - If no filters are provided, it retrieves all appointments for the patient.
// This flexible method supports patient-specific querying and enhances user experience on the client side.
}
