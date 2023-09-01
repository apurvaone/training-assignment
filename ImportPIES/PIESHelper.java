// Importing necessary packages
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.imaging.ImageReadException;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.base.util.UtilGenerics;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.base.util.string.FlexibleStringExpander;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityJoinOperator;
import org.apache.ofbiz.entity.condition.EntityOperator;

/*
 Approach 1:

 Helper class to facilitate Utility and IO operations of PIES data import

 This class contains different utility methods and methods to process particular type of data
 and import it into database.

 The processing methods uses inbuilt services to Add/Update data as per the requirement
*/

public final class PIESHelper {

    private static final String MODULE = PIESHelper.class.getName();

    // Method to check if a product exists in the database
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

    // Utility method to create UOM
    private static void createUOM(String uomId, GenericValue userLogin, LocalDispatcher dispatcher) throws GenericServiceException {
        Map < String, Object > uomContext = new HashMap < > ();
        uomContext.put("uomId", uomId);
        uomContext.put("userLogin", userLogin);
        dispatcher.runSync("createUom", uomContext);
    }

    // Method to process Part data for Product entity using Stream API
    public static void processItemData(Map < String, String > itemData, LocalDispatcher dispatcher, Delegator delegator, GenericValue userLogin) {
        // Extract data from the itemData map
        String partNumber = itemData.get("PartNumber");
        String brandLabel = itemData.get("BrandLabel");
        String quantityIncluded = itemData.get("ItemQuantitySize");
        String productName = itemData.get("Alliance Product Name");
        String description = itemData.get("aca_dwStdPartDesc");
        String smallImage = itemData.get("aca_DefaultThumbnail");
        String detailImage = itemData.get("aca_DefaultImage");
        String productLength = itemData.get("productLength");
        String productHeight = itemData.get("productHeight");
        String productWidth = itemData.get("productWidth");
        String productWeight = itemData.get("productWeight");
        String heightUomId = itemData.get("heightUomId");
        String weightUomId = itemData.get("weightUomId");
        String depthUomId = itemData.get("depthUomId");
        String widthUomId = itemData.get("widthUomId");

        try {
            // Check if UOM exists using utility method for height and weight
            boolean heightUomCheck = checkUOMIdExists(heightUomId, delegator);
            boolean weightUomCheck = checkUOMIdExists(weightUomId, delegator);

            // Create UOM if it doesn't exist for height
            if (!heightUomCheck) {
                createUOM(heightUomId, userLogin, dispatcher);
            }

            // Create UOM if it doesn't exist for weight
            if (!weightUomCheck) {
                createUOM(weightUomId, userLogin, dispatcher);
            }

            // Create a map to store product entity data
            Map < String, Object > productEntityMap = new HashMap < > ();
            productEntityMap.put("productId", partNumber);
            productEntityMap.put("brandName", brandLabel);
            productEntityMap.put("internalName", "Fuel Injectors");
            productEntityMap.put("productTypeId", "SUBASSEMBLY");
            productEntityMap.put("quantityIncluded", quantityIncluded);
            productEntityMap.put("productName", productName);
            productEntityMap.put("description", description);
            productEntityMap.put("smallImageUrl", smallImage);
            productEntityMap.put("detailImageUrl", detailImage);
            productEntityMap.put("productLength", productLength);
            productEntityMap.put("productHeight", productHeight);
            productEntityMap.put("productWidth", productWidth);
            productEntityMap.put("productWeight", productWeight);
            productEntityMap.put("heightUomId", heightUomId);
            productEntityMap.put("weightUomId", weightUomId);
            productEntityMap.put("depthUomId", depthUomId);
            productEntityMap.put("widthUomId", widthUomId);
            productEntityMap.put("userLogin", userLogin);

            // Checking if product exists
            boolean productExists = checkProductExists(partNumber, delegator);

            // If exists update it, if not create product
            if (productExists) {
                dispatcher.runSync("updateProduct", productEntityMap);
            } else {
                dispatcher.runSync("createProduct", productEntityMap);
            }

        } catch (GenericServiceException e) {
            Debug.log("\n\n\nGSerror" + e);
        } catch (Exception e) {
            Debug.log("\n\n\nException " + e);
        }
    }

