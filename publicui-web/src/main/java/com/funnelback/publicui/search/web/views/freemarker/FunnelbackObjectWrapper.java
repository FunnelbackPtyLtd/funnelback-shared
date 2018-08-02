package com.funnelback.publicui.search.web.views.freemarker;

import com.google.common.collect.ListMultimap;

import freemarker.template.AdapterTemplateModel;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.MapKeyValuePairIterator;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleCollection;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx2;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.WrappingTemplateModel;

/**
 * Custom object wrapper which knows how to handle any special object
 * types that Funnelback uses.
 */
public class FunnelbackObjectWrapper extends DefaultObjectWrapper {

    public FunnelbackObjectWrapper() {
        super(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        // That default is apparently to maintain compatibility with freemarker 2.3.0.
        // For now we'll keep doing that because I think it's what we had before.
        //
        // It might be valuable to investigate what the changes are and whether
        // they are things that would be worth more than whatever pain
        // switching to them might cause...Someday.
    }

    @Override
    protected TemplateModel handleUnknownType(final Object obj) throws TemplateModelException {
        if (obj instanceof ListMultimap) {
            // We only support String keyed maps - Freemarker wouldn't work with anything else anyway I think.
            @SuppressWarnings("unchecked")
            ListMultimap<String, ?> stringKeyedMultimap = (ListMultimap<String, ?>) obj;
            return new ListMultimapAdapter(stringKeyedMultimap, this);
        }
        
        return super.handleUnknownType(obj);
    }

    /**
     * An adapter which allows Freemarker to treat Guava ListMulitmaps as if they
     * were a Map<String, List<Object>>.
     */
    public class ListMultimapAdapter extends WrappingTemplateModel implements TemplateHashModelEx2, AdapterTemplateModel {

        private final ListMultimap<String, ?> multimap;

        public ListMultimapAdapter(ListMultimap<String, ?> multimap, ObjectWrapper objectWrapper) {
            super(objectWrapper);
            this.multimap = multimap;
        }

        @Override
        public Object getAdaptedObject(Class<?> hint) {
            return multimap;
        }

        @Override
        public TemplateModel get(String key) throws TemplateModelException {
            return wrap(multimap.get(key));
        }

        @Override
        public boolean isEmpty() throws TemplateModelException {
            return multimap.isEmpty();
        }

        @Override
        public int size() throws TemplateModelException {
            return multimap.size();
        }
        
        public TemplateCollectionModel keys() {
            return new SimpleCollection(multimap.keySet(), getObjectWrapper());
        }

        public TemplateCollectionModel values() {
            return new SimpleCollection(multimap.values(), getObjectWrapper());
        }

        public KeyValuePairIterator keyValuePairIterator() {
            return new MapKeyValuePairIterator(multimap.asMap(), getObjectWrapper());
        }
    }

}