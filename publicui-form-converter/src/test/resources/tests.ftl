<#ftl encoding="utf-8" />
<#import "/share/freemarker/funnelback_legacy.ftl" as s/>


<@s.BestBets>Best bets:Best bets content</@s.BestBets>

<@s.boldicize bold=abc>Camel case boldicize</@s.boldicize>

<@s.italicize italics=abc>Italicize tag</@s.italicize>
<@s.italicize italics=def>Camel case italicize</@s.italicize>

<@s.cut cut="abc">Camel case cut</@s.cut>

<@s.URLEncode>${Camel case URLEncode}</@s.URLEncode>

<@s.HtmlDecode>${htmldecode}</@s.HtmlDecode>
<@s.HtmlDecode>${htmldecode}</@s.HtmlDecode>

<@s.CurrentDate></@s.CurrentDate>
<@s.Date prefix="ABC"></@s.Date>
<@s.Date></@s.Date>

<@s.rss>link</@s.rss>
<@s.rss>button</@s.rss>