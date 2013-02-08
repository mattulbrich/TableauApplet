
# Passwort war "keystore"
jar cmf MANIFEST.MF tablet.jar -C classes .
jarsigner tablet.jar mykey
