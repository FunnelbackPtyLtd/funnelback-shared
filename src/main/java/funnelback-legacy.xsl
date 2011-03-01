<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema">
<xsl:output media-type="text/xml" encoding="UTF-8" method="xml" indent="yes" cdata-section-elements="title summary"/>

<xsl:template match="/com.funnelback.publicui.search.model.transaction.SearchTransaction">
	<PADRE_result_packet>
		<xsl:apply-templates select="response/resultPacket/details" />
		
		<query><xsl:value-of select="response/resultPacket/query" /></query>
  		<query_as_processed><xsl:value-of select="response/resultPacket/queryAsProcessed" /></query_as_processed>
  		
  		<xsl:apply-templates select="response/resultPacket/resultsSummary" />
  		
  		<xsl:apply-templates select="response/resultPacket/rmcs" />
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
  		<xsl:if test="response/resultPacket/phlusterElapsedTime">
  			<phluster_elapsed_time><xsl:value-of select="response/resultPacket/phlusterElapsedTime" /> sec.</phluster_elapsed_time>
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

<xsl:template match="response/resultPacket/rmcs">
	<xsl:for-each select="entry">
		<rmc>
			<xsl:attribute name="item">
				<xsl:value-of select="string" />
			</xsl:attribute>
			<xsl:value-of select="int" />
		</rmc>
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
		<xsl:for-each select="com.funnelback.publicui.search.model.padre.Result">
			<result>
				<rank><xsl:value-of select="rank" /></rank>
				<score><xsl:value-of select="score" /></score>
				<title><xsl:value-of select="title" /></title>
				<collection><xsl:value-of select="collection" /></collection>
				<component><xsl:value-of select="component" /></component>
				<click_tracking_url><xsl:value-of select="clickTrackingUrl" /></click_tracking_url>
				<live_url><xsl:value-of select="liveUrl" /></live_url>
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

</xsl:stylesheet>