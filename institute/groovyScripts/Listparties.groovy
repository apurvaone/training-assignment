import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.util.EntityQuery

// Get the Institute
institute= from("Party").where("partyId","acropolis").queryOne()
context.institute= institute

// Get the related ids of Students with the institute
partyCondtionMap= ["partyIdFrom":"acropolis","roleTypeIdTo":"CUSTOMER"]
studentIds=runService("getRelatedParties",partyCondtionMap).get("relatedPartyIdList")

// Creates a condition list containing party id of students
List<EntityCondition> conditionsList = []
studentIds.each { studentId ->
    conditionsList << EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, studentId)
}

EntityCondition conditions = EntityCondition.makeCondition(conditionsList, EntityOperator.OR)

personList = from("Person").where(conditions).queryList()
context.personList = personList

partyList = from("Party").queryList()
context.partyList = partyList
