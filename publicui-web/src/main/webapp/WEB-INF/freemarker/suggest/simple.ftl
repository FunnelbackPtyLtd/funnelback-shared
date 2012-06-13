<#compress>
<#if callback??>${callback}(</#if>[
  <#list suggestions as s>"${s.key}"<#if s_has_next>,</#if></#list>
]<#if callback??>)</#if></#compress>