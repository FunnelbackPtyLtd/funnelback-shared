<#compress>
<#if callback??>${callback}(</#if>[
<#list suggestions as s>{
  "key": "${s.key?json_string}",
  "disp": <#if s.displayType.toString() != "J">"${s.display?json_string}"<#else>${s.display}</#if>,
  "disp_t": "${s.displayType?json_string}",
  "wt": "${s.weight?json_string}",
  "cat": "${s.category?json_string}",
  "cat_t": "${s.categoryType?json_string}",
  "action": "${s.action?json_string}",
  "action_t": "${s.actionType?json_string}"
}<#if s_has_next>,</#if>
</#list>]<#if callback??>)</#if></#compress>