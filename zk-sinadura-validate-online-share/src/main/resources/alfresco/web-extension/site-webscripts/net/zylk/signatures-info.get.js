<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function main()
{
   // Populate model with parameters
   AlfrescoUtil.param("nodeRef");
   AlfrescoUtil.param("api", "api");
   AlfrescoUtil.param("proxy", "alfresco");
   
   var nodeMetadata = AlfrescoUtil.getMetaData(model.nodeRef);
   model.hasSignaturesInfo = nodeMetadata.aspects.indexOf("{zylk.validate.model}validable") > -1;
   if (model.hasSignaturesInfo) {
	   model.signaturesInfo = nodeMetadata.properties["{zylk.validate.model}info_firma"];
   } else {
	   model.signaturesInfo = {};
   }
    
}

// Start the webscript
main();