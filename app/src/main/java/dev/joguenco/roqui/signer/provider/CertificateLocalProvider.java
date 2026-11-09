package dev.joguenco.roqui.signer.provider;

import dev.joguenco.roqui.signer.exception.InvalidCertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

public class CertificateLocalProvider {

    private static final int DIGITAL_SIGNATURE_KEY_USAGE = 0;

    /**
     * This method is used for return the local certificate provider, this method no validate the
     * local provider
     */
    public X509Certificate selectCertificate(List<X509Certificate> availableCertificates) {
        if (availableCertificates != null)
            for (X509Certificate x509Certificate : availableCertificates) {
                var keyUsage = x509Certificate.getKeyUsage();
                if (keyUsage == null) {
                    return x509Certificate;
                }
                if (keyUsage[DIGITAL_SIGNATURE_KEY_USAGE]) return x509Certificate;
            }
        throw new InvalidCertificateException("No existe un proveedor local del certificado");
    }
}
