#!/bin/bash

rm -f server-keystore.jks client-truststore.jks client-keystore.jks server-truststore.jks client-keystore-untrusted.jks server-certificate.p12 client-certificate.p12

# Create a self-signed certificate for jetty to use
keytool -genkey -dname "CN=localhost, OU=Funnelback R&D, O=Funnelback, L=Canberra, ST=ACT, C=AU" -alias jetty -keyalg RSA -validity 3650 -keypass funnelback -storepass funnelback -keystore server-keystore.jks

# Export the certificate jetty will use
keytool -exportcert -alias "jetty" -keypass funnelback -storepass funnelback -keystore server-keystore.jks > server-certificate.p12

# Import the server's certificate to the client's truststore
keytool -import -alias "localhost-certificate" -file server-certificate.p12 -noprompt -keypass funnelback -storepass funnelback -keystore client-truststore.jks

# Create a client certificate in the client's keystore
keytool -genkey -dname "CN=testuser, OU=Funnelabck R&D, O=Funnelback, L=Canberra, ST=ACT, C=AU" -alias "client-alias" -keyalg RSA -validity 3650 -keypass funnelback -storepass funnelback -keystore client-keystore.jks

# Export the client's certificate
keytool -exportcert -alias "client-alias" -storepass funnelback -keypass funnelback -storepass funnelback -keystore client-keystore.jks > client-certificate.p12

# Import the client's certicicate into the server's truststore
keytool -import -trustcacerts -alias "client-alias" -file client-certificate.p12 -noprompt -keypass funnelback -storepass funnelback -keystore server-truststore.jks

# Create an untrusted client certificate in the client's keystore
keytool -genkey -dname "CN=testuser, OU=Funnelabck R&D, O=Funnelback, L=Canberra, ST=ACT, C=AU" -alias "client-alias" -keyalg RSA -validity 3650 -keypass funnelback -storepass funnelback -keystore client-keystore-untrusted.jks

rm server-certificate.p12 client-certificate.p12