<#assign controlId = fieldHtmlId + "-cntrl">
<@link rel="stylesheet" type="text/css" href="${url.context}/components/validate-online/zk-validate-online-actions.css" />

<script type="text/javascript">//<![CDATA[


YAHOO.util.Event.addListener(window, "load", function(){
	
		var columnDesc = [
		    {key:"estado", label:"${msg("zk.label.datatable.control.status")}", sortable:true, resizeable:true},
 			{key:"firmante", label:"${msg("zk.label.datatable.control.signer")}", sortable:true, resizeable:true},
 			{key:"fecha", label:"${msg("zk.label.datatable.control.date")}", sortable:true, resizeable:true},
 			{key:"detalles", label:"${msg("zk.label.datatable.control.details")}", sortable:true, resizeable:true}		
		];
		var myDataSource = new YAHOO.util.DataSource(${field.value});	
		myDataSource.reponseType = YAHOO.util.DataSource.TYPE_JSON;
		myDataSource.reponseSchema = {
			resultsList: "", 
			fields: ["statusCode","estado","firmante", "fecha", "detalles"]
		};

		var Dom = YAHOO.util.Dom; 		

		var myRowFormatter = function(elTr, oRecord) { 
		if (oRecord.getData('statusCode') == "VALID" || oRecord.getData('statusCode') == "VALID_WARNING") { 
		  Dom.addClass(elTr, 'valid'); 
		} else {
		  Dom.addClass(elTr, 'invalid');
		}
		   return true; 
		};  
		var myDataTable = new YAHOO.widget.DataTable("container", columnDesc, myDataSource);
	
});
</script>
<#if field.value != "" && field.value != "undefined">
   <#if form.mode == "view">
   <div class="form-field">
   ${msg("zk.label.datatable.control.view.mode")}
   </div>
  <#else>
  <div id="container" class="form-field">
      <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      <input id="${fieldHtmlId}" name="${field.name}" tabindex="0"
             <#if field.control.params.password??>type="password"<#else>type="text"</#if>
             <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
             <#if field.control.params.style??>style="${field.control.params.style}"</#if>
             <#if field.value?is_number>value="${field.value?c}"<#else>value="${field.value?html}"</#if>
             <#if field.description??>title="${field.description}"</#if>
             <#if field.control.params.maxLength??>maxlength="${field.control.params.maxLength}"</#if> 
             <#if field.control.params.size??>size="${field.control.params.size}"</#if> 
             <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if> />
      <@formLib.renderFieldHelp field=field />
   </div>
   </#if>
</#if>
