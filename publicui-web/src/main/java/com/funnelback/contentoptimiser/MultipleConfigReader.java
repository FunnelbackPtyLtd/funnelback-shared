package com.funnelback.contentoptimiser;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;



public interface MultipleConfigReader<T> {

    Map<String, T> read(List<String> fileNames);

    Map<String, T> read(List<String> fileNames, Set<String> mustExist) throws FileNotFoundException;

}
