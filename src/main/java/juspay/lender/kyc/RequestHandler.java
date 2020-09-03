package juspay.lender.kyc;


import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.LocalFileHeader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


@RestController
public class RequestHandler {

    public OfflinePaperLessKycData readXML(String data) throws IOException, SAXException, ParserConfigurationException {

        DocumentBuilder newDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document parse = newDocumentBuilder.parse(new ByteArrayInputStream(data.getBytes()));

        Element root = parse.getDocumentElement();
        Node Poi = root.getElementsByTagName("Poi").item(0);
        Node Poa = root.getElementsByTagName("Poa").item(0);
        NamedNodeMap PAttr = Poa.getAttributes();
        Node Pht = root.getElementsByTagName("Pht").item(0);
        // XML Version
        String version = parse.getXmlVersion();
        // User Data
        String name = Poi.getAttributes().getNamedItem("name").getNodeValue();
        String referenceId = root.getAttribute("referenceId");
        String photo = Pht.getTextContent();
        String dob = Poi.getAttributes().getNamedItem("dob").getNodeValue();
        String email = Poi.getAttributes().getNamedItem("e").getNodeValue();
        String mobile = Poi.getAttributes().getNamedItem("m").getNodeValue();
        String gender = Poi.getAttributes().getNamedItem("gender").getNodeValue();
        // Address
        String careof = PAttr.getNamedItem("careof").getNodeValue();
        String country = PAttr.getNamedItem("country").getNodeValue();
        String dist = PAttr.getNamedItem("dist").getNodeValue();
        String house = PAttr.getNamedItem("house").getNodeValue();
        String loc = PAttr.getNamedItem("loc").getNodeValue();
        String pc = PAttr.getNamedItem("pc").getNodeValue();
        String po = PAttr.getNamedItem("po").getNodeValue();
        String state = PAttr.getNamedItem("state").getNodeValue();
        String street = PAttr.getNamedItem("street").getNodeValue();
        String subdist = PAttr.getNamedItem("subdist").getNodeValue();
        String vtc = PAttr.getNamedItem("vtc").getNodeValue();// careof + country + dist + house + loc + pc + po + state + street + subdist + vtc
        // Signature
        String signature = parse.getElementsByTagName("SignatureValue").item(0).getTextContent();

        return new OfflinePaperLessKycData(version, name, referenceId, photo, dob, email, mobile, gender, new Address(careof, country, dist,house,loc,pc,po,state,street,subdist,vtc), signature);
    }

    public Boolean validate(String xmlData, String publicKeyPath) {
        DocumentBuilderFactory documentBuilderFactory = new DocumentBuilderFactoryImpl();
        documentBuilderFactory.setNamespaceAware(true);

        DocumentBuilder newDocumentBuilder = null;
        try {
            newDocumentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = newDocumentBuilder.parse(new ByteArrayInputStream(xmlData.getBytes()));
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
            throw new Exception("Cannot find Signature element");
        }
        FileInputStream keyFileIn = new FileInputStream(publicKeyPath);
        CertificateFactory cerFile = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) cerFile.generateCertificate(keyFileIn);
        PublicKey publicKey = certificate.getPublicKey();

        DOMValidateContext valContext = new DOMValidateContext(publicKey, nl.item(0));
        XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
        XMLSignature signature = factory.unmarshalXMLSignature(valContext);
        Boolean coreValidity = signature.validate(valContext);
        return coreValidity;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in parsing");
            return false;
        }

    }



    @PostMapping("/")
    public ResponseEntity<?> submit(@RequestParam("okyc") MultipartFile okyc, @RequestParam("udyam") MultipartFile udyam, @RequestParam("okycShareCode") String okySchareCode) {

        try {
            // XML is Extracted
            ZipInputStream okycZipStream = new ZipInputStream(okyc.getInputStream(), okySchareCode.toCharArray());
            LocalFileHeader okyclocalFileHeader = okycZipStream.getNextEntry();
            if(okyclocalFileHeader != null) {
                InputStreamReader isReader = new InputStreamReader(okycZipStream);
                BufferedReader reader = new BufferedReader(isReader);
                StringBuffer xmlData = new StringBuffer();
                String lineData;
                while((lineData = reader.readLine())!= null) {
                    xmlData.append(lineData);
                }
                // XML data parsed
                OfflinePaperLessKycData data = readXML(xmlData.toString());
            }else{
                System.out.println("Zip File does not contain anything");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Zip File Error");
            }

            // udyam is Extracted
            ZipInputStream udyamZipStream = new ZipInputStream(udyam.getInputStream());
            LocalFileHeader udyamlocalFileHeader = udyamZipStream.getNextEntry();
            if(udyamlocalFileHeader != null) {
                File targetFile = new File("udyam.pdf");
                OutputStream outStream = new FileOutputStream(targetFile);
                byte[] buffer = new byte[2048];
                int bytesRead;
                while((bytesRead = udyamZipStream.read(buffer))!=-1){
                    outStream.write(buffer,0,bytesRead);
                }
                return ResponseEntity.ok("Accepted");
            }else{
                System.out.println("Zip File does not contain anything");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Zip File Error");
            }

        } catch ( IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        } catch (SAXException | ParserConfigurationException e){
            System.out.println("Wrong Password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong Password for Zip");
        }
    }

}
