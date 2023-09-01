import java.math.BigDecimal;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.stream.IntStream;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.entity.util.EntityQuery;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ModelService;
import org.apache.ofbiz.service.ServiceUtil;
import org.jdom.JDOMException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import javax.xml.stream.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.*;
import org.apache.commons.imaging.ImageReadException;
import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.base.util.UtilGenerics;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.base.util.string.FlexibleStringExpander;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityJoinOperator;
import org.apache.ofbiz.entity.condition.EntityOperator;


/*

 Approach 2:

 Helper class to facilitate Utility and IO operations of PIES data import
 This class contains different utility methods and methods to process particular type of data
 and import it into database.

 The processing methods uses batch processing of data using storeAll() method provided by Delegator

*/


public final class PIESHelper2 {

    private static final String MODULE = PIESHelper.class.getName();

    // Method to check if a product of particular PartNumber exists in the database
    public static boolean checkProductExists(String productId, Delegator delegator) {
        GenericValue genericValue;
        boolean productExists = false;
        try {
            genericValue = EntityQuery.use(delegator).from("Product").where("productId", productId).queryOne();
            if (genericValue != null && productId.equals(genericValue.getString("productId"))) {
                productExists = true;
            }
        } catch (GenericEntityException e) {
            Debug.logError("Problem in reading data of product", MODULE);
        }
        return productExists;
    }

    // Method to check if a specific type of good identification exists for a product
    public static boolean checkGoodIdentificationExists(String productId, String goodIdentificationTypeId,
                                                        Delegator delegator) {
        GenericValue genericValue;
        boolean goodIdentificationExists = false;
        try {
            genericValue = EntityQuery.use(delegator).from("GoodIdentification")
                    .where("productId", productId, "goodIdentificationTypeId", goodIdentificationTypeId)
                    .queryOne();
            if (genericValue != null && productId.equals(genericValue.getString("productId"))) {
                goodIdentificationExists = true;
            }
        } catch (GenericEntityException e) {
            Debug.logError("Problem in reading data of good identification", MODULE);
        }
        return goodIdentificationExists;
    }

    // Method to check if a specific type of product category member exists for a product
    public static boolean categoryMemberExists(String productId, String partTerminologyId,
                                               Delegator delegator) {
        GenericValue genericValue;
        boolean productCategoryMemberExists = false;
        try {
            genericValue = EntityQuery.use(delegator).from("ProductAndCategoryMember")
                    .where("productId", productId, "productCategoryId", partTerminologyId)
                    .queryFirst();
            if (genericValue != null && productId.equals(genericValue.getString("productId"))) {
                productCategoryMemberExists = true;
            }
        } catch (GenericEntityException e) {
            Debug.logError("Problem in reading data of product catag member", MODULE);
        }
        return productCategoryMemberExists;
    }

    // Method to check if a specific type of good identification exists for a product
    public static boolean checkUOMIdExists(String uomId,
                                           Delegator delegator) {
        GenericValue genericValue;
        boolean uomIdExists = false;
        try {
            genericValue = EntityQuery.use(delegator).from("Uom")
                    .where("uomId", uomId)
                    .queryOne();
            if (genericValue != null) {
                uomIdExists = true;
            }
        } catch (GenericEntityException e) {
            Debug.logError("Problem in reading data of good identification", MODULE);
        }
        return uomIdExists;
    }

    // Method to check if a product attribute exists for a product
    public static boolean checkProductAttributeExists(String productId, String attrName,
                                                      Delegator delegator) {
        GenericValue genericValue;
        boolean productAttributeExists = false;
        try {
            genericValue = EntityQuery.use(delegator).from("ProductAttribute")
                    .where("productId", productId, "attrName", attrName)
                    .queryOne();
            if (genericValue != null && productId.equals(genericValue.getString("productId"))) {
                productAttributeExists = true;
            }
        } catch (GenericEntityException e) {
            Debug.logError("Problem in reading data of product Attribute", MODULE);
        }
        return productAttributeExists;
    }

