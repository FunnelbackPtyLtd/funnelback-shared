// fb-content-auditor.js
// Functions
function createIcons() {
	/*var target = $('#fb-facets-navigation .nav-stacked .facetLabel');
		target.find('.fa-circle-thin').remove();
		target.each(function(key, elem) {
			$(elem).prepend('<span class="fa fa-circle-thin"></span>&nbsp;');
		});*/
}

function cleanFacetsNavigation() {
	/*	var a = $('#fb-facets-navigation ul li > .facetLabel');
			if(a.length){
				
				var facetFilters = 'contentReportFacetedFilters'; 
				$('#fb-facet-details h3').after('<div id="' + facetFilters + '" class="col-md-12"><p><strong>Attributes Added<strong></p></div>');
				
				
				$.each(a,function(){
					if($(this).parents('li').hasClass('active')){$(this).addClass('active');}
					$(this).appendTo('#'+facetFilters).css('marginBottom','5px');	
				});
					$('#' + facetFilters).append(a);
				}*/
}

function refreshModalContent() {
		var modal = $('#modal-overlay .modal-content');
		//modal.find('style, meta, title, script, head, img').remove();
		//modal.find('h3, div, td').removeAttr('style');
		modal.find('table').addClass('table table-hover table-condenesed table-responsive table-striped table-row-clickable');
	}
	// jQuery 
