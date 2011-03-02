<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output
	media-type="text/xml"
	encoding="UTF-8"
	method="xml"
	indent="yes"
	cdata-section-elements="title summary bb_title bb_desc md"
	standalone="no" />

<xsl:template match="/com.funnelback.publicui.search.model.transaction.SearchTransaction">
	<PADRE_result_packet>
		<xsl:if test="error">
			<details><padre_version>Unknown</padre_version></details>
			<error>
				<xsl:if test="error/reason = 'InvalidCollection'">
					<usermsg>Please specify a valid collection to search</usermsg>
					<adminmsg>The collection parameter is invalid or missing</adminmsg>
				</xsl:if>
			</error>
		</xsl:if>
		
		<xsl:if test="not(error) and not(question/query)">
			<details><padre_version>Unknown</padre_version></details>
			<error>
				<usermsg>Please specify a valid query</usermsg>
				<adminmsg>The query parameter is invalid or missing</adminmsg>
			</error>
		</xsl:if>
		
		<xsl:if test="not(error) and question/query">
			
			<xsl:apply-templates select="response/resultPacket/details" />
			
			<query><xsl:value-of select="response/resultPacket/query" /></query>
	  		<query_as_processed><xsl:value-of select="response/resultPacket/queryAsProcessed" /></query_as_processed>
	  		
	  		<xsl:apply-templates select="response/resultPacket/resultsSummary" />
	  		
	  		<xsl:apply-templates select="response/resultPacket/spell" />
	  		
			<xsl:apply-templates select="response/resultPacket/bestBets" />
	  		
	  		<xsl:apply-templates select="response/resultPacket/rmcs" />
	  		<xsl:apply-templates select="response/resultPacket/urlCounts" />
	  		<xsl:apply-templates select="response/resultPacket/gScopeCounts" />
	  		
	  		<xsl:apply-templates select="response/resultPacket/results" />
	  		
	  		<include_scope>
	  			<xsl:for-each select="response/resultPacket/includeScopes/string">
	  				<xsl:value-of select="." />
	  				<xsl:if test="not(position() = last())">@</xsl:if>
	  			</xsl:for-each>
	  		</include_scope>
	  		
	  		<exclude_scope>
	  			<xsl:for-each select="response/resultPacket/excludeScopes/string">
	  				<xsl:value-of select="." />
	  				<xsl:if test="not(position() = last())">@</xsl:if>
	  			</xsl:for-each>
	  		</exclude_scope>
	  		
	  		<query_processor_codes><xsl:value-of select="response/resultPacket/queryProcessorCodes" /></query_processor_codes>
	  		<padre_elapsed_time><xsl:value-of select="response/resultPacket/padreElapsedTime" /></padre_elapsed_time>
	  		<xsl:apply-templates select="response/resultPacket/contextualNavigation" />
	  		
	  		
	  		<xsl:if test="response/resultPacket/phlusterElapsedTime">
	  			<phluster_elapsed_time><xsl:value-of select="format-number(response/resultPacket/phlusterElapsedTime, '0.000')" /> sec.</phluster_elapsed_time>
	  		</xsl:if>
	  	</xsl:if>
	</PADRE_result_packet>
</xsl:template>

<xsl:template match="response/resultPacket/details">
	<details>
		<padre_version><xsl:value-of select="padreVersion" /></padre_version>
		<collection_size><xsl:value-of select="collectionSize" /></collection_size>
		<collection_updated><xsl:value-of select="collectionUpdated" /></collection_updated>
	</details>
</xsl:template>

<xsl:template match="response/resultPacket/resultsSummary">
	<results_summary>
		<fully_matching><xsl:value-of select="fullyMatching" /></fully_matching>
		<estimated_hits><xsl:value-of select="estimatedHits" /></estimated_hits>
		<partially_matching><xsl:value-of select="partiallyMatching" /></partially_matching>
		<total_matching><xsl:value-of select="totalMatching" /></total_matching>
		<num_ranks><xsl:value-of select="numRanks" /></num_ranks>
		<currstart><xsl:value-of select="currStart" /></currstart>
		<currend><xsl:value-of select="currEnd" /></currend>
		<nextstart><xsl:value-of select="nextStart" /></nextstart>
	</results_summary>
</xsl:template>

<xsl:template match="response/resultPacket/spell">
	<spell>
		<url><xsl:value-of select="url" /></url>
		<text><xsl:value-of select="text" /></text>
	</spell>
</xsl:template>

<xsl:template match="response/resultPacket/bestBets">
	<best_bets>
		<xsl:for-each select="com.funnelback.publicui.search.model.padre.BestBet">
		<bb>
			<bb_trigger><xsl:value-of select="trigger" /></bb_trigger>
			<bb_link><xsl:value-of select="link" /></bb_link>
			<bb_title><xsl:value-of select="title" /></bb_title>
			<bb_desc><xsl:value-of select="description" /></bb_desc>		
		</bb>		
		</xsl:for-each>
	</best_bets>
</xsl:template>

