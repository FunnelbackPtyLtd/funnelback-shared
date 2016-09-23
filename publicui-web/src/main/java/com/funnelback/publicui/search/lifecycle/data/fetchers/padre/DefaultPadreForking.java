package com.funnelback.publicui.search.lifecycle.data.fetchers.padre;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreQueryStringBuilder;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.ExecutionReturn;
import com.funnelback.publicui.xml.XmlParsingException;

/**
 * <p>Default implementation of {@link AbstractPadreForking}.</p>
 * 
 * <p>Builds a standard query string and update the main data model with
 * PADRE output.</p>
 */
@Component
public class DefaultPadreForking extends AbstractPadreForking {

    @Override
    protected String getQueryString(SearchTransaction transaction) {
        return new PadreQueryStringBuilder(transaction.getQuestion(), true).buildQueryString();
    }

    @Override
    protected void updateTransaction(SearchTransaction transaction, ExecutionReturn padreOutput) throws XmlParsingException {
        
        transaction.getResponse().setResultPacket(padreXmlParser.parse(
            padreOutput.getOutBytes(),
            padreOutput.getCharset(),
            transaction.getQuestion().getInputParameterMap().containsKey(RequestParameters.DEBUG)));
        transaction.getResponse().setReturnCode(padreOutput.getReturnCode());
    }

    public void setI18n(I18n i18n) {
        this.i18n = i18n;
    }


}
