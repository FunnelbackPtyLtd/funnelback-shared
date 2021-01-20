package com.funnelback.publicui.xml;

import java.util.List;

import com.funnelback.publicui.search.model.transaction.Facet;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class FacetConverter implements Converter {
    
    @Override
    public boolean canConvert(Class type) {
        return type.equals(Facet.class);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        // Rather than manually write to the writer like the doco would suggest you do
        // simply make the object we want then tell XStream to serialise that instead.
        context.convertAnother(new FacetWithFields((Facet) source));
    }

    /**
     * XStream will only serialise fields so this is a Facet with selected and unselected fields for values
     *
     */
    static class FacetWithFields extends Facet {
        
        private List<CategoryValue> selectedValues;
        
        protected FacetWithFields(Facet facet) {
            super(facet.getName(), facet.getUnselectAllUrl(), facet.getSelectionType(), 
                facet.getConstraintJoin(), facet.getOrder(), facet.getFacetValues(), 
                facet.getGuessedDisplayType(), facet.getCustomComparator());
            // For anything where the class has a defined field e.g. List categories = new List(); we need to re-add everything.
            this.getCustomData().putAll(facet.getCustomData());
            
            this.getAllValues().addAll(facet.getAllValues());
            // Now add set the fields that we want serialised.
            this.selectedValues = facet.getSelectedValues();
        }
        
    }
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {    
        return null;
    }

}
