package dev.joguenco.roqui.signer.provider;

import dev.joguenco.roqui.signer.exception.InvalidCertificateException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class KeyStoreManager {

    private static final String KEY_STORE_TYPE = "pkcs12";

    private final KeyStore keyStore;
    private final char[] password;
    private final CertificateLocalProvider certificateLocalProvider =
            new CertificateLocalProvider();

    public KeyStoreManager(byte[] certificate, String password) {
        this.password = password.toCharArray();
        this.keyStore = createKeyStore(certificate);
    }

    private KeyStore createKeyStore(byte[] certificate) {
        try (var certificateInputStream = new ByteArrayInputStream(certificate)) {
            var keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
            keyStore.load(certificateInputStream, password);
            return keyStore;
        } catch (IOException
                | NoSuchAlgorithmException
                | CertificateException
                | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public X509Certificate getSigningCertificate() {
        return certificateLocalProvider.selectCertificate(getAvailableCertificates());
    }

    public PrivateKey getPrivateKey() {
        try {
            return (PrivateKey) keyStore.getKey(getSigningAlias(), password);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new InvalidCertificateException(
                    "No se pudo obtener la llave privada del certificado");
        }
    }

    public List<X509Certificate> getCertificateChain() {
        try {
            var certificateChain = new ArrayList<X509Certificate>();
            Certificate[] chain = keyStore.getCertificateChain(getSigningAlias());
            if (chain != null) {
                for (Certificate certificate : chain) {
                    certificateChain.add((X509Certificate) certificate);
                }
            }
            return certificateChain;
        } catch (KeyStoreException e) {
            throw new InvalidCertificateException("No se pudo obtener la cadena del certificado");
        }
    }

    private String getSigningAlias() {
        var signingCertificate = getSigningCertificate();
        for (String alias : getKeyAliases()) {
            try {
                if (signingCertificate.equals(keyStore.getCertificate(alias))) {
                    return alias;
                }
            } catch (KeyStoreException e) {
                throw new InvalidCertificateException("No se pudo leer el certificado digital");
            }
        }
        throw new InvalidCertificateException("No existe un proveedor local del certificado");
    }

    private List<X509Certificate> getAvailableCertificates() {
        var certificates = new ArrayList<X509Certificate>();
        for (String alias : getKeyAliases()) {
            try {
                Certificate certificate = keyStore.getCertificate(alias);
                if (certificate instanceof X509Certificate x509Certificate) {
                    certificates.add(x509Certificate);
                }
            } catch (KeyStoreException e) {
                throw new InvalidCertificateException("No se pudo leer el certificado digital");
            }
        }
        return certificates;
    }

    private List<String> getKeyAliases() {
        try {
            var aliases = new ArrayList<String>();
            Enumeration<String> enumeration = keyStore.aliases();
            while (enumeration.hasMoreElements()) {
                String alias = enumeration.nextElement();
                if (keyStore.isKeyEntry(alias)) {
                    aliases.add(alias);
                }
            }
            return aliases;
        } catch (KeyStoreException e) {
            throw new InvalidCertificateException("No se pudo leer el certificado digital");
        }
    }
}
