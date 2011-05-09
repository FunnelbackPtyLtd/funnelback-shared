<#include "../inc/header.ftl" />

<h1>Stats</h1>

<table>
<tr>
	<th></th>
	<th>Name</th>	
	<th>Elements</th>
	<th>Actions</th>
</tr>

<#list statistics as stats>
<tr>
	<td>
		<input type="checkbox" class="switch" id="switch.${stats.associatedCache.name}" <#if stats.associatedCache.disabled><#else>checked="checked"</#if> />				
	</td>
	<td>${stats.associatedCache.name}</td>
	<td>
		<table>
		<tr>
			<th>Name</th>
			<th>Creation time</th>
			<th>Size</th>
			<th>Actions</th>
		</tr>		
		<#if !stats.associatedCache.disabled>
			<#list stats.associatedCache.keys as key>
				<tr>
					<th>${key}</th>
					<td>${stats.associatedCache.get(key).creationTime}</td>
					<td>${stats.associatedCache.get(key).serializedSize}</td>
					<td><a href="${contextPath}/dashboard/caches/${stats.associatedCache.name}/remove/${key}/">Remove</a></td>
				</tr>
			</#list>
		<#else>
			<tr>Cache disabled</tr>
		</#if>

		</table>
	</td>
	<td><a href="${contextPath}/dashboard/caches/${stats.associatedCache.name}/flush/">Flush</a></td>
</tr>	
</#list>

<script type="text/javascript">
	jQuery('input.switch').change( function () {
		var id = '#' + $(this).attr('id');
		var cacheId = $(this).attr('id').substring('switch.'.length); 
		var state = $(this).is(':checked');
		var action = (state) ? 'enable' : 'disable';
		jQuery.ajax( {
			url:  cacheId + '/' + action + '.ajax',
			success: function() {
				jQuery('#ajax-messages>p.success').text(action + ' cache ' + id + ': success').fadeIn('slow').delay(3000).fadeOut('slow');
			},
			error: function() {
				jQuery('#ajax-messages>p.error').text(action + ' cache ' + id + ': error').fadeIn('slow').delay(3000).fadeOut('slow');
			} 	
		});
	});
</script>

<#include "../inc/footer.ftl" /> 