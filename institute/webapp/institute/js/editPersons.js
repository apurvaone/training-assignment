// Include jQuery library
src = "https://code.jquery.com/jquery-3.6.3.min.js";

// Wait for the DOM to be fully loaded before executing the code
document.addEventListener('DOMContentLoaded', function () {

  // Attach click event listeners to various buttons
  const saveButton = document.getElementById('saveButton');
  if (saveButton) {
    saveButton.addEventListener('click', saveBasicDetails);
  }

  const saveEmail = document.getElementById('saveEmailButton');
  if (saveEmail) {
    saveEmail.addEventListener('click', saveEmailChanges);
  }

  const savePhone = document.getElementById('savePhoneButton');
  if (savePhone) {
    savePhone.addEventListener('click', savePhoneChanges);
  }

  const saveAddress = document.getElementById('saveAddressButton');
  if (saveAddress) {
    saveAddress.addEventListener('click', saveAddressChanges);
  }

  const deleteEmail = document.getElementById('deleteEmailButton');
  if (deleteEmail) {
    deleteEmail.addEventListener('click', deleteCurrentEmail);
  }

  const deleteAlternateEmail = document.getElementById('deleteAlternateEmailButton');
  if (deleteAlternateEmail) {
    deleteAlternateEmail.addEventListener('click', deleteAlternateEmailAddress);
  }

  const deleteAddress = document.getElementById('deleteAddressButton');
  if (deleteAddress) {
    deleteAddress.addEventListener('click', deleteCurrentAddress);
  }

  const deletePhone = document.getElementById('deletePhoneButton');
  if (deletePhone) {
    deletePhone.addEventListener('click', deleteCurrentPhone);
  }

  const addPerson = document.getElementById('addPersonButton');
  if (addPerson) {
    addPerson.addEventListener('click', addNewStudent);
  }

  const saveAlternateEmail = document.getElementById('saveAlternateEmailButton');
  if (saveAlternateEmail) {
    saveAlternateEmail.addEventListener('click', addAlternateEmail);
  }

  // Attach click event listeners to delete icons
  const deleteIcons = document.querySelectorAll('[id^="deleteIcon-"]');
  deleteIcons.forEach((icon) => {
    icon.addEventListener("click", function (event) {
      const partyId = this.id.split("-")[1];
      deleteStudent(partyId);
    });
  });

  // Disable delete buttons if the corresponding data is not available
  var primaryEmailText = document.getElementById("currentEmail").textContent.trim();
  if (primaryEmailText && primaryEmailText === "NA") {
    document.getElementById("deletePrimaryEmail").disabled = true;
  }

  var addressText = document.getElementById("currentAddress").textContent.trim();
  if (addressText && addressText === "NA") {
    document.getElementById("deleteAddressIcon").disabled = true;
  }

  var phoneText = document.getElementById("currentPhone").textContent.trim();
  if (phoneText && phoneText === "NA") {
    document.getElementById("deletePhoneIcon").disabled = true;
  }

  var alternateEmailText = document.getElementById("currentAlternateEmail").textContent.trim();
  if (alternateEmailText && alternateEmailText === "NA") {
    document.getElementById("deleteAlternateEmailIcon").disabled = true;
  }
});

// Function to handle saving address changes
function saveAddressChanges() {
  // Retrieve data from input fields
  const address1 = $('#currentAddress1').val();
  const address2 = $('#currentAddress2').val();
  const city = $('#currentCity').val();
  const contactMech = $('#saveAddressButton').attr('address-contact-mech');
  const currentPartyId = $('#saveAddressButton').attr('party-id');

  // Prepare data object
  const data = {
    address1: address1,
    address2: address2,
    city: city,
    partyId: currentPartyId,
    postalCode: '452020'
  };

  // Include contactMechId if it's not null
  if (contactMech !== 'null') {
    data.contactMechId = contactMech;
  }

  // Log data for debugging
  console.log('contactMechId' + contactMech);
  console.log('address' + address1 + address2 + city);
  console.log('partyId' + currentPartyId);

  // Make an AJAX request to update the address data
  $.ajax({
    url: 'https://localhost:8443/institute/control/updateAddress',
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(data),
    success: function (response) {
      if (response) {
        console.log('Address Data Updated successfully: ' + address1 + ',' + address2 + ',' + city);

        const dialog = $('#editAddressDialog');
        dialog.hide();

        $('body').removeClass('modal-open');
        $('.modal-backdrop').remove();

        // Reload the page after successful update
        location.reload();
      }
    },
    error: function (error) {
      console.error('Error:', error);
    }
  });
}

