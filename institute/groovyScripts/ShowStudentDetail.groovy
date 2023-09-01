import org.apache.ofbiz.party.contact.ContactMechWorker
import org.apache.ofbiz.base.util.Debug

//Current Person and Party Id
currentPartyId= parameters.party_id
currentPerson= from("Person").where("partyId",currentPartyId).queryOne()
context.currentPerson= currentPerson
context.currentPartyId= parameters.party_id

// Stores Contact Mech data maps of the current student
studentData= ContactMechWorker.getPartyContactMechValueMaps(delegator,parameters.party_id,false)

// Iterating through each contact mech of student and acceccing the required data
studentData.each{
    contact->
        Debug.log("stdata:\n\n\n"+ studentData+ "\n\n\n")
        contactType=contact.contactMech.getString("contactMechTypeId")
        postalAddress=contact.postalAddress
        telecomNumber=contact.telecomNumber
        partyContactMechPurposes= contact.partyContactMechPurposes

        // Adding Email data in context
        if(contactType=="EMAIL_ADDRESS"){

            if (partyContactMechPurposes.size()>0){
              if (partyContactMechPurposes[0].contactMechPurposeTypeId=="OTHER_EMAIL"){
                    context.email_other=contact.contactMech.infoString
                    context.emailContactMechId_other= contact.contactMech.contactMechId
                }
                else if (partyContactMechPurposes[0].contactMechPurposeTypeId=="PRIMARY_EMAIL"){
                    context.email=contact.contactMech.infoString
                    context.emailContactMechId= contact.contactMech.contactMechId
                }
              }
            else{
                context.email=contact.contactMech.infoString
                context.emailContactMechId= contact.contactMech.contactMechId
            }
        }

        // Adding Postal Address data in context
        if(contactType=="POSTAL_ADDRESS"){
            context.addressContactMechId= contact.contactMech.contactMechId
            context.address=postalAddress.address1+','+postalAddress.address2+','+postalAddress.city

        }

        // Adding Telecom Number data in context
        if(contactType=="TELECOM_NUMBER"){
            context.phoneContactMechId= contact.contactMech.contactMechId
            context.phone=telecomNumber.contactNumber

        }

}














