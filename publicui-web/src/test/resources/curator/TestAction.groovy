import java.util.Map;

import com.funnelback.publicui.search.model.curator.config.Action.Phase;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class TestAction implements com.funnelback.publicui.curator.action.GroovyActionInterface {
  def boolean runsInPhase(Phase phase, Map<String, Object> properties) {
      return Phase.INPUT.equals(phase);
  }
  
  def void performAction(SearchTransaction searchTransaction, Phase phase, Map<String, Object> properties) {
      searchTransaction.question.query = "modified";
  }
}