    // Method to check if a specific product description exists for a product
    public static String checkProductDescriptionExists(String productId, String descriptionCode,
                                                       Delegator delegator) {

        GenericValue genericValue;
        String descriptionDataResourceId = null;
        try {
            genericValue = EntityQuery.use(delegator).from("ProductContentAndInfo")
                    .where("productId", productId, "contentName", descriptionCode)
                    .queryOne();
            if (genericValue != null && productId.equals(genericValue.getString("productId"))) {
                descriptionDataResourceId = genericValue.getString("dataResourceId");
            }
        } catch (GenericEntityException e) {
            Debug.logError("Problem in reading data of product descriptions", MODULE);
        }
        return descriptionDataResourceId;
    }

    // Method to check if a content exists between dataResource and productId
    public static String checkProductContentExists(Object productId, Object dataResourceId,
                                                   Delegator delegator) {

        GenericValue genericValue;
        String contentId = null;
        try {
            genericValue = EntityQuery.use(delegator).from("ProductContentAndInfo")
                    .where("productId", productId, "dataResourceId", dataResourceId)
                    .queryOne();
            if (genericValue != null && productId.equals(genericValue.getString("productId"))) {
                contentId = genericValue.getString("contentId");
            }
        } catch (GenericEntityException e) {
            Debug.logError("Problem in reading data of product content", MODULE);
        }
        return contentId;
    }

    // Method to check if a product assoc exists betten product and Interchange
    public static boolean checkProductAssocExists(String productId, String productIdTo, String reason,
                                                  Delegator delegator) {

        GenericValue genericValue;
        boolean assocExists = false;
        try {
            genericValue = EntityQuery.use(delegator).from("ProductAssoc")
                    .where("productId", productId, "productIdTo", productIdTo, "reason", reason)
                    .queryFirst();
            if (genericValue != null && productId.equals(genericValue.getString("productId"))) {
                assocExists = true;
            }
        } catch (GenericEntityException e) {
            Debug.logError("Problem in reading data of product assoc", MODULE);
        }
        return assocExists;
    }

