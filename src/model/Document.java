package model;

/**
 * Represents a multilingual document in the retrieval engine.
 * Contains the document's ID, title, language, content, and category.
 * Supports Unicode text for English, Telugu, Hindi, and Tamil.
 */
public class Document {
    private final int id;
    private final String title;
    private final String language;
    private final String content;
    private final String category;

    public Document(int id, String title, String language, String content) {
        this(id, title, language, content, inferCategory(title, content));
    }

    public Document(int id, String title, String language, String content, String category) {
        this.id = id;
        this.title = title != null ? title.trim() : "";
        this.language = language != null ? language.trim() : "Unknown";
        this.content = content != null ? content.trim() : "";
        this.category = (category != null && !category.trim().isEmpty()) ? category.trim() : inferCategory(this.title, this.content);
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLanguage() {
        return language;
    }

    public String getContent() {
        return content;
    }

    public String getCategory() {
        return category;
    }

    public static String inferCategory(String title, String content) {
        String lower = ((title != null ? title : "") + " " + (content != null ? content : "")).toLowerCase();
        if (lower.contains("airport") || lower.contains("aviation") || lower.contains("విమానాశ్రయం") || lower.contains("हवाई") || lower.contains("விமான")) {
            return "Aviation & Travel";
        }
        if (lower.contains("isro") || lower.contains("space") || lower.contains("chandrayaan") || lower.contains("ఇస్రో") || lower.contains("इसरो") || lower.contains("இஸ்ரோ") || lower.contains("science") || lower.contains("satellite")) {
            return "Science & Space";
        }
        if (lower.contains("railway") || lower.contains("vande bharat") || lower.contains("రైల్వే") || lower.contains("रेल") || lower.contains("ரயில்") || lower.contains("transport")) {
            return "Travel & Transport";
        }
        if (lower.contains("technology") || lower.contains("software") || lower.contains("computer") || lower.contains("intelligence") || lower.contains("సాంకేతిక") || lower.contains("कंप्यूटर") || lower.contains("கணினி")) {
            return "Technology";
        }
        if (lower.contains("education") || lower.contains("university") || lower.contains("research") || lower.contains("విద్య") || lower.contains("शिक्षा") || lower.contains("கல்வி")) {
            return "Education";
        }
        if (lower.contains("hyderabad") || lower.contains("హైదరాబాద్") || lower.contains("हैदराबाद") || lower.contains("ஹைதராபாத்") || lower.contains("charminar") || lower.contains("city")) {
            return "Cities & Culture";
        }
        if (lower.contains("telangana") || lower.contains("తెలంగాణ") || lower.contains("तेलंगाना") || lower.contains("தெலங்கானா") || lower.contains("history") || lower.contains("heritage")) {
            return "History & Heritage";
        }
        return "General Knowledge";
    }

    @Override
    public String toString() {
        return "[" + id + "] " + title + " (" + language + " | " + category + ")";
    }
}
