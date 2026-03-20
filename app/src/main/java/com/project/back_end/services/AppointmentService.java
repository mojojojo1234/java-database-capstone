package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import jakarta.transaction.Transactional;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final SharedService service;

    public AppointmentService(AppointmentRepository appointmentRepository, TokenService tokenService, PatientRepository patientRepository, DoctorRepository doctorRepository,SharedService service) {
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.service=service;
    }
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        }
        catch(Exception e){
            return 0;
        }
    }


    public ResponseEntity<Map<String,String>> updateAppointment(Appointment appointment) {
        Map<String,String> response = new HashMap<>();
        if(appointmentRepository.findById(appointment.getId()).isEmpty()) {
            response.put("message", "Appointment not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        // Replace validateAppointment with direct slot check
        boolean alreadyBooked = appointmentRepository.existsByDoctorIdAndAppointmentTime(
                appointment.getDoctor().getId(),
                appointment.getAppointmentTime()
        );
        if(alreadyBooked) {
            response.put("message", "This time slot is already booked");
            return ResponseEntity.badRequest().body(response);
        }
        appointmentRepository.save(appointment);
        response.put("message", "Appointment updated successfully");
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, String>> cancelAppointment(Long id, String token) {
        Map<String, String> response=new HashMap<>();
       Optional<Appointment> optionalAppointment= appointmentRepository.findById(id);
       if(optionalAppointment.isEmpty()){
           response.put("message","Appointment not found");
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
       }
       int i=service.validateAppointment(appointmentRepository.getReferenceById(id));
       if(i!=1) {
           response.put("error","Error occurred");
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
       }
       appointmentRepository.deleteById(id);
       response.put("message","Appointment Deleted");
       return ResponseEntity.ok(response);

    }

    @Transactional
    public Map<String, Object> getAppointment(String name, LocalDate date, String token) {
        Map<String, Object> response=new HashMap<>();
        if(!tokenService.validateToken(token, "doctor")) {
            response.put("message","Invalid token");
            return response;
        }
        String email=tokenService.extractEmail(token);
        System.out.println("DEBUG - doctor email: " + email);
        Optional<Doctor> optionalDoctor=doctorRepository.findByEmail(email);
        System.out.println("DEBUG - doctor found: " + optionalDoctor.isPresent());

        if(optionalDoctor.isEmpty()){
            response.put("message","Doctor");
            return response;
        }
        Long doctorId = optionalDoctor.get().getId();
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        if(optionalDoctor.isPresent()) {
            System.out.println("DEBUG - doctor id: " + optionalDoctor.get().getId());
            System.out.println("DEBUG - start: " + start + " end: " + end);
            System.out.println("DEBUG - appointments count: " +
                    appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                            optionalDoctor.get().getId(), start, end).size());
        }
        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetweenWithDetails(doctorId, start, end);
        System.out.println("DEBUG - JOIN FETCH result size: " + appointments.size());
        if(name != null && !name.isEmpty()) {
            appointments = appointments.stream()
                    .filter(a -> a.getPatient().getName().equalsIgnoreCase(name))
                    .toList();
        }
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(a -> {
                    System.out.println("DEBUG - mapping appointment id: " + a.getId());
                    System.out.println("DEBUG - doctor: " + (a.getDoctor() != null ? a.getDoctor().getId() : "NULL"));
                    System.out.println("DEBUG - patient: " + (a.getPatient() != null ? a.getPatient().getId() : "NULL"));
                    try {
                        return new AppointmentDTO(
                                a.getId(),
                                a.getDoctor().getId(),
                                a.getDoctor().getName(),
                                a.getPatient().getId(),
                                a.getPatient().getName(),
                                a.getPatient().getEmail(),
                                a.getPatient().getPhone(),
                                a.getPatient().getAddress(),
                                a.getAppointmentTime(),
                                a.getStatus()
                        );
                    } catch(Exception e) {
                        e.printStackTrace();
                        System.out.println("DEBUG - exception type: " + e.getClass().getName());
                        System.out.println("DEBUG - exception message: " + e.getMessage());
                        System.out.println("DEBUG - appointment id: " + a.getId());
                        return null;
                    }
                })
                .filter(a -> a != null)
                .toList();

        System.out.println("DEBUG - DTO list size: " + appointmentDTOs.size());
        response.put("appointments", appointmentDTOs);
        return response;
    }
    public boolean isSlotTaken(Long doctorId, LocalDateTime appointmentTime) {
        return appointmentRepository.existsByDoctorIdAndAppointmentTime(doctorId, appointmentTime);
    }

    @Transactional
    public void completeAppointment(Long id) {
        appointmentRepository.updateStatus(id);
    }
//    public ResponseEntity<Map<String, String>> completeAppointment(Long id) {
//        Map<String, String> response = new HashMap<>();
//        Optional<Appointment> optional = appointmentRepository.findById(id);
//        if(optional.isEmpty()) {
//            response.put("message", "Appointment not found");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//        Appointment appointment = optional.get();
//        appointment.setStatus(1);
//        appointmentRepository.save(appointment);
//        response.put("message", "Appointment completed");
//        return ResponseEntity.ok(response);
//    }
}


// 8. **Change Status Method**:
//    - This method updates the status of an appointment by changing its value in the database.
//    - It should be annotated with `@Transactional` to ensure the operation is executed in a single transaction.
//    - Instruction: Add `@Transactional` before this method to ensure atomicity when updating appointment status.


    // 1. **Add @Service Annotation**:
//    - To indicate that this class is a service layer class for handling business logic.
//    - The `@Service` annotation should be added before the class declaration to mark it as a Spring service component.
//    - Instruction: Add `@Service` above the class definition.

// 2. **Constructor Injection for Dependencies**:
//    - The `AppointmentService` class requires several dependencies like `AppointmentRepository`, `Service`, `TokenService`, `PatientRepository`, and `DoctorRepository`.
//    - These dependencies should be injected through the constructor.
//    - Instruction: Ensure constructor injection is used for proper dependency management in Spring.

// 3. **Add @Transactional Annotation for Methods that Modify Database**:
//    - The methods that modify or update the database should be annotated with `@Transactional` to ensure atomicity and consistency of the operations.
//    - Instruction: Add the `@Transactional` annotation above methods that interact with the database, especially those modifying data.

// 4. **Book Appointment Method**:
//    - Responsible for saving the new appointment to the database.
//    - If the save operation fails, it returns `0`; otherwise, it returns `1`.
//    - Instruction: Ensure that the method handles any exceptions and returns an appropriate result code.
// 5. **Update Appointment Method**:
//    - This method is used to update an existing appointment based on its ID.
//    - It validates whether the patient ID matches, checks if the appointment is available for updating, and ensures that the doctor is available at the specified time.
//    - If the update is successful, it saves the appointment; otherwise, it returns an appropriate error message.
//    - Instruction: Ensure proper validation and error handling is included for appointment updates.

// 6. **Cancel Appointment Method**:
//    - This method cancels an appointment by deleting it from the database.
//    - It ensures the patient who owns the appointment is trying to cancel it and handles possible errors.
//    - Instruction: Make sure that the method checks for the patient ID match before deleting the appointment.
// 7. **Get Appointments Method**:
//    - This method retrieves a list of appointments for a specific doctor on a particular day, optionally filtered by the patient's name.
//    - It uses `@Transactional` to ensure that database operations are consistent and handled in a single transaction.
//    - Instruction: Ensure the correct use of transaction boundaries, especially when querying the database for appointments.

