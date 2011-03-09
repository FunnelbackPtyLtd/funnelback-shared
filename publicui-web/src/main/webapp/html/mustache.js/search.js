var tpl = '';

jQuery(document).ready( function() {
	
	// Request template file
	jQuery.ajax({
		url: 'template.txt',
		dataType: 'text',
		success: function(tplData) {
			tpl = tplData;
			if (param('query') != null && param('collection') != null) {
				search(param('collection'), param('query'));
				jQuery('input[name="query"]').attr('value', param('query'));
			}
		}
	});
	
	
	// jQuery('form').submit( function() {
	jQuery('input[name="query"]').keyup( function() {
		search(param('collection'), jQuery(this).attr('value'));
	});
});

function search(collection, query) {
	jQuery.getJSON(
		'../../search.json',
		{
			collection: collection,
			query: query
		},
		function(data) {
			jQuery('#template').replaceWith(Mustache.to_html(tpl, data));
		}
	);
}