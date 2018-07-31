<@markup id="bd-new" target="bd" action="replace" scope="global">
   <div id="bd">
      <@region id="actions-common" scope="template"/>
      <@region id="actions" scope="template"/>
      <@region id="node-header" scope="template"/>
      <div class="yui-gc">
         <div class="yui-u first">
            <#if (config.scoped['DocumentDetails']['document-details'].getChildValue('display-web-preview') == "true")>
               <@region id="web-preview" scope="template"/>
            </#if>
            <@region id="signatures-info" scope="template"/>
            <@region id="comments" scope="template"/>
         </div>
         <div class="yui-u">
            <@region id="document-actions" scope="template"/>
            <@region id="document-tags" scope="template"/>
            <@region id="document-links" scope="template"/>
            <@region id="document-metadata" scope="template"/>
            <@region id="document-sync" scope="template"/>
            <@region id="document-workflows" scope="template"/>
            <@region id="document-versions" scope="template"/>
            <#if imapServerEnabled>
               <@region id="document-attachments" scope="template"/>
            </#if>
         </div>
      </div>

      <@region id="html-upload" scope="template"/>
      <@region id="flash-upload" scope="template"/>
      <@region id="file-upload" scope="template"/>
      <@region id="dnd-upload" scope="template"/>
   </div>
   <@region id="doclib-custom" scope="template"/>
</@markup>