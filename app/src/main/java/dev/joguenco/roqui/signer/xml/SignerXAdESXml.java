package dev.joguenco.roqui.signer.xml;

import dev.joguenco.roqui.signer.provider.KeyStoreManager;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.reference.CanonicalizationTransform;
import eu.europa.esig.dss.xades.reference.DSSReference;
import eu.europa.esig.dss.xades.reference.EnvelopedSignatureTransform;
import eu.europa.esig.dss.xades.signature.XAdESService;
import eu.europa.esig.dss.xml.utils.XMLCanonicalizer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SignerXAdESXml {

    private static final DigestAlgorithm DIGEST_ALGORITHM = DigestAlgorithm.SHA256;
    private static final String DOCUMENT_NAME = "document.xml";

    public Document sign(ResourcesForSign resourcesForSign) {
        var keyStoreManager =
                new KeyStoreManager(
                        resourcesForSign.getCertificate(), resourcesForSign.getPassword());

        DSSDocument documentToSign =
                new InMemoryDocument(
                        serialize(resourcesForSign.getDocumentXmlToSign()), DOCUMENT_NAME);
        XAdESSignatureParameters parameters =
                createSignatureParameters(resourcesForSign, documentToSign, keyStoreManager);

        var xadesService = new XAdESService(new CommonCertificateVerifier());
        ToBeSigned dataToSign = xadesService.getDataToSign(documentToSign, parameters);
        SignatureValue signatureValue =
                sign(dataToSign, parameters, keyStoreManager.getPrivateKey());
        DSSDocument signedDocument =
                xadesService.signDocument(documentToSign, parameters, signatureValue);

        try {
            return parse(signedDocument.openStream().readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private XAdESSignatureParameters createSignatureParameters(
            ResourcesForSign resourcesForSign,
            DSSDocument documentToSign,
            KeyStoreManager keyStoreManager) {
        var parameters = new XAdESSignatureParameters();
        parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
        parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED);
        parameters.setDigestAlgorithm(DIGEST_ALGORITHM);
        parameters.setSigningCertificate(
                new CertificateToken(keyStoreManager.getSigningCertificate()));
        parameters.setCertificateChain(toCertificateTokens(keyStoreManager.getCertificateChain()));
        parameters.setReferences(List.of(createReference(resourcesForSign, documentToSign)));
        return parameters;
    }

    private DSSReference createReference(
            ResourcesForSign resourcesForSign, DSSDocument documentToSign) {
        Element rootElement = resourcesForSign.getDocumentXmlToSign().getDocumentElement();
        String id = rootElement.getAttribute(resourcesForSign.getIdToSign());

        var reference = new DSSReference();
        reference.setUri("#" + id);
        reference.setContents(documentToSign);
        reference.setDigestMethodAlgorithm(DIGEST_ALGORITHM);
        reference.setTransforms(
                List.of(
                        new EnvelopedSignatureTransform(),
                        new CanonicalizationTransform(XMLCanonicalizer.DEFAULT_DSS_C14N_METHOD)));
        return reference;
    }

    private SignatureValue sign(
            ToBeSigned dataToSign, XAdESSignatureParameters parameters, PrivateKey privateKey) {
        try {
            var signatureAlgorithm = parameters.getSignatureAlgorithm();
            var signature = Signature.getInstance(signatureAlgorithm.getJCEId());
            signature.initSign(privateKey);
            signature.update(dataToSign.getBytes());
            return new SignatureValue(signatureAlgorithm, signature.sign());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<CertificateToken> toCertificateTokens(List<X509Certificate> certificates) {
        return certificates.stream().map(CertificateToken::new).toList();
    }

    private byte[] serialize(Document document) {
        try {
            var transformerFactory = TransformerFactory.newInstance();
            var transformer = transformerFactory.newTransformer();
            var outputStream = new ByteArrayOutputStream();
            transformer.setOutputProperty("encoding", "UTF-8");
            transformer.transform(new DOMSource(document), new StreamResult(outputStream));
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Document parse(byte[] xml) {
        try {
            var documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            return documentBuilderFactory.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
