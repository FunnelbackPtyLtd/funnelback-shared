<#ftl encoding="utf-8" />
<#import "/share/freemarker/funnelback_legacy.ftl" as s/>

<head>

	<script type="text/javascript">
		// Number of categories to display
		var displayedCategories = 5;
	
		jQuery(document).ready(function() {
		    jQuery('div.facet-class').each( function() {
		        jQuery(this).children('div.category-class:gt('+(displayedCategories-1)+')').css('display', 'none');
		    });
		
		    jQuery('.moreless-class>a').each( function() {
		        var nbCategories = jQuery(this).parent().parent().children('div.category-class').size();
		        if ( nbCategories <= displayedCategories ) {
		            jQuery(this).parent().css('display', 'none');
		        } else {
		            jQuery(this).css('display', 'inline');
		            jQuery(this).click( function() {
		                if (jQuery(this).text().indexOf('more...') < 0) {
		                    jQuery(this).parent().parent().children('div.category-class:gt('+(displayedCategories-1)+')').css('display', 'none');
		                    jQuery(this).text('more...');
		                } else {
		                    jQuery(this).parent().parent().children('div.category-class').css('display', 'block');
		                    jQuery(this).text('less...');
		                }
		            });
		        }
		    });
		});
	</script>

</head>


<@s.BestBets>Best bets:Best bets content</@s.BestBets>

<@s.boldicize bold=abc>Camel case boldicize</@s.boldicize>

<@s.italicize italics=abc>Italicize tag</@s.italicize>
<@s.italicize italics=def>Camel case italicize</@s.italicize>

<@s.cut cut="abc">Camel case cut</@s.cut>

<@s.URLEncode>${Camel case URLEncode}</@s.URLEncode>

<@s.HtmlDecode>${htmldecode}</@s.HtmlDecode>
<@s.HtmlDecode>${htmldecode}</@s.HtmlDecode>

<@s.CurrentDate></@s.CurrentDate>
<@s.Date prefix="ABC"></@s.Date>
<@s.Date></@s.Date>

<@s.rss>link</@s.rss>
<@s.rss>button</@s.rss>

<@s.FormChoice></@s.FormChoice>

<@s.FacetedSearch>
<div id="fb-facets">
<@s.Facet class="facet-class">
	
	
	<h3><@s.FacetLabel></@s.FacetLabel></h3>
	<@s.Category sortBy="dummy" class="category-class" max=10>
		<@s.CategoryName></@s.CategoryName>&nbsp;<span class="fb-facet-count">(<@s.CategoryCount></@s.CategoryCount>)</span>
	</@s.Category>
	<@s.Category max=10>
		<@s.CategoryName></@s.CategoryName>&nbsp;<span class="fb-facet-count">(<@s.CategoryCount></@s.CategoryCount>)</span>
	</@s.Category>
	<@s.MoreOrLessCategories></@s.MoreOrLessCategories>
</@s.Facet>
</div>
</@s.FacetedSearch>

<!-- Old old tags -->
<@s.BestBets>Best bets:FP content</@s.BestBets>
<@s.ContextualNavigation></@s.ContextualNavigation>
${s.result.something}
<#if s.result.something_else?exists>
A multi
line
thing
</#if>
<#if ! s.result.something?exists>a single line</#if>
<#if s.result.collection == "mycollection">A single line</#if>
<#if s.result.collection != "mycollection">Multi
line
statement</#if>
${s.cluster.href}