<#macro facetCategoryType categoryType>
	<div class="category-type">
		<#if categoryType.label?exists && categoryType.categories?size &gt; 0>
			<h4>${categoryType.label}</h4>
		</#if>
		<#list categoryType.categories as category>
			<div class="category">
				<#assign checked = "" />
				<#if SearchTransaction.question.selectedCategories[category.urlParams?split("=")[0]]?exists>
					<#if SearchTransaction.question.selectedCategories[category.urlParams?split("=")[0]]?seq_contains(category.label)>
						<#assign checked="checked=\"checked\"" />
					</#if>
				</#if>
				<input type="checkbox" name="${category.urlParams?split("=")[0]}" value="${category.urlParams?split("=")[1]}" ${checked} />
				<span class="categoryName">										
					<a href="?query=${SearchTransaction.question.query}&amp;collection=${SearchTransaction.question.collection.id}&amp;start_rank=${SearchTransaction.response.resultPacket.resultsSummary.currStart}&num_ranks=${SearchTransaction.response.resultPacket.resultsSummary.numRanks}&${category.urlParams}">${category.label}</a>
				</span>
				&nbsp;<span class="fb-facet-count">(<span class="categoryCount">${category.count}</span>)
				
				<div class="sub-categories">
					<#list categoryType.subCategoryTypes as subCategoryType>
						<@facetCategoryType subCategoryType />
					</#list>
				</div>
				
			</div>
		</#list>
		
		<#if categoryType.subCategoryTypes?size &gt; 0>
			<div class="category apart">
				<#list categoryType.subCategoryTypes as subCategoryType>
					<@facetCategoryType subCategoryType />
				</#list>
			</div>
		</#if>
		
		<span class="moreOrLessCategories">
			<a href="#">more...</a>
		</span>
	</div>
</#macro>