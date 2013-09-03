import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class TestTrigger implements com.funnelback.publicui.curator.trigger.GroovyTriggerInterface {
  def boolean activatesOn(SearchTransaction searchTransaction, Map<String, Object> properties) {
      if (searchTransaction.question.query.contains("triggertrue")) {
          return true;
      } else {
          return false;
      }
  }
}