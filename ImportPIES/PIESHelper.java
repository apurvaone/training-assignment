import java.math.BigDecimal;
import java.util.HashMap;
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
import javax.xml.stream. * ;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util. * ;
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

// Helper class to facilitate Utility and IO operations of PIES data import
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

    // Method to check if a specific type of good identification exists for a product
    public static boolean checkUOMIdExists(String uomId,
                                                        Delegator delegator) {
        GenericValue genericValue;
        boolean uomIdExists = false;
        try {
            genericValue = EntityQuery.use(delegator).from("Uom")
                    .where("uomId", uomId)
                    .queryOne();
            if (genericValue != null ) {
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
        String descriptionContentId = null;
        try {
            genericValue = EntityQuery.use(delegator).from("ProductContentAndInfo")
                    .where("productId", productId, "contentName", descriptionCode)
                    .queryOne();
            if (genericValue != null && productId.equals(genericValue.getString("productId"))) {
                descriptionContentId = genericValue.getString("contentId");
            }
        } catch (GenericEntityException e) {
            Debug.logError("Problem in reading data of product descriptions", MODULE);
        }
        return descriptionContentId;
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
                    .where("fileName", fileName)
                    .queryOne();
            if (genericValue != null) {
                dataResourceId = genericValue.getString("dataResourceId");
            }
        } catch (GenericEntityException e) {
            Debug.logError("Problem in reading data of product data resource", MODULE);
        }
        return dataResourceId;
    }


    // Method to process Part data for Product entity
    public static void processItemData(Map < String, String > itemData, LocalDispatcher dispatcher, Delegator delegator, GenericValue userLogin) {

        // Initialize a contextMap to store the parameters
        Map < String, Object > productEntityMap = new HashMap < >();


        // Getting the parsed data for Product
        String partNumber = itemData.get("PartNumber");
        String brandLabel = itemData.get("BrandLabel");
        String itemEffectiveDate = itemData.get("ItemEffectiveDate");
        String availableDate = itemData.get("AvailableDate");
        String containerType = itemData.get("ContainerType");
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

            boolean heightUomCheck,
                    weightUomCheck;

            // Check if UOM exists using utility method
            heightUomCheck =  checkUOMIdExists(heightUomId,delegator);
            weightUomCheck =  checkUOMIdExists(weightUomId,delegator);


            // Check if height/length/width UOM type exists, if not create one
            if (! heightUomCheck ) {

                Map < String,
                        Object > uomContext = new HashMap < >();

                uomContext.put("uomId", heightUomId);
                uomContext.put("userLogin", userLogin);

                dispatcher.runSync("createUom", uomContext);
            }

            // Check if weight UOM type exists, if not create one
            if (!weightUomCheck ) {
                Map < String,
                        Object > uomContext = new HashMap < >();

                uomContext.put("uomId", weightUomId);
                uomContext.put("userLogin", userLogin);
                dispatcher.runSync("createUom", uomContext);
            }


            // adding the data into contextMap to be passed
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
            if(productExists)
            {
                dispatcher.runSync("updateProduct", productEntityMap);
            }
            else{
                dispatcher.runSync("createProduct", productEntityMap);
            }

        }
        // Catching the exceptions
        catch(GenericServiceException e) {
            Debug.log("\n\n\nGSerror" + e);
        }  catch(Exception e) {
            Debug.log("\n\n\nException " + e);
        }

    }


    //Method to process GTIN of the product
    public static void processGTINs(String itemLevelGTIN, LocalDispatcher dispatcher, Delegator delegator, String partNumber, GenericValue userLogin) {


        // Initialize a contextMaps to store the parameters
        Map <String,Object> sContext = new HashMap < >();
        Map <String,Object> typeContext = new HashMap < >();

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
            boolean goodIdentificationExists = checkGoodIdentificationExists(partNumber,"GTIN", delegator);
            if(!goodIdentificationExists)
                dispatcher.runSync("createGoodIdentification", sContext);

        } catch(GenericServiceException e) {
            Debug.log("\n\n\nGSerror" + e);
        } catch(GenericEntityException e) {
            Debug.log("\n\n\nGEerror" + e);
        }
    }


    //Method to process product attributes of the Part
    public static void processProductAttributes(List < List < String >> productAttributeData, LocalDispatcher dispatcher, Delegator delegator,String partNumber, String productBrandAAIAD, String itemLevelGTIN,  GenericValue userLogin) {

        // Initialize a contextMap to store the parameters
        Map < String,
                Object > productAttributeContext = new HashMap < >();

        // Convert itemLevelGTIN into packageLevelGTIN by adding starting zeroes, to make it 14 digit
        String packageLevelGTIN = String.format("%1$" + 14 + "s", itemLevelGTIN).replace(' ', '0');

        // Add brandAAIA, packageLevelGTIN, packageBarCodeCharacters into productAttributeData map
        List brandAAAIDData = new ArrayList();
        brandAAAIDData.add("BrandAAIAD");
        brandAAAIDData.add(productBrandAAIAD);
        List packageLevelGTINData = new ArrayList();
        packageLevelGTINData.add("packageLevelGTIN");
        packageLevelGTINData.add(packageLevelGTIN);
        List packageBarCodeCharacters = new ArrayList();
        packageBarCodeCharacters.add("packageBarCodeCharacters");
        packageBarCodeCharacters.add(itemLevelGTIN);

        productAttributeData.add(brandAAAIDData);
        productAttributeData.add(packageLevelGTINData);
        productAttributeData.add(packageBarCodeCharacters);

        // Iterating through each Product Attribute
        for (int i = 0; i < productAttributeData.size(); i++) {

            System.out.println(productAttributeData.get(i).get(0) + " " + productAttributeData.get(i).get(1));
            String attributeName = productAttributeData.get(i).get(0);
            String attributeValue = productAttributeData.get(i).get(1);

            try {

                // Adding the parameters into contextMap
                productAttributeContext.put("attrName", attributeName);
                productAttributeContext.put("userLogin", userLogin);
                productAttributeContext.put("attrValue", attributeValue);
                productAttributeContext.put("productId", partNumber);

                // Check if productAttributeExists
                boolean productAttributeExists = checkProductAttributeExists(partNumber,attributeName, delegator);

                // if productAttribute Exists, then update it else create a new ProductAttribute
                if(!productAttributeExists)
                    dispatcher.runSync("createProductAttribute", productAttributeContext);
                else{
                    dispatcher.runSync("updateProductAttribute", productAttributeContext);
                }


            } catch(GenericServiceException e) {
                Debug.log("\n\n\nGSerror" + e);
            }
        }

    }


    //Method to process ExtendedProductInfo of the Part
    public static void processExtendedProductInfo(List < List < String >> extendedInfoData, LocalDispatcher dispatcher, Delegator delegator, String partNumber,  GenericValue userLogin) {

        // Initialize a contextMap to store the parameters
        Map < String,Object > sContext = new HashMap < >();


        // Iterating through each Extendend Product Information
        for (int i = 0; i < extendedInfoData.size(); i++) {

            String contentName = extendedInfoData.get(i).get(0);
            String localeString = extendedInfoData.get(i).get(1);
            String description = extendedInfoData.get(i).get(2);

            try {
                // adding the params into contextMap
                sContext.put("productId", partNumber);
                sContext.put("userLogin", userLogin);
                sContext.put("productContentTypeId", "DESCRIPTION");
                sContext.put("text", description);
                sContext.put("contentName", contentName);
                dispatcher.runSync("createSimpleTextContentForProduct", sContext);

            } catch(GenericServiceException e) {
                Debug.log("\n\n\nGSerror" + e);
            }
        }

    }

    //Method to process Part Interchanges of the Part
    public static void processPartInterchangeData(List < Map < String, String >> partInterchanges, LocalDispatcher dispatcher, Delegator delegator, String partNumber, GenericValue userLogin) {


        // Initialize contextMaps to store the parameters
        Map < String,
                Object > productAssocData = new HashMap < >();
        Map < String,
                Object > productData = new HashMap < >();

        // Iterate through each PartInterchange Information
        for (int i = 0; i < partInterchanges.size(); i++) {

            String partNumberTo = partInterchanges.get(i).get("PartNumber");
            String typeCode = partInterchanges.get(i).get("TypeCode");
            String brandAAIAID = partInterchanges.get(i).get("BrandAAIAID");
            String brandLabel = partInterchanges.get(i).get("BrandLabel");

            try {

                // Get current data time in the required format from utility service
                String formattedDateTime = getFormatedCurrentDateTime();

                // Adding the data into contextMap
                productAssocData.put("productId", partNumber);
                productAssocData.put("productIdTo", partNumberTo);
                productAssocData.put("fromDate", formattedDateTime);
                productAssocData.put("productAssocTypeId", "PRODUCT_SUBSTITUTE");
                productAssocData.put("userLogin", userLogin);
                productAssocData.put("reason", typeCode);


                // Check if Product of given partNumber(Interchange) exists
                GenericValue dummyProduct = EntityQuery.use(delegator).from("Product").where("productId", partNumberTo).queryOne();

                // If it doesnt exist,create a Product of given partNumber (Interchange)
                if (dummyProduct == null) {

                    productData.put("productId", partNumberTo);
                    if (brandLabel != null) productData.put("brandName", brandLabel);
                    productData.put("internalName", "afterpart");
                    productData.put("userLogin", userLogin);
                    productData.put("productTypeId", "SUBASSEMBLY");

                    dispatcher.runSync("createProduct", productData);

                }

                // create Product Association
                dispatcher.runSync("createProductAssoc", productAssocData);

            } catch(GenericServiceException e) {
                Debug.log("\n\n\nGSerror" + e);
            } catch(GenericEntityException e) {
                Debug.log("\n\n\nGEerror" + e);
            }
        }
    }

    //Method to process Descriptions of the Part
    public static void processDescriptionData(List < List < String >> descriptionContentData, LocalDispatcher dispatcher, Delegator delegator, String partNumber, GenericValue userLogin) {

        // Initialize contextMaps to store the parameters
        Map <String,Object> electronicTextEntityData = new HashMap < >();
        Map <String,Object> contentEntityData = new HashMap < >();
        Map <String,Object> productContentEntity = new HashMap < >();
        Map <String,Object> sContext = new HashMap < >();


        // Iterate through each Description Information
        for (int i = 0; i < descriptionContentData.size(); i++) {

            String contentName = descriptionContentData.get(i).get(0);
            String localeString = descriptionContentData.get(i).get(1);
            String description = descriptionContentData.get(i).get(2);

            try {

                // Adding the data into contextMap
                sContext.put("productId", partNumber);
                sContext.put("userLogin", userLogin);
                sContext.put("contentName", contentName);
                sContext.put("productContentTypeId", "DESCRIPTION");
                sContext.put("text", description);

                // Service call to insert data
                dispatcher.runSync("createSimpleTextContentForProduct", sContext);

            } catch(GenericServiceException e) {
                Debug.log("\n\n\nGSerror" + e);
            }
        }

    }


    //Method to process Digital Assets of the Part
    public static void processDigitalAssets(List < Map < String, String >> digitalFiles, LocalDispatcher dispatcher, Delegator delegator, String partNumber, GenericValue userLogin) {

        // Initialize contextMaps to store the parameters
        Map < String,
                Object > dataResourceData = new HashMap < >();

        Map < String,
                Object > contentEntityData = new HashMap < >();
        Map < String,
                Object > productContentEntity = new HashMap < >();


        // Iterate through each Digital File Information
        for (int i = 0; i < digitalFiles.size(); i++) {

            String fileName = digitalFiles.get(i).get("FileName");
            String assetType = digitalFiles.get(i).get("AssetType");
            String fileType = digitalFiles.get(i).get("FileType");
            String representation = digitalFiles.get(i).get("Representation");
            String background = digitalFiles.get(i).get("Background");
            String uri = digitalFiles.get(i).get("URI");
            String filePath = digitalFiles.get(i).get("FilePath");
            String fileSize = digitalFiles.get(i).get("FileSize");
            String assetId = digitalFiles.get(i).get("AssetId");
            String resolution = digitalFiles.get(i).get("Resolution");

            try {

            // Creating The DataResource of type "URL_RESOURCE" for each digital file

                // Adding the data into contextMap
                dataResourceData.put("dataResourceName", fileName);
                dataResourceData.put("objectInfo", uri);
                dataResourceData.put("dataResourceTypeId", "URL_RESOURCE");
                dataResourceData.put("userLogin", userLogin);
                dataResourceData.put("roleTypeId", "OWNER");

                // Adding the different mimeTypeIds for different type of DigitalFile in the contextMap
                if (fileType != null) {
                    if (fileType.equalsIgnoreCase("jpg") || fileType.equalsIgnoreCase("jpeg")) {
                        dataResourceData.put("mimeTypeId", "image/jpeg");
                    } else if (fileType.equalsIgnoreCase("pdf")) {
                        dataResourceData.put("mimeTypeId", "application/pdf");
                    } else if (fileType.equalsIgnoreCase("mp4")) {
                        dataResourceData.put("mimeTypeId", "video/mp4");
                    }
                }

                Object dataResourceId= null;
                String dataResourceIdIfExists= checkDataResourceExists(fileName,delegator);

                if(dataResourceIdIfExists==null) {
                    Map<String,
                            Object> dataResourceServiceResult = dispatcher.runSync("createDataResource", dataResourceData);
                    dataResourceId = dataResourceServiceResult.get("dataResourceId");
                }else{
                    dataResourceId= dataResourceIdIfExists;
                }

                // Adding the assetType into dataResourceAttribute if it exists
                if (assetType != null) {
                    Map < String,
                            Object > dataResourceAttribute = new HashMap < >();
                    dataResourceAttribute.put("dataResourceId", dataResourceId);
                    dataResourceAttribute.put("attrName", "assetType");
                    dataResourceAttribute.put("attrValue", assetType);
                    dataResourceAttribute.put("userLogin", userLogin);

                    dispatcher.runSync("createDataResourceAttribute", dataResourceAttribute);
                }

                // Adding the representation into dataResourceAttribute if it exists
                if (representation != null) {
                    Map < String,
                            Object > dataResourceAttribute = new HashMap < >();
                    dataResourceAttribute.put("dataResourceId", dataResourceId);
                    dataResourceAttribute.put("attrName", "representation");
                    dataResourceAttribute.put("attrValue", representation);
                    dataResourceAttribute.put("userLogin", userLogin);

                    dispatcher.runSync("createDataResourceAttribute", dataResourceAttribute);

                }

                // Adding the background into dataResourceAttribute if it exists
                if (background != null) {
                    Map < String,
                            Object > dataResourceAttribute = new HashMap < >();
                    dataResourceAttribute.put("dataResourceId", dataResourceId);
                    dataResourceAttribute.put("attrName", "background");
                    dataResourceAttribute.put("attrValue", background);
                    dataResourceAttribute.put("userLogin", userLogin);

                    dispatcher.runSync("createDataResourceAttribute", dataResourceAttribute);

                }

                // Adding the assetId into dataResourceAttribute if it exists
                if (assetId != null) {
                    Map < String,
                            Object > dataResourceAttribute = new HashMap < >();
                    dataResourceAttribute.put("dataResourceId", dataResourceId);
                    dataResourceAttribute.put("attrName", "assetId");
                    dataResourceAttribute.put("attrValue", assetId);
                    dataResourceAttribute.put("userLogin", userLogin);

                    dispatcher.runSync("createDataResourceAttribute", dataResourceAttribute);

                }

                // Adding the resolution into dataResourceAttribute if it exists
                if (resolution != null) {
                    Map < String,
                            Object > dataResourceAttribute = new HashMap < >();
                    dataResourceAttribute.put("dataResourceId", dataResourceId);
                    dataResourceAttribute.put("attrName", "resolution");
                    dataResourceAttribute.put("attrValue", resolution);
                    dataResourceAttribute.put("userLogin", userLogin);

                    dispatcher.runSync("createDataResourceAttribute", dataResourceAttribute);

                }

                // Adding the fileSize into dataResourceAttribute if it exists
                if (fileSize != null) {
                    Map < String,
                            Object > dataResourceAttribute = new HashMap < >();
                    dataResourceAttribute.put("dataResourceId", dataResourceId);
                    dataResourceAttribute.put("attrName", "fileSize");
                    dataResourceAttribute.put("attrValue", fileSize);

                    dataResourceAttribute.put("userLogin", userLogin);
                    dispatcher.runSync("createDataResourceAttribute", dataResourceAttribute);

                }

                // Creating Content context map, for creating Content related to created data resource
                contentEntityData.put("dataResourceId", dataResourceId);
                contentEntityData.put("userLogin", userLogin);
                contentEntityData.put("contentName", fileName);
                contentEntityData.put("description", filePath);
                Map < String,Object > contentServiceResult = dispatcher.runSync("createContent", contentEntityData);


                // Creating ProductContent, to create association with content and product
                productContentEntity.put("contentId", contentServiceResult.get("contentId"));
                productContentEntity.put("productId", partNumber);
                productContentEntity.put("productContentTypeId", "Image");
                productContentEntity.put("userLogin", userLogin);

                dispatcher.runSync("createProductContent", productContentEntity);

            } catch(GenericServiceException e) {
                Debug.log("\n\n\nGSerror" + e);
            }
        }

    }



}