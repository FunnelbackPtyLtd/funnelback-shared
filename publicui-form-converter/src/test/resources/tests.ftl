<#ftl encoding="utf-8" />
<#import "/share/freemarker/funnelback_legacy.ftl" as s/>


<@s.BestBets>Best bets:Best bets content</@s.BestBets>

<@s.boldicize bold=abc>Camel case boldicize</@s.boldicize>

<@s.italicize italics=abc>Italicize tag</@s.italicize>
<@s.italicize italics=def>Camel case italicize</@s.italicize>

<@s.cut cut="abc">Camel case cut</@s.cut>

${Camel case URLEncode?url}

${htmlDecode(htmldecode)}
${htmlDecode(htmldecode)}