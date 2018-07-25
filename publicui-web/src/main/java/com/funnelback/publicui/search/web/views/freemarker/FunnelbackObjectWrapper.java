package com.funnelback.publicui.search.web.views.freemarker;

import com.google.common.collect.ListMultimap;

import freemarker.template.AdapterTemplateModel;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.MapKeyValuePairIterator;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleCollection;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateHashModelEx2;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.WrappingTemplateModel;
import freemarker.template.TemplateHashModelEx2.KeyValuePairIterator;

public class FunnelbackObjectWrapper extends DefaultObjectWrapper {

    public FunnelbackObjectWrapper() {
        super(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
    }

    @Override
    protected TemplateModel handleUnknownType(final Object obj) throws TemplateModelException {
        if (obj instanceof ListMultimap) {
            return new ListMultimapAdapter((ListMultimap<String, ?>) obj, this);
        }

        return super.handleUnknownType(obj);
    }

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