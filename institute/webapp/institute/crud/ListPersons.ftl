<div class="container">
   <#if personList?has_content>
   <div class="h4">
      <span>Students</span>
      <button type="button" class="btn btn-primary btn-sm float-right custom-add-student-button" data-bs-toggle="modal" data-bs-target="#addStudentDialog" id="editButton">Add</button>
      <br>
      <br>
   </div>
   <table class="table table-bordered table-striped table-hover id="student_table">
      <thead>
         <tr>
            <th>firstName</th>
            <th>lastName</th>
            <th>gender</th>
            <th>birthDate</th>
            <th>details</th>
            <th>Delete</th>
         </tr>
      </thead>
      <tbody>
         <#list personList as person>
         <tr>
            <td>${person.firstName}</td>
            <td>${person.lastName}</td>
            <td>${person.gender}</td>
            <td>${person.birthDate}</td>
            <td>
               <a href="<@ofbizUrl>StudentDetail</@ofbizUrl>/?party_id=${person.partyId}" class="view-details-link">View Details</a>
            </td>
            <td class="delete-icon-container" >
               <a href="#"  id="deleteIcon-${person.partyId}" party_id=${person.partyId} "><i class="fa fa-trash"></i></a>
            </td>
         </tr>
         </#list>
      </tbody>
   </table>
   </#if>
</div>
<div class="modal fade" id="addStudentDialog" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
   <div class="modal-dialog" role="document">
      <div class="modal-content">
         <div class="modal-header">
            <h5 class="modal-title" id="exampleModalLabel">Add Student</h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
            </button>
         </div>
         <div class="modal-body">
            <form>
               <div class="form-group">
                  <label for="firstName">First</label>
                  <input type="text" class="form-control" id="currentFirstName" >
               </div>
               <div class="form-group">
                  <label for="lastName">Last Name</label>
                  <input type="text" class="form-control" id="currentLastName">
               </div>
               <div class="form-group">
                  <label for="gender">Gender</label>
                  <input type="text" class="form-control" id="currentGender" ">
               </div>
               <div class="form-group">
                  <label for="gender">DateOf Birth</label>
                  <input type="date" class="form-control" id="currentDOB" ">
               </div>
            </form>
         </div>
         <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" style= "margin-right:auto;">Close</button>
            <#if address?has_content>
            <button type="button" class="btn btn-primary" id="addPersonButton">Add Student</button>
            <#else>
            <button type="button" class="btn btn-primary" id="addPersonButton" >Add Student</button>
            </#if>
         </div>
      </div>
   </div>
</div>

<div class="modal fade" id="deleteStudentDialog" tabindex="-1" role="dialog" aria-labelledby="deleteStudentModalLabel" aria-hidden="true">
      <div class="modal-dialog" role="document">
         <div class="modal-content">
            <div class="modal-header">
               <h5 class="modal-title" id="deleteStudentModalLabel">Delete Student</h5>
               <div class="modal-footer">
                  <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                  <button type="button" class="btn btn-primary" id="deleteStudentButton"  party-id=${currentPerson.partyId} >Delete</button>
               </div>
            </div>
         </div>
      </div>
   </div>


   <div id="alertContainer"></div>
