// Please use jquery.funnelback-completion.js in preference to this
// It is kept only to maintain compatibility with old form files.
jQuery.noConflict();
jQuery(document).ready( function() {
    var boldRegExp = new RegExp('<strong>(.*)</strong>');
    var urlRegExp = new RegExp('href="([^"]*)"');

    if (window.fb_completion && window.fb_completion.enabled == 'enabled' ) {
        jQuery('#query').autocomplete( {
            source: function (request, response) {
                jQuery.ajax({
                    type:       'GET',
                    url:        window.fb_completion.program
                                    + '?collection=' + window.fb_completion.collection
                                    + '&partial_query=' + request.term.replace(/ /g, '+')
                                    + '&show=' + window.fb_completion.show
                                    + '&sort=' + window.fb_completion.sort
                                    + '&alpha=' + window.fb_completion.alpha
                                    + '&fmt=json'
                    ,
                    dataType:   'json',
                    success:    function(data) {
                        // Suggestions from padre-qs
                        var dynamics = new Array();
                        // Suggestions from a static list
                        var statics = new Array();

                        var regex = new RegExp(request.term, 'i');

                        // Boldicize search term
                        var replacement = '<strong>' + request.term + '</strong>';
     
                        for (var i=0; i<data.length; i++) {
                            var out;
                            var suggestion = data[i];

                            if (suggestion.u) {
                                // Has URL field, static suggestion
                                var word = suggestion.s.replace(regex, replacement);
                                if (suggestion.t) { word = suggestion.t; } 
                                out = '<a class="url" href="' + suggestion.u + '">' + word + '</a>';

                                if (suggestion.d) { out += '<a class="description"><span>' + suggestion.d + '</span></a>'; }

                                statics.push(out);
                            } else {
                                // Dynamic suggestion
                                out = suggestion.replace(regex, replacement);
                                dynamics.push(out);
                            }
                        }
                        response (dynamics.concat(statics));
                    }
                });
            },

            delay: window.fb_completion.delay,

            minLength: window.fb_completion.length,

            focus: function(evt, ui) {
                // Don't replace input value if suggestion contains an URL
                if (ui.item.value.search(/href/) > -1) { return false; }
                // Un-boldicize
                ui.item.value = ui.item.value.replace(boldRegExp, '$1');
            },

            select: function(evt, ui) {
                if (ui.item.value.search(/href/) > -1) {
                    // If suggestion contains an URL, navigate to it
                    document.location.href = ui.item.value.match(urlRegExp)[1];
                    return false;
                }
                // Un-boldicize
                jQuery(this).attr('value', ui.item.value.replace(boldRegExp, '$1'));
                // Submit the form on select
                jQuery(this).context.form.submit();
            }
        });
    }

});