// Function to add alternate email
function addAlternateEmail() {
  // Retrieve email and other data from input fields
  const email = $('#currentAlternateEmailAddress').val();
  const contactMech = $('#saveAlternateEmailButton').attr('email-contact-mech');
  const currentPartyId = $('#saveAlternateEmailButton').attr('party-id');

  // If contactMech is not null, update existing email
  if (contactMech !== 'null') {
    const data = {
      contactMechId: contactMech,
      emailAddress: email,
      partyId: currentPartyId,
      contactMechPurposeTypeId: 'OTHER_EMAIL'
    };

    // Log data for debugging
    console.log('contactMechId' + contactMech);
    console.log('infoString' + email);

    // Make an AJAX request to update the email data
    $.ajax({
      url: 'https://localhost:8443/institute/control/updateEmailData',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(data),
      success: function (response) {
        if (response) {
          console.log('Email Data updated successfully!');

          // Update the displayed email
          $('#currentAddress').text(email);
          const dialog = $('#editAlternateEmailDialog');
          dialog.hide();

          $('body').removeClass('modal-open');
          $('.modal-backdrop').remove();

          // Reload the page after successful update
          location.reload();
        }
      },
      error: function (error) {
        console.error('Error:', error);
      }
    });
  } else {
    // If contactMech is null, add a new email
    const data = {
      emailAddress: email,
      partyId: currentPartyId,
      contactMechPurposeTypeId: 'OTHER_EMAIL'
    };

    // Make an AJAX request to add the email data
    $.ajax({
      url: 'https://localhost:8443/institute/control/updateEmailData',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(data),
      success: function (response) {
        if (response) {
          console.log('Email Data added successfully!');

          const dialog = $('#editAlternateEmailDialog');
          dialog.hide();

          $('body').removeClass('modal-open');
          $('.modal-backdrop').remove();

          // Reload the page after successful update
          location.reload();
        }
      },
      error: function (error) {
        console.error('Error:', error);
      }
    });
  }
}

// Function to delete the primary email address
async function deleteCurrentEmail() {
  // Retrieve contactMech and partyId
  const contactMech = document.getElementById('deleteEmailButton').getAttribute('email-contact-mech');
  const currentPartyId = document.getElementById('deleteEmailButton').getAttribute('party-id');

  // Prepare data object
  const data = {
    contactMechId: contactMech,
    partyId: currentPartyId,
    contactMechPurposeTypeId: "OTHER_EMAIL"
  };

  // Make a fetch request to delete the email
  fetch('https://localhost:8443/institute/control/deleteCurrentEmail', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  }).then((response) => {
    if (response.ok) {
      console.log('Email Deleted successfully!');

      // Hide the delete email dialog
      const dialog = document.getElementById('deleteEmailDialog');
      dialog.style.display = 'none';

      // Update the displayed email as "NA"
      document.getElementById("currentEmail").innerText = "NA";

      // Remove modal-open class and backdrop element
      document.body.classList.remove('modal-open');
      const modalBackdrop = document.querySelector('.modal-backdrop');
      if (modalBackdrop) {
        modalBackdrop.remove();
      }

      // Reload the page after successful delete
      location.reload();
    }
  }).catch((error) => {
    console.error('Error:', error);
  });
}

