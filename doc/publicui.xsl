<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:import href="docbook-xsl-ns-1.76.1/html/docbook.xsl" />
	<xsl:import href="docbook-xsl-ns-1.76.1/html/highlight.xsl" />

	<xsl:template match="ulink[@type='issue']">
		<xsl:element name="a">
			<xsl:attribute name="href">https://jira.funnelback.com/browse/<xsl:value-of
				select="." /></xsl:attribute>
			<xsl:value-of select="." />
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>