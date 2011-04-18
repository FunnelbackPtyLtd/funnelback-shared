	<script type="text/javascript">
		// Number of categories to display
		var displayedCategories = {defaultDisplayedCategories};
	
		jQuery(document).ready(function() {
		    jQuery('div.{facetClassName}').each( function() {
		        jQuery(this).children('div.{categoryClassName}:gt('+(displayedCategories-1)+')').css('display', 'none');
		    });
		
		    jQuery('.{linkClassName}>a').each( function() {
		        var nbCategories = jQuery(this).parent().parent().children('div.{categoryClassName}').size();
		        if ( nbCategories <= displayedCategories ) {
		            jQuery(this).css('display', 'none');
		        } else {
		            jQuery(this).css('display', 'inline');
		            jQuery(this).click( function() {
		                if (jQuery(this).text().indexOf('more...') < 0) {
		                    jQuery(this).parent().parent().children('div.{categoryClassName}:gt('+(displayedCategories-1)+')').css('display', 'none');
		                    jQuery(this).text('more...');
		                } else {
		                    jQuery(this).parent().parent().children('div.{categoryClassName}').css('display', 'block');
		                    jQuery(this).text('less...');
		                }
		            });
		        }
		    });
		});
	</script>
