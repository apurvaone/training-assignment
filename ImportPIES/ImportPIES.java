// Importing necessary packages
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
import javax.xml.stream.*;
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
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.entity.util.EntityUtil;
import org.apache.ofbiz.entity.util.EntityUtilProperties;
import org.apache.ofbiz.product.catalog.CatalogWorker;
import org.apache.ofbiz.product.category.CategoryWorker;
import org.apache.ofbiz.product.image.ScaleImage;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ModelService;
import org.apache.ofbiz.service.ServiceUtil;
import org.jdom.JDOMException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

public final class ImportPIES {

    // Service to Import PIES XML data into OFBiz
    public static Map < String, Object > parseData(DispatchContext dctx, Map < String, ? extends Object > context) {

        // Get necessary context objects
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        try {

            // Create an ExecutorService with a thread pool of 8 threads
            ExecutorService executorService = Executors.newFixedThreadPool(8);

            // Initialize XML parsing from the "TargetFile.xml" file
            InputStream inputStream = new FileInputStream("TargetFile.xml");
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStream);

            //boolean flags to keep track of various parsing states and data
            boolean insideItem = false;
            boolean insideDescriptions = false;
            boolean insideProductAttributes = false;
            boolean insidePackages = false;
            boolean insideDimensions = false;
            boolean insideWeights = false;
            boolean insideExtendedInformation = false;
            boolean insideDigitalAssets = false;
            boolean insideDigitalFileInformation = false;
            boolean partNumberFound = false;
            boolean brandLabelFound = false;
            boolean brandAAIAIDFound = false;
            boolean insidePartInterchangeInfo = false;
            boolean insidePartInterchange = false;

            // Set of attributes for quick lookup of some attributes
            String productData[] = {
                    "ContainerType",
                    "AvailableDate",
                    "ItemEffectiveDate",
                    "ItemQuantitySize"
            };
            HashSet < String > productDataSet = new HashSet < > (Arrays.asList(productData));

            // Map to store Item data for Product entity
            Map < String, String > itemData = new HashMap < > ();

            // List to store Descriptions data
            List < Map < String, String >> descriptionContentData = new ArrayList < > ();

            // List to store Extended Product informations data
            List < Map < String, String >> extendedInfoData = new ArrayList < > ();

            // List to store Product Attributes Data
            List < List < String >> productAttributeData = new ArrayList < > ();

            // Variable to store list of digitalfiles of particular item
            List < Map < String, String >> digitalFiles = new ArrayList < > ();

            // Variable  to store current Part Interchange Data
            HashMap < String, String > currentPartInterchange = new HashMap < > ();

            // Map to store current Part Interchange Data
            HashMap < String, String > currentDigitalFile = new HashMap < > ();

            // List to Store partInterchanges of particular item
            List < Map < String, String >> partInterchanges = new ArrayList < > ();

            // Other variables for storing parsed data
            String productBrandAAIAID = null;
            String itemLevelGTIN = null;
            String partTerminologyId = null;
            String AAIAProductCategoryCode = null;
            String dimensionUOM = null;
            String weightsUOM = null;

            //Starting the Parser
            try {
                while (xmlStreamReader.hasNext()) {
                    try {
                        int eventType = xmlStreamReader.next();

                        switch (eventType) {
                            case XMLStreamConstants.START_ELEMENT:
                                String localName = xmlStreamReader.getLocalName();

                                // Start parsing different XML elements
                                switch (localName) {
                                    case "PIES":
                                        Debug.log("Start Parsing PIES");
                                        break;

                                    case "Item":
                                        // Initialize data structures for current item
                                        itemData = new HashMap < > ();
                                        descriptionContentData = new ArrayList < > ();
                                        extendedInfoData = new ArrayList < > ();
                                        productAttributeData = new ArrayList < > ();
                                        digitalFiles = new ArrayList < > ();
                                        currentPartInterchange = new HashMap < > ();
                                        currentDigitalFile = new HashMap < > ();
                                        partInterchanges = new ArrayList < > ();
                                        productBrandAAIAID = null;
                                        itemLevelGTIN = null;
                                        partTerminologyId = null;
                                        AAIAProductCategoryCode = null;
                                        dimensionUOM = null;
                                        weightsUOM = null;
                                        insideItem = true;

                                        break;

                                    // Parsing Descriptions
                                    case "Descriptions":
                                        insideDescriptions = true;
                                        descriptionContentData.clear();
                                        break;
                                    case "Description":
                                        if (insideDescriptions) {
                                            parseDescriptions(delegator, dispatcher, descriptionContentData, xmlStreamReader);
                                        }
                                        break;

                                    // Parsing Extended Product Information
                                    case "ExtendedInformation":
                                        insideExtendedInformation = true;
                                        break;
                                    case "ExtendedProductInformation":
                                        if (insideExtendedInformation) {
                                            parseExtendedProducts(delegator, dispatcher, extendedInfoData, xmlStreamReader);
                                        }
                                        break;

                                    // Parsing ProductAttributes
                                    case "ProductAttributes":
                                        insideProductAttributes = true;
                                        productAttributeData.clear();
                                        break;
                                    case "ProductAttribute":
                                        if (insideProductAttributes) {
                                            String attrId = xmlStreamReader.getAttributeValue(null, "AttributeID");
                                            switch (attrId) {
                                                // Parsing attributes, to be added in Product entity
                                                case "Alliance Product Name":
                                                    itemData.put("Alliance Product Name", xmlStreamReader.getElementText());
                                                    break;
                                                case "aca_dwStdPartDesc":
                                                    itemData.put("aca_dwStdPartDesc", xmlStreamReader.getElementText());
                                                    break;
                                                case "aca_DefaultThumbnail":
                                                    itemData.put("aca_DefaultThumbnail", xmlStreamReader.getElementText());
                                                    break;
                                                case "aca_DefaultImage":
                                                    itemData.put("aca_DefaultImage", xmlStreamReader.getElementText());
                                                    break;

                                                // Parsing other attributes, to be added as Product Attributes
                                                default:
                                                    List < String > currentProductAttribute = new ArrayList < > ();
                                                    currentProductAttribute.add(attrId);
                                                    currentProductAttribute.add(xmlStreamReader.getElementText());
                                                    productAttributeData.add(currentProductAttribute);
                                                    break;
                                            }
                                        }
                                        break;

                                    // Parsing Packages
                                    case "Packages":
                                        // Start parsing Packages element
                                        insidePackages = true;
                                        break;
                                    case "Dimensions":
                                        if (insidePackages) {
                                            // Start parsing Dimensions element within Packages
                                            insideDimensions = true;
                                            dimensionUOM = xmlStreamReader.getAttributeValue(null, "UOM");
                                        }
                                        break;
                                    case "Weights":
                                        if (insidePackages) {
                                            // Start parsing Weights element within Packages
                                            insideWeights = true;
                                            weightsUOM = xmlStreamReader.getAttributeValue(null, "UOM");
                                        }
                                        break;
                                    case "Height":
                                        if (insideDimensions) {
                                            // Parsing Height element within Dimensions
                                            String heightValue = xmlStreamReader.getElementText();
                                            itemData.put("productHeight", heightValue);
                                            itemData.put("heightUomId", dimensionUOM);
                                        }
                                        break;
                                    case "Width":
                                        if (insideDimensions) {
                                            // Parsing Width element within Dimensions
                                            String widthValue = xmlStreamReader.getElementText();
                                            itemData.put("productWidth", widthValue);
                                            itemData.put("widthUomId", dimensionUOM);
                                        }
                                        break;
                                    case "Length":
                                        if (insideDimensions) {
                                            // Parsing Length element within Dimensions
                                            String lengthValue = xmlStreamReader.getElementText();
                                            itemData.put("productDepth", lengthValue);
                                            itemData.put("heightUomId", dimensionUOM);
                                        }
                                        break;
                                    case "Weight":
                                        if (insideWeights) {
                                            // Parsing Weight element within Weights
                                            String weightValue = xmlStreamReader.getElementText();
                                            itemData.put("productWeight", weightValue);
                                            itemData.put("weightUomId", weightsUOM);
                                        }
                                        break;

                                    // Parsing PartInterchange Information
                                    case "PartInterchangeInfo":
                                        // Start parsing PartInterchangeInfo element
                                        insidePartInterchangeInfo = true;
                                        break;
                                    case "PartInterchange":
                                        // Start parsing PartInterchange element
                                        insidePartInterchange = true;
                                        currentPartInterchange = new HashMap < > ();
                                        break;
                                    case "TypeCode":
                                        if (insidePartInterchange) {
                                            // Parsing TypeCode element within PartInterchange
                                            String typeCode = xmlStreamReader.getElementText();
                                            currentPartInterchange.put("TypeCode", typeCode);
                                        }
                                        break;

                                    case "PartNumber":
                                        if (insidePartInterchange) {
                                            // Parsing PartNumber element within PartInterchange
                                            String partNumber = xmlStreamReader.getElementText();
                                            currentPartInterchange.put("PartNumber", partNumber);
                                        } else if (insideItem) {
                                            // Parsing PartNumber element within Item (root)
                                            itemData.put("PartNumber", xmlStreamReader.getElementText());
                                            partNumberFound = true;
                                        }
                                        break;

                                    case "BrandAAIAID":
                                        if (insidePartInterchange) {
                                            // Parsing BrandAAIAID element within PartInterchange
                                            String brandAAIAID = xmlStreamReader.getElementText();
                                            currentPartInterchange.put("BrandAAIAID", brandAAIAID);
                                        } else {
                                            // Parsing BrandAAIAID element outside PartInterchange
                                            brandAAIAIDFound = true;
                                            productBrandAAIAID = xmlStreamReader.getElementText();
                                        }
                                        break;
                                    case "BrandLabel":
                                        if (insidePartInterchange) {
                                            // Parsing BrandLabel element within PartInterchange
                                            String brandLabel = xmlStreamReader.getElementText();
                                            currentPartInterchange.put("BrandLabel", brandLabel);
                                        } else if (insideItem) {
                                            // Parsing BrandLabel element within Item (root)
                                            itemData.put("BrandLabel", xmlStreamReader.getElementText());
                                            brandLabelFound = true;
                                        }
                                        break;

                                    // Parsing Digital Assets data
                                    case "DigitalAssets":
                                        // Start parsing DigitalAssets element
                                        digitalFiles.clear();
                                        insideDigitalAssets = true;
                                        digitalFiles = new ArrayList < > ();
                                        break;
                                    case "DigitalFileInformation":
                                        // Start parsing DigitalFileInformation element
                                        insideDigitalFileInformation = true;
                                        currentDigitalFile = new HashMap < > ();
                                        break;
                                    case "AssetType":
                                        // Parsing AssetType element
                                        String assetType = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("AssetType", assetType);
                                        break;
                                    case "AssetID":
                                        // Parsing AssetID element
                                        String assetId = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("AssetId", assetId);
                                        break;
                                    case "Resolution":
                                        // Parsing Resolution element
                                        String resolution = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("Resolution", resolution);
                                        break;
                                    case "FileSize":
                                        // Parsing FileSize element
                                        String fileSize = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("FileSize", fileSize);
                                        break;
                                    case "FileName":
                                        // Parsing FileName element
                                        String fileName = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("FileName", fileName);
                                        break;
                                    case "FileType":
                                        // Parsing FileType element
                                        String fileType = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("FileType", fileType);
                                        break;
                                    case "Representation":
                                        // Parsing Representation element
                                        String representation = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("Representation", representation);
                                        break;
                                    case "ColorMode":
                                        // Parsing ColorMode element
                                        String colorMode = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("ColorMode", colorMode);
                                        break;
                                    case "Background":
                                        // Parsing Background element
                                        String background = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("Background", background);
                                        break;
                                    case "URI":
                                        // Parsing URI element
                                        String uri = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("URI", uri);
                                        break;
                                    case "FilePath":
                                        // Parsing FilePath element
                                        String filePath = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("FilePath", filePath);
                                        break;

                                    // Parsing ItemLevelGTIN
                                    case "ItemLevelGTIN":
                                        itemLevelGTIN = xmlStreamReader.getElementText();
                                        break;

                                    // Parsing PartTerminologyID
                                    case "PartTerminologyID":
                                        partTerminologyId = xmlStreamReader.getElementText();
                                        break;

                                    // Parsing other Attributes for product entity
                                    default:
                                        if (insideItem && productDataSet.contains(localName)) {
                                            itemData.put(localName, xmlStreamReader.getElementText());
                                        }
                                        break;
                                }
                                break;

                            case XMLStreamConstants.END_ELEMENT:
                                String endLocalName = xmlStreamReader.getLocalName();

                                // End parsing different XML elements
                                switch (endLocalName) {
                                    case "PIES":
                                        // End of the PIES element
                                        Debug.log("End Parsing PIES");
                                        break;
                                    case "Descriptions":
                                        // End of the Descriptions element
                                        insideDescriptions = false;
                                        break;
                                    case "ProductAttributes":
                                        // End of the ProductAttributes element
                                        insideProductAttributes = false;
                                        break;
                                    case "ExtendedInformation":
                                        // End of the ExtendedInformation element
                                        insideExtendedInformation = false;
                                        break;
                                    case "PartInterchangeInfo":
                                        // End of the PartInterchangeInfo element
                                        insidePartInterchangeInfo = false;
                                        break;
                                    case "PartInterchange":
                                        // End of the PartInterchange element
                                        insidePartInterchange = false;
                                        partInterchanges.add(currentPartInterchange);
                                        break;
                                    case "DigitalAssets":
                                        // End of the DigitalAssets element
                                        insideDigitalAssets = false;
                                        break;
                                    case "DigitalFileInformation":
                                        // End of the DigitalFileInformation element
                                        insideDigitalFileInformation = false;
                                        digitalFiles.add(currentDigitalFile);
                                        break;
                                    case "Packages":
                                        // End of the Packages element
                                        insidePackages = false;
                                        break;
                                    case "Dimensions":
                                        // End of the Dimensions element
                                        insideDimensions = false;
                                        break;
                                    case "Weights":
                                        // End of the Weights element
                                        insideWeights = false;
                                        break;
                                    case "Item":
                                        // End of the Item element

                                        // Retrieve data for concurrent processing
                                        final String finalItemLevelGTIN = itemLevelGTIN;
                                        final String finalPartTerminologyId = partTerminologyId;
                                        final String finalproductBrandAAIAID = productBrandAAIAID;
                                        final List < Map < String, String >> finalDigitalFiles = digitalFiles;
                                        final Map < String, String > finalItemData = itemData;
                                        final List < Map < String, String >> finalPartInterchanges = partInterchanges;
                                        final List < Map < String, String >> finalDescriptionContentData = descriptionContentData;
                                        final List < Map < String, String >> finalExtendedInfoData = extendedInfoData;
                                        final List < List < String >> finalProductAttributeData = productAttributeData;

                                        // Submit tasks for concurrent processing using executorService with the help of processing methods defined in PIESHelper class
                                        Future < ? > processItemDataTask = executorService.submit(() -> PIESHelper.processItemData(finalItemData, dispatcher, delegator, userLogin));

                                        // Wait till the Product is created
                                        processItemDataTask.get();

                                        // Submit other tasks for concurrent processing
                                        Future < ? > processDigitalAssetsTask = executorService.submit(() -> PIESHelper.processDigitalAssets(finalDigitalFiles, dispatcher, delegator, finalItemData.get("PartNumber"), userLogin));
                                        Future < ? > processGTINsTask = executorService.submit(() -> PIESHelper.processGTINs(finalItemLevelGTIN, dispatcher, delegator, finalItemData.get("PartNumber"), userLogin));
                                        Future < ? > processCategoriesTask = executorService.submit(() -> PIESHelper.processCategories(finalPartTerminologyId, dispatcher, delegator, finalItemData.get("PartNumber"), userLogin));
                                        Future < ? > processProductAttributesTask = executorService.submit(() -> PIESHelper.processProductAttributes(finalProductAttributeData, dispatcher, delegator, finalItemData.get("PartNumber"), finalproductBrandAAIAID, finalItemLevelGTIN, userLogin));
                                        Future < ? > processDescriptionDataTask = executorService.submit(() -> PIESHelper.processDescriptionData(finalDescriptionContentData, dispatcher, delegator, finalItemData.get("PartNumber"), userLogin));
                                        Future < ? > processPartInterchangeDataTask = executorService.submit(() -> PIESHelper.processPartInterchangeData(finalPartInterchanges, dispatcher, delegator, finalItemData.get("PartNumber"), userLogin));
                                        Future < ? > processExtendedProductDataTask = executorService.submit(() -> PIESHelper.processExtendedProductInfo(finalExtendedInfoData, dispatcher, delegator, finalItemData.get("PartNumber"), userLogin));

                                        // Reset flags
                                        partNumberFound = false;
                                        brandLabelFound = false;
                                        brandAAIAIDFound = false;

                                        break;
                                }
                                break;
                        }} catch (Exception e) {
                            // Handle exceptions and log errors
                            Debug.log("Error: " + e);
                        }

                    }

                    // closing the resources
                    executorService.shutdown();
                    xmlStreamReader.close();
                    inputStream.close();

                    // Returning the rservice result
                    Map < String, Object > result = ServiceUtil.returnSuccess("Data Imported");
                    return result;

                } catch (XMLStreamException e) {
                    e.printStackTrace();
                    return ServiceUtil.returnError("An error occurred during data processing.");

                } catch (Exception e) {
                    e.printStackTrace();
                    return ServiceUtil.returnError("An error occurred during data processing.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return ServiceUtil.returnError("An error occurred during data processing.");
            }
        }

        //Method to parse Descriptions of product
        public static void parseDescriptions(Delegator delegator, LocalDispatcher dispatcher, List < Map < String, String >> descriptionContentData, XMLStreamReader xmlStreamReader) throws XMLStreamException {

            Map < String, String > currentDescription = new HashMap < > ();
            currentDescription.put("DescriptionCode", xmlStreamReader.getAttributeValue(null, "DescriptionCode"));
            currentDescription.put("LanguageCode", xmlStreamReader.getAttributeValue(null, "LanguageCode"));
            currentDescription.put("Text", xmlStreamReader.getElementText());

            descriptionContentData.add(currentDescription);

        }

        //Method to parse ExtendedProducts information
        public static void parseExtendedProducts(Delegator delegator, LocalDispatcher dispatcher, List < Map < String, String >> extendedInfoData, XMLStreamReader xmlStreamReader) throws XMLStreamException {

            Map < String, String > currentInfo = new HashMap < > ();
            currentInfo.put("EXPICode", xmlStreamReader.getAttributeValue(null, "EXPICode"));
            currentInfo.put("LanguageCode", xmlStreamReader.getAttributeValue(null, "LanguageCode"));
            currentInfo.put("Text", xmlStreamReader.getElementText());

            extendedInfoData.add(currentInfo);

        }

    }