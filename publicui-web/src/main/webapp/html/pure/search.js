$p.core.error = function() {};

function hasResults(arg) {
	return arg.context.SearchTransaction.response.resultPacket.resultsSummary.totalMatching > 0;
}

var directives = {
	'div#fb-initial@style' : function (arg) {
		if (hasResults(arg)) {
			return 'display:none';
		} else {
			return 'display:block';
		}
	},
	
	'#fb-logo@style+' : function (arg) {
		if (hasResults(arg)) {
			return 'display: block';
		} else {
			return 'display: none';
		}
	},
		
	'span#fb-page-start' : 'SearchTransaction.response.resultPacket.resultsSummary.currStart',
	'span#fb-page-end' : 'SearchTransaction.response.resultPacket.resultsSummary.currEnd',
	'span#fb-total-matching' : 'SearchTransaction.response.resultPacket.resultsSummary.totalMatching',
	
	'#fb-matching strong' : 'SearchTransaction.response.resultPacket.query',
	'input[name="query"]@value' : 'SearchTransaction.response.resultPacket.query',
	'input[name="collection"]@value' : 'SearchTransaction.question.collection.id',
	
	'ol#fb-results li.result' : {
		'result<-SearchTransaction.response.resultPacket.results' : {
			'h3 span.fb-filetype' : 'result.fileType',
			'h3 a@href' : 'result.displayUrl',
			'h3 a' : 'result.title',
			
			'p span.fb-summary' : 'result.summary',
			'p span.fb-date' : function (arg) {
					return new Date(arg.item.date).toUTCString();
				},
			
			'cite' : 'result.displayUrl',
			
			'a.fb-cached@href' : 'result.cacheUrl'
		}
	},
	
	'div.fb-best-bet' : 'SearchTransaction.response.resultPacket.bestBets'
};

jQuery(document).ready(function() {
	jQuery.getJSON(
			'../../search.json',
			{
				collection: param('collection'),
				query: param('query')
			},
			function(data) {
				jQuery('#template')
					.render(data, directives);
			}
		);
});