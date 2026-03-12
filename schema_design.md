## MySQL Database Design

### Table: patients
- id: INT, Primary Key, Auto Increment
- first_name: VARCHAR(100), Not Null
- last_name: VARCHAR(100), Not Null
- email: VARCHAR(150), Unique, Not Null
- phone: VARCHAR(20), Unique, Not Null
- date_of_birth: DATE
- gender: ENUM('Male','Female','Other')
- address: TEXT
- created_at: DATETIME, Default CURRENT_TIMESTAMP

**Design Notes**

- Patient records should **not be hard deleted** because medical systems must preserve historical data.
- If deletion is required, implement a **soft delete strategy** (e.g., `is_active` or `deleted_at` column).
- Keeping patient information separate from appointments ensures **normalized relational design**.
- `created_at` helps track when the patient was registered in the system.

### Table: doctors
- id: INT, Primary Key, Auto Increment
- first_name: VARCHAR(100), Not Null
- last_name: VARCHAR(100), Not Null
- email: VARCHAR(150), Unique, Not Null
- phone: VARCHAR(20), Unique
- specialization: VARCHAR(150)
- license_number: VARCHAR(100), Unique
- consultation_fee: DECIMAL(10,2)
- created_at: DATETIME, Default CURRENT_TIMESTAMP

  **Design Notes**
- Doctors are not typically deleted from the system even if they stop practicing, because **past appointments must remain valid**.
- `consultation_fee` allows flexibility in pricing for different doctors.

  ### Table: admin
- id: INT, Primary Key, Auto Increment
- username: VARCHAR(100), Unique, Not Null
- password_hash: VARCHAR(255), Not Null
- email: VARCHAR(150), Unique
- role: ENUM('SUPER_ADMIN','STAFF')
- created_at: DATETIME, Default CURRENT_TIMESTAMP

  **Design Notes**
- Passwords must always be stored as **secure hashed values**, never plain text.
- `role` allows **role-based access control**, distinguishing super admins from staff users.

  ### Table: doctor_availability
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id)
- available_date: DATE, Not Null
- start_time: TIME, Not Null
- end_time: TIME, Not Null
- is_available: BOOLEAN, Default TRUE

  **Design Notes**
- `is_available` enables temporary disabling of slots without deleting records.
- `ON DELETE CASCADE` ensures that if a doctor is removed from the system, their availability slots are also removed.

  ### Table: appointments
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id)
- patient_id: INT, Foreign Key → patients(id)
- appointment_time: DATETIME, Not Null
- status: ENUM('SCHEDULED','COMPLETED','CANCELLED'), Default 'SCHEDULED'
- reason_for_visit: TEXT
- created_at: DATETIME, Default CURRENT_TIMESTAMP

  **Design Notes**
- `doctor_id` and `patient_id` enforce relational integrity through foreign keys.
- A **unique constraint on (doctor_id, appointment_time)** prevents overlapping appointments for the same doctor.
- `reason_for_visit` allows doctors to understand the patient's issue before consultation.

### Design Considerations & Real-World Decisions

#### 1. What happens if a patient is deleted? Should appointments also be deleted?

Appointments should **NOT be automatically deleted** when a patient record is removed.

Reasoning:

- Medical systems must preserve **historical treatment records**.

Recommended approach:

Instead of hard deleting a patient, implement a **soft delete strategy**, such as adding a column:

```
is_active BOOLEAN DEFAULT TRUE
```
Database constraint used:

```
FOREIGN KEY (patient_id) REFERENCES patients(id)
ON DELETE RESTRICT
```

This prevents accidental deletion of a patient if related appointment records exist.

---

### 2. Should a doctor be allowed to have overlapping appointments?

No, a doctor should **not be allowed to have overlapping appointments**.

Reasoning:

- A doctor cannot consult multiple patients at the same time.
- Overlapping bookings would lead to **scheduling conflicts and poor patient experience**.

Implementation:

A **unique constraint** is placed on the `appointments` table:

```
UNIQUE (doctor_id, appointment_time)
```

This ensures that the same doctor cannot have two appointments at the exact same time.

Example location in schema:

```
### Table: appointments
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id)
- patient_id: INT, Foreign Key → patients(id)
- appointment_time: DATETIME, Not Null
- status: ENUM('SCHEDULED','COMPLETED','CANCELLED')

Constraint:
UNIQUE (doctor_id, appointment_time)
```

#### 3. Should each doctor have their own available time slots?

Yes. Each doctor should manage their **own availability schedule**.

Reasoning:

- Different doctors may work on different days or shifts.
- Some doctors may only consult during specific hours.
- Availability may change due to leave, surgery schedules, or emergencies.

Implementation:

The system stores doctor availability in a dedicated table:

```
doctor_availability
```

This table allows:

- defining daily working hours
- blocking off unavailable periods
- dynamically managing schedules

#### 4. Should a patient's past appointment history be retained forever?

Yes. Patient appointment history should generally be **retained permanently**.

Reasoning:

- Healthcare providers must maintain records for **legal and regulatory compliance**.

Instead of deleting appointments, systems typically:

- mark them as `COMPLETED` or `CANCELLED`
- archive them if needed

#### 5. Should doctors be deletable from the system?

In most real healthcare systems, doctors are **not permanently deleted**.

Reasoning:

- Past appointments must remain valid.
- Historical medical records must remain traceable to the doctor who treated the patient.

Recommended approach:

- Use an `is_active` column for doctors
- Disable booking for inactive doctors
- Preserve historical appointment relationships

---

## MongoDB Collection Design

### Collection: prescriptions

```json
{
  "_id": "ObjectId",
  "appointmentId": 51,
  "doctorId": 7,
  "patientId": 14,
  "medications": [
    {
      "name": "Paracetamol",
      "dosage": "500mg",
      "frequency": "Every 6 hours",
      "duration": "5 days"
    }
  ],
  "doctorNotes": "Patient shows symptoms of viral fever.",
  "refillAllowed": true,
  "pharmacy": {
    "name": "City Care Pharmacy",
    "location": "Main Street"
  },
  "createdAt": "2026-03-12T10:00:00Z"
}
```

### Design Notes

- The prescription document is linked to a specific appointment using `appointmentId`, ensuring prescriptions remain tied to a medical consultation.
- `doctorId` and `patientId` are stored for quick lookup without requiring joins with relational databases.
- The `medications` field is an array to support multiple medicines within a single prescription.
- Nested objects such as `pharmacy` allow storing related data together in a flexible structure.
- MongoDB is chosen for prescriptions because prescription formats can vary between doctors and treatments.
- Document-based storage allows easy extension of the schema if additional fields (e.g., lab results, follow-up notes) are needed later.
- `createdAt` helps track when the prescription was issued and supports auditing or medical history review.
