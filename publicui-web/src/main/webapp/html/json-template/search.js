function formatterFbDate(s) {
	return new Date(s).toUTCString();
}

function formatterDump(s) {
	var out = '';
	for(var key in s) {
		out += key + ':' + s[key] + '<br />';
	}
	return '<pre>' + out + '</pre>';
}

function more_formatters(formatterName) {
	if (formatterName === 'fb-date') {
		return formatterFbDate;
	} else if (formatterName === 'dump') {
		return formatterDump;
	} else {
		return null;
	}
}

function predicateEven(s) {
	return s%2 == 0;
}

function more_predicates(predicateName) {
	if (predicateName === 'even?') {
		return predicateEven;
	} else {
		return null;
	}
}


var t;

jQuery(document).ready( function() {
	
	// Request template file
	jQuery.ajax({
		url: 'html/json-template/template.txt',
		dataType: 'text',
		success: function(tplData) {
			t = jsontemplate.Template(tplData,
				{
					more_formatters: more_formatters,
					more_predicates: more_predicates
				}
			);
			
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
		'search.json',
		{
			collection: collection,
			query: query
		},
		function(data) {
			jQuery('#template').replaceWith(t.expand(data));
		}
	);
}

