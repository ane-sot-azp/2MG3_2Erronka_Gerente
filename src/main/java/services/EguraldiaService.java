package services;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class EguraldiaService {
    public static final String DEFAULT_XML_PATH = "c:\\Users\\irait\\Documents\\2MG3\\2Erronka\\2MG3_2Erronka\\Eguraldi XML\\Eguraldia.xml";

    public static EguraldiInfo loadDonostiaGaur() {
        return loadCityGaur("Donostia", DEFAULT_XML_PATH);
    }

    public static EguraldiInfo loadCityGaur(String cityContains, String xmlPath) {
        try {
            File file = new File(xmlPath);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            String azkenEguneraketa = root.getAttribute("doneDate");

            Element todayForecast = findForecastByDay(root, "today");
            if (todayForecast == null) {
                todayForecast = findFirstForecast(root);
            }

            String egunaTestua = textOfFirst(todayForecast, "forecastDateText");

            Element cityNode = findCityForecast(todayForecast, cityContains);
            if (cityNode == null) {
                return new EguraldiInfo("Donostia", egunaTestua, azkenEguneraketa, 0, 0, "Ez dago daturik", "");
            }

            String cityName = cityNode.getAttribute("cityName");
            int tempMax = parseIntSafe(textOfFirst(cityNode, "tempMax"), 0);
            int tempMin = parseIntSafe(textOfFirst(cityNode, "tempMin"), 0);

            Element symbol = firstChildElement(cityNode, "symbol");
            String descriptionEu = "";
            String symbolCode = "";
            if (symbol != null) {
                Element descriptions = firstChildElement(symbol, "descriptions");
                if (descriptions != null) {
                    descriptionEu = textOfFirst(descriptions, "eu");
                }
                String symbolImage = textOfFirst(symbol, "symbolImage");
                symbolCode = extractSymbolCode(symbolImage);
            }

            if (descriptionEu == null || descriptionEu.isBlank()) descriptionEu = "—";
            if (egunaTestua == null || egunaTestua.isBlank()) egunaTestua = "Gaur";

            return new EguraldiInfo(cityName, egunaTestua.trim(), azkenEguneraketa, tempMin, tempMax, descriptionEu.trim(), symbolCode);
        } catch (Exception e) {
            return new EguraldiInfo("Donostia", "Gaur", "", 0, 0, "Errorea eguraldia irakurtzean: " + e.getMessage(), "");
        }
    }

    private static Element findForecastByDay(Element root, String forecastDay) {
        NodeList forecasts = root.getElementsByTagName("forecast");
        for (int i = 0; i < forecasts.getLength(); i++) {
            Node n = forecasts.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) continue;
            Element e = (Element) n;
            if (forecastDay.equalsIgnoreCase(e.getAttribute("forecastDay"))) {
                return e;
            }
        }
        return null;
    }

    private static Element findFirstForecast(Element root) {
        NodeList forecasts = root.getElementsByTagName("forecast");
        for (int i = 0; i < forecasts.getLength(); i++) {
            Node n = forecasts.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) return (Element) n;
        }
        return null;
    }

    private static Element findCityForecast(Element forecast, String cityContains) {
        if (forecast == null) return null;
        NodeList cityList = forecast.getElementsByTagName("cityForecastData");
        for (int i = 0; i < cityList.getLength(); i++) {
            Node n = cityList.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) continue;
            Element e = (Element) n;
            String name = e.getAttribute("cityName");
            if (name != null && name.toLowerCase().contains(cityContains.toLowerCase())) {
                return e;
            }
        }
        return null;
    }

    private static String textOfFirst(Element parent, String tagName) {
        if (parent == null) return "";
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() == 0) return "";
        Node node = list.item(0);
        return node != null ? node.getTextContent() : "";
    }

    private static Element firstChildElement(Element parent, String tagName) {
        if (parent == null) return null;
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() == 0) return null;
        Node n = list.item(0);
        return n.getNodeType() == Node.ELEMENT_NODE ? (Element) n : null;
    }

    private static int parseIntSafe(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String extractSymbolCode(String symbolImagePath) {
        if (symbolImagePath == null) return "";
        String s = symbolImagePath.trim();
        int slash = s.lastIndexOf('/');
        if (slash >= 0) s = s.substring(slash + 1);
        int dot = s.indexOf('.');
        if (dot > 0) s = s.substring(0, dot);
        return s;
    }
}
