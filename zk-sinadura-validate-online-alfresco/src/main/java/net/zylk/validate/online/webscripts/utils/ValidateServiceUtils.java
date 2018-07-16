package net.zylk.validate.online.webscripts.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.esle.sinadura.core.certificate.CertificateUtil;
import net.esle.sinadura.core.exceptions.ValidationFatalException;
import net.esle.sinadura.core.exceptions.XadesValidationFatalException;
import net.esle.sinadura.core.interpreter.MessageInfo;
import net.esle.sinadura.core.interpreter.SignatureInfo;
import net.esle.sinadura.core.interpreter.ValidationInterpreterUtil;
import net.esle.sinadura.core.model.PDFSignatureInfo;
import net.esle.sinadura.core.model.Status;
import net.esle.sinadura.core.model.ValidationPreferences;
import net.esle.sinadura.core.model.XadesSignatureInfo;
import net.esle.sinadura.core.service.PdfService;
import net.esle.sinadura.core.service.XadesService;
import net.esle.sinadura.core.util.FileUtil;
import net.esle.sinadura.core.util.KeystoreUtil;
import net.esle.sinadura.core.util.LanguageUtil;
import net.esle.sinadura.core.util.PropertiesCoreUtil;
import net.esle.sinadura.core.xades.validator.XadesValidator;
import net.esle.sinadura.core.xades.validator.XadesValidatorFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.utils.URI;
import org.apache.xml.utils.URI.MalformedURIException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ValidateServiceUtils {

	private static Log logger = LogFactory.getLog(ValidateServiceUtils.class);
	
	private final static QName PROP_INFO_FIRMA = QName.createQName("{zylk.validate.model}info_firma");
	private final static QName ASPECT_VALIDABLE = QName.createQName("{zylk.validate.model}validable");
	
	private final static String MIMETYPE_SAR = "application/sinadura";
	
	private static KeyStore trustedKeystore = null;
	private static KeyStore cacheKeystore = null;

	public static KeyStore getTrustedKeystoreComplete(InputStream is, String password) {

		if (trustedKeystore == null) {
			trustedKeystore = loadKeystorePreferences(is, password);
		}
		return trustedKeystore;
	}

	public static KeyStore getCacheKeystoreComplete(InputStream is, String password) {

		if (cacheKeystore == null) {
			cacheKeystore = loadKeystorePreferences(is, password);
		}
		return cacheKeystore;
	}

	private static KeyStore loadKeystorePreferences(InputStream is, String password) {

		KeyStore ks = null;
		try {
			ks = KeystoreUtil.loadKeystorePreferences(is, password);

		} catch (KeyStoreException e) {
			logger.error(e);
			throw new WebScriptException(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e);
			throw new WebScriptException(e.getMessage(), e);
		} catch (CertificateException e) {
			logger.error(e);
			throw new WebScriptException(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e);
			throw new WebScriptException(e.getMessage(), e);
		}

		return ks;
	}

	public static void validateSign(ServiceRegistry registry, NodeRef nodeRef) {

		ResultSet results = null;
		
		try {

			registry.getNodeService().addAspect(nodeRef, ASPECT_VALIDABLE, null);

			results = registry.getSearchService().query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
					SearchService.LANGUAGE_XPATH, Constantes.CERTS_XPATH);
			NodeRef parentref = null;

			if (results != null && results.length() > 0) {
				parentref = results.getNodeRef(0);
			}

			NodeRef cacheKeystore = registry.getFileFolderService().searchSimple(parentref, Constantes.CACHE_KEYSTORE_NAME);
			NodeRef trustedKeystore = registry.getFileFolderService().searchSimple(parentref, Constantes.TRUSTED_KEYSTORE_NAME);
			String keyPassword = Constantes.VALIDATION_CERT_KEYPASSWORD;

			ContentReader cacheKeyReader = getReader(registry, cacheKeystore);
			ContentReader trustedKeyReader = getReader(registry, trustedKeystore);
			ContentReader nodeContent = getReader(registry, nodeRef);

			String mimetype = nodeContent.getMimetype();

			KeyStore ksCache = ValidateServiceUtils.getCacheKeystoreComplete(cacheKeyReader.getContentInputStream(), keyPassword);
			KeyStore ksTrusted = ValidateServiceUtils.getTrustedKeystoreComplete(trustedKeyReader.getContentInputStream(),
					keyPassword);

			ValidationPreferences validationPreferences = new ValidationPreferences();
			validationPreferences.setCheckRevocation(new Boolean(Constantes.PREFERENCE_CHECK_REVOCATION));
			validationPreferences.setValidateEpesPolicy(new Boolean(Constantes.PREFERENCE_VALIDATE_EPES_POLICY));
			validationPreferences.setKsCache(ksCache);
			validationPreferences.setKsTrust(ksTrusted);

			// configuracion estatica, lo dejo aqui para que este con el resto de preferencias de validacion
			PropertiesCoreUtil.setCheckNodeName(new Boolean(Constantes.PREFERENCE_CHECK_NODE_NAME));
	
			
			if (mimetype.equals(MimetypeMap.MIMETYPE_PDF)) {

				if (Constantes.SIGN_PDF_SIGNATURE_TYPE.equals("PDF")) {
					
					List<PDFSignatureInfo> pdfSignatureInfos = PdfService.validate(nodeContent.getContentInputStream(), ksCache,
							ksTrusted);

					if (pdfSignatureInfos != null && pdfSignatureInfos.size() > 0) {
						JSONArray jsonArray = createPDFJSONArray(pdfSignatureInfos, nodeRef);

						if (!registry.getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE)) {
							registry.getNodeService().addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
						}
						registry.getNodeService().setProperty(nodeRef, PROP_INFO_FIRMA, jsonArray.toString());
					} else {
						throw new WebScriptException(Constantes.ERROR_NOT_SIGNATURES);
					}
					
				} else {
					
					validateDocument(registry, nodeRef, nodeContent, validationPreferences);
				}

			} else if (mimetype.equals(MIMETYPE_SAR)) {

				XadesValidator xadesValidator = XadesValidatorFactory.getSinaduraInstance();

				List<XadesSignatureInfo> xadesSignatureInfos = XadesService.validateArchiver(xadesValidator,
						nodeContent.getContentInputStream(), validationPreferences);

				if (xadesSignatureInfos != null && xadesSignatureInfos.size() > 0) {
					JSONArray jsonArray = createXadesJSONArray(xadesSignatureInfos, nodeRef);

					if (!registry.getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE)) {
						registry.getNodeService().addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
					}
					registry.getNodeService().setProperty(nodeRef, PROP_INFO_FIRMA, jsonArray.toString());

				} else {
					throw new WebScriptException(Constantes.ERROR_NOT_SIGNATURES);
				}

			} else if (mimetype.equals(MimetypeMap.MIMETYPE_XML)) {

				XadesValidator xadesValidator = XadesValidatorFactory.getSinaduraInstance();

				List<XadesSignatureInfo> xadesSignatureInfos = XadesService.validateXml(xadesValidator,
						nodeContent.getContentInputStream(), validationPreferences);

				if (xadesSignatureInfos != null && xadesSignatureInfos.size() > 0) {
					JSONArray jsonArray = createXadesJSONArray(xadesSignatureInfos, nodeRef);

					if (!registry.getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE)) {
						registry.getNodeService().addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
					}
					registry.getNodeService().setProperty(nodeRef, PROP_INFO_FIRMA, jsonArray.toString());

				} else {
					throw new WebScriptException(Constantes.ERROR_NOT_SIGNATURES);
				}

			} else {
			
				validateDocument(registry, nodeRef, nodeContent, validationPreferences);
			}

		} catch (ContentIOException e) {
			
			logger.error(e);
			throw new WebScriptException(e.getMessage(), e);
			
		} catch (ValidationFatalException e) {
			logger.error(e);
			throw new WebScriptException(e.getMessage(), e);

		} catch (XadesValidationFatalException e) {
			logger.error(e);
			throw new WebScriptException(e.getMessage(), e);
			
		} catch (FileNotFoundException e) {
			logger.error(e);
			throw new WebScriptException(e.getMessage(), e);
			
		} catch (IOException e) {
			logger.error(e);
			throw new WebScriptException(e.getMessage(), e);
			
		} catch (XPathExpressionException e) {
			logger.error(e);
			throw new WebScriptException(e.getMessage(), e);
			
		} catch (ParserConfigurationException e) {
			logger.error(e);
			throw new WebScriptException(e.getMessage(), e);
			
		} catch (SAXException e) {
			logger.error(e);
			throw new WebScriptException(e.getMessage(), e);
			
		} catch (URISyntaxException e) {
			logger.error(e);
			throw new WebScriptException(e.getMessage(), e);
			
		} finally{
			if(results != null){
				results.close();
			}
		}

	}
	
	private static void validateDocument(ServiceRegistry registry, NodeRef nodeRef, ContentReader nodeContent,
			ValidationPreferences validationPreferences) throws ContentIOException, IOException, XadesValidationFatalException,
			XPathExpressionException, ParserConfigurationException, SAXException, URISyntaxException {
		
		List<NodeRef> xmlNodeRefs = new ArrayList<NodeRef>();
		
		logger.debug("comprobando las asociaciones target");
		// TODO aqui se genera una dependencia con el modulo de firma
		QName ASSOC_SIGNED_BY = QName.createQName("{zylk.sign.model}signedBy");
		List<AssociationRef> associations = registry.getNodeService().getTargetAssocs(nodeRef, ASSOC_SIGNED_BY);
		
		if (associations != null && associations.size() > 0) {
		
			for (AssociationRef associationRef : associations) {
				
				logger.debug("associationRef.getTargetRef(): " + associationRef.getTargetRef());
				NodeRef xmlNodeRef = associationRef.getTargetRef();
				xmlNodeRefs.add(xmlNodeRef);
			}
			
		} else {
		
			// si no se encuentran asociaciones, se realiza una busqueda por nombre en el mismo path
			ChildAssociationRef parentAssociation = registry.getNodeService().getPrimaryParent(nodeRef);
			NodeRef parentNodeRef = parentAssociation.getParentRef();
			
			String documentName = (String)registry.getNodeService().getProperties(nodeRef).get(ContentModel.PROP_NAME);
			
			// la busqueda es case sensitive
			String query = "PRIMARYPARENT:\"" + parentNodeRef + "\" AND @cm\\:name:\"" + documentName + "*.xml\" NOT ID:\"" + nodeRef + "\"";
			logger.debug("query: " + query);

			SearchService searchService = registry.getSearchService();
			StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
			ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, query);
			
			List<NodeRef> searchNodeRefs = resultSet.getNodeRefs();
			for (NodeRef searchNodeRef : searchNodeRefs) {
				String nameSearchNode = (String)registry.getNodeService().getProperties(searchNodeRef).get(ContentModel.PROP_NAME);
				logger.debug("NODO encontrado: " + nameSearchNode);
				
				xmlNodeRefs.add(searchNodeRef);
			}
			resultSet.close();
		}
		
		List<XadesSignatureInfo> xadesSignatureInfos = validateDetached(registry, nodeRef, nodeContent, xmlNodeRefs, validationPreferences);
     
		
		if (xadesSignatureInfos != null && xadesSignatureInfos.size() > 0) {
			JSONArray jsonArray = createXadesJSONArray(xadesSignatureInfos, nodeRef);

			if (!registry.getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE)) {
				registry.getNodeService().addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
			}
			registry.getNodeService().setProperty(nodeRef, PROP_INFO_FIRMA, jsonArray.toString());

		} else {
			throw new WebScriptException(Constantes.ERROR_NOT_SIGNATURES);
		}
		
	}
	
	private static List<XadesSignatureInfo> validateDetached(ServiceRegistry registry, NodeRef documentNodeRef,
			ContentReader nodeContent, List<NodeRef> xmlNodeRefs, ValidationPreferences validationPreferences)
			throws ContentIOException, IOException, XadesValidationFatalException, XPathExpressionException,
			ParserConfigurationException, SAXException, URISyntaxException {
		

		List<XadesSignatureInfo> xadesSignatureInfos = new ArrayList<XadesSignatureInfo>();
        
		// el validador de Sinadura requiere que el documento este en fileSystem
		File javaTmpFolderFile = new File(System.getProperty("java.io.tmpdir"));
        File tmpFolderFile = new File(javaTmpFolderFile.getAbsolutePath() + File.separatorChar + System.currentTimeMillis());
        tmpFolderFile.mkdir();

        boolean isFirst = true;
        File lastDocumentFile = null;
        
		for (NodeRef xmlNodeRef : xmlNodeRefs) {
			
			// - Copia de la firma a fileSystem
			String nameXmlNode = (String)registry.getNodeService().getProperties(xmlNodeRef).get(ContentModel.PROP_NAME);
			ContentReader xmlNodeContent = getReader(registry, xmlNodeRef);
			String xmlPath = tmpFolderFile.getAbsolutePath() + File.separatorChar + nameXmlNode;
			OutputStream xmlOs = new FileOutputStream(xmlPath);
			IOUtils.copy(xmlNodeContent.getContentInputStream(), xmlOs);
			
			// - Copia del documento a fileSystem
			// leer el fileName de la URI de la firma y copiarlo a fileSystem con ese nombre
			FileInputStream xmlAuxIs = new FileInputStream(xmlPath);
			String documentReferenceUri = findDetachedReferenceUri(xmlAuxIs);
			
			String documentPath;
			if (documentReferenceUri != null) {
				
				// Conversion de URI a filePATH
				// Es el mismo codigo que se utiliza en sinaduraCore para la conversion de URI -> Path (clase
				// XadesService.java).
				// 
				// Parece que el baseUri tiene que terminar en "/" (File.separatorChar) para que el metodo uriToFilePath funcione
				// correctamente.
				String normalicedURI = FileUtil.normaliceLocalURI(tmpFolderFile.getAbsolutePath() + File.separatorChar);
				java.net.URI baseUri = new java.net.URI(normalicedURI);
                String baseUtf8 = baseUri.toASCIIString();
                
				documentPath = uriToFilePath(documentReferenceUri, baseUtf8);
				
			} else {
				String nameDocumentNode = (String)registry.getNodeService().getProperties(documentNodeRef).get(ContentModel.PROP_NAME);
				documentPath = tmpFolderFile.getAbsolutePath() + File.separatorChar + nameDocumentNode;
			}

			logger.debug("documentPath: " + documentPath);
			File documentFile = new File(documentPath);
			
			if (isFirst) {
				OutputStream documentOs = new FileOutputStream(documentFile);
				IOUtils.copy(nodeContent.getContentInputStream(), documentOs);
				
			} else if (!documentFile.exists()) {
				// TODO corregir dependencias
				FileUtils.moveFile(lastDocumentFile, documentFile);
			}
			
			lastDocumentFile = documentFile;
			isFirst = false;

			
			XadesValidator xadesValidator = XadesValidatorFactory.getSinaduraInstance();					
			List<XadesSignatureInfo> tmpResults = XadesService.validateXml(xadesValidator, xmlPath, documentPath, validationPreferences);
            for (XadesSignatureInfo xsi : tmpResults) {
            	xadesSignatureInfos.add(xsi);
            }
            
		}
		
		// borramos tmp
		FileUtils.deleteDirectory(tmpFolderFile);
		
		return xadesSignatureInfos;
	}
	
	
	/**
	 * 
	 * Este metodo es parecido al que hay en el modulo EE de Sinadura (previo a la validacion en ZAIN).
	 *  
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * 
	 */
	private static String findDetachedReferenceUri(InputStream is) throws ParserConfigurationException, SAXException, IOException {

		try {
			String referenceUri = null;

			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(false);
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(is);

			XPath xpath = XPathFactory.newInstance().newXPath();

			// PATH ejemplo: /ds:Signature/ds:SignedInfo/ds:Reference
			XPathExpression expr = xpath.compile("/Signature/SignedInfo/Reference");
			NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); i++) {

				Element element = (Element) nodeList.item(i);

				String uri = element.getAttribute("URI");
				String type = element.getAttribute("Type");

				String algorithm = null;
				String digestValue = null;

				NodeList nodeList2 = element.getChildNodes();
				for (int j = 0; j < nodeList2.getLength(); j++) {

					Node node2 = (Node) nodeList2.item(j);

					if (node2 instanceof Element) {

						Element element2 = (Element) node2;
						String nodeName = element2.getNodeName();
						if (nodeName.endsWith("DigestMethod")) {
							algorithm = element2.getAttribute("Algorithm");
						} else if (nodeName.endsWith("DigestValue")) {
							digestValue = element2.getTextContent();
						}
					}
				}

				logger.debug("reference uri: " + uri);
				logger.debug("reference type: " + type);
				logger.debug("reference algorithm: " + algorithm);
				logger.debug("reference digest: " + digestValue);

				if (uri != null && !uri.startsWith("#")) {

					if (referenceUri != null) {
						// Se traza un error indicando que se han encontrado dos nodos con una uri no local (sin que coincida el
						// hash). Se deja continuar el proceso, para que falle la validacion.
						logger.warn("multiple reference nodes with local uri");
					}

					// si hay varios que tengan una uri no local se devuelve el ultimo
					logger.debug("non local uri found: " + uri);
					referenceUri = uri;
					
					
					// TODO incluir algun tipo de comprobacion para verificar que la URI es un path relativo.
					// se puede excluir http, file...
					
					
				}
			}

			return referenceUri;

		} catch (XPathExpressionException e) {
			logger.error("detached reference uri not found", e);
			return null;
		}

	}
	

	/**
	 * 
	 * Es el mismo codigo que se utiliza en sinaduraCore para la conversion de URI -> Path (clase
	 * Utf8ResolverBigLocalFileSystem.java).
	 * 
	 * @param nameUri
	 * @param BaseURI
	 * @return
	 * @throws MalformedURIException 
	 * @throws URISyntaxException 
	 * 
	 */
	private static String uriToFilePath(String nameUri, String BaseURI) throws MalformedURIException, URISyntaxException {
		
		URI uriNew = getNewURI(nameUri, BaseURI);
		
		// if the URI contains a fragment, ignore it
		URI uriNewNoFrag = new URI(uriNew);
		
		uriNewNoFrag.setFragment(null);
		
		// alfredo -> para paths utf8
		java.net.URI javaUri = new java.net.URI(uriNewNoFrag.toString());
		File file = new File(javaUri);
		String fileName = file.getAbsolutePath();
		 
		return fileName; 
	 }
	
	 private static URI getNewURI(String uri, String BaseURI) throws URI.MalformedURIException {

         if ((BaseURI == null) || "".equals(BaseURI)) {
                 return new URI(uri);
         }
         
         return new URI(new URI(BaseURI), uri);
	 }

	
	
	private static ContentReader getReader(ServiceRegistry registry, NodeRef nodeRef) {
		// First check that the node is a sub-type of content
		QName typeQName = registry.getNodeService().getType(nodeRef);
		if (registry.getDictionaryService().isSubClass(typeQName, ContentModel.TYPE_CONTENT) == false) {
			// it is not content, so can't transform
			return null;
		}

		// Get the content reader
		ContentReader contentReader = registry.getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT);

		return contentReader;
	}

	private static JSONArray createPDFJSONArray(List<PDFSignatureInfo> pdfSignatureInfos, NodeRef nodeRef) {

		JSONArray jsonArray = new JSONArray();

		for (PDFSignatureInfo pdfInfo : pdfSignatureInfos) {

			SignatureInfo signatureInfo = ValidationInterpreterUtil.parsePdfSignatureInfo(pdfInfo);

			if (signatureInfo != null) {
				JSONObject jsonObj = createJSONObject(signatureInfo);
				jsonArray.put(jsonObj);
			}

		}

		return jsonArray;

	}

	public static JSONArray createXadesJSONArray(List<XadesSignatureInfo> xadesSignatureInfos, NodeRef nodeRef) {

		JSONArray jsonArray = new JSONArray();

		for (XadesSignatureInfo xadesInfo : xadesSignatureInfos) {

			SignatureInfo signatureInfo = ValidationInterpreterUtil.parseResultadoValidacion(xadesInfo);

			if (signatureInfo != null) {
				JSONObject jsonObj = createJSONObject(signatureInfo);
				jsonArray.put(jsonObj);
			}

		}

		return jsonArray;

	}

	private static JSONObject createJSONObject(SignatureInfo signatureInfo) {

		JSONObject jsonObj = new JSONObject();

		try {

			jsonObj.put("statusCode", signatureInfo.getStatus());
			jsonObj.put("estado", parseStatus(signatureInfo.getStatus()));

			if (signatureInfo.getChain() != null) {
				jsonObj.put("firmante", CertificateUtil.getFormattedName(signatureInfo.getChain().get(0)));
			} else {
				jsonObj.put("firmante", "-");
			}

			if (signatureInfo.getDate() != null) {
				SimpleDateFormat dateFormat = LanguageUtil.getFullFormater();
				jsonObj.put("fecha", dateFormat.format(signatureInfo.getDate()));
			} else {
				jsonObj.put("fecha", "-");
			}

			if (signatureInfo.getMessages() != null) {

				StringBuffer messages = new StringBuffer();

				for (MessageInfo message : signatureInfo.getMessages()) {
					messages.append("<p>" + parseMessageInfo(message) + "<p/>");
				}

				jsonObj.put("detalles", messages.toString());
			} else {
				jsonObj.put("detalles", "-");
			}

		} catch (JSONException e) {
			logger.error(e);
		}

		return jsonObj;
	}

	private static String parseMessageInfo(MessageInfo messageInfo) {

		String msg = "-";

		if (messageInfo.getSimpleStatus().equals(Status.VALID)) {

			msg = Constantes.IMAGE_VALID + messageInfo.getText();

		} else if (messageInfo.getSimpleStatus().equals(Status.INVALID)) {

			msg = Constantes.IMAGE_INVALID + messageInfo.getText();

		} else if (messageInfo.getSimpleStatus().equals(Status.VALID_WARNING)) {

			msg = Constantes.IMAGE_WARNING_VALID + messageInfo.getText();

		} else if (messageInfo.getSimpleStatus().equals(Status.UNKNOWN)) {

			msg = Constantes.IMAGE_UNKNOWN + messageInfo.getText();

		}

		return msg;
	}

	private static String parseStatus(Status status) {

		String msg = "-";

		if (status.equals(Status.VALID)) {

			msg = Constantes.IMAGE_VALID;

		} else if (status.equals(Status.INVALID)) {

			msg = Constantes.IMAGE_INVALID;

		} else if (status.equals(Status.VALID_WARNING)) {

			msg = Constantes.IMAGE_WARNING_VALID;

		} else if (status.equals(Status.UNKNOWN)) {

			msg = Constantes.IMAGE_UNKNOWN;

		}

		return msg;
	}

	public static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;

		try {

			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {

			logger.error(e);

		} finally {

			try {
				is.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}

		return sb.toString();

	}

}