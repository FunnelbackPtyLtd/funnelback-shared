<#compress>
<#if callback??>${callback}(</#if>[
<#list suggestions as s>{
  "key": "${s.key}",
  "disp": <#if s.displayType.toString() != "J">"</#if>${s.display}<#if s.displayType.toString() != "J">"</#if>,
  "disp_t": "${s.displayType}",
  "wt": "${s.weight}",
  "cat": "${s.category}",
  "cat_t": "${s.categoryType}",
  "action": "${s.action}",
  "action_t": "${s.actionType}"
}<#if s_has_next>,</#if>
</#list>]<#if callback??>)</#if></#compress>