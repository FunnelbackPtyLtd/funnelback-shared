/*
 * Extensions for json-template
 */


var months = ['Jan', 'Feb', 'Mar', 'Apr', 'Jun', 'Jul', 'Oct', 'Sep', 'Nov', 'Dec'];

// --- Additional formatters
function more_formatters(formatterName) {

	if (formatterName === 'date') {
		return function(s) {
			var d = new Date(s);
			return d.getDay() + ' ' + months[d.getMonth()] + ' ' + d.getFullYear();
		};

	} else if (formatterName === 'upper') {
		return function(s) {return s.toUpperCase(); };

	} else if (formatterName === 'boldicize') {
		return function (s, context) {
			var query = context.get('question.originalQuery');
			var re = new RegExp('('+query+')', 'i');			
			return s.replace(re, '<b>$1</b>');
		};

	} else if (formatterName === 'filesize') {
		return function(s) {
			if (s != 0) {
				return new Number(s / 1024).toFixed(0) + 'k';
			} else {
				return '0k';
			}
		};
		
	} else if (formatterName === 'dump') {
		return function(s) {
			var out = '';
			for(var key in s) {
				out += key + ':' + s[key] + '<br />';
			}
			return '<pre>' + out + '</pre>';
		};
		
	} else if (formatterName.search(/trunc\./) > -1) {
		return function(s) {
			return s.slice(0, formatterName.slice('trunc.'.length));
		};
		
	} else if (formatterName.search(/cut\./) > -1) {
		return function(s) {
			var str = formatterName.slice(4, formatterName.length);
			return s.replace(str, '');
		};
		
	} else {
		return null;
	}
}

// --- Additional predicates
function more_predicates(predicateName) {

	if (predicateName === 'even?') {
		return function(s) { return s%2 == 0; };

	} else if (predicateName.search(/eq\..*/) == 0) {
		return function(x) {
			var str = predicateName.slice(3, predicateName.length-1);
			return x === str;
		};

	} else if (predicateName.search(/regexp\./) == 0) {
		return function(s) {
			var re = new RegExp(predicateName.slice('regexp.'.length, predicateName.length-1));
			return re.test(s);
		};		

	} else {
		return null;
	}
}

// --- Model augmentation
function augment_model(model) {
	if (model.response && model.response.resultPacket) {
		var summary = model.response.resultPacket.resultsSummary;

		// Previous
		if (summary.prevStart > 0) {
			if (model.question.queryString.search(/start_rank=/) > -1) {
				summary.prevLink = '?' + model.question.queryString.replace(/start_rank=([^&]*)/, 'start_rank=' + summary.prevStart);
			} else {
				summary.prevLink = '?' + model.question.queryString + '&ampstart_rank=' + summary.prevStart;
			}
		}

		var pages = (summary.totalMatching + summary.numRanks - 1) / summary.numRanks;
		var currPage = (summary.currStart + summary.numRanks - 1) / summary.numRanks;
		var firstPage = 1 ;
		if (currPage > 4) {
			firstPage = currPage - 4;
		}
		var pageLinks = new Array();
		for (var pg = firstPage; pg < firstPage+10 && pg < pages ; pg++) {
			var link = {};
			link.page = pg;
			if (pg == currPage) {
				link.current = true;
			} else {
				if (model.question.queryString.search(/start_rank=/) > -1) {
					link.link = '?' + model.question.queryString.replace(/start_rank=([^&]*)/, 'start_rank=' + ((pg-1)*summary.numRanks));
				} else {
					link.link = '?' + model.question.queryString + "&amp;start_rank=" + ((pg-1)*summary.numRanks);
				}
			}
			pageLinks.push(link);
		}
		summary.pageLinks = pageLinks;
		
		// Next
		if (summary.nextStart > 0) {
			if (model.question.queryString.search(/start_rank=/) > -1) {
				summary.nextLink = '?' + model.question.queryString.replace(/start_rank=([^&]*)/, 'start_rank=' + summary.nextStart);
			} else {
				summary.nextLink = '?' + model.question.queryString + "&amp;start_rank=" + summary.nextStart;
			}
		}
	}
}