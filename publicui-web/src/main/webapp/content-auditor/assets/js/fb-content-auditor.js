/* fb-content-auditor.js */
// Functions

var delay = (function() {
    var timer = 0;
    return function(callback, ms) {
        clearTimeout(timer);
        timer = setTimeout(callback, ms);
    };
})();

function refreshModalContent() {
    var modal = $('#modal-overlay .modal-content');
    modal.find('table').addClass(
        'table table-hover table-condenesed table-responsive table-striped table-row-clickable');
}

function makePreviewIcons() {

    // If the class of 'thumbnails-on' is detected then add thumbnail previews to the results rows.
    if ($('body').hasClass('thumbnails-on')) {

        $.each($('#report-details tbody tr'), function(index, value) {
            var href = $(this).find('a').attr('href');
            var popoverContent = '<a href="' + href + '"><img src="/s/preview?url=' + href +
                '&width=350&height=230&render_width=350&render_height=230&type=keep_aspect"></a>';
            var html = '<a data-toggle="popover" data-placement="top"  href="' + href + '" data-html="true" target="_blank" class="open-thumbnail"><span class="fa-stack fa-xs"><i class="fa fa-square fa-stack-2x"></i><i class="fa fa-eye fa-stack-1x fa-inverse"></i></span></a>';
            $(this).find('.open-wcag').after(html).on('click', function() {
                return false;
            });
        });

    }
}

function makeChartScrollable(index) {
    // force tables to scroll   
    var table = $('#chart-attr-' + index);
    var rowCount = table.parents('.facet-container').find('.row-count').attr('data-row-count');
    if (rowCount > 13) {
        table.tableScroll({
            height: 592
        });
        $('body').attr('make-chart-scrollable', 1);
    }
}

function resetChartScrollable() {
    // reset scroll on scrollable tables    
    chart = $('#fb-facets-navigation ul.nav li.active a').attr('data-chart_ref');
    content_auditor[chart].invalidateSize();

    if (chart) {
        var index = chart.replace(/\D/g, '');
    } else {
        /* looks like we're in a different tab so open the first chart */
        index = 0;
    }


    var table = $('#chart-attr-' + index);
    table.tableScroll('undo');
    $('body').attr('make-chart-scrollable', 0);

    delay(function() {
        makeChartScrollable(index);
    }, 500);

}


function toggleSlice(item, chart) {
    content_auditor[chart].clickSlice(item);
}

function hoverSlice(item, chart) {
    content_auditor[chart].rollOverSlice(item);
}

function blurSlice(item, chart) {
    content_auditor[chart].rollOutSlice(item);
}


