(function() {
    YAHOO.Bubbling.fire("registerAction",
    {
        actionName: "onActionZKValidateOnline",
        fn: function ZK_onActionZKValidateOnline(files) {

		var selectedFiles = [];
		if(files.length != undefined){
			selectedFiles = files;
		}else {
			selectedFiles[0] = files;	
		}

       	this.modules.actions.genericAction(
            {
                success:
                { 
                    callback:
                    {
                       fn: function ZK_onActionZKValidateOnline_success(data)
                       {
                    	    window.location.reload();
                    	   
                       },
                       scope: this
                    },
            
            	    message: this.msg("zk.message.validate.success")
                },
                failure:
                {
                	callback:
                    {
                       fn: function(data)
                       {  
                    	   var errorMessage = data.json.message;
                    	      
                    	   if (errorMessage.indexOf("ZK_ERROR_NOT_SIGNATURES") > -1) { // contains 
                               Alfresco.util.PopupManager.displayMessage({
                                   text: this.msg("zk.message.validate.error.not_signatures")
                               });   
                    	   } else {
                               Alfresco.util.PopupManager.displayMessage({
                                   text: this.msg("zk.message.validate.error.general")
                               });
                    	   }
                       },
                       scope: this
                    },
                    
                },
                webscript:
                {
                    name: "validate",
                    method: Alfresco.util.Ajax.POST
                },
                wait:
                {
                   message: this.msg("zk.message.validate.wait")
                },
                config:
                {
                    requestContentType: Alfresco.util.Ajax.JSON,
                    dataObj:
                    {
                       files: selectedFiles
                    }
                 }

            });

        }
    });
})();