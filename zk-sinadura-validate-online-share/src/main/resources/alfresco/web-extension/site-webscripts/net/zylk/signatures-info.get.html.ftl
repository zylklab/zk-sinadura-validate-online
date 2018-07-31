<@link rel="stylesheet" type="text/css" href="${url.context}/components/validate-online/zk-validate-online-actions.css" />

<#if hasSignaturesInfo == true>
	<script type="text/javascript">//<![CDATA[
	
		YAHOO.util.Event.addListener(window, "load", function(){
		
			var columnDesc = [
			    {key:"estado", label:"${msg("zk.label.datatable.control.status")}", sortable:true, resizeable:true},
	 			{key:"firmante", label:"${msg("zk.label.datatable.control.signer")}", sortable:true, resizeable:true},
	 			{key:"fecha", label:"${msg("zk.label.datatable.control.date")}", sortable:true, resizeable:true},
	 			{key:"detalles", label:"${msg("zk.label.datatable.control.details")}", sortable:true, resizeable:true}		
			];
			var myDataSource = new YAHOO.util.DataSource(${signaturesInfo});	
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
	
	<div class="comments-list">
		<h2 class="thin dark">${msg("zk.label.aspect.sign.info")}</h2>
		
		<div id="container" class="form-field">
		</div>
	</div>
</#if>