if (console) console.log('fb-content-auditor.js is initiated.');
jQuery(function() {
	$('[data-toggle="tooltip"]').tooltip();
	$(document)
		/*.on('click', '.table-row-clickable tbody tr', function() {
		 	var href = $(this).find('td:first-child a').attr('href');
		})*/
		.on('ready', function() {
			//read the URL hash and open the tab.
			var hash = window.location.hash;
			$('a[href="' + hash + '"]').tab('show');
			//history.pushState("", document.title, window.location.pathname + window.location.search);
			//window.location.href.substr(0, window.location.href.indexOf('#'));
			//window.location.href.split('#')[0];
		})
		.on('click', 'a[href="#collection-1-tab-1"]', function(){
			chart = $('#fb-facets-navigation ul.nav li.active a').attr('data-chart_ref');
			content_auditor[chart].invalidateSize();
			
		})
		.on('click', '[data-dismiss="modal"]', function(){
			var modal = $('.modal');
			modal.removeClass('in');
			setTimeout(function(){
				modal.hide();
			},888);	
		})
		.on('shown.bs.modal load', '#modal-overlay', function() {
			refreshModalContent();
		})
		.on('click', '.facet-search-details table tbody tr, #duplicates tbody tr', function() {
			var href = $(this).find('a').attr('href');
			window.location = href;
		})
		.on('click', '#modal-overlay', function() {
			$('#modal-overlay').addClass('fade').removeClass('in').hide();
		})
		.on('click', '.modal-content', function() {
			return false;
		})
		.on('click', '.modal-content a', function() {
			var t = $(this);
			var href = t.attr('href');
			var targetOutput = t.parents('.modal-content');
			if (t.hasClass('out')) {
				window.open(href, '_blank');
			}
			/*else if(t.hasClass('in')){
				$.get('/s/anchors-list.html' + href + '&ajax=1', function(data){
					targetOutput.html(data);
					refreshModalContent();
				});
			}*/
			else {
				$.get('/s/anchors.html' + href + '&ajax=1', function(data) {
				})
				 .done(function(data) {
					targetOutput.html(data);
					refreshModalContent();
				})
				.fail(function(error){
					targetOutput.html("<h3>Error "+  error['status'] + '</h3><p>A processing error has occurred. Either the data is non-existent or has since been removed.</p><p><br><span class="btn btn-primary"  data-dismiss="modal"><i class="fa fa-remove"></i> Close</span></p>');
				});
			}
			return false;
		})
		.on('click', '#btn-facet-container', function(){
			$(this).parents('#tabbable-content').toggleClass('closed');
		})
		.on('click', '[data-modal="overlay"]', function(e) {
			$('#modal-overlay').addClass('fade');
			var href = $(this).attr('href');
			$.get(href, function(data) {
				
				$('#modal-overlay').show().addClass('in').find('.modal-content').html(data);
				refreshModalContent();
			});
			return false;
		}).on('change', '.field-sort', function() {
			var sort = $(this).val();
			var target = $(this).attr('data-url');
			//var currentURL = window.location.href;
			alert('the params that change need to be implemented onto this select box');
			//window.location = target + '&sort=' + sort;
		}).on('click', '.navbar-nav li a', function() {
			var target = $(this).parents('li');
			target.siblings('.active').removeClass('active');
			target.addClass('active');
		}).on('click', '#toggle-sidebar', function() {
			$('body').toggleClass('sidebar-closed');
			$('.navbar-collapse').toggleClass('in');
		}).on('click', '#toggle-search', function() {
			$('body').toggleClass('searchform-open');
			$('.navbar-collapse ').toggleClass('in');
			$('#query').focus();
		}).on('mouseenter', '#fb-facets-navigation', function() {
			// $(this).parent('div').removeClass('closed').addClass('opened');
		}).on('mouseleave', '#fb-facets-navigation', function() {
			// $(this).parent('div').addClass('closed has-animated').removeClass('opened');
		}).on('focus', '.search-form input', function() {
			$(this).parents('form, div').addClass('has-focus');
		}).on('blur', '.search-form input', function() {
			$(this).parents('form, div').removeClass('has-focus');
		}).on('click', '.table-row-clickable tr', function(e) {
			if (!$(this).hasClass('fa-stack')) {
				//window.open($(this).find('.clickable-link').attr('href'), '_blank')
			}
		})
		// bootstrap tab listener. on shown event.
		// mainly needed to update AM Charts
		.on('shown.bs.tab', 'a[data-toggle="tab"]', function(e) {
			var hash = $(e.target).attr("href").substr(1);
			window.location.hash = hash;
			/*//too heavy
			$.each($('.facet-search-details a') , function(){
				var href = $(this).attr('href'.split('#')[0])
				//.replace(/#*$/, '#' + hash)
				;
				
				console.log(href);
				console.log('#' + hash);
				$(this).attr('href',href + '#' + hash);
			});*/
			
			
			//(^|\s)#(\w*[a-zA-Z_]+\w*)
			
			
			/*
			if(!$('body').attr('data-hash')){
				$.each($('#my-tab-content .tab-pane'), function(){
					var newHash =  $(this).attr('id');
					
					$.each($(this).find('a'),function(){
					$(this).attr('href',$(this).attr('href') + '#' + newHash);
					});
				});	
			} else{ }
			
			*/
			$('body').attr('hash', hash);
			// This is for AMCharts and if the tab element has an attribute of 'data-chart_ref'
			
			var chart = $(this).attr('data-chart_ref');
			if(chart){
				var index = chart.replace(/\D/g, '');
			} else {
			/* looks like we're in a different tab so open the first chart */	
				var index = 0;
			}
			// look in the content_auditor js object for our charts. Force them to display on tab change
			if (content_auditor[chart]) {
				content_auditor[chart].invalidateSize();
			}
			if ($('.bootstrap-table #chart-attr-' + index).length) {
				// once is enough
			}
			else {
				// force tables to scroll	
				var table = $('#chart-attr-' + index);
				var rowCount = table.parents('.facet-container').find('.row-count').attr('data-row-count');
				if (rowCount > 10) {
					table.tableScroll({
						height: 400
					});
				}
			}
		}).on('mouseout', '.table-scrollers span', function() {
			var target = $(this).parents('.tablescroll').find('.tablescroll_wrapper');
			target.stop();
		}).on('mouseover', '.table-scrollers span', function() {
			var target = $(this).parents('.tablescroll').find('.tablescroll_wrapper');
			var targetPos = target.scrollTop();
			var scrollEnd = target.find('tbody').height();
			var scrollVal = 1;
			var speed = 5;
			if ($(this).hasClass('up')) {
				target.stop().animate({
					scrollTop: 0
				}, 3000, "linear");
			}
			else {
				target.stop().animate({
					scrollTop: scrollEnd
				}, 3000, "linear");
			}
		}).on('click', '.table-scrollers span', function() {
			var target = $(this).parents('.tablescroll').find('.tablescroll_wrapper');
			var scrollEnd = target.find('tbody').height();
			target.stop();
			if ($(this).hasClass('up')) {
				target.scrollTop(0);
			}
			else {
				target.scrollTop(scrollEnd);
			}
		})
		// DOM ready...
		.ready(function() {
			var target = $('html');
			//PJAX Magic...
			/*pjax.connect({
				'parseJS': true,
				'container': 'result-tabs',
				'excludeClass':'pass',
				'beforeSend': function(e) {
					target.addClass('fb-loading');
					$('html, body').animate({scrollTop:0},333);
				},
				'complete': function(e) {
					target.removeClass('fb-loading').addClass('fb-loading-after');
					setTimeout(function() {target.removeClass('fb-loading-after');}, 1);
					createIcons();
					cleanFacetsNavigation();
				},
			});*/
			//createIcons();
			//cleanFacetsNavigation();
		}); //End of Daisy Chain
});