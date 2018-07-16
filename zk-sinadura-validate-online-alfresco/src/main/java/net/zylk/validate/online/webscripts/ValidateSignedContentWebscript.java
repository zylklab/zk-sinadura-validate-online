package net.zylk.validate.online.webscripts;

import java.io.IOException;
import java.util.Locale;
import java.util.StringTokenizer;

import net.esle.sinadura.core.util.LanguageUtil;
import net.zylk.validate.online.webscripts.utils.ValidateServiceUtils;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class ValidateSignedContentWebscript extends AbstractWebScript {

	private ServiceRegistry registry;
	
	private static Log logger = LogFactory.getLog(ValidateSignedContentWebscript.class);
	
	public static final QName USER_LOCALE_PROPERTY = QName.createQName("{user.locale.model}user_locale_id");

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		try {

//			String currentUserName = registry.getAuthenticationService().getCurrentUserName();
//			NodeRef user = registry.getPersonService().getPerson(currentUserName);
//			
//			String acceptLang = null;
//			
//			if (registry.getNodeService().getProperty(user, USER_LOCALE_PROPERTY) != null) {
//				acceptLang = registry.getNodeService().getProperty(user, USER_LOCALE_PROPERTY).toString();
//
//			}
//			
//			logger.debug("user locale property: " + acceptLang);
//
//			if (acceptLang == null) {
//				acceptLang = req.getHeader("Accept-Language");
//			}
//
//			if (acceptLang != null && acceptLang.length() != 0) {
//
//				StringTokenizer t = new StringTokenizer(acceptLang, ",; ");
//				// get language and convert to java locale format
//				String language = t.nextToken().replace('-', '_');
//				logger.debug("Cambiando locale... " + language);
//				String[] locale = language.split("_");
//				LanguageUtil.reloadLocale(new Locale(locale[0], locale[1]));
//			}
			
			// TODO el language de momento tiene que ser fijo ya que es un singleton en sinaduraCore
			LanguageUtil.reloadLocale(new Locale("ES", "es"));

			JSONObject json = new JSONObject(ValidateServiceUtils.convertStreamToString(req.getContent().getInputStream()));

			if (json.has("files")) {

				JSONArray files = json.getJSONArray("files");

				logger.debug("files: " + files.length());

				for (int i = 0; i < files.length(); i++) {
					JSONObject file = files.getJSONObject(i);
					logger.debug("NodeRef to validate: " + file.getString("nodeRef"));
					NodeRef nodeRef = new NodeRef(file.getString("nodeRef"));
					ValidateServiceUtils.validateSign(registry, nodeRef);
				}
			}

			// Cambiar el locale en función del idioma del navegador

		} catch (JSONException e) {
			logger.error(e);
			throw new WebScriptException(e.getMessage());
		}

	}

	public ServiceRegistry getRegistry() {
		return registry;
	}

	public void setRegistry(ServiceRegistry registry) {
		this.registry = registry;
	}

}
