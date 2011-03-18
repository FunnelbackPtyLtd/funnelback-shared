...

<c:if test="${SearchTransaction.response == null}">
  <!-- NO QUERY SPECIFIED -->
  <div id="fb-initial">
    <a href="http://funnelback.com/"><img src="img/funnelback.png" alt="Funnelback logo"></a>
  </div>
</c:if>

...

<c:choose>
  <c:when test="${SearchTransaction.response.resultPacket.resultsSummary.totalMatching > 0}">
    <span class="fb-result-count" id="fb-page-start">
    	${SearchTransaction.response.resultPacket.resultsSummary.currStart}
    </span> -
    <span class="fb-result-count" id="fb-page-end">
    	${SearchTransaction.response.resultPacket.resultsSummary.currEnd}
    </span> of
    <span class="fb-result-count" id="fb-total-matching">
    	${SearchTransaction.response.resultPacket.resultsSummary.totalMatching}
    </span> search results 
  </c:when>
  <c:otherwise>
    <span class="fb-result-count" id="fb-total-matching">0</span> search results
  </c:otherwise>
</c:choose>
for <strong>{query}</strong>

...