package com.funnelback.publicui.search.lifecycle.input.processors.explore;

import java.io.IOException;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Component;

import com.funnelback.collection.update.execute.utils.PadreExploreQueryGenerator;
import com.funnelback.publicui.search.model.collection.Collection;

@Component
@Log4j2
public class ForkingGenerator implements ExploreQueryGenerator {
    
    @Getter(value=AccessLevel.PROTECTED) 
    private PadreExploreQueryGenerator padreExploreQueryGenerator = new PadreExploreQueryGenerator();
    
    @Override
    public String getExploreQuery(Collection c, String url, Integer nbOfTerms) {
        Optional<String> optional;
        try {
            optional = getPadreExploreQueryGenerator().getExploreQuery(c.getConfiguration(), url, nbOfTerms);
            if(optional.isPresent()) {
                return optional.get();
            }
        } catch (IOException e) {
            log.error("Error when getting padre explore query", e);
        }
        return null;
    }

}
