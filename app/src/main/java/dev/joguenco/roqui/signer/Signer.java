package dev.joguenco.roqui.signer;

import dev.joguenco.roqui.signer.xml.SignerXml;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

public class Signer {

    public void sign(
            String pathDigitalCertificate,
            String password,
            String pathXmlFile,
            String pathOutPutXmlFileSigned)
            throws IOException {

        var signerXml = new SignerXml();

        byte[] certificate = Files.readAllBytes(Paths.get(pathDigitalCertificate));
        byte[] xmlFile = Files.readAllBytes(Paths.get(pathXmlFile));

        Document documentSigned = signerXml.sign(certificate, password, xmlFile);
        saveSignedDocument(documentSigned, pathOutPutXmlFileSigned);
    }

    void saveSignedDocument(Document document, String path) {
        try {
            var transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            var source = new DOMSource(document);

            var writer = new FileWriter(path);
            var result = new StreamResult(writer);

            transformer.setOutputProperty("encoding", "UTF-8");
            transformer.transform(source, result);

        } catch (IOException | TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