    // Method to process GTIN of the product using Stream API
    public static void processGTINs(String itemLevelGTIN, LocalDispatcher dispatcher, Delegator delegator, String partNumber, GenericValue userLogin) {
        // Initialize context maps to store the parameters
        Map < String, Object > sContext = new HashMap < > ();
        Map < String, Object > typeContext = new HashMap < > ();

        try {
            // Retrieve the TypeGtin using EntityQuery
            GenericValue TypeGtin = EntityQuery.use(delegator).from("GoodIdentificationType").where("goodIdentificationTypeId", "GTIN").queryOne();

            // Check if TypeGtin exists, if not create one
            if (TypeGtin == null) {
                typeContext.put("goodIdentificationTypeId", "GTIN");
                typeContext.put("description", "Global Trade Identification Number");
                typeContext.put("userLogin", userLogin);

                dispatcher.runSync("createGoodIdentificationType", typeContext);
            }

            // Adding the data into sContext
            sContext.put("goodIdentificationTypeId", "GTIN");
            sContext.put("idValue", itemLevelGTIN);
            sContext.put("productId", partNumber);
            sContext.put("userLogin", userLogin);

            // Check if goodIdentification exists, create only if id does not exist
            boolean goodIdentificationExists = checkGoodIdentificationExists(partNumber, "GTIN", delegator);
            if (!goodIdentificationExists) {
                dispatcher.runSync("createGoodIdentification", sContext);
            }

        } catch (GenericServiceException e) {
            Debug.log("Error in service: " + e.getMessage());
        } catch (GenericEntityException e) {
            Debug.log("Error in Entity operation" + e);
        }
    }

    // Method to process Categories of the product (partTerminologyId here) using Stream API
    public static void processCategories(String partTerminologyId, LocalDispatcher dispatcher, Delegator delegator, String partNumber, GenericValue userLogin) {
        // Initialize context maps to store the parameters
        Map < String, Object > categoryContext = new HashMap < > ();
        Map < String, Object > categoryMemberContext = new HashMap < > ();

        try {
            // Retrieve the partTerminologyCatag using EntityQuery
            GenericValue partTerminologyCatag = EntityQuery.use(delegator).from("ProductCategory").where("productCategoryId", partTerminologyId).queryOne();

            // Check if partTerminologyCatag exists, if not create one
            if (partTerminologyCatag == null) {
                categoryContext.put("productCategoryId", partTerminologyId);
                categoryContext.put("productCategoryTypeId", "INTERNAL_CATEGORY");
                categoryContext.put("categoryName", "Part Terminology Category");
                categoryContext.put("userLogin", userLogin);

                dispatcher.runSync("createProductCategory", categoryContext);
            }

            // Adding the data into categoryMemberContext
            categoryMemberContext.put("productId", partNumber);
            categoryMemberContext.put("productCategoryId", partTerminologyId);
            categoryMemberContext.put("userLogin", userLogin);

            boolean categoryMemberExists = categoryMemberExists(partNumber, partTerminologyId, delegator);

            // If category member does not exist, add the product to the category
            if (!categoryMemberExists) {
                dispatcher.runSync("addProductToCategory", categoryMemberContext);
            }

        } catch (GenericServiceException e) {
            Debug.log("Error in service: " + e.getMessage());
        } catch (GenericEntityException e) {
            Debug.log("Error in Entity operation" + e);
        }
    }

