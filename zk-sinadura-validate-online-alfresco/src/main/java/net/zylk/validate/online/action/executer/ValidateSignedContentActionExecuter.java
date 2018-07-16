package net.zylk.validate.online.action.executer;

import java.util.List;

import net.zylk.validate.online.webscripts.utils.ValidateServiceUtils;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ValidateSignedContentActionExecuter extends ActionExecuterAbstractBase {

	public static final String NAME = "validate-signed-content";
	private ServiceRegistry registry;
	
	private static Log logger = LogFactory.getLog(ValidateSignedContentActionExecuter.class);
	
	
	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
		
		logger.debug("Action executer.... " + actionedUponNodeRef);
		
		ValidateServiceUtils.validateSign(registry, actionedUponNodeRef);
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		// TODO Auto-generated method stub

	}
	
		
	public ServiceRegistry getRegistry() {
		return registry;
	}

	public void setRegistry(ServiceRegistry registry) {
		this.registry = registry;
	}

}