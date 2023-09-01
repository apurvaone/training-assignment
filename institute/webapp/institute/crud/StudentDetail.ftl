<div class="container">
   <div class="container">
      <div class="container mt-4">
         <div class="heading">
            <span>Basic Details
            <button type="button "class="btn btn-primary btn-sm float-right" data-bs-toggle="modal" data-bs-target="#editBasicDetailDialog" id="editButton">Edit</button>
            </span>
         </div>
         <div class="row">
            <div class="col-md-12">
               <div class="card custom-box">
                  <div class="card-body">
                     <h2 class="mb-0" id="currentFullname" >${currentPerson.firstName} ${currentPerson.lastName}</h2>
                  </div>
               </div>
            </div>
         </div>
         <!-- Dashboard layout begins here -->
         <div class="row mt-4">
            <div class="col-md-6">
               <!-- Gender section -->
               <div class="card custom-box">
                  <div class="card-body">
                     <h3>Gender</h3>
                     <p id="currentGender">${currentPerson.gender}</p>
                  </div>
               </div>
            </div>
            <div class="col-md-6">
               <!-- Date of Birth section -->
               <div class="card custom-box">
                  <div class="card-body">
                     <h3>Date of Birth</h3>
                     <p>${currentPerson.birthDate}</p>
                  </div>
               </div>
            </div>
         </div>
      </div>



      <div class="container mt-4">
         <div class="heading">
            <span>Contact Details
            </span>
         </div>
         <div class="row">
            <div class="col-md-4">
               <div class="card custom-box">
                  <div class="card-body">
                     <div class="content-section">
                        <div class="row align-items-center">
                           <div class="col-md-7">
                              <h3>Primary Email</h3>
                           </div>
                           <div class="col-md-3  text-right">
                              <button class="btn btn-link pencil-icon-button btn-sm" data-bs-toggle="modal" data-bs-target="#editEmailDialog">
                              <i class="fas fa-pencil-alt"></i>
                              </button>
                           </div>
                           <div class="col-md-2 text-right">
                              <button class="btn btn-link trash-icon-button btn-sm"  data-bs-toggle="modal" data-bs-target="#deleteEmailDialog" id="deletePrimaryEmail" >
                              <i class="fas fa-trash"></i>
                              </button>
                           </div>
                        </div>
                        <p id="currentEmail">
                           <#if email?has_content>${email}<#else>NA</#if>
                        </p>
                     </div>
                     <div class="content-section">

                        <#if email?has_content>
                     <br>

                        <div class="row align-items-center">
                           <div class="col-md-7">
                              <h3>Alternate Email</h3>
                           </div>
                           <div class="col-md-3  text-right">
                              <button class="btn btn-link pencil-icon-button btn-sm" data-bs-toggle="modal" data-bs-target="#editAlternateEmailDialog">
                              <i class="fas fa-pencil-alt"></i>
                              </button>
                           </div>
                           <div class="col-md-2 text-right">
                              <button class="btn btn-link trash-icon-button btn-sm"  data-bs-toggle="modal" data-bs-target="#deleteAlternateEmailDialog"  id="deleteAlternateEmailIcon" >
                              <i class="fas fa-trash"></i>
                              </button>
                           </div>
                        </div>
                        <p id="currentAlternateEmail">
                           <#if email_other?has_content>${email_other}<#else>NA</#if>
                        </p>
                        </#if>
                     </div>
                  </div>
               </div>
            </div>
            <div class="col-md-4">
               <div class="card custom-box">
                  <div class="card-body">
                     <div class="row align-items-center">
                        <div class="col-md-7">
                           <h3>Phone</h3>
                        </div>
                        <div class="col-md-3 text-right">
                           <button class="btn btn-link pencil-icon-button btn-sm" data-bs-toggle="modal" data-bs-target="#editPhoneDialog">
                           <i class="fas fa-pencil-alt"></i>
                           </button>
                        </div>
                        <div class="col-md-2 text-right">
                           <button class="btn btn-link trash-icon-button btn-sm" data-bs-toggle="modal" data-bs-target="#deletePhoneDialog" id="deletePhoneIcon">
                           <i class="fas fa-trash"></i>
                           </button>
                        </div>
                     </div>
                     <p id="currentPhone" > <#if phone?has_content>${phone}<#else>NA</#if></p>
                  </div>
               </div>
            </div>
            <div class="col-md-4">
               <div class="card custom-box">
                  <div class="card-body">
                     <div class="row align-items-center">
                        <div class="col-md-8">
                           <h3>Address</h3>
                        </div>
                        <div class="col-md-2 text-right">
                           <button class="btn btn-link pencil-icon-button btn-sm" data-bs-toggle="modal" data-bs-target="#editAddressDialog">
                           <i class="fas fa-pencil-alt"></i>
                           </button>
                        </div>
                        <div class="col-md-1 text-right">
                           <button class="btn btn-link trash-icon-button btn-sm" data-bs-toggle="modal" data-bs-target="#deleteAddressDialog" id="deleteAddressIcon" >
                           <i class="fas fa-trash"></i>
                           </button>
                        </div>
                     </div>
                     <p id="currentAddress">
                        <#if address?has_content>${address}<#else>NA</#if>
                     </p>
                  </div>
               </div>
            </div>
         </div>
      </div>
   </div>



   <div class="modal fade" id="editBasicDetailDialog" tabindex="-1" role="dialog" aria-labelledby="basicDetailModalLabel" aria-hidden="true">
      <div class="modal-dialog" role="document">
         <div class="modal-content">
            <div class="modal-header">
               <h5 class="modal-title" id="basicDetailModalLabel">Edit Student Details</h5>
               <button type="button" class="close" data-dismiss="modal" aria-label="Close">
               <span aria-hidden="true">&times;</span>
               </button>
            </div>
            <div class="modal-body">
               <form>
                  <div class="form-group">
                     <label for="firstName">First Name</label>
                     <input type="text" class="form-control" id="firstName"
                        placeholder="${currentPerson.firstName}">
                  </div>
                  <div class="form-group">
                     <label for="lastName">Last Name</label>
                     <input type="text" class="form-control" id="lastName" placeholder="${currentPerson.lastName}">
                  </div>
                  <div class="form-group">
                     <label for="gender">Gender</label>
                     <select class="form-control" id="gender">
                        <option value="M">M</option>
                        <option value="F">F</option>
                     </select>
                  </div>
                  <div class="form-group">
                     <label for="birthDate">Birth Date</label>
                     <input type="date" class="form-control" id="birthDate" placeholder=${currentPerson.birthDate}>
                  </div>
               </form>
            </div>
            <div class="modal-footer">
               <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" style= "margin-right:auto;">Close</button>
               <button type="button" class="btn btn-primary" id="saveButton" data-value= ${currentPerson.partyId}>Save changes</button>
            </div>
         </div>
      </div>
   </div>
   <div class="modal fade" id="editEmailDialog" tabindex="-1" role="dialog" aria-labelledby="editEmailModalLabel" aria-hidden="true">
      <div class="modal-dialog" role="document">
         <div class="modal-content">
            <div class="modal-header">
               <h5 class="modal-title" id="editEmailModalLabel">Edit Email</h5>
               <button type="button" class="close" data-dismiss="modal" aria-label="Close">
               <span aria-hidden="true">&times;</span>
               </button>
            </div>
            <div class="modal-body">
               <form>
                  <div class="form-group">
                     <label for="email">Email</label>
                     <input type="text" class="form-control" id="currentEmailAddress" placeholder="<#if email?has_content>${email}<#else>Enter email...</#if>"">
                  </div>
               </form>
            </div>
            <div class="modal-footer">
               <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" style= "margin-right:auto;" >Close</button>
               <#if email?has_content>
               <button type="button" class="btn btn-primary" id="saveEmailButton" email-contact-mech="${emailContactMechId}" party-id="${currentPerson.partyId}">Save Changes</button>
               <#else>
               <button type="button" class="btn btn-primary" id="saveEmailButton" email-contact-mech="${emailContactMechId?default('null')}" party-id="${currentPerson.partyId}">Save Email</button>
               </#if>
            </div>
         </div>
      </div>
   </div>

    <div class="modal fade" id="editAlternateEmailDialog" tabindex="-1" role="dialog" aria-labelledby="editAlternateEmailModalLabel" aria-hidden="true">
         <div class="modal-dialog" role="document">
            <div class="modal-content">
               <div class="modal-header">
                  <h5 class="modal-title" id="editEmailModalLabel">Edit Email</h5>
                  <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                  <span aria-hidden="true">&times;</span>
                  </button>
               </div>
               <div class="modal-body">
                  <form>
                     <div class="form-group">
                        <label for="email">Email</label>
                        <input type="text" class="form-control" id="currentAlternateEmailAddress" placeholder="<#if email_other?has_content>${email_other}<#else>Enter alternate email...</#if>"">
                     </div>
                  </form>
               </div>
               <div class="modal-footer">
                      <button type="button" class="btn btn-secondary" data-bs-dismiss="modal"  style= "margin-right:auto;">Close </button>
                  <#if email_other?has_content>
                  <button type="button" class="btn btn-primary" id="saveAlternateEmailButton" email-contact-mech="${emailContactMechId_other}" party-id="${currentPerson.partyId}">Save Changes</button>
                  <#else>
                  <button type="button" class="btn btn-primary" id="saveAlternateEmailButton" email-contact-mech="${emailContactMechId_other?default('null')}" party-id="${currentPerson.partyId}">Save Email</button>
                  </#if>
               </div>
            </div>
         </div>
      </div>


   <div class="modal fade" id="editPhoneDialog" tabindex="-1" role="dialog" aria-labelledby="editPhoneModalLabel" aria-hidden="true">
      <div class="modal-dialog" role="document">
         <div class="modal-content">
            <div class="modal-header">
               <h5 class="modal-title" id="editPhoneModalLabel">Edit Phone</h5>
               <button type="button" class="close" data-dismiss="modal" aria-label="Close">
               <span aria-hidden="true">&times;</span>
               </button>
            </div>
            <div class="modal-body">
               <form>
                  <div class="form-group">
                     <label for="phone">Phone</label>
                     <input type="text" class="form-control" id="currentPhoneNumber" placeholder="<#if phone?has_content>${phone}<#else>Enter Phone ...</#if>">
                  </div>
               </form>
            </div>
            <div class="modal-footer">
               <button type="button" class="btn btn-secondary" data-bs-dismiss="modal"  style= "margin-right:auto;">Close</button>
               <#if phone?has_content>
               <button type="button" class="btn btn-primary" id="savePhoneButton" phone-contact-mech="${phoneContactMechId}" party-id="${currentPerson.partyId}">Save Changes</button>
               <#else>
               <button type="button" class="btn btn-primary" id="savePhoneButton" phone-contact-mech="${phoneContactMechId?default('null')}" party-id="${currentPerson.partyId}">Save Phone</button>
               </#if>
            </div>
         </div>
      </div>
   </div>


   <div class="modal fade" id="editAddressDialog" tabindex="-1" role="dialog" aria-labelledby="editAddressModalLabel" aria-hidden="true">
      <div class="modal-dialog" role="document">
         <div class="modal-content">
            <div class="modal-header">
               <h5 class="modal-title" id="editAddressModalLabel">Edit Address</h5>
               <button type="button" class="close" data-dismiss="modal" aria-label="Close">
               <span aria-hidden="true">&times;</span>
               </button>
            </div>
            <div class="modal-body">
               <form>
                  <div class="form-group">
                     <label for="address1">Address Line 1</label>
                     <input type="text" class="form-control" id="currentAddress1" >
                  </div>
                  <div class="form-group">
                     <label for="address2">Address Line 2</label>
                     <input type="text" class="form-control" id="currentAddress2">
                  </div>
                  <div class="form-group">
                     <label for="city">City</label>
                     <input type="text" class="form-control" id="currentCity" ">
                  </div>
               </form>
            </div>
            <div class="modal-footer">
               <button type="button" class="btn btn-secondary" data-bs-dismiss="modal"  style= "margin-right:auto;">Close</button>
               <#if address?has_content>
               <button type="button" class="btn btn-primary" id="saveAddressButton" address-contact-mech="${addressContactMechId}" party-id="${currentPerson.partyId}">Save Changes</button>
               <#else>
               <button type="button" class="btn btn-primary" id="saveAddressButton" address-contact-mech="${addressContactMechId?default('null')}" party-id="${currentPerson.partyId}">Save Address</button>
               </#if>
            </div>
         </div>
      </div>
   </div>


   <div class="modal fade" id="deleteEmailDialog" tabindex="-1" role="dialog" aria-labelledby="deleteEmailModalLabel" aria-hidden="true">
      <div class="modal-dialog" role="document">
         <div class="modal-content">
            <div class="modal-header">
               <h5 class="modal-title" id="deleteEmailModalLabel">Delete Email</h5>
               <div class="modal-footer">
                  <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" style= "margin-right:auto;">Close</button>
                  <button type="button" class="btn btn-primary" id="deleteEmailButton" email-contact-mech= ${emailContactMechId} party-id=${currentPerson.partyId} >Delete Email</button>
               </div>
            </div>
         </div>
      </div>
   </div>


   <div class="modal fade" id="deleteAlternateEmailDialog" tabindex="-1" role="dialog" aria-labelledby="deleteAlternateEmailModalLabel" aria-hidden="true">
         <div class="modal-dialog" role="document">
            <div class="modal-content">
               <div class="modal-header">
                  <h5 class="modal-title" id="deleteAlternateEmailModalLabel">Delete Email</h5>
                  <div class="modal-footer">
                     <button type="button" class="btn btn-secondary" data-bs-dismiss="modal"  style= "margin-right:auto;" >Close</button>
                     <button type="button" class="btn btn-primary" id="deleteAlternateEmailButton" email-contact-mech=${emailContactMechId_other} party-id=${currentPerson.partyId} >Delete Alternate Email</button>
                  </div>
               </div>
            </div>
         </div>
      </div>


   <div class="modal fade" id="deleteAddressDialog" tabindex="-1" role="dialog" aria-labelledby="deleteAddressModalLabel" aria-hidden="true">
      <div class="modal-dialog" role="document">
         <div class="modal-content">
            <div class="modal-header">
               <h5 class="modal-title" id="deleteAddressModalLabel">Delete Address</h5>
               <div class="modal-footer">
         <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" style= "margin-right:auto;">Close</button>
                  <button type="button" class="btn btn-primary" id="deleteAddressButton" address-contact-mech= ${addressContactMechId} party-id=${currentPerson.partyId} >Delete</button>
               </div>
            </div>
         </div>
      </div>
   </div>

  <div class="modal fade" id="deletePhoneDialog" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
       <div class="modal-dialog" role="document">
          <div class="modal-content">
             <div class="modal-header">
                <h5 class="modal-title" id="deleteModalLabel">Delete Phone</h5>
                <div class="modal-footer">
                   <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                   <button type="button" class="btn btn-primary" id="deletePhoneButton" phone-contact-mech= ${phoneContactMechId} party-id=${currentPerson.partyId} >Delete</button>
                </div>
             </div>
          </div>
       </div>
    </div>