// Function to delete alternate email address
async function deleteAlternateEmailAddress() {
  console.log("in alternate world");

  // Retrieve contactMech and partyId
  const contactMech = document.getElementById('deleteAlternateEmailButton').getAttribute('email-contact-mech');
  const currentPartyId = document.getElementById('deleteAlternateEmailButton').getAttribute('party-id');

  // Prepare data object
  const data = {
    contactMechId: contactMech,
    partyId: currentPartyId
  };

  // Make a fetch request to delete the alternate email
  fetch('https://localhost:8443/institute/control/deleteCurrentEmail', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  }).then((response) => {
    if (response.ok) {
      console.log('Email Deleted successfully!');

      // Hide the delete alternate email dialog
      const dialog = document.getElementById('deleteAlternateEmailDialog');
      dialog.style.display = 'none';

      // Update the displayed alternate email as "NA"
      document.getElementById("currentAlternateEmail").innerText = "NA";

      // Remove modal-open class and backdrop element
      document.body.classList.remove('modal-open');
      const modalBackdrop = document.querySelector('.modal-backdrop');
      if (modalBackdrop) {
        modalBackdrop.remove();
      }

      // Reload the page after successful delete
      location.reload();
    }
  }).catch((error) => {
    console.error('Error:', error);
  });
}

// Function to delete current address
function deleteCurrentAddress() {
  // Retrieve contactMech and partyId
  const contactMech = $('#deleteAddressButton').attr('address-contact-mech');
  const currentPartyId = $('#deleteAddressButton').attr('party-id');

  // Prepare data object
  const data = {
    contactMechId: contactMech,
    partyId: currentPartyId
  };

  // Make an AJAX request to delete the address
  $.ajax({
    url: 'https://localhost:8443/institute/control/deleteCurrentAddress',
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(data),
    success: function (response) {
      if (response) {
        console.log('Address Deleted successfully!');

        const dialog = $('#deleteAddressDialog');
        dialog.hide();

        // Update the displayed address as "NA"
        $('#currentAddress').text('NA');

        $('body').removeClass('modal-open');
        const modalBackdrop = $('.modal-backdrop');
        if (modalBackdrop) {
          modalBackdrop.remove();
        }

        // Reload the page after successful delete
        location.reload();
      }
    },
    error: function (error) {
      console.error('Error:', error);
    }
  });
}

// Function to delete current phone number
function deleteCurrentPhone() {
  // Retrieve contactMech and partyId
  const contactMech = $('#deletePhoneButton').attr('phone-contact-mech');
  const currentPartyId = $('#deletePhoneButton').attr('party-id');

  // Prepare data object
  const data = {
    contactMechId: contactMech,
    partyId: currentPartyId
  };

  // Make an AJAX request to delete the phone number
  $.ajax({
    url: 'https://localhost:8443/institute/control/deleteCurrentPhone',
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(data),
    success: function (response) {
      if (response) {
        console.log('Phone Deleted successfully!');

        const dialog = $('#deletePhoneDialog');
        dialog.hide();

        $('body').removeClass('modal-open');
        $('.modal-backdrop').remove();

        // Reload the page after successful delete
        location.reload();
      } else {
        console.error('Failed to delete phone.');
      }
    },
    error: function (error) {
      console.error('Error:', error);
    }
  });
}

// Function to save basic student details
function saveBasicDetails() {
  // Retrieve data from input fields
  const firstName = $('#firstName').val();
  const lastName = $('#lastName').val();
  const gender = $('#gender').val();
  const partyId = $('#saveButton').attr('data-value');
  const birthDate = $('#birthDate').val();

  // Prepare data object
  const data = {
    partyId: partyId,
    firstName: firstName,
    lastName: lastName,
    gender: gender,
    birthDate: birthDate
  };

  // Make an AJAX request to update the basic student details
  $.ajax({
    url: 'https://localhost:8443/institute/control/updatePersonData',
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(data),
    success: function (response) {
      if (response) {
        console.log('Data updated successfully!');
        $('[data-bs-dismiss="modal"]').click();
        location.reload();
      } else {
        console.error('Failed to update data.');
      }
    },
    error: function (error) {
      console.error('Error:', error);
    }
  });
}

// Function to add a new student
function addNewStudent() {
  // Retrieve data from input fields
  const firstName = $('#currentFirstName').val();
  const lastName = $('#currentLastName').val();
  const gender = $('#currentGender').val();
  const birthDate = $('#currentDOB').val();

  // Prepare data object
  const data = {
    firstName: firstName,
    lastName: lastName,
    gender: gender,
    birthDate: birthDate,
    roleTypeId: 'CUSTOMER'
  };

  // Make an AJAX request to create a new student
  $.ajax({
    url: 'https://localhost:8443/institute/control/createPartyAndPerson',
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(data),
    success: function (response) {
      if (response) {
        console.log('Data updated successfully!');
        $('[data-bs-dismiss="modal"]').click();
        location.reload();
      } else {
        console.error('Failed to update data.');
      }
    },
    error: function (error) {
      console.error('Error:', error);
    }
  });
}

