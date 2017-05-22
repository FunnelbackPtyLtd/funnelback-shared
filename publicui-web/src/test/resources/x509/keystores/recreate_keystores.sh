#!/bin/bash -x


## Cleanup from last time

rm -f server-keystore.jks client-truststore.jks client-keystore.jks server-truststore.jks client-keystore-untrusted.jks server-certificate.p12 client-certificate.p12 server-certificate-for-curl.pem client-keystore-for-curl.pfx client-keystore-untrusted-for-curl.pfx


## Setup the server and client keystores and truststores

# Create a self-signed certificate for jetty to use
keytool -genkey -dname "CN=localhost, OU=Funnelback R&D, O=Funnelback, L=Canberra, ST=ACT, C=AU" -alias jetty -keyalg RSA -validity 3650 -keypass funnelback -storepass funnelback -keystore server-keystore.jks

# Export the certificate jetty will use
keytool -exportcert -alias "jetty" -keypass funnelback -storepass funnelback -keystore server-keystore.jks -file server-certificate.p12

# Import the server's certificate to the client's truststore
keytool -import -alias "localhost-certificate" -file server-certificate.p12 -noprompt -keypass funnelback -storepass funnelback -keystore client-truststore.jks

# Create a client certificate in the client's keystore
keytool -genkey -dname "CN=testuser, OU=Funnelabck R&D, O=Funnelback, L=Canberra, ST=ACT, C=AU" -alias "client-alias" -keyalg RSA -validity 3650 -keypass funnelback -storepass funnelback -keystore client-keystore.jks

# Export the client's certificate
keytool -exportcert -alias "client-alias" -storepass funnelback -keypass funnelback -storepass funnelback -keystore client-keystore.jks -file client-certificate.p12

# Export the server's certificate in a form curl can use
keytool -exportcert -rfc -keystore server-keystore.jks -storepass funnelback -alias jetty -file server-certificate-for-curl.pem

# Import the client's certicicate into the server's truststore
keytool -import -trustcacerts -alias "client-alias" -file client-certificate.p12 -noprompt -keypass funnelback -storepass funnelback -keystore server-truststore.jks

# Create an untrusted client certificate in the client's keystore
keytool -genkey -dname "CN=testuser, OU=Funnelabck R&D, O=Funnelback, L=Canberra, ST=ACT, C=AU" -alias "client-alias" -keyalg RSA -validity 3650 -keypass funnelback -storepass funnelback -keystore client-keystore-untrusted.jks


## Stuff just for curl

# Export the server's certificate in a form curl can use
keytool -exportcert -rfc -keystore server-keystore.jks -storepass funnelback -alias jetty -file server-certificate-for-curl.pem

# Export the client's certificate and key in a form curl can use (in case we want to try it manually)
# You can do it with `curl "https://localhost:8443/s/search.html" --cacert server-certificate-for-curl.pem --cert client-keystore-untrusted-for-curl.pfx`
keytool -importkeystore -srckeystore client-keystore.jks -srcalias "client-alias" -srcstorepass funnelback -destkeystore client-keystore-for-curl.pfx -deststoretype PKCS12 -deststorepass funnelback -destkeypass funnelback

# Export the untrusted client's certificate and key in a form curl can use (in case we want to try it manually)
# You can do it with `curl "https://localhost:8443/s/search.html" --cacert server-certificate-for-curl.pem --cert client-keystore-untrusted-for-curl.pfx:funnelback`
keytool -importkeystore -srckeystore client-keystore-untrusted.jks -srcalias "client-alias" -srcstorepass funnelback -destkeystore client-keystore-untrusted-for-curl.pfx -deststoretype PKCS12 -deststorepass funnelback -destkeypass funnelback


## Remove intermediate files

rm server-certificate.p12 client-certificate.p12