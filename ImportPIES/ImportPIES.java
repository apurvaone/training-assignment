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

            // Initialize XML parsing
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

            // Set of attrbutes for quick lookup
            String productData[] = {
                    "ContainerType",
                    "AvailableDate",
                    "ItemEffectiveDate",
                    "ItemQuantitySize"
            };
            HashSet < String > productDataSet = new HashSet < > (Arrays.asList(productData));

            // Variables to keep track of various parsing states and data
            Map < String, String > itemData = new HashMap < > ();
            List < Map < String, String >> descriptionContentData = new ArrayList < > ();
            List < Map < String, String >> extendedInfoData = new ArrayList < > ();
            List < List < String >> productAttributeData = new ArrayList < > ();
            List < Map < String, String >> digitalFiles = new ArrayList < > ();
            HashMap < String, String > currentPartInterchange = new HashMap < > ();
            HashMap < String, String > currentDigitalFile = new HashMap < > ();
            List < Map < String, String >> partInterchanges = new ArrayList < > ();
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
                                        System.out.println("Start Parsing PIES");
                                        break;
                                    case "Item":
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
                                        insidePackages = true;
                                        break;
                                    case "Dimensions":
                                        if (insidePackages) {
                                            insideDimensions = true;
                                            dimensionUOM = xmlStreamReader.getAttributeValue(null, "UOM");
                                        }
                                        break;
                                    case "Weights":
                                        if (insidePackages) {
                                            insideWeights = true;
                                            weightsUOM = xmlStreamReader.getAttributeValue(null, "UOM");
                                        }
                                        break;
                                    case "Height":
                                        if (insideDimensions) {
                                            String heightValue = xmlStreamReader.getElementText();
                                            itemData.put("productHeight", heightValue);
                                            itemData.put("heightUomId", dimensionUOM);
                                        }
                                        break;
                                    case "Width":
                                        if (insideDimensions) {
                                            String widthValue = xmlStreamReader.getElementText();
                                            itemData.put("productWidth", widthValue);
                                            itemData.put("widthUomId", dimensionUOM);
                                        }
                                        break;
                                    case "Length":
                                        if (insideDimensions) {
                                            String lengthValue = xmlStreamReader.getElementText();
                                            itemData.put("productDepth", lengthValue);
                                            itemData.put("heightUomId", dimensionUOM);
                                        }
                                        break;
                                    case "Weight":
                                        if (insideWeights) {
                                            String weightValue = xmlStreamReader.getElementText();
                                            itemData.put("productWeight", weightValue);
                                            itemData.put("weightUomId", weightsUOM);
                                        }
                                        break;

                                    // Parsing PartInterchange Information
                                    case "PartInterchangeInfo":
                                        partInterchanges.clear();
                                        insidePartInterchangeInfo = true;
                                        break;
                                    case "PartInterchange":
                                        insidePartInterchange = true;
                                        currentPartInterchange = new HashMap < > ();
                                        break;
                                    case "TypeCode":
                                        if (insidePartInterchange) {
                                            String typeCode = xmlStreamReader.getElementText();
                                            currentPartInterchange.put("TypeCode", typeCode);
                                        }
                                        break;

                                    case "PartNumber":
                                        if (insidePartInterchange) {
                                            String partNumber = xmlStreamReader.getElementText();
                                            currentPartInterchange.put("PartNumber", partNumber);
                                        } else if (insideItem) {
                                            itemData.put("PartNumber", xmlStreamReader.getElementText());
                                            partNumberFound = true;
                                        }
                                        break;

                                    case "BrandAAIAID":
                                        if (insidePartInterchange) {
                                            String brandAAIAID = xmlStreamReader.getElementText();
                                            currentPartInterchange.put("BrandAAIAID", brandAAIAID);
                                        } else {
                                            brandAAIAIDFound = true;
                                            productBrandAAIAID = xmlStreamReader.getElementText();
                                        }
                                        break;
                                    case "BrandLabel":
                                        if (insidePartInterchange) {
                                            String brandLabel = xmlStreamReader.getElementText();
                                            currentPartInterchange.put("BrandLabel", brandLabel);
                                        } else if (insideItem) {
                                            itemData.put("BrandLabel", xmlStreamReader.getElementText());
                                            brandLabelFound = true;
                                        }
                                        break;

                                    // Parsing Digital Assets data
                                    case "DigitalAssets":
                                        digitalFiles.clear();
                                        insideDigitalAssets = true;
                                        digitalFiles = new ArrayList < > ();
                                        break;
                                    case "DigitalFileInformation":
                                        insideDigitalFileInformation = true;
                                        currentDigitalFile = new HashMap < > ();
                                        break;
                                    case "AssetType":
                                        String assetType = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("AssetType", assetType);
                                        break;
                                    case "AssetID":
                                        String assetId = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("AssetId", assetId);
                                        break;
                                    case "Resolution":
                                        String resolution = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("Resolution", resolution);
                                        break;
                                    case "FileSize":
                                        String fileSize = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("FileSize", fileSize);
                                        break;
                                    case "FileName":
                                        String fileName = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("FileName", fileName);
                                        break;
                                    case "FileType":
                                        String fileType = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("FileType", fileType);
                                        break;
                                    case "Representation":
                                        String representation = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("Representation", representation);
                                        break;
                                    case "ColorMode":
                                        String colorMode = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("ColorMode", colorMode);
                                        break;
                                    case "Background":
                                        String background = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("Background", background);
                                        break;
                                    case "URI":
                                        String uri = xmlStreamReader.getElementText();
                                        currentDigitalFile.put("URI", uri);
                                        break;
                                    case "FilePath":
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
                                        System.out.println("End Parsing PIES");
                                        break;
                                    case "Descriptions":
                                        insideDescriptions = false;
                                        break;
                                    case "ProductAttributes":
                                        insideProductAttributes = false;
                                        break;
                                    case "ExtendedInformation":
                                        insideExtendedInformation = false;
                                        break;
                                    case "PartInterchangeInfo":
                                        insidePartInterchangeInfo = false;
                                        break;
                                    case "PartInterchange":
                                        insidePartInterchange = false;
                                        partInterchanges.add(currentPartInterchange);
                                        break;
                                    case "DigitalAssets":
                                        insideDigitalAssets = false;
                                        break;
                                    case "DigitalFileInformation":
                                        insideDigitalFileInformation = false;
                                        digitalFiles.add(currentDigitalFile);
                                        break;
                                    case "Packages":
                                        insidePackages = false;
                                        break;
                                    case "Dimensions":
                                        insideDimensions = false;
                                        break;
                                    case "Weights":
                                        insideWeights = false;
                                        break;
                                    case "Item":
                                        insideItem = false;
                                        // Calling helper methods to process and import the parsed data
                                        PIESHelper.processItemData(itemData, dispatcher, delegator, userLogin);
                                        PIESHelper.processGTINs(itemLevelGTIN, dispatcher, delegator, itemData.get("PartNumber"), userLogin);
                                        PIESHelper.processCategories(partTerminologyId, dispatcher, delegator, itemData.get("PartNumber"), userLogin);
                                        PIESHelper.processProductAttributes(productAttributeData, dispatcher, delegator, itemData.get("PartNumber"), productBrandAAIAID, itemLevelGTIN, userLogin);
                                        PIESHelper.processDescriptionData(descriptionContentData, dispatcher, delegator, itemData.get("PartNumber"), userLogin);
                                        PIESHelper.processPartInterchangeData(partInterchanges, dispatcher, delegator, itemData.get("PartNumber"), userLogin);
                                        PIESHelper.processExtendedProductInfo(extendedInfoData, dispatcher, delegator, itemData.get("PartNumber"), userLogin);
                                        PIESHelper.processDigitalAssets(digitalFiles, dispatcher, delegator, itemData.get("PartNumber"), userLogin);

                                        partNumberFound = false;
                                        brandLabelFound = false;
                                        brandAAIAIDFound = false;

                                        // Clearing the reference of used data after end parsing each item
                                        descriptionContentData.clear();
                                        extendedInfoData.clear();
                                        productAttributeData.clear();
                                        digitalFiles.clear();
                                        currentPartInterchange.clear();
                                        currentDigitalFile.clear();
                                        partInterchanges.clear();
                                        productBrandAAIAID = null;
                                        itemLevelGTIN = null;
                                        partTerminologyId = null;
                                        AAIAProductCategoryCode = null;
                                        dimensionUOM = null;
                                        weightsUOM = null;
                                        itemData.clear();
                                        break;
                                }
                                break;
                        }
                    } catch (Exception e) {

                    }

                }

                xmlStreamReader.close();
                inputStream.close();

            } catch (XMLStreamException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map < String, Object > result = ServiceUtil.returnSuccess("Data Imported");
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return ServiceUtil.returnError("An error occurred during data processing.");
        } finally {}
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