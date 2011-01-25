Lambok for beans
EhCache

Reminders:
----------

* Lookup for config files in both collection config directory and default profile
  * default profile takes precedence if both exists
  
TODO:
-----

* Check the status of submitted patches to see if we can use the latest version of these library instead of a custom one:
  * JNA: https://jna.dev.java.net/issues/show_bug.cgi?id=169
  * Waffle: http://waffle.codeplex.com/workitem/8559

* Check license implications of re-using a Waffle ServletFilter

* Waffle:
  * Impersonation using Basic method won't allow network access until the logon type is changed from LOGON_TYPE_NETWORK to LOGON_TYPE_NETWORK_CLEARTEXT
  * Test impersonation with delegation (Kerberos)

* Look at Funnelback::SearchInterface::pre_search() to replement logic as an Interceptor ?

* Remove test shorcuts
  * /_/* request mapping on SearchController

* Find a way to externally configure some web.xml features:
  * Auth filter

* Click tracking
  * CGI transform
  * Unique identifier
  * click.cgi supported "url" and "index_url" for the target url. Only "index_url" has been reimplemented
  * file:// URLs on Windows improperly parsed (See: http://stackoverflow.com/questions/1131273/java-file-touri-tourl-on-windows-file)
    * Could be tested on the funnelback_documentation collection.
  * Check how the logging of local:// URL is done.
    * It seems that the URL that arrives in click.cgi is without the local:// prefix (directly "serve-document..."
    * But in the resulting click logs there is the "local://" prefix !?
    
* Data binding: Find out how to use a custom DataBinder, to move some code to the data binding stage:
  * PassThroughParametersInputProcessor
  * Set SearchQuestion.authenticated depending of the request UserPrincipal
  * Set query to NULL when it's an empty String

* Early binding DLS:
  * Check what plugins could we reimplement in Java?
  * Have a way to have per-collection specific plugins (Groovy ?)