//jQuery 
jQuery(function() {
    //Need to initiate BS popover & tooltip manually for Twitter Bootstrap.
    $('[data-toggle="popover"]').popover();
    $('[data-toggle="tooltip"]').tooltip();
    $(window)
        .on('hashchange', function() {})
        .on('popstate', function() {
            var hash = '#' + location.hash.slice(1);
            $('a[href="' + hash + '"]').tab('show');
            $('body').attr('hash', hash);
        });





    $(window).on('resize', function() {


        resetChartScrollable();
    });


    //Start daisy chain for document
    $(document)
        // Refresh the content of TWBS Modal on load 

    .on('shown.bs.modal load', '#modal-overlay', function() {
            refreshModalContent();
        })
        // Event on tab change, need to update the current chart as AMCharts will try and display at 0 x 0 because it is hidden. We need to correct this when the selected tab comes ito focus. Also we need to scroll to the top of the page and also reference the current URL hash somewhere.  
        .on('shown.bs.tab', 'a[data-toggle="tab"]', function(e) {
            var hash = $(e.target).attr("href").substr(1);
            window.location.hash = hash;
            $('body').attr('hash', hash);
            // This is for AMCharts and if the tab element has an attribute of 'data-chart_ref'
            var chart = $(this).attr('data-chart_ref');
            if (chart) {
                index = chart.replace(/\D/g, '');
            } else {
                /* looks like we're in a different tab so open the first chart */
                index = 0;
            }
            // look in the content_auditor js object for our charts. Force them to display on tab change
            if (content_auditor[chart]) {
                content_auditor[chart].invalidateSize();
            }
            if ($('.bootstrap-table #chart-attr-' + index).length) {
                // once is enough
            } else {
                // force tables to scroll   
                makeChartScrollable(index);
            }
            window.scrollTo(0, 0);
        })

    //AJAX the pagination, why? Why wait for a page to re-load? 20 x Faster, the content is placed right in front of you. 
    .on('click', '.tab-pane .pagination li', function() {

            var t = $(this);
            var pane = t.parents('.tab-pane');
            var paneId = '#' + pane.attr('id');

            pane.addClass('loading background-danger');
            $('html').addClass('fb-loading');
            if (!t.hasClass('active')) {

                var href = t.find('a').attr('href');

                $.get(href + ' ' + paneId, function(data) {

                    var content = $(data).find(paneId).addClass('active');
                    $(paneId).replaceWith(content);
                    makePreviewIcons();
                    window.scrollTo(0, 0);
                    setTimeout(function(){
                        $('html').removeClass('fb-loading');
                    },333);
                });

                return false;
            }


        })
        .on('click', '#tab-nav-attributes', function() {
            chart = $('#fb-facets-navigation ul.nav li.active a').attr('data-chart_ref');
            content_auditor[chart].invalidateSize();
            var chart = $(this).attr('data-chart_ref');
            if (chart) {
                var index = chart.replace(/\D/g, '');
            } else {
                /* looks like we're in a different tab so open the first chart */
                index = 0;
            }
            makeChartScrollable(index);

        })
        .on('click', '[data-dismiss="modal"]', function() {
            var modal = $('.modal');
            modal.removeClass('in');
            setTimeout(function() {
                modal.hide();
            }, 888);
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
            } else {
                $.get('/s/anchors.html' + href + '&ajax=1', function(data) {}).done(function(data) {
                    targetOutput.html(data);
                    refreshModalContent();
                }).fail(function(error) {
                    targetOutput.html("<h3>Error " + error.status + '</h3><p>A processing error has occurred. Either the data is non-existent or has since been removed.</p><p><br><span class="btn btn-primary"  data-dismiss="modal"><i class="fa fa-remove"></i> Close</span></p>');
                });
            }
            return false;
        })
        .on('click', '#btn-facet-container', function() {
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
        })
        .on('change', '#sort', function() {
            var sortValue = $(this).val();
            var currentURL = $(this).parents('.form-field').attr('data-url');
            if (currentURL.indexOf('sort') === -1) {
                var href = currentURL.replace(/(&nocache=)[^\&]+/, '') + '&sort=' + sortValue;
            } else {
                href = currentURL.replace(/(sort=)[^\&]+/, '$1' + sortValue).replace(
                    /(&nocache=)[^\&]+/, '');
            }
            href = href + '&nocache=' + (new Date()).getTime();
            var hash = $('body').attr('hash');
            if (hash) {
                hash = '#' + hash;
            } else {
                hash = '';
            }
            window.location = href + hash;
        })
        .on('click', '.navbar-nav li a', function() {
            var target = $(this).parents('li');
            target.siblings('.active').removeClass('active');
            target.addClass('active');
        })
        .on('click', '#toggle-sidebar', function() {
            $('body').toggleClass('sidebar-closed');
            $('.navbar-collapse').toggleClass('in');
        })
        .on('click', '#toggle-search', function() {
            $('body').toggleClass('searchform-open');
            $('.navbar-collapse ').toggleClass('in');
            $('#query').focus();
        })
        .on('mouseenter', '#fb-facets-navigation', function() {
            // $(this).parent('div').removeClass('closed').addClass('the params that change need to be implemented onto this select box');
        })
        .on('mouseleave', '#fb-facets-navigation', function() {
            // $(this).parent('div').addClass('closed has-animated').removeClass('opened');
        })
        .on('focus', '.search-form input', function() {
            $(this).parents('form, div').addClass('has-focus');
        })
        .on('blur', '.search-form input', function() {
            $(this).parents('form, div').removeClass('has-focus');
        })
        // commented out as it may not be needed
        //.on('click', '.table-row-clickable tr', function(e) {
        //  if (!$(this).hasClass('fa-stack')) {
        //      window.open($(this).find('.clickable-link').attr('href'), '_blank')
        //  }
        // })

    // Force the scrolling of the scrollable table box to stop
    .on('mouseout', '.table-scrollers span', function() {
            var target = $(this).parents('.tablescroll').find('.tablescroll_wrapper');
            target.stop();
        })
        //Start scrolling the  scrollable table box..
        .on('mouseover', '.table-scrollers span', function() {
            var target = $(this).parents('.tablescroll').find('.tablescroll_wrapper');
            var targetPos = target.scrollTop();
            var scrollEnd = target.find('tbody').height();
            var scrollVal = 1;
            var speed = 5;
            if ($(this).hasClass('up')) {
                target.stop().animate({
                    scrollTop: 0
                }, 3000, "linear");
            } else {
                target.stop().animate({
                    scrollTop: scrollEnd
                }, 3000, "linear");
            }
        })
        //Take you to the end / top of the scrollable table box
        .on('click', '.table-scrollers span', function() {
            var target = $(this).parents('.tablescroll').find('.tablescroll_wrapper');
            var scrollEnd = target.find('tbody').height();
            target.stop();
            if ($(this).hasClass('up')) {
                target.scrollTop(0);
            } else {
                target.scrollTop(scrollEnd);
            }
        })

    //Load in the thumbnail for the preview. Why? so we can foresee the page the row is refering to
    .on('mouseenter', '.open-thumbnail', function() {
            var href = $(this).attr('href');
            var popoverContent = '<a class="thumb-preview img-responsive" href="' + href +
                '"><img src="/s/preview?url=' + href +
                '&width=310&height=160&render_width=1024&render_height=768&type=keep_aspect"></a>';
            $(this).popover({
                html: true,
                content: popoverContent,
                title: "<span class=\"fa fa-eye\"></span> Preview "
            });
            $(this).popover('show');
            return false;
        })
        // We could potentially make it do something here if it annoys users that when you click on it, it takes you straight to the resulting page. For now we can let it just be a link into the result...
        .on('click', '.open-thumbnail', function() {
            //return false; 
        })
        // Hide the thumbnail...
        .on('mouseleave', '.open-thumbnail', function() {
            $(this).popover('hide');
        })
        // DOM ready...
        .ready(function() {

            makePreviewIcons();

            var what = [];
            if ($('.drilled-to-last').length) {
                $.each($('.facet-search-details'), function(i, val) {
                    if ($(this).find('.page-attr').length) {
                        var a = [];
                        a.heading = $('#facet-' + i + ' .fb-facet-header h3').text().replace(
                            '\n', '').trim();
                        a.index = i;
                        what[i] = a;
                    } else {
                        what[i] = null;
                    }
                });
                console.log(what);
                var html = '';
                $.each(what, function(i, val) {
                    if (val !== null) {
                        html += '<li><a href="#facet-' + val.index +
                            '.tab-pane" aria-controls="profile" role="tab" data-toggle="tab" title="" aria-expanded="false"><span class="fa fa-long-arrow-right"></span> ' +
                            val.heading + '</a></li>';
                    }
                });
                var html =
                    '<hr><p>You can always drill down further in the following attributes: </p><ul class="list-nice">' + html + '</ul>';
                $('.drilled-to-last p').after(html);
            }
            //read the URL hash and open the tab.
            var hash = window.location.hash;
            if (hash) {
                var currentTab = $('a[href="' + hash + '"]');
                currentTab.tab('show');
                var id = hash;
                if (id.indexOf('facet-')) {
                    makeChartScrollable(hash.replace(/\D/g, ''));
                }
            } else {
                // Make chart navigation active     
                if (!$('#fb-facets-navigation li.active')) {
                    $('[data-chart_ref=chart_1]').parent('li').addClass('active');
                }
                makeChartScrollable(0);
            }
            window.scrollTo(0, 0);
            

            //PJAX, load pages via AJAX and change the URL 
             
            /* PJAX is turned off

            pjax.connect({
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
                    
                    makeChartScrollable(0);
                    window.scrollTo(0,0);
                    var chart = $('#fb-facets-navigation ul li.active a').attr('data-chart_ref');
                    alert(chart);
                    content_auditor[chart].invalidateSize();
                },
            });

            End of PJAX turned off */

        }); //End of Daisy Chain
});