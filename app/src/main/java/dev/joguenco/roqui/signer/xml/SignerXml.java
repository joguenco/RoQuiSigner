package dev.joguenco.roqui.signer.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class SignerXml {

    public Document sign(byte[] certificate, String password, byte[] archivoXML) {
        var signerXAdESXml = new SignerXAdESXml();
        var resourcesForSign = new ResourcesForSign();

        InputStream xmlInputStream = new ByteArrayInputStream(archivoXML);

        resourcesForSign.setDocumentXmlToSign(parseXmlDocument(xmlInputStream));
        resourcesForSign.setCertificate(certificate);
        resourcesForSign.setPassword(password);
        resourcesForSign.setIdToSign("id");

        return signerXAdESXml.sign(resourcesForSign);
    }

    Document parseXmlDocument(InputStream inputStream) {

        var dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            return db.parse(inputStream);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
