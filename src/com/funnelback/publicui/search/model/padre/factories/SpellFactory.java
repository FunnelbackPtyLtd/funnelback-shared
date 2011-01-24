package com.funnelback.publicui.search.model.padre.factories;

import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.XmlStreamUtils;
import com.funnelback.publicui.search.model.padre.Spell;

public class SpellFactory {

	public static Spell fromMap(Map<String, String> data) {
		return new Spell(data.get(Spell.Schema.URL), data.get(Spell.Schema.TEXT));
	}
	
	public static Spell fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws NumberFormatException, XMLStreamException {
		return fromMap(XmlStreamUtils.tagsToMap(Spell.Schema.SPELL, xmlStreamReader));
	}
}
