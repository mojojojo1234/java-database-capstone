import {openModal} from "./components/modals.js";
import {getDoctors, filterDoctors, saveDoctor} from "./services/doctorServices.js";
import {createDoctorCard} from "./components/doctorCard.js";
window.openModal = openModal; // ← add this after the import
window.addEventListener("DOMContentLoaded", () => {
    // wait a bit for header to be injected
    setTimeout(() => {
        const addDocBtn = document.getElementById('addDocBtn');
        if (addDocBtn) {
            addDocBtn.addEventListener('click', () => {
                openModal('addDoctor');
            });
        }
        const searchBar = document.getElementById("searchBar");
        const filterTime = document.getElementById("filterTime");
        const filterSpecialty = document.getElementById("filterSpecialty");

        if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);
        if (filterTime) filterTime.addEventListener("change", filterDoctorsOnChange);
        if (filterSpecialty) filterSpecialty.addEventListener("change", filterDoctorsOnChange);

    }, 50); // 50ms delay to ensure header renders
});




window.addEventListener("DOMContentLoaded",loadDoctorCards);

async function loadDoctorCards() {
    const doctors=await getDoctors();
    renderDoctorCards(doctors);
}

function renderDoctorCards(doctors) {
    const contentDiv=document.getElementById("content");
    contentDiv.innerHTML="";

    if(!doctors || doctors.length===0) {
        contentDiv.innerHTML="<p>No doctors found </p>"
        return;
    }
    doctors.forEach(doctor=>{
        const card=createDoctorCard(doctor);
        contentDiv.appendChild(card);
    })
}
// document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
// document.getElementById("filterTime").addEventListener("change", filterDoctorsOnChange);
// document.getElementById("filterSpecialty").addEventListener("change", filterDoctorsOnChange);


async function filterDoctorsOnChange() {
    const search=document.getElementById("searchBar").value;
    const time=document.getElementById("filterTime").value;
    const specialty=document.getElementById("filterSpecialty").value;

    const response=await filterDoctors(search, time, specialty);
    renderDoctorCards(response.doctors || []);
}

async function adminAddDoctor(event) {
    event.preventDefault();

    const token = localStorage.getItem("token");

    if (!token) {
        alert("Not authenticated");
        return;
    }
    const availability = [];
    document.querySelectorAll('input, [name="availability"], :checked').forEach(checkbox => {
        availability.push(checkbox.value);
    })
    const doctorData = {
        name: document.getElementById("doctorName").value,
        specialty: document.getElementById("specialization").value,
        email: document.getElementById("doctorEmail").value,
        password: document.getElementById("doctorPassword").value,
        phone: document.getElementById("doctorPhone").value,
        availableTimes: Array.from(
            document.querySelectorAll('input[name="availability"]:checked')
        ).map(cb => cb.value)
    };
    console.log("doctorData:", JSON.stringify(doctorData));
    try {
        const result = await saveDoctor(doctorData, token);

        if (result.success) {
            alert("Doctor added successfully");
            document.getElementById('modal').style.display='none';
            loadDoctorCards();
        } else {
            alert("Failed to add doctor: " + result.message);
        }

    } catch (error) {
        alert("Error adding doctor");
        console.error(error);
    }
}
window.adminAddDoctor=adminAddDoctor;
/*

  This script handles the admin dashboard functionality for managing doctors:
  - Loads all doctor cards
  - Filters doctors by name, time, or specialty
  - Adds a new doctor via modal form


  Attach a click listener to the "Add Doctor" button
  When clicked, it opens a modal form using openModal('addDoctor')


  When the DOM is fully loaded:
    - Call loadDoctorCards() to fetch and display all doctors


  Function: loadDoctorCards
  Purpose: Fetch all doctors and display them as cards

    Call getDoctors() from the service layer
    Clear the current content area
    For each doctor returned:
    - Create a doctor card using createDoctorCard()
    - Append it to the content div

    Handle any fetch errors by logging them


  Attach 'input' and 'change' event listeners to the search bar and filter dropdowns
  On any input change, call filterDoctorsOnChange()


  Function: filterDoctorsOnChange
  Purpose: Filter doctors based on name, available time, and specialty

    Read values from the search bar and filters
    Normalize empty values to null
    Call filterDoctors(name, time, specialty) from the service

    If doctors are found:
    - Render them using createDoctorCard()
    If no doctors match the filter:
    - Show a message: "No doctors found with the given filters."

    Catch and display any errors with an alert


  Function: renderDoctorCards
  Purpose: A helper function to render a list of doctors passed to it

    Clear the content area
    Loop through the doctors and append each card to the content area


  Function: adminAddDoctor
  Purpose: Collect form data and add a new doctor to the system

    Collect input values from the modal form
    - Includes name, email, phone, password, specialty, and available times

    Retrieve the authentication token from localStorage
    - If no token is found, show an alert and stop execution

    Build a doctor object with the form values

    Call saveDoctor(doctor, token) from the service

    If save is successful:
    - Show a success message
    - Close the modal and reload the page

    If saving fails, show an error message
*/
