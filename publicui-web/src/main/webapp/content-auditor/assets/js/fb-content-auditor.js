// fb-content-auditor.js
// Functions
function createIcons() {
		var target = $('#fb-facets-navigation .nav-stacked .facetLabel');
		target.find('.fa-circle-thin').remove();
		target.each(function(key, elem) {
			$(elem).prepend('<span class="fa fa-circle-thin"></span>&nbsp;');
		});
	}
	// jQuery 
if (console) console.log('fb-content-auditor.js is initiated.');
jQuery(function() {
	$('[data-toggle="tooltip"]').tooltip();
	$(document).on('click', '#fb-facet-details tbody tr', function() {
			var anchor = $(this).find('td:first-child a');
			//if(anchor.length > -1){
			// var href = anchor.attr('href');
			//window.location = href;
			//window.open(href, '_blank');
			//}  
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
		}).on('click', '.table-row-clickable tr td:not(.table-hide)', function(e) {
			if (!$(this).hasClass('fa-stack')) {
				window.location = $(this).find('.clickable-link').attr('href');
			}
		})
		// bootstrap tab listener. on shown event.
		// mainly needed to update AM Charts
		.on('shown.bs.tab', 'a[data-toggle="tab"]', function(e) {
			// This is for AMCharts and if the tab element has an attribute of 'data-chart_ref'
			var chart = $(this).attr('data-chart_ref');
			var index = chart.replace(/\D/g, '');
			// look in the content_auditor js object for our charts. Force them to display on tab change
			if (content_auditor[chart]) {
				//console.log(content_auditor[chart]);
				content_auditor[chart].invalidateSize();
			}
			if ($('.bootstrap-table #chart-attr-' + index).length) {
				// once is enough
			}
			else {
				// force tables to scroll	
				var table = $('#chart-attr-' + index);
				var rowCount = table.parents('.facet-container').find('.row-count').attr('data-row-count');
				//alert(count);
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
			//target.css({background:"#ff0033"});
			var targetPos = target.scrollTop();
			var scrollEnd = target.height();
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
			var scrollEnd = target.height();
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
			/*$('.facet-search-chart').affix({
    offset: {
      top: $('.facet-search-chart').offset().top;
    , bottom: function () {
        return (this.bottom = $('.footer').outerHeight(true))
      }
    }
  });*/
			//DOM is ready...
			var target = $('html');
			//PJAX Magic...
			pjax.connect({
				'parseJS': true,
				'container': 'result-tabs',
				'beforeSend': function(e) {
					target.addClass('fb-loading');
					$('html, body').animate({
						scrollTop: 0
					}, 333);
				},
				'complete': function(e) {
					target.removeClass('fb-loading').addClass('fb-loading-after');
					setTimeout(function() {
						target.removeClass('fb-loading-after');
					}, 1);
					createIcons();
				},
			});
			createIcons();
		}); //End of Daisy Chain
});