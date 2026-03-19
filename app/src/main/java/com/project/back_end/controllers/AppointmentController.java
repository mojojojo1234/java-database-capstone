package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.SharedService;
import com.project.back_end.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    @Autowired
    AppointmentService appointmentService;
    @Autowired
    TokenService tokenService;

    @Autowired
    SharedService service;

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<?> getAppointments(@PathVariable String date, @PathVariable(required = false) String patientName, @PathVariable String token) {
        Map<String, String> response=new HashMap<>();
        boolean valid=tokenService.validateToken(token, "doctor");
        if(!valid) {
            response.put("message","Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        Map<String, Object> appointments = appointmentService.getAppointment(patientName, LocalDate.parse(date), token);

        return ResponseEntity.ok(appointments);
    }
    @PostMapping("/{token}")
    public ResponseEntity<?> bookAppointment(@RequestBody Appointment appointment, @PathVariable String token) {
        Map<String, String> response = new HashMap<>();
        boolean valid = tokenService.validateToken(token, "patient");
        if (!valid) {
            response.put("message", "Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            response.put("message", "Doctor not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Check slot not already booked — direct DB query, no lazy loading
        boolean alreadyBooked = appointmentService.isSlotTaken(
                appointment.getDoctor().getId(),
                appointment.getAppointmentTime()
        );
        if (alreadyBooked) {
            response.put("message", "This time slot is already booked");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
//        int i = service.validateAppointment(appointment);
//        if (i == -1) {
//            response.put("message", "Doctor not found");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//        if (i == 0) {
//            response.put("message", "Selected time slot is not available");
//            return ResponseEntity.badRequest().body(response);
//        }
        int result = appointmentService.bookAppointment(appointment);
        if (result == 1) {
            response.put("message", "Appointment booked successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Failed to book appointment");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{token}")
    public ResponseEntity<?> updateAppointment(@RequestBody Appointment appointment, @PathVariable String token) {
        Map<String, String> response = new HashMap<>();
        boolean valid = tokenService.validateToken(token, "patient");
        if (!valid) {
            response.put("message", "Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return appointmentService.updateAppointment(appointment);
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id, @PathVariable String token) {
        Map<String, String> response = new HashMap<>();
        boolean valid = tokenService.validateToken(token, "patient");
        if (!valid) {
            response.put("message", "Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return appointmentService.cancelAppointment(id, token);
    }




// 3. Define the `getAppointments` Method:
//    - Handles HTTP GET requests to fetch appointments based on date and patient name.
//    - Takes the appointment date, patient name, and token as path variables.
//    - First validates the token for role `"doctor"` using the `Service`.
//    - If the token is valid, returns appointments for the given patient on the specified date.
//    - If the token is invalid or expired, responds with the appropriate message and status code.


// 4. Define the `bookAppointment` Method:
//    - Handles HTTP POST requests to create a new appointment.
//    - Accepts a validated `Appointment` object in the request body and a token as a path variable.
//    - Validates the token for the `"patient"` role.
//    - Uses service logic to validate the appointment data (e.g., check for doctor availability and time conflicts).
//    - Returns success if booked, or appropriate error messages if the doctor ID is invalid or the slot is already taken.


// 5. Define the `updateAppointment` Method:
//    - Handles HTTP PUT requests to modify an existing appointment.
//    - Accepts a validated `Appointment` object and a token as input.
//    - Validates the token for `"patient"` role.
//    - Delegates the update logic to the `AppointmentService`.
//    - Returns an appropriate success or failure response based on the update result.


// 6. Define the `cancelAppointment` Method:
//    - Handles HTTP DELETE requests to cancel a specific appointment.
//    - Accepts the appointment ID and a token as path variables.
//    - Validates the token for `"patient"` role to ensure the user is authorized to cancel the appointment.
//    - Calls `AppointmentService` to handle the cancellation process and returns the result.


// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to define it as a REST API controller.
//    - Use `@RequestMapping("/appointments")` to set a base path for all appointment-related endpoints.
//    - This centralizes all routes that deal with booking, updating, retrieving, and canceling appointments.

// 2. Autowire Dependencies:
//    - Inject `AppointmentService` for handling the business logic specific to appointments.
//    - Inject the general `Service` class, which provides shared functionality like token validation and appointment checks.
}