    // Method to get the current date and time in a specific format
    public static String getFormatedCurrentDateTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDateTime = currentDateTime.format(formatter);
        return formattedDateTime;
    }

    // Method to check if a data resource (file) exists
    public static String checkDataResourceExists(String fileName, Delegator delegator) {
        GenericValue genericValue;
        String dataResourceId = null;
        try {
            genericValue = EntityQuery.use(delegator).from("DataResource")
                    .where("dataResourceName", fileName)
                    .queryFirst();
            if (genericValue != null) {
                dataResourceId = genericValue.getString("dataResourceId");
            }
        } catch (GenericEntityException e) {
            Debug.logError("Problem in reading data of product data resource" + e, MODULE);
        }
        return dataResourceId;
    }

    // Method to check if a DataResourceAttribute (for digital file) exists
    public static boolean checkDataResourceAttributeExists(Object dataResourceId, String attrName,
                                                           Delegator delegator) {
        GenericValue genericValue;
        boolean dataResourceAttributeExists = false;
        try {
            genericValue = EntityQuery.use(delegator).from("DataResourceAttribute")
                    .where("dataResourceId", dataResourceId, "attrName", attrName)
                    .queryOne();
            if (genericValue != null) {
                dataResourceAttributeExists = true;
            }
        } catch (GenericEntityException e) {
            Debug.logError("Problem in reading data of DR Attribute", MODULE);
        }
        return dataResourceAttributeExists;
    }

    public static void createBasicProduct(String partNumber, LocalDispatcher dispatcher, Delegator delegator, GenericValue userLogin) {

        try {
            boolean productExists = checkProductExists(partNumber, delegator);

            Map < String, Object > productEntityMap = new HashMap < > ();

            productEntityMap.put("productId", partNumber);
            productEntityMap.put("internalName", partNumber);
            productEntityMap.put("userLogin", userLogin);
            productEntityMap.put("productTypeId", "SUBASSEMBLY");
            dispatcher.runSync("createProduct", productEntityMap);

            // Determine the service name based on whether the product exists or not
            String serviceName = productExists ? "updateProduct" : "createProduct";

            // Call the appropriate service to create or update the product entity
            dispatcher.runSync(serviceName, productEntityMap);
        } catch (GenericServiceException e) {
            Debug.logError("Problem in reading data of DR Attribute", MODULE);
        }

    }

    // Method to process Product Entity data
    public static void processItemData(Map < String, String > itemData, LocalDispatcher dispatcher, Delegator delegator, GenericValue userLogin) {
        try {
            // Check if heightUomId and weightUomId exist in UOM (Unit of Measure)
            boolean heightUomCheck = checkUOMIdExists(itemData.get("heightUomId"), delegator);
            boolean weightUomCheck = checkUOMIdExists(itemData.get("weightUomId"), delegator);

            // Create UOM if heightUomId doesn't exist
            if (!heightUomCheck) {
                Map < String, Object > uomContext = new HashMap < > ();
                uomContext.put("uomId", itemData.get("heightUomId"));
                uomContext.put("userLogin", userLogin);
                dispatcher.runSync("createUom", uomContext);
            }

            // Create UOM if weightUomId doesn't exist
            if (!weightUomCheck) {
                Map < String, Object > uomContext = new HashMap < > ();
                uomContext.put("uomId", itemData.get("weightUomId"));
                uomContext.put("userLogin", userLogin);
                dispatcher.runSync("createUom", uomContext);
            }

            // Prepare data for creating or updating a product entity
            Map < String, Object > productEntityMap = new HashMap < > ();
            productEntityMap.put("productId", itemData.get("PartNumber"));
            productEntityMap.put("brandName", itemData.get("BrandLabel"));
            productEntityMap.put("internalName", "Fuel Injectors");
            productEntityMap.put("productTypeId", "SUBASSEMBLY");
            productEntityMap.put("quantityIncluded", itemData.get("ItemQuantitySize"));
            productEntityMap.put("productName", itemData.get("Alliance Product Name"));
            productEntityMap.put("description", itemData.get("aca_dwStdPartDesc"));
            productEntityMap.put("smallImageUrl", itemData.get("aca_DefaultThumbnail"));
            productEntityMap.put("detailImageUrl", itemData.get("aca_DefaultImage"));
            productEntityMap.put("productLength", itemData.get("productLength"));
            productEntityMap.put("productHeight", itemData.get("productHeight"));
            productEntityMap.put("productWidth", itemData.get("productWidth"));
            productEntityMap.put("productWeight", itemData.get("productWeight"));
            productEntityMap.put("heightUomId", itemData.get("heightUomId"));
            productEntityMap.put("weightUomId", itemData.get("weightUomId"));
            productEntityMap.put("depthUomId", itemData.get("depthUomId"));
            productEntityMap.put("widthUomId", itemData.get("widthUomId"));
            productEntityMap.put("userLogin", userLogin);

            // Check if the product already exists
            boolean productExists = checkProductExists(itemData.get("PartNumber"), delegator);

            //             Determine the service name based on whether the product exists or not
            String serviceName = productExists ? "updateProduct" : "createProduct";

            //             Call the appropriate service to create or update the product entity
            dispatcher.runSync(serviceName, productEntityMap);

        } catch (GenericServiceException e) {
            Debug.log("\n\n\nGSerror" + e);
        } catch (Exception e) {
            Debug.log("\n\n\nException " + e);
        }
    }

    //Method to process GTIN of the product
    public static void processGTINs(String itemLevelGTIN, LocalDispatcher dispatcher, Delegator delegator, String partNumber, GenericValue userLogin) {

        // Initialize a contextMaps to store the parameters
        Map < String, Object > sContext = new HashMap < > ();
        Map < String, Object > typeContext = new HashMap < > ();

        try {

            GenericValue TypeGtin = EntityQuery.use(delegator).from("GoodIdentificationType").where("goodIdentificationTypeId", "GTIN").queryOne();

            // Check if GoodIdentificationType exists, if not create one
            if (TypeGtin == null) {

                typeContext.put("goodIdentificationTypeId", "GTIN");
                typeContext.put("description", "Global Trade Identification NUmber");
                typeContext.put("userLogin", userLogin);

                dispatcher.runSync("createGoodIdentificationType", typeContext);
            }

            // adding the data into contextMap to be passed
            sContext.put("goodIdentificationTypeId", "GTIN");
            sContext.put("idValue", itemLevelGTIN);
            sContext.put("productId", partNumber);
            sContext.put("userLogin", userLogin);

            //Check if goodIdentification exists, create only if id does not exist
            boolean goodIdentificationExists = checkGoodIdentificationExists(partNumber, "GTIN", delegator);
            if (!goodIdentificationExists)
                dispatcher.runSync("createGoodIdentification", sContext);

        } catch (GenericServiceException e) {
            Debug.log("\n\n\nGSerror" + e);
        } catch (GenericEntityException e) {
            Debug.log("\n\n\nGEerror" + e);
        }
    }

    //Method to process Categories of the product (partTerminologyId here)
    public static void processCategories(String partTerminologyId, LocalDispatcher dispatcher, Delegator delegator, String partNumber, GenericValue userLogin) {

        // Initialize a contextMaps to store the parameters
        Map < String, Object > categoryContext = new HashMap < > ();
        Map < String, Object > categoryMemberContext = new HashMap < > ();

        try {
            GenericValue partTerminologyCatag = EntityQuery.use(delegator).from("ProductCategory").where("productCategoryId", partTerminologyId).queryOne();

            // Check if GoodIdentificationType exists, if not create one
            if (partTerminologyCatag == null) {

                categoryContext.put("productCategoryId", partTerminologyId);
                categoryContext.put("productCategoryTypeId", "INTERNAL_CATEGORY");
                categoryContext.put("categoryName", "Part Terminology Category");
                categoryContext.put("userLogin", userLogin);

                dispatcher.runSync("createProductCategory", categoryContext);

            }

            // adding the data into contextMap to be passed
            categoryMemberContext.put("productId", partNumber);
            categoryMemberContext.put("productCategoryId", partTerminologyId);
            categoryMemberContext.put("userLogin", userLogin);

            boolean categoryMemberExists = categoryMemberExists(partNumber, partTerminologyId, delegator);

            // if categoryMember Exists, then update it else create a new categoryMember
            if (!categoryMemberExists) {
                dispatcher.runSync("addProductToCategory", categoryMemberContext);

            }

        } catch (GenericServiceException e) {
            Debug.log("\n\n\nGSerror" + e);
        } catch (GenericEntityException e) {
            Debug.log("\n\n\nGEerror" + e);
        }
    }

    // Method to process Product Attributes
    public static void processProductAttributes(List < List < String >> productAttributeData, LocalDispatcher dispatcher, Delegator delegator, String partNumber, String productBrandAAIAD, String itemLevelGTIN, GenericValue userLogin) {
        // Format itemLevelGTIN to create packageLevelGTIN
        String packageLevelGTIN = String.format("%1$" + 14 + "s", itemLevelGTIN).replace(' ', '0');

        // Prepare data for brandAAAID, packageLevelGTIN, and packageBarCodeCharacters
        List < String > brandAAAIDData = List.of("BrandAAIAD", productBrandAAIAD);
        List < String > packageLevelGTINData = List.of("packageLevelGTIN", packageLevelGTIN);
        List < String > packageBarCodeCharacters = List.of("packageBarCodeCharacters", itemLevelGTIN);

        // Add the prepared data to the productAttributeData list
        productAttributeData.add(brandAAAIDData);
        productAttributeData.add(packageLevelGTINData);
        productAttributeData.add(packageBarCodeCharacters);

        // Map productAttributeData to create ProductAttribute entities
        List < GenericValue > productAttributesToInsert = productAttributeData.stream()
                .map(attributeData -> {
                    String attributeName = attributeData.get(0);
                    String attributeValue = attributeData.get(1);

                    // Create a ProductAttribute entity and populate its fields
                    GenericValue productAttribute = delegator.makeValue("ProductAttribute");
                    productAttribute.set("attrName", attributeName);
                    productAttribute.set("attrValue", attributeValue);
                    productAttribute.set("productId", partNumber);

                    return productAttribute;
                })
                .collect(Collectors.toList());

        try {
            // Store all the ProductAttribute entities in the database
            delegator.storeAll(productAttributesToInsert);
        } catch (Exception e) {
            Debug.log("Error inserting ProductAttribute records: " + e.getMessage());
        }
    }

    // Method to process Extended Product Information
    public static void processExtendedProductInfo(List < Map < String, String >> extendedInfoData, LocalDispatcher dispatcher, Delegator delegator, String partNumber, GenericValue userLogin) {
        try {
            // Create lists to hold content and product content entities
            List < GenericValue > contentsToInsert = extendedInfoData.stream()
                    .map(info -> {
                        String contentName = info.get("EXPICode");
                        String localeString = info.get("LanguageCode");
                        String description = info.get("Text");

                        // Create a Content entity and populate its fields
                        GenericValue content = delegator.makeValue("Content");
                        content.set("contentName", contentName);
                        content.set("contentId", delegator.getNextSeqId("Content"));
                        content.set("localeString", localeString);
                        content.set("description", description);
                        return content;
                    })
                    .collect(Collectors.toList());

            // Create product content entities and associate them with the contents
            List < GenericValue > productContentsToInsert = contentsToInsert.stream()
                    .map(content -> {
                        Map < String,
                                Object > productContentData = new HashMap < > ();
                        productContentData.put("productId", partNumber);
                        productContentData.put("productContentTypeId", "DESCRIPTION");
                        productContentData.put("contentId", content.get("contentId"));
                        productContentData.put("fromDate", getFormatedCurrentDateTime());

                        // Create a ProductContent entity and populate its fields
                        GenericValue productContent = delegator.makeValue("ProductContent", productContentData);
                        return productContent;
                    })
                    .collect(Collectors.toList());

            // Store all the content and product content entities in the database
            delegator.storeAll(contentsToInsert);
            delegator.storeAll(productContentsToInsert);
        } catch (Exception e) {
            // Handle any exceptions that occur during the data processing
        }
    }

    // Method to process PartInterchange data of product
    public static void processPartInterchangeData(List < Map < String, String >> partInterchanges, LocalDispatcher dispatcher, Delegator delegator, String partNumber, GenericValue userLogin) {
        // Loop through each partInterchange map in the list
        partInterchanges.forEach(partInterchange -> {
            String partNumberTo = partInterchange.get("PartNumber");
            String typeCode = partInterchange.get("TypeCode");
            String brandAAIAID = partInterchange.get("BrandAAIAID");
            String brandLabel = partInterchange.get("BrandLabel");

            try {
                // Get the current date and time in a formatted string
                String formattedDateTime = getFormatedCurrentDateTime();

                // Prepare data for creating a product association
                Map < String, Object > productAssocData = new HashMap < > ();
                productAssocData.put("productId", partNumber);
                productAssocData.put("productIdTo", partNumberTo);
                productAssocData.put("fromDate", formattedDateTime);
                productAssocData.put("productAssocTypeId", "PRODUCT_SUBSTITUTE");
                productAssocData.put("userLogin", userLogin);
                productAssocData.put("reason", typeCode);

                // Check if the product associated with partNumberTo exists
                boolean productExists = checkProductExists(partNumberTo, delegator);

                // Create the product if it doesn't exist
                if (!productExists) {
                    Map < String, Object > productData = new HashMap < > ();
                    productData.put("productId", partNumberTo);
                    if (brandLabel != null) productData.put("brandName", brandLabel);
                    productData.put("internalName", "afterpart");
                    productData.put("userLogin", userLogin);
                    productData.put("productTypeId", "SUBASSEMBLY");

                    dispatcher.runSync("createProduct", productData);
                }

                // Check if the product association already exists
                boolean assocExists = checkProductAssocExists(partNumber, partNumberTo, typeCode, delegator);

                // Create the product association if it doesn't exist
                if (!assocExists) {
                    dispatcher.runSync("createProductAssoc", productAssocData);
                }
            } catch (GenericServiceException e) {
                Debug.log("\n\n\nGSerror" + e);
            }
        });
    }

    // Method to process Descriptions of Product
    public static void processDescriptionData(List < Map < String, String >> descriptionContentData, LocalDispatcher dispatcher, Delegator delegator, String partNumber, GenericValue userLogin) {
        int batchSize = 10;

        // Lists to hold data for insertion
        List < GenericValue > contentsToInsert = new ArrayList < > ();
        List < GenericValue > productContentsToInsert = new ArrayList < > ();

        // Loop through the list of descriptionContentData
        IntStream.range(0, descriptionContentData.size()).forEach(i -> {
            Map < String,
                    String > descriptionData = descriptionContentData.get(i);
            String contentName = descriptionData.get("DescriptionCode");
            String localeString = descriptionData.get("LanguageCode");
            String description = descriptionData.get("Text");

            // Check if the product description exists
            String dataResourceIdIfExists = checkProductDescriptionExists(partNumber, contentName, delegator);

            if (dataResourceIdIfExists != null) {
                // Update the existing description
                try {
                    Map < String, Object > updateDataResourceContext = new HashMap < > ();
                    updateDataResourceContext.put("textData", description);
                    updateDataResourceContext.put("dataResourceId", dataResourceIdIfExists);
                    updateDataResourceContext.put("userLogin", userLogin);

                    dispatcher.runSync("updateElectronicText", updateDataResourceContext);
                } catch (GenericServiceException e) {
                    Debug.log("\n\n\nGSerror" + e);
                }
            } else {
                // Create a new description
                try {
                    Map < String, Object > createDataResourceContext = new HashMap < > ();
                    createDataResourceContext.put("textData", description);
                    createDataResourceContext.put("userLogin", userLogin);

                    Map < String, Object > result = dispatcher.runSync("createElectronicText", createDataResourceContext);

                    // Create a Content entity
                    GenericValue content = delegator.makeValue("Content");
                    content.set("contentName", contentName);
                    content.set("contentId", delegator.getNextSeqId("Content"));
                    content.set("dataResourceId", result.get("dataResourceId"));
                    content.set("localeString", localeString);
                    contentsToInsert.add(content);

                    // Create a ProductContent entity
                    Map < String, Object > productContentData = new HashMap < > ();
                    productContentData.put("productId", partNumber);
                    productContentData.put("productContentTypeId", "DESCRIPTION");
                    productContentData.put("contentId", content.get("contentId"));
                    productContentData.put("fromDate", getFormatedCurrentDateTime());

                    GenericValue productContent = delegator.makeValue("ProductContent", productContentData);
                    productContentsToInsert.add(productContent);

                } catch (Exception e) {
                    Debug.log("\n\n\nError processing description data: " + e);
                }
            }

            // Batch processing: Insert records when batchSize is reached or end of descriptionContentData
            if (i % batchSize == (batchSize - 1) || i == descriptionContentData.size() - 1) {
                try {
                    delegator.storeAll(contentsToInsert);
                    delegator.storeAll(productContentsToInsert);
                } catch (Exception e) {
                    Debug.log("Error inserting records: " + e.getMessage());
                }

                // Clear lists for the next batch
                contentsToInsert.clear();
                productContentsToInsert.clear();
            }
        });
    }

    // Method to process digital assets
    public static void processDigitalAssets(List < Map < String, String >> digitalFiles, LocalDispatcher dispatcher, Delegator delegator, String partNumber, GenericValue userLogin) {

        // Batch size
        int batchSize = 30;

        // Lists to hold data for insertion
        List < GenericValue > dataResourcesToInsert = new ArrayList < > ();
        List < GenericValue > contentsToInsert = new ArrayList < > ();
        List < GenericValue > productContentsToInsert = new ArrayList < > ();
        List < GenericValue > dataResourceAttributesToInsert = new ArrayList < > ();

        // Loop through the list of digitalFiles
        IntStream.range(0, digitalFiles.size()).forEach(i -> {
            Map < String,
                    String > digitalFile = digitalFiles.get(i);

            // Extract data from the digitalFile map
            String fileName = digitalFile.get("FileName");
            String assetType = digitalFile.get("AssetType");
            String fileType = digitalFile.get("FileType");
            String representation = digitalFile.get("Representation");
            String background = digitalFile.get("Background");
            String uri = digitalFile.get("URI");
            String filePath = digitalFile.get("FilePath");
            String fileSize = digitalFile.get("FileSize");
            String assetId = digitalFile.get("AssetId");
            String resolution = digitalFile.get("Resolution");

            try {
                // Create and populate a DataResource entity
                GenericValue dataResource = delegator.makeValue("DataResource");
                dataResource.set("dataResourceName", fileName);
                dataResource.set("objectInfo", uri);
                dataResource.set("dataResourceTypeId", "URL_RESOURCE");
                dataResource.set("dataResourceId", delegator.getNextSeqId("DataResource"));

                // Determine mimeTypeId based on fileType
                if (fileType != null) {
                    if (fileType.equalsIgnoreCase("jpg") || fileType.equalsIgnoreCase("jpeg")) {
                        dataResource.set("mimeTypeId", "image/jpeg");
                    } else if (fileType.equalsIgnoreCase("pdf")) {
                        dataResource.set("mimeTypeId", "application/pdf");
                    } else if (fileType.equalsIgnoreCase("mp4")) {
                        dataResource.set("mimeTypeId", "video/mp4");
                    }
                }

                // Add DataResource entity to the list for insertion
                dataResourcesToInsert.add(dataResource);

                // Create and populate a Content entity
                GenericValue content = delegator.makeValue("Content");
                content.set("contentName", fileName);
                content.set("description", filePath);
                content.set("contentId", delegator.getNextSeqId("Content"));
                content.set("dataResourceId", dataResource.get("dataResourceId"));
                contentsToInsert.add(content);

                // Create and populate a ProductContent entity
                GenericValue productContent = delegator.makeValue("ProductContent");
                productContent.set("productId", partNumber);
                productContent.set("contentId", content.get("contentId"));
                productContent.set("fromDate", getFormatedCurrentDateTime());
                productContent.set("productContentTypeId", (fileType.equalsIgnoreCase("jpg") || fileType.equalsIgnoreCase("jpeg")) ? "Image" : "DIGITAL_DOWNLOAD");
                productContentsToInsert.add(productContent);

                // List of attribute names to process
                List < String > attributeNames = Arrays.asList("AssetType", "Representation", "Background", "AssetId", "Resolution", "FileSize");
                // Iterate through attributeNames and create DataResourceAttribute entities
                attributeNames.forEach(attributeName -> {
                    String attributeValue = digitalFile.get(attributeName);
                    if (attributeValue != null) {
                        try {
                            GenericValue dataResourceAttribute = delegator.makeValue("DataResourceAttribute");
                            dataResourceAttribute.set("dataResourceId", dataResource.get("dataResourceId"));
                            dataResourceAttribute.set("attrName", attributeName);
                            dataResourceAttribute.set("attrValue", attributeValue);
                            dataResourceAttributesToInsert.add(dataResourceAttribute);
                        } catch (Exception e) {
                            Debug.log("Error creating DataResourceAttribute: " + e.getMessage());
                        }
                    }
                });

                // Batch processing: Insert records when batchSize is reached or end of digitalFiles
                if (i % batchSize == (batchSize - 1) || i == digitalFiles.size() - 1) {
                    try {
                        delegator.storeAll(dataResourcesToInsert);
                        delegator.storeAll(contentsToInsert);
                        delegator.storeAll(productContentsToInsert);
                        delegator.storeAll(dataResourceAttributesToInsert);
                    } catch (Exception e) {
                        Debug.log("Error inserting records: " + e.getMessage());
                    }

                    // Clear lists for the next batch
                    dataResourcesToInsert.clear();
                    contentsToInsert.clear();
                    productContentsToInsert.clear();
                    dataResourceAttributesToInsert.clear();
                }
            } catch (Exception e) {
                Debug.log("Error processing digital file: " + e.getMessage());
            }
        });
    }

}