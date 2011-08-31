<#ftl encoding="utf-8" />
<#setting number_format="computer">
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8" />
	<style type="text/css">
		body {
			font-family: Arial, Helvetica, sans-serif;
			background-color: #F0F0F0;
		}
		h3 {
			color: #FF9F00;
			text-shadow: #DDD 1px 1px 0px;
			color: #FF9F00;
			letter-spacing: .2em;
			line-height: 30px;
			font-size: 24px;
			font-weight: normal;
			margin: 0px;
			padding-top: 0px;
		}
		
		.messages {
			background-color: #ffaaaa;
			border: 1px solid #ff0000;
		}
	
		#anchors-pane {
			padding: 10px; border: 1px solid #A0A0A4;
			margin-bottom: 15px; width: 800px; position: relative; left: 50%; margin-left: -400px;
			-moz-box-shadow: 2px 2px 8px #BBB;
			-webkit-box-shadow: 2px 2px 8px #BBB;
			box-shadow: 2px 2px 8px #BBB;
			background-color: #FFFFFF;
			-moz-border-radius: 10px;
			-webkit-border-radius: 10px;
			-khtml-border-radius: 10px;
			border-radius: 10px;
		}
		table, td, th {
			border: 1px solid #dddddd;
			border-spacing: 0px;
            padding: 5px;
			border-collapse:collapse;
		}
		
	</style>
	<title>Anchors Information: ${anchors.collection} ${anchors.docNum}</title>
</head>
<body>
    <div id="anchors-pane">
        <div style="margin-bottom: 30px;">
        	<h3 style="position: relative; top: 34px; left: 195px; margin: 0px; padding: 0px;">	Anchors Summary</h3>
        	<img src="/search/funnelback-small.png" alt="Funnelback logo" width="170" height="36">
       	</div>
       	<#if anchors.error??> 
       		<div class="messages">
				<ul>
				 	<li>${anchors.error}</li>	
				</ul>
			</div>     
       	<#else>
	    		<p>Showing links to document <a href="${anchors.url}">${anchors.url}</a> in 
                collection <span>${anchors.collection}</span>:</p>
  
	  <!--  Document Number: {anchors.docNum} -->
	  <!--  Distilled File: ${anchors.distilledFileName} -->
	       	<table>
                <thead>
	       			<th>Link type</th>
	       			<th>Anchor text</th>
	       			<th>Same-site links</th>
                    <th>Off-site links</th>
	       		</thead>
                <#assign totalInternal = 0/>
                <#assign totalExternal = 0/>
	       		<#list anchors.anchors as anchor>
	       			<tr>
	       				<td style="text-align: center;">
                          <#if anchor.linkType == "2" || anchor.linkType == "3">
                            within site 
                          <#elseif anchor.linkType == "1">
                            between related sites
                          <#elseif anchor.linkType == "0">
                            between unrelated sites
                          <#elseif anchor.linkType == "K">
                            click associated queries
                          <#else>
                            ${anchor.linkType}
                          </#if>
                        </td>
	       				<td><a href="?collection=${anchors.collection?url}&docnum=${anchors.docNum}&anchortext=${anchor.linkAnchorText?url}">${anchor.anchorText}</td>
	       				<td style="text-align: right;">${anchor.internalLinkCount} <#assign totalInternal = totalInternal + anchor.internalLinkCount?number></td>
                        <td style="text-align: right;">${anchor.externalLinkCount} <#assign totalExternal = totalExternal + anchor.externalLinkCount?number></td>
	       			</tr>	
	       		</#list>
                    <tfoot>
                        <td style="text-align: right;" colspan=2>Total:</td>
                        <td style="text-align: right;">${totalInternal}</td>
                        <td style="text-align: right;">${totalExternal}</td>
                    </tfoot>
	       	</table>
	     </#if>
          <p><a href="javascript:history.go(-1);">return to previous page</a></p>
    </div>
</body>
</html>