// Function to delete a student by partyId
function deleteStudent(partyId) {
  // Prepare data object
  const data = {
    partyId: partyId,
  };

  // Make an AJAX request to delete the student
  $.ajax({
    url: 'https://localhost:8443/institute/control/deletePartyAndPerson',
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(data),
    success: function (response) {
      if (response) {
        console.log('Data updated successfully!');
        location.reload();
      } else {
        console.error('Failed to update data.');
      }
    },
    error: function (xhr, status, error) {
      console.error('Error:', error);
    }
  });
}

// Function to save phone number changes
function savePhoneChanges() {
  // Retrieve data from input fields
  const phone = $('#currentPhoneNumber').val();
  const contactMech = $('#savePhoneButton').attr('phone-contact-mech');
  const currentPartyId = $('#savePhoneButton').attr('party-id');

  // Prepare data object
  const data = {
    contactNumber: phone,
    partyId: currentPartyId,
    countryCodeCode: "+91"
  };

  // Include contactMechId if it's not null
  if (contactMech !== 'null') {
    data.contactMechId = contactMech;
  }

  // Make an AJAX request to update the phone number data
  $.ajax({
    url: 'https://localhost:8443/institute/control/updateTelecomNumber',
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(data),
    success: function (response) {
      if (response) {
        console.log('Telecom Number Data Updated successfully!');

        const dialog = $('#editPhoneDialog');
        dialog.hide();

        $('body').removeClass('modal-open');
        $('.modal-backdrop').remove();

        // Reload the page after successful update
        location.reload();
      }
    },
    error: function (error) {
      console.error('Error:', error);
    }
  });
}

// Function to save email changes
function saveEmailChanges() {
  // Retrieve data from input fields
  const email = $('#currentEmailAddress').val();
  const contactMech = $('#saveEmailButton').attr('email-contact-mech');
  const currentPartyId = $('#saveEmailButton').attr('party-id');

  // If contactMech is not null, update existing email
  if (contactMech !== 'null') {
    const data = {
      contactMechId: contactMech,
      emailAddress: email,
      partyId: currentPartyId,
      contactMechPurposeTypeId: 'PRIMARY_EMAIL'
    };

    // Log data for debugging
    console.log('contactMechId' + contactMech);
    console.log('infoString' + email);

    // Make an AJAX request to update the email data
    $.ajax({
      url: 'https://localhost:8443/institute/control/updateEmailData',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(data),
      success: function (response) {
        if (response) {
          console.log('Email Data updated successfully!');

          const dialog = $('#editEmailDialog');
          dialog.hide();

          $('body').removeClass('modal-open');
          $('.modal-backdrop').remove();

          // Reload the email data after successful update
          $('#currentEmail').load("https://localhost:8443/institute/control/StudentDetail/?party_id=" + currentPartyId + ' #currentEmail');
        }
      },
      error: function (error) {
        console.error('Error:', error);
      }
    });
  } else {
    // If contactMech is null, add a new email
    const data = {
      emailAddress: email,
      partyId: currentPartyId,
      contactMechPurposeTypeId: 'PRIMARY_EMAIL'
    };

    // Make an AJAX request to add the email data
    $.ajax({
      url: 'https://localhost:8443/institute/control/updateEmailData',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(data),
      success: function (response) {
        if (response) {
          console.log('Email Data added successfully!');

          const dialog = $('#editEmailDialog');
          dialog.hide();

          $('body').removeClass('modal-open');
          $('.modal-backdrop').remove();

          // Reload the email data after successful update
          $('#currentEmail').load("https://localhost:8443/institute/control/StudentDetail/?party_id=" + currentPartyId + ' #currentEmail');
        }
      },
      error: function (error) {
        console.error('Error:', error);
      }
    });
  }
}
