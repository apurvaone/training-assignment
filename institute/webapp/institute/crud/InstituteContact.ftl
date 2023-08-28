<div class="container">
   <#if partyContactMechList?has_content>
   <br>
   <h3> Contact </h3>
   <br>
   <table class="table table-bordered table-striped table-hover">
      <thead>
         <tr>
            <th>contactMech</th>
            <th>detail</th>
         </tr>
      </thead>
      <tbody>
         <#list partyContactMechList as partyContactMech>
         <tr>
            <#assign contactMech = partyContactMech.getRelatedOne("ContactMech").get("contactMechId", locale)>
            <#if contactMech == "institute_phone">
            <td>Phone</td>
            <td>${partyContactMech.getRelatedOne("TelecomNumber").get("contactNumber", locale)}</td>
            <#elseif contactMech == "institute_address">
            <td>Address</td>
            <#assign address1 = partyContactMech.getRelatedOne("PostalAddress").get("address1", locale)>
            <#assign address2 = partyContactMech.getRelatedOne("PostalAddress").get("address2", locale)>
            <td>${partyContactMech.getRelatedOne("PostalAddress").get("address1", locale)},${ partyContactMech.getRelatedOne("PostalAddress").get("address2", locale)}</td>
            <#else>
            <td>Email</td>
            <td>${partyContactMech.getRelatedOne("ContactMech").get("infoString", locale)}</td>
            </#if>
         </tr>
         </#list>
      </tbody>
   </table>
   </#if>
</div>