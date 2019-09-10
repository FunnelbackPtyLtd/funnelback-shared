package com.funnelback.publicui.search.model.transaction.padrecmd;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Holds the command and env given to padre-sw
 * 
 * To use this
 * 
 * export "QUERY_STRING=blah"
 * then run what is in command
 *
 */
@AllArgsConstructor
@Getter
public class DefaultPadreSwCmd implements PadreSwCmd {

    private List<String> command;
    private Map<String, String> env;
    
}