    //Method to process product attributes of the Part
    public static void processProductAttributes(List < List < String >> productAttributeData, LocalDispatcher dispatcher, Delegator delegator, String partNumber, String productBrandAAIAD, String itemLevelGTIN, GenericValue userLogin) {
        // Convert itemLevelGTIN into packageLevelGTIN by adding starting zeroes, to make it 14 digit
        String packageLevelGTIN = String.format("%1$" + 14 + "s", itemLevelGTIN).replace(' ', '0');

        // Create lists with attribute data and add them to the productAttributeData list
        List < List < String >> attributeData = new ArrayList < > ();
        attributeData.add(Arrays.asList("BrandAAIAD", productBrandAAIAD));
        attributeData.add(Arrays.asList("packageLevelGTIN", packageLevelGTIN));
        attributeData.add(Arrays.asList("packageBarCodeCharacters", itemLevelGTIN));
        productAttributeData.addAll(attributeData);

        // Using Stream API to iterate through each Product Attribute
        productAttributeData.stream().forEach(attributeEntry -> {
            // Extract attribute name and value from the current entry
            String attributeName = attributeEntry.get(0);
            String attributeValue = attributeEntry.get(1);

            try {
                // Create a map to store parameters for creating/updating product attribute
                Map < String, Object > productAttributeContext = new HashMap < > ();
                productAttributeContext.put("attrName", attributeName);
                productAttributeContext.put("userLogin", userLogin);
                productAttributeContext.put("attrValue", attributeValue);
                productAttributeContext.put("productId", partNumber);

                // Check if productAttributeExists
                boolean productAttributeExists = checkProductAttributeExists(partNumber, attributeName, delegator);

                // Dispatch the appropriate service based on whether the attribute exists or not
                if (!productAttributeExists) {
                    dispatcher.runSync("createProductAttribute", productAttributeContext);
                } else {
                    dispatcher.runSync("updateProductAttribute", productAttributeContext);
                }

            } catch (GenericServiceException e) {
                Debug.log("Error in service: " + e.getMessage());
            }
        });
    }

    // Method to process ExtendedProductInfo of the Part using Stream API
    public static void processExtendedProductInfo(List < Map < String, String >> extendedInfoData, LocalDispatcher dispatcher, Delegator delegator, String partNumber, GenericValue userLogin) {
        // Using Stream API to iterate through each Extended Product Information
        extendedInfoData.stream().forEach(extendedInfo -> {
            // Extract information from the current Extended Product Information
            String contentName = extendedInfo.get("EXPICode");
            String localeString = extendedInfo.get("LanguageCode");
            String description = extendedInfo.get("Text");

            try {
                // Create a map to store parameters for creating simple text content
                Map < String, Object > sContext = new HashMap < > ();
                sContext.put("productId", partNumber);
                sContext.put("userLogin", userLogin);
                sContext.put("productContentTypeId", "DESCRIPTION");
                sContext.put("text", description);
                sContext.put("contentName", contentName);

                // Dispatch a service to create simple text content for the product
                dispatcher.runSync("createSimpleTextContentForProduct", sContext);

            } catch (GenericServiceException e) {
                Debug.log("Error in service: " + e.getMessage());
            }
        });
    }

