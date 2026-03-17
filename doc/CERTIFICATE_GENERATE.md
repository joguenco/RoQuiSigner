# Generate and export certificates - Linux (strongSwan).

## Requirements
GNU/Linux

## Attention is only for test.
For generate your own certificate, you have the following next steps.

1. Generate certificate with a certificate hierarchy using OpenSSL (Recommended)
Generate the root CA certificate and private key.
```
openssl req -new -x509 -out rootCA.crt -keyout rootCA.key
```
Generate the intermediate CA certificate and private key, signed by the root CA
```
openssl req -new -keyout intermediateCA.key -out intermediateCA.csr
```
```
openssl x509 -req -in intermediateCA.csr -CA rootCA.crt -CAkey rootCA.key -CAcreateserial -out intermediateCA.crt
```
Generate the leaf certificate and private key, signed by the intermediate CA.
```
openssl req -new -keyout leaf.key -out leaf.csr
```
```
openssl x509 -req -in leaf.csr -CA intermediateCA.crt -CAkey intermediateCA.key -CAcreateserial -out leaf.crt
```
Finally, to create the .p12 certificate file, use the following command.
```
openssl pkcs12 -export -out certificate.p12 -inkey leaf.key -in leaf.crt -certfile intermediateCA.crt -CAfile rootCA.crt
```
Example custom identifier
```
openssl req -new -keyout private.key -out cert.csr -subj "/C=US/ST=California/L=San Francisco/O=Example Corp/CN=www.example.com"
```
```
openssl req -new -key key.pem -out cert.csr -subj "/CN=example.com/O=My Organization/UID=12345678"
```
```
openssl req -new -key key.pem -out cert.csr -config openssl.cnf
```
```
[req]
distinguished_name = req_distinguished_name
req_extensions = v3_req

[req_distinguished_name]

[v3_req]
subjectAltName = email:user@example.com
```


2. Install strongSwan.
```
sudo apt install strongswan
```
```
sudo apt install strongswan-pki
```
```
sudo apt install libstrongswan-extra-plugins
```

## Linux CLI instructions (strongSwan).
### Generate the CA certificate.
```
ipsec pki --gen --outform pem > caKey.pem
ipsec pki --self --in caKey.pem --dn "CN=Jorge Luis" --ca --outform pem > caCert.pem
```
### Print the CA certificate in base64 format.
```
openssl x509 -in caCert.pem -outform der | base64 -w0 ; echo
```
### Generate the user certificate.
```
export PASSWORD="No_Piratear"
export USER_CERTIFICATE="Hacker"

ipsec pki --gen --outform pem > "${USER_CERTIFICATE}Key.pem"
ipsec pki --pub --in "${USER_CERTIFICATE}Key.pem" | ipsec pki --issue --cacert caCert.pem --cakey caKey.pem --dn "CN=${USER_CERTIFICATE}" --san "${USER_CERTIFICATE}" --flag clientAuth --outform pem > "${USER_CERTIFICATE}Cert.pem"
```
### Generate a p12 bundle containing the user certificate.
```
openssl pkcs12 -in "${USER_CERTIFICATE}Cert.pem" -inkey "${USER_CERTIFICATE}Key.pem" -certfile caCert.pem -export -out "${USER_CERTIFICATE}.p12" -password "pass:${PASSWORD}"
```
