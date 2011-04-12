Lambok for beans
EhCache

Reminders:
----------

* Lookup for config files in both collection config directory and default profile
  * default profile takes precedence if both exists

Benchmark:
----------

ab -c <x> -n 1000 http:// ....

* Extract response time:  grep "(mean)" *.txt | grep Time | cut -d '-' -f 2 |sort -n | cut -d ':' -f 3 | cut -d "[" -f 1
* Extract processing time: grep "(mean," *.txt | cut -d '-' -f 2 |sort -n | cut -d ':' -f 3 | cut -d "[" -f 1
  
Apache conf:

      # NG: 1 Feb 2001: Public UI tests
    <Proxy balancher://mycluster>
        BalancerMember http://localhost:8585
        BalancerMember http://localhost:8586
        BalancerMember http://localhost:8587
        BalancerMember http://localhost:8588
    </Proxy>
    ProxyPass /publicui/ balancher://mycluster/publicui/ nocanon

    <Location /balancer-manager>
        SetHandler balancer-manager
        Order Deny,Allow
        Deny from all
        Allow from 121.127.216.199
    </Location>
  
  
TODO:
-----

* Check the status of submitted patches to see if we can use the latest version of these library instead of a custom one:
  * JNA: https://jna.dev.java.net/issues/show_bug.cgi?id=169
  * Waffle: http://waffle.codeplex.com/workitem/8559

* Check license implications of re-using a Waffle ServletFilter

* Waffle:
  * Impersonation using Basic method won't allow network access until the logon type is changed from LOGON_TYPE_NETWORK to LOGON_TYPE_NETWORK_CLEARTEXT
  * Test impersonation with delegation (Kerberos)

* Remove test shorcuts
  * /_/* request mapping on SearchController

* Find a way to externally configure some web.xml features:
  * Auth filter
  
* XML logging
  * Improve fault tolerance of writing an existing XML log (algorithm to search the closing </log> tag)

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

* Dynamic query processor options
  * Are currently set on top of collection.cfg's option
  * They should probably be merged, with one taking precedence on the other ?
  
* CollectionFormViewResolver: find a way to activate cache on it

* Collection config reloading: Find a more clever mechanism for reloading only a specific config file and/or lazy loading.

* Form tags:
  * Implement CURRENT_DATE range in <Select> tag