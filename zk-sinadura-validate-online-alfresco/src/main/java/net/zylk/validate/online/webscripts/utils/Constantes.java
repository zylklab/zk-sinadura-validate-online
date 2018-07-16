package net.zylk.validate.online.webscripts.utils;


public class Constantes {
	
	// TODO esta propiedad esta repetida en "Constantes.java" del modulo de sign
	// valores: PDF o XADES
	public static final String SIGN_PDF_SIGNATURE_TYPE = PropertiesManager.getProperty("zk.sign.pdf.signature.type");

	public static final String CERTS_XPATH = PropertiesManager.getProperty("zk.validation.certs.xpath");
	public static final String CACHE_KEYSTORE_NAME = PropertiesManager.getProperty("zk.validation.cache.keystore");
	public static final String TRUSTED_KEYSTORE_NAME = PropertiesManager.getProperty("zk.validation.trusted.keystore");		
	public static final String VALIDATION_CERT_KEYPASSWORD = PropertiesManager.getProperty("zk.validation.cert.keyPassword");
	
	public static final String PREFERENCE_CHECK_REVOCATION = PropertiesManager.getProperty("zk.validation.preference.check.revocation");
	public static final String PREFERENCE_VALIDATE_EPES_POLICY = PropertiesManager.getProperty("zk.validation.preference.validate.epes.policy");
	public static final String PREFERENCE_CHECK_NODE_NAME = PropertiesManager.getProperty("zk.validation.preference.check.node.name");
	
	// Iconos
	public static final String IMAGE_VALID = PropertiesManager.getProperty("zk.validation.image.valid");
	public static final String IMAGE_INVALID = PropertiesManager.getProperty("zk.validation.image.invalid");
	public static final String IMAGE_UNKNOWN = PropertiesManager.getProperty("zk.validation.image.unknown");
	public static final String IMAGE_WARNING = PropertiesManager.getProperty("zk.validation.image.warning");
	public static final String IMAGE_WARNING_VALID = PropertiesManager.getProperty("zk.validation.image.warning-valid");		
	
	// CODIGOS DE ERROR CONTROLADOS
	// No se si la traduccion de errores de WS a cliente se esta haciendo del todo bien. Ya que en el json que se
	// genera a partir de una WebScriptException no parece haber ningun campo donde indicar un codigo de error unico.
	// Ni siquiera el campo con el mensaje es limpio ya que le añade una especie de timestamp numerico delante.
	// En el javadoc (WebScriptException) el parametro se llama "msgId", asi que igual hay que añadir una key de idioma? 
	public static final String ERROR_NOT_SIGNATURES = "ZK_ERROR_NOT_SIGNATURES";
	
}