    //Method to process Part Interchanges of the Part
    public static void processPartInterchangeData(List < Map < String, String >> partInterchanges, LocalDispatcher dispatcher, Delegator delegator, String partNumber, GenericValue userLogin) {
        // Iterate through each PartInterchange Information using Stream API
        partInterchanges.stream().forEach(partInterchange -> {
            // Extract information from the current PartInterchange
            String partNumberTo = partInterchange.get("PartNumber");
            String typeCode = partInterchange.get("TypeCode");
            String brandAAIAID = partInterchange.get("BrandAAIAID");
            String brandLabel = partInterchange.get("BrandLabel");

            try {
                // Get current data time in the required format from utility service
                String formattedDateTime = getFormatedCurrentDateTime();

                // Create a map to store product association data
                Map < String, Object > productAssocData = new HashMap < > ();
                productAssocData.put("productId", partNumber);
                productAssocData.put("productIdTo", partNumberTo);
                productAssocData.put("fromDate", formattedDateTime);
                productAssocData.put("productAssocTypeId", "PRODUCT_SUBSTITUTE");
                productAssocData.put("userLogin", userLogin);
                productAssocData.put("reason", typeCode);

                // Check if the product of given partNumber (Interchange) exists
                boolean productExists = checkProductExists(partNumberTo, delegator);

                // If the product doesn't exist, create a new product
                if (!productExists) {
                    Map < String, Object > productData = new HashMap < > ();
                    productData.put("productId", partNumberTo);
                    if (brandLabel != null) productData.put("brandName", brandLabel);
                    productData.put("internalName", "afterpart");
                    productData.put("userLogin", userLogin);
                    productData.put("productTypeId", "SUBASSEMBLY");

                    // Dispatch a service to create the product
                    dispatcher.runSync("createProduct", productData);
                }

                // Check if the product association exists
                boolean assocExists = checkProductAssocExists(partNumber, partNumberTo, typeCode, delegator);

                // If the association doesn't exist, create a new product association
                if (!assocExists)
                    dispatcher.runSync("createProductAssoc", productAssocData);

            } catch (GenericServiceException e) {
                Debug.log("Error in service: " + e.getMessage());
            }
        });
    }

    // Process Descriptions data
    public static void processDescriptionData(List < Map < String, String >> descriptionContentData, LocalDispatcher dispatcher, Delegator delegator, String partNumber, GenericValue userLogin) {
        descriptionContentData.stream().forEach(descriptionData -> {
            // Extract data from the descriptionContentData map
            String contentName = descriptionData.get("DescriptionCode");
            String localeString = descriptionData.get("LanguageCode");
            String description = descriptionData.get("Text");

            // Check if the description with the description code exists
            String dataResourceIdIfExists = checkProductDescriptionExists(partNumber, contentName, delegator);

            try {
                if (dataResourceIdIfExists != null) {
                    // Update existing electronic text data
                    Map < String, Object > updateDataResourceContext = new HashMap < > ();
                    updateDataResourceContext.put("textData", description);
                    updateDataResourceContext.put("dataResourceId", dataResourceIdIfExists);
                    updateDataResourceContext.put("userLogin", userLogin);
                    Object dataResourceServiceResult = dispatcher.runSync("updateElectronicText", updateDataResourceContext);
                } else {
                    // Create new simple text content for the product
                    Map < String, Object > sContext = new HashMap < > ();
                    sContext.put("productId", partNumber);
                    sContext.put("userLogin", userLogin);
                    sContext.put("contentName", contentName);
                    sContext.put("productContentTypeId", "DESCRIPTION");
                    sContext.put("text", description);
                    sContext.put("localeString", localeString);

                    dispatcher.runSync("createSimpleTextContentForProduct", sContext);
                }
            } catch (GenericServiceException e) {
                Debug.log("Error in service: " + e.getMessage());
            }
        });
    }

