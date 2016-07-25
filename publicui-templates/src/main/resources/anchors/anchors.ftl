<#ftl encoding="utf-8" />
<#setting number_format="computer">
<#if !RequestParameters.ajax??>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8" />
	<style type="text/css">
		body {
			font-family: Arial, Helvetica, sans-serif;
			background-color: #F0F0F0;
			font-size: 90%;
			color: #222;
		}
		a {
		  color: #3e62a4;
		  text-decoration: none;
		}
		a:hover{ text-decoration: underline}
		a:visited{ color: #8276CB}
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
			background-color: #ffffff;
			border: 1px solid #ccc;
			left: 50%;
			margin: 0 auto;
			padding: 25px;
			width: 800px;

			-moz-border-radius: 4px;
			-webkit-border-radius: 4px;
			-khtml-border-radius: 4px;
			border-radius: 4px;
			-moz-box-shadow: 0 1px 1px #ccc;
			-webkit-box-shadow:0 1px 1px #ccc;
			box-shadow: 0 1px 1px #ccc;
		}
		#anchors-pane > div:first-child { margin-bottom: 30px}
		#anchors-pane h3{
			position: relative; top: 34px; left: 195px; margin: 0px; padding: 0px;
		}
		table{ width: 100%}
		table, td, th {
			border: 1px solid #eee;
			border-spacing: 0px;
            padding: 5px;
			border-collapse:collapse;
		}
		#headingtext-align: center;{ margin-bottom: 30px}
		.fb-a-text-centered{text-align:center}
		.fb-a-text-right{text-align:right}
		.fb-a-text-left{text-align:left}
		
	</style>
	<title>Anchors Information: ${anchors.collection} ${anchors.docNum}</title>
</head>

<body>
	</#if>
    <div id="anchors-pane">
        <div id="heading">
        	<h3>Anchors Summary</h3>
        	<#if !RequestParameters.ajax??><img src="${GlobalResourcesPrefix}funnelback-small.png" alt="Funnelback logo" width="170" height="36"></#if>
       	</div>


       	<#if anchors.error??> 
       		<div class="messages">
				<ul>
				 	<li>${anchors.error}</li>	
				</ul>
			</div>     
       	<#else>
	    		<p>Showing links to document <a class="out" href="${anchors.url}">${anchors.url}</a> in collection <strong id="collection">${anchors.collection}</strong>:</p>
	  <!--  Document Number: {anchors.docNum} -->
	  <!--  Distilled File: ${anchors.distilledFileName} -->
	       	<table>
                <thead>
	       			<th>Link type</th>
	       			<th>Anchor text</th>
	       			<th>Within-collection links</th>
                    <th>External links</th>
	       		</thead>
                <#assign totalInternal = 0/>
                <#assign totalExternal = 0/>
	       		<#list anchors.anchors as anchor>
	       			<tr>
	       				<td class="fb-a-text-centered">
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
	       				<td class="fb-a-text-right">${anchor.internalLinkCount} <#assign totalInternal = totalInternal + anchor.internalLinkCount?number></td>
                        <td class="fb-a-text-right">${anchor.externalLinkCount} <#assign totalExternal = totalExternal + anchor.externalLinkCount?number></td>
	       			</tr>	
	       		</#list>
                <tfoot>
                    <tr>
                        <td class="fb-a-text-right" colspan=2>Total:</td>
                        <td class="fb-a-text-right">${totalInternal}</td>
                        <td class="fb-a-text-right">${totalExternal}</td>
                    </tr>
                </tfoot>
	       	</table>
	     </#if>
	     <#if !RequestParameters.ajax??>
          <p><a href="javascript:history.go(-1);">return to previous page</a></p>
    </div>
</body>
</html>
</#if>
