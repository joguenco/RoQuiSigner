package dev.joguenco.roqui.signer.xml;

import org.w3c.dom.Document;

public class ResourcesForSign {
    private Document documentXmlToSign;

    private String idToSign;

    private byte[] certificate;

    private String password;

    public Document getDocumentXmlToSign() {
        return this.documentXmlToSign;
    }

    public void setDocumentXmlToSign(Document documentXmlToSign) {
        this.documentXmlToSign = documentXmlToSign;
    }

    public String getIdToSign() {
        return this.idToSign;
    }

    public void setIdToSign(String idToSign) {
        this.idToSign = idToSign;
    }

    public byte[] getCertificate() {
        return this.certificate;
    }

    public void setCertificate(byte[] certificate) {
        this.certificate = certificate;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
