// Funnelback query terms highlight plugin
// Copyright Funnelback, 2012

jQuery.fn.fbhighlight = function(options) {

    var settings = jQuery.extend( {
        'element': 'strong',
        'className': 'fb-highlight'
    } , options);

    // Converts an URL to an associative array containing the
    // query string parameters
    function urlToArray(url) {
      var request = {};
      var pairs = url.substring(url.indexOf('?') + 1).split('&');
      for (var i = 0; i < pairs.length; i++) {
          var pair = pairs[i].split('=');
          request[decodeURIComponent(pair[0])] = decodeURIComponent(pair[1]);
      }
      return request;
    }

    // Get highlight regexes to use
    var params = urlToArray(document.location.href);
    // Strip of inline flags not supported by Javascript
    // and split on pipe separator
    var regexes = params['hl'].replace(/\(.+\)/g, '').split(/\|/);
    
    // Highlight term on each selected element
    this.each( function() {
        for (var i=0; i<regexes.length; i++) {
            // Use different CSS class for each term
            jQuery(this).highlight('unused', {
                element: settings.element,
                pattern: regexes[i],
                className: settings.className + ' ' + settings.className + (i+1),
            });
        }
    });

    // Display terms that we highlight in the control bar, *after* we've
    // highlighted the terms in the document, and highlight them too.
    // Do *not* apply the 'highlight' class to them to prevent those
    // instances being counted in the prev/next navigation
    // Remove word boundaries \b and other escape characters before display
    jQuery('#fb-highlight-control em').text(regexes.join(' ').replace(/\\./g, ''));
    for (var i=0; i<regexes.length; i++) {
        jQuery('#fb-highlight-control em').highlight('unused', {
            element: settings.element,
            pattern: regexes[i],
            className: settings.className + (i+1),
        });
    }

    // Index of currently shown instance
    var scrolled = -1;
    var maxScrolled = jQuery('.'+settings.className).size()-1;

    jQuery('#fb-highlight-control .fb-highlight-nav a.next').click( function(event) {
        event.preventDefault();

        // Wrap on last
        if (++scrolled > maxScrolled) {
            scrolled = 0;
        }

        jQuery('body').animate({
            scrollTop: jQuery('body .'+settings.className+':eq('+scrolled+')').offset().top
        }, 250);
    });

    jQuery('#fb-highlight-control .fb-highlight-nav a.prev').click( function(event) {
        event.preventDefault();

        // Wrap on first
        if (--scrolled < 0) {
            scrolled = maxScrolled;
        }

        jQuery('body').animate({
            scrollTop: jQuery('body .'+settings.className+':eq('+scrolled+')').offset().top
        }, 250);
    });

    jQuery('#fb-highlight-control a.back').attr('href',
        document.referrer ? document.referrer : 'javascript:history.back()' );

    jQuery('#fb-highlight-control').show();

    return this;

};
