import org.apache.ofbiz.base.util.Debug


def createStudentDetails() {
    // Creates a Person with specified role
    def personData = runService("createPersonRoleAndContactMechs", parameters)

    def relationData = [
            partyIdFrom: "acropolis",
            partyIdTo: personData.partyId,
            roleTypeIdFrom: "ORGANIZATION_ROLE",
            roleTypeIdTo: "CUSTOMER"
    ]

    runService("createPartyRelationshipAndRole", relationData)
    return success()
}



def deleteStudent() {
    def fromDate = from('PartyRelationship').where('partyIdTo', partyId, 'partyIdFrom', 'acropolis').queryOne().fromDate

    def data = [
            partyIdFrom: "acropolis",
            partyIdTo: partyId,
            fromDate: fromDate,
            roleTypeIdTo: "CUSTOMER",
            roleTypeIdFrom: "ORGANIZATION_ROLE"
    ]

    runService("deletePartyRelationship", data)
}




def deleteParty() {

    String partyId = (String) context.get("partyId")
    party = from('Party').where('partyId', partyId).queryOne()
    person = from('Person').where('partyId', partyId).queryOne()

    try {

        Map<String, String> data2 = new HashMap<>();
        data2.put("roleTypeId","_NA_");
        data2.put("partyId",partyId);
        runService("deletePartyRole", parameters)
        runService("deletePartyRole",data2)

        fromDate = from('PartyRelationship').where('partyIdTo', partyId,'partyIdFrom','acropolis').queryOne().fromDate
        partyStatus = from('PartyStatus').where('partyId',partyId, 'statusId','PARTY_ENABLED').queryOne()

        delegator.removeValue(partyStatus)
        println("Party Status\n\n\n"+ partyStatus + "\n\n\n")
        Map<String, String> data = new HashMap<>();
        data.put("partyIdFrom","acropolis");
        data.put("partyIdTo",partyId);
        data.put("fromDate",fromDate)
        runService("deletePartyRelationship",data)
        delegator.removeValue(person)
        delegator.removeValue(party)

    } catch (Exception e) {

        println("\n\n\n\nError occurred while deleting the party" + e + "\n\n\n\n")
    }

    return success()
}

