import { getAllAppointments } from "../js/services/appointmentRecordService.js";
import { createPatientRow } from "../js/components/patientRows.js";

window.addEventListener("DOMContentLoaded", () => {
    // DOM elements
    const tableBody = document.getElementById("patientTableBody");
    const searchBar = document.getElementById("searchBar");
    const todayButton = document.getElementById("todayButton");
    const datePicker = document.getElementById("datePicker");

    // Variables
    let selectedDate = new Date().toISOString().split("T")[0];
    let patientName = null;
    const token = localStorage.getItem("token");

    if (datePicker) datePicker.value = selectedDate;

    async function loadAppointments() {
        if (!token) {
            alert("Not authenticated");
            return;
        }

        try {
            const url = patientName
                ? `/appointments/${selectedDate}/${patientName}/${token}`
                : `/appointments/${selectedDate}/${token}`;

            const response = await fetch(url, { method: "GET" });

            // Clear table
            if (tableBody) tableBody.innerHTML = "";

            if (!response.ok) {
                const err = await response.json();
                const tr = document.createElement("tr");
                tr.innerHTML = `<td colspan="5" class="noPatientRecord">${err.message || "Error loading appointments"}</td>`;
                tableBody.appendChild(tr);
                return;
            }

            const data = await response.json();
            const appointments = data.appointments || [];

            if (appointments.length === 0) {
                const tr = document.createElement("tr");
                tr.innerHTML = `<td colspan="5" class="noPatientRecord">No appointments found for ${selectedDate}</td>`;
                tableBody.appendChild(tr);
                return;
            }

            appointments.forEach(app => {
                const patient = {
                    id: app.patientId,
                    name: app.patientName,
                    phone: app.patientPhone,
                    email: app.patientEmail
                };
                const row = createPatientRow(patient, app.id, app.doctorId); // pass IDs
                tableBody.appendChild(row);
            });

        } catch (err) {
            console.error("Error loading appointments:", err);
            if (tableBody) {
                const tr = document.createElement("tr");
                tr.innerHTML = `<td colspan="5" class="noPatientRecord">Error loading appointments. Try again later.</td>`;
                tableBody.appendChild(tr);
            }
        }
    }

    // Search bar filter
    if (searchBar) {
        searchBar.addEventListener("input", () => {
            const value = searchBar.value.trim();
            patientName = value !== "" ? value : null;
            loadAppointments();
        });
    }

    // Today button
    if (todayButton && datePicker) {
        todayButton.addEventListener("click", () => {
            selectedDate = new Date().toISOString().split("T")[0];
            datePicker.value = selectedDate;
            loadAppointments();
        });
    }

    // Date picker change
    if (datePicker) {
        datePicker.addEventListener("change", () => {
            console.log("Date picker changed to:", datePicker.value);
            selectedDate = datePicker.value;
            loadAppointments();
        });
    }

    // Initial load
    loadAppointments();
});



/*
  Import getAllAppointments to fetch appointments from the backend
  Import createPatientRow to generate a table row for each patient appointment


  Get the table body where patient rows will be added
  Initialize selectedDate with today's date in 'YYYY-MM-DD' format
  Get the saved token from localStorage (used for authenticated API calls)
  Initialize patientName to null (used for filtering by name)


  Add an 'input' event listener to the search bar
  On each keystroke:
    - Trim and check the input value
    - If not empty, use it as the patientName for filtering
    - Else, reset patientName to "null" (as expected by backend)
    - Reload the appointments list with the updated filter


  Add a click listener to the "Today" button
  When clicked:
    - Set selectedDate to today's date
    - Update the date picker UI to match
    - Reload the appointments for today


  Add a change event listener to the date picker
  When the date changes:
    - Update selectedDate with the new value
    - Reload the appointments for that specific date


  Function: loadAppointments
  Purpose: Fetch and display appointments based on selected date and optional patient name

  Step 1: Call getAllAppointments with selectedDate, patientName, and token
  Step 2: Clear the table body content before rendering new rows

  Step 3: If no appointments are returned:
    - Display a message row: "No Appointments found for today."

  Step 4: If appointments exist:
    - Loop through each appointment and construct a 'patient' object with id, name, phone, and email
    - Call createPatientRow to generate a table row for the appointment
    - Append each row to the table body

  Step 5: Catch and handle any errors during fetch:
    - Show a message row: "Error loading appointments. Try again later."


  When the page is fully loaded (DOMContentLoaded):
    - Call renderContent() (assumes it sets up the UI layout)
    - Call loadAppointments() to display today's appointments by default
*/