    // Method to process Digital Assets
    public static void processDigitalAssets(List < Map < String, String >> digitalFiles, LocalDispatcher dispatcher, Delegator delegator, String partNumber, GenericValue userLogin) {
        // Iterate through each digital file in the list
        digitalFiles.stream().forEach(digitalFile -> {
            // Create maps to store data for DataResource, ContentEntity, and ProductContentEntity
            Map < String,
                    Object > dataResourceContext = new HashMap < > ();
            Map < String,
                    Object > contentContext = new HashMap < > ();
            Map < String,
                    Object > productContentContext = new HashMap < > ();

            // Extract relevant information from the digitalFile map
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
                // Creating DataResource with specific properties
                dataResourceContext.put("dataResourceName", fileName);
                dataResourceContext.put("objectInfo", uri);
                dataResourceContext.put("dataResourceTypeId", "URL_RESOURCE");
                dataResourceContext.put("userLogin", userLogin);
                dataResourceContext.put("roleTypeId", "OWNER");

                // Mapping file types to mime types
                if (fileType != null) {
                    Map < String, String > fileTypeToMimeType = new HashMap < > ();
                    fileTypeToMimeType.put("jpg", "image/jpeg");
                    fileTypeToMimeType.put("jpeg", "image/jpeg");
                    fileTypeToMimeType.put("pdf", "application/pdf");
                    fileTypeToMimeType.put("mp4", "video/mp4");

                    String mimeType = fileTypeToMimeType.getOrDefault(fileType.toLowerCase(), "");
                    dataResourceContext.put("mimeTypeId", mimeType);
                }

                // Check if a DataResource with the given file name exists
                String dataResourceIdIfExists = checkDataResourceExists(fileName, delegator);
                Object dataResourceId;

                // If DataResource exists, update it; otherwise, create a new one
                if (dataResourceIdIfExists == null) {
                    Map < String, Object > dataResourceServiceResult = dispatcher.runSync("createDataResource", dataResourceContext);
                    dataResourceId = dataResourceServiceResult.get("dataResourceId");
                } else {
                    dataResourceId = dataResourceIdIfExists;
                    dataResourceContext.put("dataResourceId", dataResourceId);
                    dispatcher.runSync("updateDataResource", dataResourceContext);
                }

                // Create or update DataResourceAttributes based on attributeMap
                Map < String, String > attributeMap = new HashMap < > ();
                attributeMap.put("assetType", assetType);
                attributeMap.put("representation", representation);
                attributeMap.put("background", background);
                attributeMap.put("assetId", assetId);
                attributeMap.put("resolution", resolution);
                attributeMap.put("fileSize", fileSize);

                attributeMap.forEach((attrName, attrValue) -> {
                    if (attrValue != null) {
                        Map < String, Object > dataResourceAttribute = new HashMap < > ();
                        dataResourceAttribute.put("dataResourceId", dataResourceId);
                        dataResourceAttribute.put("attrName", attrName);
                        dataResourceAttribute.put("attrValue", attrValue);
                        dataResourceAttribute.put("userLogin", userLogin);

                        // Check and create/update DataResourceAttribute
                        boolean dataResourceAttributeExists = checkDataResourceAttributeExists(dataResourceId, attrName, delegator);

                        try {

                            if (dataResourceAttributeExists) {
                                dispatcher.runSync("updateDataResourceAttribute", dataResourceAttribute);
                            } else {
                                dispatcher.runSync("createDataResourceAttribute", dataResourceAttribute);
                            }
                        } catch (GenericServiceException e) {
                            Debug.log("Error in service: " + e.getMessage());
                        }
                    }
                });

                // Check if a contentId exists for the current DataResource and Product
                String contentIdIfProductContentExists = checkProductContentExists(partNumber, dataResourceId, delegator);

                // If contentId doesn't exist, create Content and ProductContent
                if (contentIdIfProductContentExists == null) {
                    // Create Content related to DataResource
                    contentContext.put("dataResourceId", dataResourceId);
                    contentContext.put("userLogin", userLogin);
                    contentContext.put("contentName", fileName);
                    contentContext.put("description", filePath);
                    Map < String, Object > contentServiceResult = dispatcher.runSync("createContent", contentContext);

                    // Create ProductContent association with Content and Product
                    productContentContext.put("contentId", contentServiceResult.get("contentId"));
                    productContentContext.put("productId", partNumber);
                    productContentContext.put("productContentTypeId",
                            fileType.equalsIgnoreCase("jpg") || fileType.equalsIgnoreCase("jpeg") ? "Image" : "DIGITAL_DOWNLOAD");
                    productContentContext.put("userLogin", userLogin);

                    dispatcher.runSync("createProductContent", productContentContext);
                }
            } catch (GenericServiceException e) {
                Debug.log("Error in service: " + e.getMessage());
            }
        });
    }

}