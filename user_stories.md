## Admin User Stories

# User Story

**Title:**  
_As an admin, I want to log into the portal with my username and password, so that I can securely manage the platform._

**Acceptance Criteria:**
1. Assuming the administrator is on the login page, the entry of valid credentials will lead to granted access to the administrative dashboard.
2. If incorrect credentials are entered during the attempt to log in, an error message will be displayed.
3. Since the administrator is authenticated, the display of the dashboard should show the administrative options.

**Priority:** High  
**Story Points:** 3  

**Notes:**
- Only admin accounts should be able to access the admin dashboard.


# User Story

**Title:**  
_As an admin, I want to log out of the portal, so that system access remains secure._

**Acceptance Criteria:**
1. When the administrator is authenticated and the logout action is performed, the session should be ended.
2. Once the logout is performed and the login page is shown, re-authentication is needed for accessing the dashboard.
3. Once an active session exists and a logout is performed, the session token should be cleared.

**Priority:** High  
**Story Points:** 1  

**Notes:**
- The user should be redirected to the login page after logout.


# User Story

**Title:**  
_As an admin, I want to add doctors to the portal, so that patients can view and book appointments with them._

**Acceptance Criteria:**
1. Assuming the administrator is authenticated, upon entry and submission of the doctor details, a corresponding doctor profile should be created.
2. If the required information is incomplete, submitting the form by the administrator should trigger a validation error.
3. Provided that the doctor has been added successfully, when the doctor list is consulted, the newly added doctor should be displayed.

**Priority:** High  
**Story Points:** 5  

**Notes:**
- Doctor details include name, specialization, and contact information.


# User Story

**Title:**  
_As an admin, I want to delete a doctor’s profile, so that outdated or incorrect records can be removed from the system._

**Acceptance Criteria:**
1. When an administrator has chosen a doctor to be deleted and has initiated the deletion process, the system should display any upcoming appointments related to that doctor.
2. After existing appointments are shown, if an administrator reviews these appointments, the system should allow these appointments to be reassigned to another doctor or canceled.
3. After the administrator has confirmed the deletion process and has completed it, the doctor profile should be deleted from the system.
4. After appointments are reassigned to another doctor and the reassignment is complete, the doctor should be updated in the appointment information.
5. In the event that appointments are related to the deleted doctor profile, after the doctor profile has been deleted from the system, patients should be notified.

**Priority:** Medium  
**Story Points:** 5  

**Notes:**
- The admin should be able to review upcoming appointments before deletion.
- Patients should be notified if their appointment doctor changes.
- Historical appointment records should remain in the database for reporting purposes.


# User Story

**Title:**  
_As an admin, I want to run a stored procedure in MySQL CLI to get the number of appointments per month, so that I can track system usage._

**Acceptance Criteria:**
1. The stored procedure should return monthly appointment statistics when executed by an administrator who has database access.  
2. The results of a successfully executed stored procedure should show that there are appointments for a particular month.  
3. In case there are no appointments for a particular month, it should show a result of zero appointments.

**Priority:** Medium  
**Story Points:** 2  

**Notes:**
- This action may be performed directly from the MySQL CLI.



## Doctor User Stories

# User Story

**Title:**  
_As a doctor, I want to log into the portal, so that I can manage my appointments._

**Acceptance Criteria:**
1. When valid credentials are entered and a login attempt is made, access to the doctor dashboard is granted.
2. If invalid credentials are entered and a login attempt is made, an error message is displayed.
3. Once the doctor is logged in and the dashboard is loaded, appointment management should be visible.

**Priority:** High  
**Story Points:** 2  

**Notes:**
- Doctors should only see their own appointment data.


# User Story

**Title:**  
_As a doctor, I want to log out of the portal, so that my account remains secure._

**Acceptance Criteria:**
1. If the user is already authenticated, clicking on the logout option should terminate the session.
2. When the user logs out and is redirected to the login page, he/she should not be able to access the dashboard.
3. If an active session is available, clicking on logout should clear the token.

**Priority:** High  
**Story Points:** 1  

**Notes:**
- Logout should redirect to the login page.


# User Story

