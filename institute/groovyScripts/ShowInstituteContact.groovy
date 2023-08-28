import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityOperator


EntityCondition contactMechCondition = EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, "acropolis")
contactMechs= from("PartyContactMech").where("partyId",parameters.party_id).queryList()
context.contactMechs= contactMechs

partyContactMechList= delegator.findList("PartyContactMech",contactMechCondition, null, null, null, false)
context.partyContactMechList= partyContactMechList