<xsl:template match="response/resultPacket/rmcs">
	<xsl:for-each select="entry">
		<xsl:sort select="int" data-type="number" order="descending" />
		<rmc>
			<xsl:attribute name="item">
				<xsl:value-of select="string" />
			</xsl:attribute>
			<xsl:value-of select="int" />
		</rmc>
	</xsl:for-each>
</xsl:template>

<xsl:template match="response/resultPacket/urlCounts">
	<xsl:for-each select="entry">
		<xsl:sort select="int" data-type="number" order="descending" />
		<urlcount>
			<xsl:attribute name="item">
				<xsl:value-of select="string" />
			</xsl:attribute>
			<xsl:value-of select="int" />
		</urlcount>
	</xsl:for-each>
</xsl:template>

<xsl:template match="response/resultPacket/gScopeCounts">
	<gscope_counts>
		<xsl:for-each select="entry">
			<gscope_matching>
				<xsl:attribute name="value"><xsl:value-of select="int[1]" /></xsl:attribute>
				<xsl:value-of select="int[2]" />
			</gscope_matching>
		</xsl:for-each>
	</gscope_counts>
</xsl:template>

<xsl:template match="response/resultPacket/results">
	<results>
		<xsl:text>

</xsl:text>
		<xsl:for-each select="com.funnelback.publicui.search.model.padre.Result">
			<result>
				<rank><xsl:value-of select="rank" /></rank>
				<score><xsl:value-of select="score" /></score>
				<title><xsl:value-of select="title" /></title>
				<collection><xsl:value-of select="collection" /></collection>
				<component><xsl:value-of select="component" /></component>
				<click_tracking_url><xsl:value-of select="clickTrackingUrl" /></click_tracking_url>
				<live_url><xsl:value-of select="liveUrl" /></live_url>
				<xsl:for-each select="metaData/entry">
					<xsl:sort select="string[1]" />
					<md>
						<xsl:attribute name="f"><xsl:value-of select="string[1]" /></xsl:attribute>
						<xsl:value-of select="string[2]" />
					</md>
					
				</xsl:for-each>
				<summary><xsl:value-of select="summary" /></summary>
				<cache_url><xsl:value-of select="cacheUrl" /></cache_url>
				<xsl:if test="date">
					<date><xsl:value-of select="date" /></date>
				</xsl:if>
				<xsl:if test="not(date) or date = ''">
					<date>No Date</date>
				</xsl:if>
				<filesize><xsl:value-of select="fileSize" /></filesize>
				<filetype><xsl:value-of select="fileType" /></filetype>
				<tier><xsl:value-of select="tier" /></tier>
				<docnum><xsl:value-of select="docNum" /></docnum>
				<xsl:apply-templates select="quickLinks" />
			</result>
		</xsl:for-each>
	</results>
</xsl:template>

<xsl:template match="quickLinks">
	<quicklinks>
		<xsl:attribute name="domain"><xsl:value-of select="domain" /></xsl:attribute>
		<xsl:for-each select="quickLinks/com.funnelback.publicui.search.model.padre.QuickLinks_-QuickLink">
			<quicklink>
				<qltext><xsl:value-of select="text" /></qltext>
				<qlurl><xsl:value-of select="url" /></qlurl>
			</quicklink>
		</xsl:for-each>
	</quicklinks>
</xsl:template>

<xsl:template match="response/resultPacket/contextualNavigation">
	<contextual_navigation>
		<search_terms><xsl:value-of select="searchTerm" /></search_terms>
		<cluster_nav>
			<xsl:attribute name="level"><xsl:value-of select="clusterNav/level" /></xsl:attribute>
			<xsl:attribute name="url"><xsl:value-of select="clusterNav/url" /></xsl:attribute>
			<xsl:value-of select="clusterNav/label" />
		</cluster_nav>
		
		<xsl:for-each select="categories/com.funnelback.publicui.search.model.padre.Category">
			<category>
				<xsl:attribute name="name"><xsl:value-of select="name" /></xsl:attribute>
				<xsl:attribute name="more"><xsl:value-of select="more" /></xsl:attribute>
				
				<xsl:for-each select="clusters/com.funnelback.publicui.search.model.padre.Cluster">
					<cluster>
						<xsl:attribute name="href"><xsl:value-of select="href" /></xsl:attribute>
						<xsl:attribute name="count"><xsl:value-of select="count" /></xsl:attribute>
						<xsl:value-of select="label" />
					</cluster>
				</xsl:for-each>
				
				<xsl:if test="moreLink">
					<more_link>
						<xsl:attribute name="label"><xsl:value-of select="name" /></xsl:attribute>
						<xsl:value-of select="moreLink" />
					</more_link>
				</xsl:if>
				
				<xsl:if test="fewerLink">
					<fewer_link>
						<xsl:attribute name="label"><xsl:value-of select="name" /></xsl:attribute>
						<xsl:value-of select="fewerLink" />
					</fewer_link>
				</xsl:if>
				
			</category>
		</xsl:for-each>
	</contextual_navigation>
</xsl:template>


</xsl:stylesheet>