**Title:**  
_As a doctor, I want to view my appointment calendar, so that I can stay organized._

**Acceptance Criteria:**
1. Once the doctor is authenticated and the calendar page is accessed, appointments should be shown.
2. Given that appointments are scheduled, when the calendar is loaded, each appointment should show the patient's name and the time.
3. In the case where there are no appointments, when the calendar is loaded, it should be empty.

**Priority:** High  
**Story Points:** 3  

**Notes:**
- Calendar should show appointments in chronological order.


# User Story

**Title:**  
_As a doctor, I want to mark my unavailability, so that patients can only book available time slots._

**Acceptance Criteria:**
1. When the doctor chooses a date or time slot, when the doctor marks the time slot as unavailabile, then the time slot should be marked as unavailable.
2. When the patient tries to book an unavailable time slot, when the patient submits the booking request, then the request should be declined.
3. If the doctor tries to mark a slot as unavailable but he/she has appointments scheduled during that time, the appointment details should popup

**Priority:** Medium  
**Story Points:** 3  

**Notes:**
- Unavailable slots should still be visible but not selectable.
- The system should ensure that existing appointments are not disrupted when unavailability is marked.


# User Story

**Title:**  
_As a doctor, I want to update my profile with specialization and contact information, so that patients have accurate information about me._

**Acceptance Criteria:**
1. If the doctor navigates to "Update profile" section, the details in the respective fields should come up and the doctor should be able to edit their own details
2. If some sort of invalidation occours in data type, then validation errors should appear.
4. Once the update is successful, when the profile page reloads, then the new details should be visible.

**Priority:** Medium  
**Story Points:** 2  

**Notes:**
- Changes should immediately reflect in the doctor listing.



## Patient User Stories

# User Story

**Title:**  
_As a patient, I want to view a list of doctors without logging in, so that I can explore available options before registering._

**Acceptance Criteria:**
1. When a user/patient opens the website, list of doctors along with welcome should be displayed. 
2. Basic doctor details should pop up even without logging in for all the doctors
3. But, if the user tries to book an appointment, when they are not logged in, then the system should prompt them to sign in or register.

**Priority:** High  
**Story Points:** 3  

**Notes:**
- Only limited information should be shown to non-registered users.


# User Story

**Title:**  
_As a patient, I want to sign up with my email and password, so that I can create an account and book appointments._

**Acceptance Criteria:**
1. When a new user opts to sign up/create an accoutn, a form should pop up. Once filled, the user should have an account created to their username.
2. If the email is already registered, when signup is attempted, then an error message should appear.
3. Once signup is successful, when the process finishes, then the user should be redirected to login.

**Priority:** High  
**Story Points:** 3  

**Notes:**
- Passwords should meet basic security requirements.


# User Story

**Title:**  
_As a patient, I want to log into the portal, so that I can manage my bookings._

**Acceptance Criteria:**
1. To login, user should enter their username and password. If correct details are entered, their dashboard should be the next view they should see.
2. If incorrect credentials are entered, when login is attempted, then an error message should be displayed.
3. Given login is successful, when the dashboard loads, then booking options should be available.

**Priority:** High  
**Story Points:** 2  

**Notes:**
- Only registered users should access booking features.


# User Story

**Title:**  
_As a patient, I want to book an hour-long appointment with a doctor, so that I can consult them about my health._

**Acceptance Criteria:**
1. Once the user is logged in, when a doctor and time slot are selected, then the appointment should be booked.
2. But, if the time slot is already booked,  then the system should show an error message.
3. When the booking is successful, the appointments page should be viewed next, and the appointment should appear.

**Priority:** High  
**Story Points:** 5  

**Notes:**
- Each appointment slot should be one hour long.


# User Story

**Title:**  
_As a patient, I want to view my upcoming appointments, so that I can prepare for my consultations._

**Acceptance Criteria:**
1. When the appointments page is opened, then upcoming appointments should be displayed.
2. If no appointments exist, when the page loads, then a message indicating no appointments should appear.
3. If appointment/s exist, when the list is shown, then doctor name, date, and time should be visible.

**Priority:** Medium  
**Story Points:** 2  

**Notes:**
- Appointments should be sorted by date and time.
