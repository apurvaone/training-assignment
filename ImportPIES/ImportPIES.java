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
    public static Map < String, Object > parseData(DispatchContext dctx, Map < String, ?extends Object > context) {

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
            HashSet productDataSet = new HashSet < >(Arrays.asList(productData));


            // Variables to keep track of various parsing states and data
            Map < String,String > itemData = new HashMap < >();
            List<List<String>> descriptionContentData = new ArrayList < >();
            List<List<String>> extendedInfoData = new ArrayList < >();
            List<List<String>> productAttributeData = new ArrayList < >();
            List<Map<String,String>> digitalFiles = new ArrayList < >();
            HashMap <String,String> currentPartInterchange = new HashMap < >();
            HashMap <String,String> currentDigitalFile = new HashMap < >();
            List <Map<String,String>> partInterchanges = new ArrayList < >();
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
                                if ("PIES".equals(localName)) {
                                    System.out.println("Start Parsing PIES");
                                } else if ("Item".equals(localName)) {
                                    insideItem = true;
                                // Parsing Description data
                                } else if ("Descriptions".equals(localName)) {
                                    insideDescriptions = true;
                                    descriptionContentData.clear();
                                    descriptionContentData = new ArrayList();
                                } else if ("Description".equals(localName) && insideDescriptions) {
                                    parseDescriptions(delegator, dispatcher, descriptionContentData, xmlStreamReader);
                                }

                                // Parsing Extended Product data
                                else if ("ExtendedInformation".equals(localName)) {
                                    insideExtendedInformation = true;
                                }
                                else if ("ExtendedProductInformation".equals(localName) && insideExtendedInformation) {
                                    parseExtendedProducts(delegator, dispatcher, extendedInfoData, xmlStreamReader);
                                }

                                // Parsing Product Attributes data
                                else if ("ProductAttributes".equals(localName)) {
                                    insideProductAttributes = true;
                                    productAttributeData.clear();
                                    productAttributeData = new ArrayList();
                                }else if ("ProductAttribute".equals(localName) && insideProductAttributes) {

                                    if (xmlStreamReader.getAttributeValue(0).equals("Alliance Product Name")) {
                                        itemData.put("Alliance Product Name", xmlStreamReader.getElementText());
                                    } else if (xmlStreamReader.getAttributeValue(0).equals("aca_dwStdPartDesc")) {
                                        itemData.put("aca_dwStdPartDesc", xmlStreamReader.getElementText());
                                    } else if (xmlStreamReader.getAttributeValue(0).equals("aca_DefaultThumbnail")) {
                                        itemData.put("aca_DefaultThumbnail", xmlStreamReader.getElementText());
                                    } else if (xmlStreamReader.getAttributeValue(0).equals("aca_DefaultImage")) {
                                        itemData.put("aca_DefaultImage", xmlStreamReader.getElementText());
                                    } else {


                                        List currentProductAttribute = new ArrayList();
                                        currentProductAttribute.add(xmlStreamReader.getAttributeValue(0));
                                        currentProductAttribute.add(xmlStreamReader.getElementText());
                                        productAttributeData.add(currentProductAttribute);
                                    }
                                }

                                // Parsing Package Data
                                else if ("Packages".equals(localName)) {
                                    insidePackages = true;
                                } else if ("Dimensions".equals(localName) && insidePackages) {
                                    insideDimensions = true;
                                    dimensionUOM = xmlStreamReader.getAttributeValue(0);
                                } else if ("Weights".equals(localName) && insidePackages) {
                                    insideWeights = true;
                                    weightsUOM = xmlStreamReader.getAttributeValue(0);

                                } else if (insideDimensions && "Height".equals(localName)) {
                                    String heightValue = xmlStreamReader.getElementText();
                                    itemData.put("productHeight", heightValue);
                                    itemData.put("heightUomId", dimensionUOM);

                                } else if (insideDimensions && "Width".equals(localName)) {
                                    String widthValue = xmlStreamReader.getElementText();
                                    itemData.put("productWidth", widthValue);
                                    itemData.put("widthUomId", dimensionUOM);

                                } else if (insideDimensions && "Length".equals(localName)) {
                                    String lengthValue = xmlStreamReader.getElementText();
                                    itemData.put("productDepth", lengthValue);
                                    itemData.put("heightUomId", dimensionUOM);

                                } else if (insideWeights && "Weight".equals(localName)) {
                                    String weightValue = xmlStreamReader.getElementText();
                                    itemData.put("productWeight", weightValue);
                                    itemData.put("weightUomId", weightsUOM);
                                }

                                // Parsing PartInterchange Information
                                else if ("PartInterchangeInfo".equals(localName)) {
                                    partInterchanges.clear();
                                    insidePartInterchangeInfo = true;
                                } else if ("PartInterchange".equals(localName)) {
                                    insidePartInterchange = true;
                                    currentPartInterchange = new HashMap < >();
                                } else if ("TypeCode".equals(localName) && insidePartInterchange) {
                                    String typeCode = xmlStreamReader.getElementText();
                                    currentPartInterchange.put("TypeCode", typeCode);
                                } else if ("BrandAAIAID".equals(localName) && insidePartInterchange) {
                                    String brandAAIAID = xmlStreamReader.getElementText();
                                    currentPartInterchange.put("BrandAAIAID", brandAAIAID);
                                } else if ("PartNumber".equals(localName) && insidePartInterchange) {
                                    String partNumber = xmlStreamReader.getElementText();
                                    currentPartInterchange.put("PartNumber", partNumber);
                                } else if ("BrandLabel".equals(localName) && insidePartInterchange) {
                                    String brandLabel = xmlStreamReader.getElementText();
                                    currentPartInterchange.put("BrandLabel", brandLabel);
                                }

                                // Parsing Digital Assets data
                                else if ("DigitalAssets".equals(localName)) {
                                    digitalFiles.clear();
                                    insideDigitalAssets = true;
                                    digitalFiles = new ArrayList < >();

                                } else if ("DigitalFileInformation".equals(localName)) {
                                    insideDigitalFileInformation = true;
                                    currentDigitalFile = new HashMap < >();
                                }
                                else if ("AssetType".equals(localName)) {
                                    String assetType = xmlStreamReader.getElementText();
                                    currentDigitalFile.put("AssetType", assetType);
                                } else if ("AssetID".equals(localName)) {
                                    String assetId = xmlStreamReader.getElementText();
                                    currentDigitalFile.put("AssetId", assetId);
                                } else if ("Resolution".equals(localName)) {
                                    String resolution = xmlStreamReader.getElementText();
                                    currentDigitalFile.put("Resolution", resolution);
                                } else if ("FileSize".equals(localName)) {
                                    String fileSize = xmlStreamReader.getElementText();
                                    currentDigitalFile.put("FileSize", fileSize);
                                } else if ("FileName".equals(localName)) {
                                    String fileName = xmlStreamReader.getElementText();
                                    currentDigitalFile.put("FileName", fileName);
                                } else if ("FileType".equals(localName)) {
                                    String fileType = xmlStreamReader.getElementText();
                                    currentDigitalFile.put("FileType", fileType);
                                } else if ("Representation".equals(localName)) {
                                    String representation = xmlStreamReader.getElementText();
                                    currentDigitalFile.put("Representation", representation);
                                } else if ("ColorMode".equals(localName)) {
                                    String colorMode = xmlStreamReader.getElementText();
                                    currentDigitalFile.put("ColorMode", colorMode);
                                } else if ("Background".equals(localName)) {
                                    String background = xmlStreamReader.getElementText();
                                    currentDigitalFile.put("Background", background);
                                } else if ("URI".equals(localName)) {
                                    String uri = xmlStreamReader.getElementText();
                                    currentDigitalFile.put("URI", uri);
                                } else if ("FilePath".equals(localName)) {
                                    String filePath = xmlStreamReader.getElementText();
                                    currentDigitalFile.put("FilePath", filePath);
                                }

                                // Parsing Product Data
                                else if (insideItem) {
                                    if (localName == "PartNumber" && !partNumberFound) {
                                        itemData.put("PartNumber", xmlStreamReader.getElementText());
                                        partNumberFound = true;
                                    }
                                    if (localName == "BrandLabel" && !brandLabelFound) {
                                        itemData.put("BrandLabel", xmlStreamReader.getElementText());
                                        brandLabelFound = true;
                                    } else if (localName == "BrandAAIAID" && !brandAAIAIDFound) {
                                        brandAAIAIDFound = true;
                                        productBrandAAIAID = xmlStreamReader.getElementText();
                                    } else if (localName == "ItemLevelGTIN") {
                                        itemLevelGTIN = xmlStreamReader.getElementText();
                                    }else if (localName == "PartTerminologyID") {
                                        partTerminologyId = xmlStreamReader.getElementText();
                                    }
                                    else if (productDataSet.contains(localName)) {
                                        itemData.put(localName, xmlStreamReader.getElementText());
                                    }
                                }
                                break;
                            case XMLStreamConstants.END_ELEMENT:

                                // End parsing different XML elements
                                if ("PIES".equals(xmlStreamReader.getLocalName())) {
                                    System.out.println("End Parsing PIES");
                                } else if ("Descriptions".equals(xmlStreamReader.getLocalName())) {
                                    insideDescriptions = false;

                                } else if ("ProductAttributes".equals(xmlStreamReader.getLocalName())) {
                                    insideProductAttributes = false;

                                } else if ("ExtendedInformation".equals(xmlStreamReader.getLocalName())) {
                                    insideExtendedInformation = false;

                                } else if ("PartInterchangeInfo".equals(xmlStreamReader.getLocalName())) {
                                    insidePartInterchangeInfo = false;

                                } else if ("PartInterchange".equals(xmlStreamReader.getLocalName())) {
                                    insidePartInterchange = false;
                                    partInterchanges.add(currentPartInterchange);

                                } else if ("DigitalAssets".equals(xmlStreamReader.getLocalName())) {
                                    insideDigitalAssets = false;

                                } else if ("DigitalFileInformation".equals(xmlStreamReader.getLocalName())) {
                                    insideDigitalFileInformation = false;
                                    digitalFiles.add(currentDigitalFile);

                                } else if ("Packages".equals(xmlStreamReader.getLocalName())) {
                                    insidePackages = false;

                                } else if ("Dimensions".equals(xmlStreamReader.getLocalName())) {
                                    insideDimensions = false;
                                } else if ("Weights".equals(xmlStreamReader.getLocalName())) {
                                    insideWeights = false;
                                } else if ("Item".equals(xmlStreamReader.getLocalName())) {
                                    insideItem = false;

                                    // Calling helper methods to process and import the parsed data
                                    PIESHelper.processItemData(itemData, dispatcher, delegator, userLogin);
                                    PIESHelper.processGTINs(itemLevelGTIN, dispatcher, delegator, itemData.get("PartNumber"),userLogin);
                                    PIESHelper.processCategories(partTerminologyId, dispatcher, delegator, itemData.get("PartNumber"),userLogin);
                                    PIESHelper.processProductAttributes(productAttributeData, dispatcher, delegator, itemData.get("PartNumber"), productBrandAAIAID, itemLevelGTIN,userLogin);
                                    PIESHelper.processDescriptionData(descriptionContentData, dispatcher, delegator, itemData.get("PartNumber"),userLogin);
                                    PIESHelper.processPartInterchangeData(partInterchanges, dispatcher, delegator, itemData.get("PartNumber"),userLogin);
                                    PIESHelper.processExtendedProductInfo(extendedInfoData, dispatcher, delegator, itemData.get("PartNumber"),userLogin);
                                    PIESHelper.processDigitalAssets(digitalFiles, dispatcher, delegator, itemData.get("PartNumber"),userLogin);

                                    partNumberFound = false;
                                    brandLabelFound = false;
                                    brandAAIAIDFound = false;


                                }

                                break;
                        }
                    } catch(Exception e) {

                    }

                }

                xmlStreamReader.close();
                inputStream.close();

            } catch(XMLStreamException e) {
                e.printStackTrace();
            } catch(Exception e) {
                e.printStackTrace();
            }
            Map < String, Object > result = ServiceUtil.returnSuccess("Data Imported");
            return result;

        } catch(Exception e) {
            e.printStackTrace();
            return ServiceUtil.returnError("An error occurred during data processing.");
        } finally {}
    }


    //Method to parse Descriptions of product
    public static void parseDescriptions(Delegator delegator, LocalDispatcher dispatcher, List < List < String >> descriptionContentData, XMLStreamReader xmlStreamReader) throws XMLStreamException {

        List currentDescription = new ArrayList();
        currentDescription.add(xmlStreamReader.getAttributeValue(0));
        currentDescription.add(xmlStreamReader.getAttributeValue(1));
        currentDescription.add(xmlStreamReader.getElementText());
        descriptionContentData.add(currentDescription);

    }


    //Method to parse ExtendedProducts information
    public static void parseExtendedProducts(Delegator delegator, LocalDispatcher dispatcher, List < List < String >> extendedInfoData, XMLStreamReader xmlStreamReader) throws XMLStreamException {

        List currentInfo = new ArrayList();

        currentInfo.add(xmlStreamReader.getAttributeValue(0));
        currentInfo.add(xmlStreamReader.getAttributeValue(1));
        currentInfo.add(xmlStreamReader.getElementText());

        extendedInfoData.add(currentInfo);

    }

}