package data;

import datastructures.CustomList;
import model.Document;
import search.Trie;
import similarity.DocumentSimilarity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * In-memory document storage and dataset manager.
 * Loads multilingual documents from UTF-8 text files and builds the search Trie.
 * Failsafe architecture ensures fallback data is available if files are missing.
 */
public class DocumentStore {

    private final CustomList<Document> documents;
    private final Trie prefixTrie;
    private final CustomList<String> vocabulary;
    private boolean isLoaded;

    public DocumentStore() {
        this.documents = new CustomList<>();
        this.prefixTrie = new Trie();
        this.vocabulary = new CustomList<>();
        this.isLoaded = false;
    }

    /**
     * Loads documents from standard data file paths or embedded fallback.
     */
    public synchronized void loadDocuments() {
        if (isLoaded && documents.size() > 0) {
            return;
        }

        documents.clear();
        vocabulary.clear();

        // Potential paths for documents.txt
        String[] candidatePaths = {
            "data/documents.txt",
            "../data/documents.txt",
            "MultilingualTextRetrievalSystem/data/documents.txt",
            System.getProperty("user.dir") + "/data/documents.txt"
        };

        boolean loadedFromFile = false;
        for (String path : candidatePaths) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                try (InputStream is = new FileInputStream(file);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    parseDocuments(reader);
                    loadedFromFile = true;
                    break;
                } catch (Exception e) {
                    System.err.println("Error reading " + path + ": " + e.getMessage());
                }
            }
        }

        // Failsafe fallback if file was not reachable
        if (!loadedFromFile || documents.size() == 0) {
            loadFallbackDocuments();
        }

        // Index all documents into Trie and vocabulary
        indexAllDocuments();
        isLoaded = true;
    }

    private void parseDocuments(BufferedReader reader) throws Exception {
        String line;
        int id = 0;
        String title = "";
        String language = "";
        String category = "";
        StringBuilder contentSb = new StringBuilder();
        boolean inDoc = false;

        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();

            if (trimmed.equals("---DOCUMENT---")) {
                if (inDoc && id > 0) {
                    documents.add(new Document(id, title, language, contentSb.toString().trim(), category));
                }
                id = 0;
                title = "";
                language = "";
                category = "";
                contentSb.setLength(0);
                inDoc = true;
                continue;
            }

            if (trimmed.startsWith("ID:")) {
                try {
                    id = Integer.parseInt(trimmed.substring(3).trim());
                } catch (NumberFormatException ignored) {}
            } else if (trimmed.startsWith("TITLE:")) {
                title = trimmed.substring(6).trim();
            } else if (trimmed.startsWith("LANGUAGE:")) {
                language = trimmed.substring(9).trim();
            } else if (trimmed.startsWith("CATEGORY:")) {
                category = trimmed.substring(9).trim();
            } else if (trimmed.startsWith("CONTENT:")) {
                // Following lines are content
            } else {
                if (inDoc) {
                    contentSb.append(line).append("\n");
                }
            }
        }

        // Last document
        if (inDoc && id > 0) {
            documents.add(new Document(id, title, language, contentSb.toString().trim(), category));
        }
    }

    private void loadFallbackDocuments() {
        documents.add(new Document(1, "Hyderabad - The City of Pearls", "English",
            "Hyderabad is the capital and largest city of Telangana. Known as the City of Pearls, Hyderabad is a global information technology and biotechnology hub. Landmarks include Charminar and Golconda Fort. Famous for Hyderabadi biryani."));
        documents.add(new Document(2, "Telangana State and Heritage", "English",
            "Telangana is a dynamic state in southern India. Formed in 2014, major rivers Godavari and Krishna flow through its plateau. Renowned for Kakatiya dynasty history, Warangal fort, and Bathukamma festival."));
        documents.add(new Document(3, "India - Unity in Diversity", "English",
            "India is the world's most populous democracy with New Delhi as its capital. Bounded by the Himalayas in the north and Indian Ocean in the south. Celebrated for rich multilingual cultures and ancient scientific knowledge."));
        documents.add(new Document(4, "హైదరాబాద్ - భాగ్యనగరం", "Telugu",
            "హైదరాబాద్ తెలంగాణ రాష్ట్ర రాజధాని. దీనిని ముత్యాల నగరం లేదా భాగ్యనగరం అంటారు. హైటెక్ సిటీ, చార్మినార్, గోల్కొండ కోట ప్రముఖ ఆకర్షణలు. హైదరాబాదీ బిర్యానీ మరియు సంస్కృతి అంతర్జాతీయంగా ప్రసిద్ధి."));
        documents.add(new Document(5, "తెలంగాణ చరిత్ర మరియు సంస్కృతి", "Telugu",
            "తెలంగాణ భారతదేశంలోని ఒక రాష్ట్రం. గోదావరి మరియు కృష్ణా నదులు ఇక్కడి వ్యవసాయానికి జీవనాడి. కాకతీయుల వారసత్వం, వరంగల్ వేయి స్తంభాల గుడి, మరియు బతుకమ్మ పండుగ ఈ రాష్ట్ర సాంస్కృతిక ప్రతీకలు."));
        documents.add(new Document(6, "हैदराबाद - मोतियों का शहर", "Hindi",
            "हैदराबाद तेलंगाना राज्य की राजधानी और भारत का प्रसिद्ध आईटी हब है। चारमीनार, गोलकोंडा किला और हुसैन सागर इसके ऐतिहासिक स्थल हैं। विश्व प्रसिद्ध बिरयानी और गंगा-जमुनी तहज़ीब यहाँ की पहचान है।"));
        documents.add(new Document(7, "भारत का गौरव और संस्कृति", "Hindi",
            "भारत विश्व का सबसे बड़ा लोकतंत्र है। नई दिल्ली भारत की राजधानी है। उत्तर में हिमालय और दक्षिण में हिंद महासागर स्थित हैं। अनेक भाषाएं और संस्कृतियां विविधता में एकता की मिसाल हैं।"));
        documents.add(new Document(8, "ஹைதராபாத் - தகவல் தொழில்நுட்ப மையம்", "Tamil",
            "ஹைதராபாத் தெலங்கானா மாநிலத்தின் தலைநகரமாகும். இது முத்துக்களின் நகரம் மற்றும் ஐடி மையமாக அறியப்படுகிறது. சார்மினார், கோல்கொண்டா கோட்டை மற்றும் சுவையான பிரியாணி இந்நகரத்தின் சிறப்புகள்."));
        documents.add(new Document(9, "இந்தியா - வேற்றுமையில் ஒற்றுமை", "Tamil",
            "இந்தியா உலகின் மிகப்பெரிய ஜனநாயக நாடு. புது தில்லி இதன் தலைநகரம். பல்வேறு மொழிகள், மதங்கள் மற்றும் பண்பாடுகளைக் கொண்ட வேற்றுமையில் ஒற்றுமை நிலவும் திருநாடு."));
    }

    private void indexAllDocuments() {
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            // Index both title and content tokens into Trie and vocabulary
            CustomList<String> tokens = DocumentSimilarity.tokenize(doc.getTitle() + " " + doc.getContent());
            for (int t = 0; t < tokens.size(); t++) {
                String token = tokens.get(t);
                if (token.length() >= 2) {
                    prefixTrie.insert(token.toLowerCase(), doc.getId());
                    if (!vocabulary.contains(token)) {
                        vocabulary.add(token);
                    }
                }
            }
        }
    }

    public CustomList<Document> getAllDocuments() {
        return documents;
    }

    public Document getDocumentById(int id) {
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            if (doc.getId() == id) {
                return doc;
            }
        }
        return null;
    }

    public CustomList<Document> getDocumentsByLanguage(String lang) {
        if (lang == null || lang.equalsIgnoreCase("All")) {
            return documents;
        }
        CustomList<Document> filtered = new CustomList<>();
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            if (doc.getLanguage().equalsIgnoreCase(lang)) {
                filtered.add(doc);
            }
        }
        return filtered;
    }

    public Trie getPrefixTrie() {
        return prefixTrie;
    }

    public CustomList<String> getVocabulary() {
        return vocabulary;
    }

    public int size() {
        return documents.size();
    }
}
