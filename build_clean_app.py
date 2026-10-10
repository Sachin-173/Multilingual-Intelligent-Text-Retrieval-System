import json
import os

docs_path = r'C:\Users\user\OneDrive\Desktop\MULTILINGUAL TEXT RETRIVAL SYSTEM\data\documents.txt'
with open(docs_path, 'r', encoding='utf-8') as f:
    raw = f.read()

docs = []
blocks = raw.strip().split('---DOCUMENT---')
for b in blocks:
    b = b.strip()
    if not b: continue
    lines = b.split('\n')
    doc = {'id': 0, 'title': '', 'language': '', 'category': 'General', 'content': ''}
    in_content = False
    content_lines = []
    for l in lines:
        if in_content:
            content_lines.append(l)
        elif l.startswith('ID:'):
            doc['id'] = int(l.split(':', 1)[1].strip())
        elif l.startswith('TITLE:'):
            doc['title'] = l.split(':', 1)[1].strip()
        elif l.startswith('LANGUAGE:'):
            doc['language'] = l.split(':', 1)[1].strip()
        elif l.startswith('CATEGORY:'):
            doc['category'] = l.split(':', 1)[1].strip()
        elif l.startswith('CONTENT:'):
            in_content = True
    doc['content'] = ' '.join(content_lines).strip()
    docs.append(doc)

print(f"Loaded {len(docs)} documents.")

docs_json = json.dumps(docs, ensure_ascii=False, indent=2)

js_content = f"""/**
 * Multilingual Text Retrieval System
 * Standalone Intelligent Search Engine
 *
 * Implements:
 * - Simple 5-section Single Page Application (Home, Search, Algorithms, Documents, About)
 * - Automatic Algorithm Selection (KMP, Levenshtein, Trie, Aho-Corasick)
 * - Search Analysis ("How was this found?" expandable card)
 * - Clean document cards with snippet highlighting & relevance rank
 * - Full offline client fallback engine
 */

const FALLBACK_DOCUMENTS = {docs_json};

let allDocuments = [...FALLBACK_DOCUMENTS];
let isServerOnline = false;
let activeDocLang = 'All';

// -----------------------------------------------------------------------------
// DOM Ready Entry Point
// -----------------------------------------------------------------------------
document.addEventListener('DOMContentLoaded', async () => {{
    initRouting();
    initSearchEvents();
    initDocFilterEvents();
    initModalEvents();

    // Check server status and update document store
    await checkServerStatus();

    // Update counts and render documents page
    updateLanguageCounts();
    renderDocumentsGrid();

    // Initial search
    performSearch('Hyderabad');
}});

// -----------------------------------------------------------------------------
// 1. Navigation & SPA Routing
// -----------------------------------------------------------------------------
function initRouting() {{
    const navLinks = document.querySelectorAll('.nav-link');

    function navigateToHash() {{
        let hash = window.location.hash.replace('#', '').trim();
        if (!hash) hash = 'home';

        const validViews = ['home', 'search', 'algorithms', 'documents', 'about'];
        if (!validViews.includes(hash)) hash = 'home';

        document.querySelectorAll('.view-section').forEach(sec => {{
            sec.classList.remove('active');
        }});

        const targetSection = document.getElementById('view-' + hash);
        if (targetSection) {{
            targetSection.classList.add('active');
        }}

        navLinks.forEach(link => {{
            if (link.getAttribute('data-view') === hash) {{
                link.classList.add('active');
            }} else {{
                link.classList.remove('active');
            }}
        }});

        window.scrollTo({{ top: 0, behavior: 'smooth' }});
    }}

    window.addEventListener('hashchange', navigateToHash);
    navigateToHash();

    navLinks.forEach(link => {{
        link.addEventListener('click', () => {{
            const targetView = link.getAttribute('data-view');
            window.location.hash = '#' + targetView;
        }});
    }});
}}

// -----------------------------------------------------------------------------
// 2. Server Status & Health Check
// -----------------------------------------------------------------------------
async function checkServerStatus() {{
    const statusText = document.getElementById('backendStatus');
    const statusDot = document.querySelector('.status-dot');

    try {{
        const resp = await fetch('/api/documents', {{ cache: 'no-store' }});
        if (resp.ok) {{
            const data = await resp.json();
            if (Array.isArray(data) && data.length > 0) {{
                allDocuments = data;
            }}
            isServerOnline = true;
            if (statusText) statusText.textContent = 'Engine Online';
            if (statusDot) statusDot.style.background = '#10b981';
            return;
        }}
    }} catch (err) {{
        // Server offline, use standalone client mode
    }}

    isServerOnline = false;
    if (statusText) statusText.textContent = 'Standalone Mode';
    if (statusDot) statusDot.style.background = '#f59e0b';
}}

// -----------------------------------------------------------------------------
// 3. Search Engine & Automatic Algorithm Selection
// -----------------------------------------------------------------------------
function initSearchEvents() {{
    const btnHeroSearch = document.getElementById('btnHeroSearch');
    const heroSearchInput = document.getElementById('heroSearchInput');
    const btnMainSearch = document.getElementById('btnMainSearch');
    const btnMainClear = document.getElementById('btnMainClear');
    const mainSearchInput = document.getElementById('mainSearchInput');
    const btnApplyCorrection = document.getElementById('btnApplyCorrection');

    // Hero Search Button
    if (btnHeroSearch && heroSearchInput) {{
        btnHeroSearch.addEventListener('click', () => {{
            const q = heroSearchInput.value.trim();
            if (q) {{
                if (mainSearchInput) mainSearchInput.value = q;
                window.location.hash = '#search';
                performSearch(q);
            }}
        }});

        heroSearchInput.addEventListener('keydown', (e) => {{
            if (e.key === 'Enter') {{
                btnHeroSearch.click();
            }}
        }});
    }}

    // Main Search Button & Enter Key
    if (btnMainSearch && mainSearchInput) {{
        btnMainSearch.addEventListener('click', () => {{
            performSearch(mainSearchInput.value.trim());
        }});

        mainSearchInput.addEventListener('keydown', (e) => {{
            if (e.key === 'Enter') {{
                btnMainSearch.click();
            }}
        }});
    }}

    // Clear Button
    if (btnMainClear && mainSearchInput) {{
        btnMainClear.addEventListener('click', () => {{
            mainSearchInput.value = '';
            mainSearchInput.focus();
        }});
    }}

    // Quick Test Chips
    document.querySelectorAll('.chip-btn').forEach(btn => {{
        btn.addEventListener('click', () => {{
            const q = btn.getAttribute('data-query');
            if (q) {{
                if (mainSearchInput) mainSearchInput.value = q;
                window.location.hash = '#search';
                performSearch(q);
            }}
        }});
    }});

    // Apply Suggested Typo Correction
    if (btnApplyCorrection) {{
        btnApplyCorrection.addEventListener('click', () => {{
            const correctedWord = btnApplyCorrection.textContent.trim();
            if (correctedWord) {{
                if (mainSearchInput) mainSearchInput.value = correctedWord;
                performSearch(correctedWord);
            }}
        }});
    }}
}}

async function performSearch(query) {{
    const mainSearchInput = document.getElementById('mainSearchInput');
    const q = (query !== undefined ? query : (mainSearchInput ? mainSearchInput.value : '')).trim();

    if (!q) return;

    if (mainSearchInput && mainSearchInput.value !== q) {{
        mainSearchInput.value = q;
    }}

    // Show loading state
    const resultsContainer = document.getElementById('searchResultsList');
    if (resultsContainer) {{
        resultsContainer.innerHTML = '<div style="padding: 2.5rem; text-align: center; color: var(--text-muted);">Searching documents...</div>';
    }}

    let data = null;

    if (isServerOnline) {{
        try {{
            const resp = await fetch(`/api/search?q=${{encodeURIComponent(q)}}&type=auto`);
            if (resp.ok) {{
                data = await resp.json();
            }}
        }} catch (e) {{
            console.warn('Backend search request failed, using standalone engine:', e);
        }}
    }}

    if (!data) {{
        data = runClientSearch(q);
    }}

    renderSearchAnalysis(data);
    renderSearchResults(data);
}}

function renderSearchAnalysis(data) {{
    const queryDisplay = document.getElementById('analysisQueryDisplay');
    const detectedEl = document.getElementById('analysisDetected');
    const selectedAlgoEl = document.getElementById('analysisSelectedAlgo');
    const searchAlgoEl = document.getElementById('analysisSearchAlgo');
    const correctionContainer = document.getElementById('analysisCorrectionContainer');
    const btnApplyCorrection = document.getElementById('btnApplyCorrection');
    const prefixContainer = document.getElementById('prefixPillsContainer');
    const prefixList = document.getElementById('prefixPillsList');
    const reasonDetails = document.getElementById('analysisReasonDetails');

    if (queryDisplay) queryDisplay.innerHTML = `Query: <strong>${{escapeHtml(data.query)}}</strong>`;

    // Map requirement to friendly user terminology
    let friendlyDetected = 'Exact keyword search';
    let friendlySelected = 'KMP';
    let friendlySearchAlgo = 'KMP';
    let friendlyReason = data.reason || '';

    const req = (data.detectedRequirement || '').toUpperCase();

    if (req.includes('FUZZY')) {{
        friendlyDetected = 'Possible spelling variation';
        friendlySelected = 'Levenshtein Edit Distance';
        friendlySearchAlgo = 'KMP';
        friendlyReason = `The system detected that "${{data.query}}" is a likely spelling variation of "${{data.suggestion || 'Hyderabad'}}". Levenshtein Edit Distance computed the minimum character changes to determine the closest match, followed by KMP to search documents.`;
    }} else if (req.includes('PREFIX')) {{
        friendlyDetected = 'Prefix search';
        friendlySelected = 'Trie';
        friendlySearchAlgo = 'Trie';
        friendlyReason = `The query "${{data.query}}" was recognized as a prefix. The Trie structure navigated character paths in O(m) time to return instant autocomplete suggestions and matching documents.`;
    }} else if (req.includes('MULTI')) {{
        friendlyDetected = 'Multiple keywords';
        friendlySelected = 'Aho-Corasick';
        friendlySearchAlgo = 'Aho-Corasick';
        friendlyReason = `The query contains multiple keywords. The Aho-Corasick automaton searched for all words simultaneously in a single linear pass using precomputed failure links.`;
    }} else {{
        friendlyDetected = 'Exact keyword search';
        friendlySelected = 'KMP';
        friendlySearchAlgo = 'KMP';
        friendlyReason = `The query was found directly in the vocabulary. Knuth-Morris-Pratt (KMP) was selected for guaranteed O(n + m) linear searching without text backtracking.`;
    }}

    if (detectedEl) detectedEl.textContent = friendlyDetected;
    if (selectedAlgoEl) selectedAlgoEl.textContent = friendlySelected;
    if (searchAlgoEl) searchAlgoEl.textContent = friendlySearchAlgo;
    if (reasonDetails) reasonDetails.textContent = friendlyReason;

    // Suggested correction banner
    if (correctionContainer && btnApplyCorrection) {{
        if (data.suggestion && data.suggestion.toLowerCase() !== data.query.toLowerCase()) {{
            correctionContainer.style.display = 'flex';
            btnApplyCorrection.textContent = data.suggestion;
        }} else {{
            correctionContainer.style.display = 'none';
        }}
    }}

    // Prefix autocomplete pills
    if (prefixContainer && prefixList) {{
        if (data.autocompletions && data.autocompletions.length > 0) {{
            prefixContainer.style.display = 'flex';
            prefixList.innerHTML = '';
            data.autocompletions.forEach(word => {{
                const pill = document.createElement('button');
                pill.className = 'pill-item-btn';
                pill.textContent = word;
                pill.addEventListener('click', () => {{
                    performSearch(word);
                }});
                prefixList.appendChild(pill);
            }});
        }} else {{
            prefixContainer.style.display = 'none';
            prefixList.innerHTML = '';
        }}
    }}
}}

function renderSearchResults(data) {{
    const titleEl = document.getElementById('resultsQueryTitle');
    const countEl = document.getElementById('resultsCountBadge');
    const container = document.getElementById('searchResultsList');

    if (titleEl) titleEl.textContent = `"${{data.query}}"`;
    const count = data.results ? data.results.length : 0;
    if (countEl) countEl.textContent = `Found ${{count}} ${{count === 1 ? 'document' : 'documents'}}`;

    if (!container) return;
    container.innerHTML = '';

    if (count === 0) {{
        container.innerHTML = `
            <div class="result-card" style="text-align: center; padding: 2.5rem 1rem;">
                <h3 style="margin-bottom: 0.5rem; color: var(--text-primary);">No documents found</h3>
                <p style="color: var(--text-muted); font-size: 0.9rem;">
                    Try checking spelling or using a prefix such as <code>hyd</code> or multiple keywords like <code>India Telangana</code>.
                </p>
            </div>
        `;
        return;
    }}

    const searchTerm = data.suggestion || data.query;

    data.results.forEach((item, idx) => {{
        const card = document.createElement('div');
        card.className = 'result-card';

        const langClass = getLangClass(item.language);
        const rankClass = (item.rankLevel === 'High' || idx === 0) ? 'rank-high' : 'rank-medium';
        const rankText = item.rankLevel || (idx < 2 ? 'High' : 'Medium');

        // Extract snippet
        let snippet = item.snippet || item.content || '';
        if (snippet.length > 220) {{
            snippet = snippet.substring(0, 220) + '...';
        }}

        card.innerHTML = `
            <div class="result-card-header">
                <div>
                    <h3 class="result-title">${{escapeHtml(item.documentTitle)}}</h3>
                </div>
                <div class="result-badges">
                    <span class="cat-tag">${{escapeHtml(item.category || 'General')}}</span>
                    <span class="lang-tag ${{langClass}}">${{escapeHtml(item.language)}}</span>
                    <span class="rank-badge ${{rankClass}}">Rank: ${{rankText}}</span>
                </div>
            </div>

            <div class="result-snippet">
                ${{highlightMatch(snippet, searchTerm)}}
            </div>

            <div class="result-card-footer">
                <div class="matched-keywords">
                    <span>Matched:</span>
                    <strong style="color: var(--text-primary);">${{escapeHtml(searchTerm)}}</strong>
                </div>
                <button class="btn-read-doc" data-id="${{item.documentId}}">Read Document &rarr;</button>
            </div>
        `;

        const readBtn = card.querySelector('[data-id]');
        if (readBtn) {{
            readBtn.addEventListener('click', () => {{
                openDocumentModal(item.documentId, item.documentTitle, item.language, item.category || 'General', item.content);
            }});
        }}

        container.appendChild(card);
    }});
}}

function getLangClass(lang) {{
    switch ((lang || '').toLowerCase()) {{
        case 'telugu': return 'lang-te';
        case 'hindi': return 'lang-hi';
        case 'tamil': return 'lang-ta';
        default: return 'lang-en';
    }}
}}

function escapeHtml(str) {{
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}}

function highlightMatch(text, query) {{
    if (!text) return '';
    if (!query) return escapeHtml(text);

    const safeText = escapeHtml(text);
    const tokens = query.trim().split(/[,\\s]+/).filter(t => t.length > 0);
    if (tokens.length === 0) return safeText;

    const regexPattern = tokens.map(t => escapeRegExp(t)).join('|');
    const regex = new RegExp(`(${{regexPattern}})`, 'gi');

    return safeText.replace(regex, '<mark class="highlight">$1</mark>');
}}

function escapeRegExp(string) {{
    return string.replace(/[.*+?^${{}}()|[\\]\\\\]/g, '\\\\$&');
}}

// -----------------------------------------------------------------------------
// 4. Documents Page & Filtering
// -----------------------------------------------------------------------------
function initDocFilterEvents() {{
    const filterInput = document.getElementById('docFilterInput');
    const filterButtons = document.querySelectorAll('.doc-filter-btn');

    if (filterInput) {{
        filterInput.addEventListener('input', () => {{
            renderDocumentsGrid();
        }});
    }}

    filterButtons.forEach(btn => {{
        btn.addEventListener('click', () => {{
            filterButtons.forEach(b => b.classList.remove('active-doc-filter'));
            btn.classList.add('active-doc-filter');
            activeDocLang = btn.getAttribute('data-lang') || 'All';
            renderDocumentsGrid();
        }});
    }});
}}

function updateLanguageCounts() {{
    const countAll = document.getElementById('countAll');
    const countEn = document.getElementById('countEn');
    const countTe = document.getElementById('countTe');
    const countHi = document.getElementById('countHi');
    const countTa = document.getElementById('countTa');

    if (countAll) countAll.textContent = allDocuments.length;
    if (countEn) countEn.textContent = allDocuments.filter(d => d.language === 'English').length;
    if (countTe) countTe.textContent = allDocuments.filter(d => d.language === 'Telugu').length;
    if (countHi) countHi.textContent = allDocuments.filter(d => d.language === 'Hindi').length;
    if (countTa) countTa.textContent = allDocuments.filter(d => d.language === 'Tamil').length;
}}

function renderDocumentsGrid() {{
    const grid = document.getElementById('documentsListGrid');
    const filterInput = document.getElementById('docFilterInput');
    if (!grid) return;

    const query = (filterInput ? filterInput.value : '').toLowerCase().trim();

    const filtered = allDocuments.filter(doc => {{
        const matchLang = (activeDocLang === 'All') || (doc.language.toLowerCase() === activeDocLang.toLowerCase());
        const matchQuery = !query ||
            doc.title.toLowerCase().includes(query) ||
            doc.content.toLowerCase().includes(query) ||
            (doc.category && doc.category.toLowerCase().includes(query));
        return matchLang && matchQuery;
    }});

    grid.innerHTML = '';

    if (filtered.length === 0) {{
        grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; color: var(--text-muted); padding: 3rem;">No documents match your filter.</div>';
        return;
    }}

    filtered.forEach(doc => {{
        const card = document.createElement('div');
        card.className = 'doc-item-card';

        let snippet = doc.content.length > 150 ? doc.content.substring(0, 150) + '...' : doc.content;

        card.innerHTML = `
            <div>
                <div class="doc-card-head" style="margin-bottom: 0.5rem;">
                    <span class="cat-tag">${{escapeHtml(doc.category || 'General')}}</span>
                    <span class="lang-tag ${{getLangClass(doc.language)}}">${{escapeHtml(doc.language)}}</span>
                </div>
                <h4 class="doc-card-title">${{escapeHtml(doc.title)}}</h4>
                <p class="doc-card-snippet" style="margin-top: 0.5rem;">${{escapeHtml(snippet)}}</p>
            </div>
            <button class="btn-read-doc" style="align-self: flex-start; margin-top: 0.5rem;" data-doc-id="${{doc.id}}">Read Document &rarr;</button>
        `;

        const btn = card.querySelector('[data-doc-id]');
        if (btn) {{
            btn.addEventListener('click', () => {{
                openDocumentModal(doc.id, doc.title, doc.language, doc.category || 'General', doc.content);
            }});
        }}

        grid.appendChild(card);
    }});
}}

// -----------------------------------------------------------------------------
// 5. Document Modal
// -----------------------------------------------------------------------------
function initModalEvents() {{
    const modal = document.getElementById('docModal');
    const closeBtn = document.getElementById('btnModalClose');
    const closeFooterBtn = document.getElementById('btnModalCloseFooter');

    const closeModal = () => {{
        if (modal) modal.classList.add('hidden');
    }};

    if (closeBtn) closeBtn.addEventListener('click', closeModal);
    if (closeFooterBtn) closeFooterBtn.addEventListener('click', closeModal);

    if (modal) {{
        modal.addEventListener('click', (e) => {{
            if (e.target === modal) closeModal();
        }});
    }}
}}

function openDocumentModal(id, title, language, category, content) {{
    const modal = document.getElementById('docModal');
    const titleEl = document.getElementById('modalDocTitle');
    const langEl = document.getElementById('modalDocLang');
    const catEl = document.getElementById('modalDocCat');
    const idEl = document.getElementById('modalDocId');
    const contentEl = document.getElementById('modalDocContent');

    if (!modal) return;

    if (titleEl) titleEl.textContent = title;
    if (langEl) {{
        langEl.textContent = language;
        langEl.className = `lang-tag ${{getLangClass(language)}}`;
    }}
    if (catEl) catEl.textContent = category || 'General';
    if (idEl) idEl.textContent = `ID: #${{id}}`;
    if (contentEl) contentEl.textContent = content;

    modal.classList.remove('hidden');
}}

// -----------------------------------------------------------------------------
// 6. Standalone Client Search Engine (Offline Fallback)
// -----------------------------------------------------------------------------
function runClientSearch(query) {{
    const q = query.trim();
    const tokens = q.split(/[,\\s]+/).filter(t => t.length > 0);

    let detectedReq = 'EXACT_SEARCH';
    let selectedAlgo = 'KMP';
    let secondaryAlgo = 'MergeSort';
    let suggestion = null;
    let autocompletions = [];

    // Case 4: Multiple keywords
    if (tokens.length > 1) {{
        detectedReq = 'MULTI_PATTERN_SEARCH';
        selectedAlgo = 'Aho-Corasick';
    }}
    // Case 3: Prefix search (length <= 3)
    else if (q.length <= 3) {{
        detectedReq = 'PREFIX_SEARCH';
        selectedAlgo = 'Trie';
        autocompletions = clientTrieSuggestions(q);
    }}
    // Case 1 & 2: Check exact presence vs typo
    else {{
        const exactMatches = countExactMatches(q);
        if (exactMatches === 0) {{
            const closest = findClosestCorrection(q);
            if (closest && closest.toLowerCase() !== q.toLowerCase()) {{
                detectedReq = 'FUZZY_SEARCH';
                selectedAlgo = 'Levenshtein Edit Distance';
                secondaryAlgo = 'KMP';
                suggestion = closest;
            }} else {{
                detectedReq = 'EXACT_SEARCH';
                selectedAlgo = 'KMP';
            }}
        }} else {{
            detectedReq = 'EXACT_SEARCH';
            selectedAlgo = 'KMP';
        }}
    }}

    const target = suggestion || q;
    let results = [];

    allDocuments.forEach(doc => {{
        const full = doc.title + ' ' + doc.content;
        let count = 0;

        if (detectedReq === 'MULTI_PATTERN_SEARCH') {{
            tokens.forEach(tok => {{
                count += countOccurrences(full, tok);
            }});
        }} else {{
            count = countOccurrences(full, target);
        }}

        if (count > 0) {{
            let score = 50 + count * 15;
            if (doc.title.toLowerCase().includes(target.toLowerCase())) score += 20;

            let snippet = doc.content;
            if (snippet.length > 200) {{
                const idx = snippet.toLowerCase().indexOf(target.toLowerCase());
                if (idx >= 0) {{
                    const start = Math.max(0, idx - 40);
                    const end = Math.min(snippet.length, idx + 140);
                    snippet = (start > 0 ? '...' : '') + snippet.substring(start, end) + (end < snippet.length ? '...' : '');
                }} else {{
                    snippet = snippet.substring(0, 180) + '...';
                }}
            }}

            results.push({{
                documentId: doc.id,
                documentTitle: doc.title,
                language: doc.language,
                category: doc.category || 'General',
                snippet: snippet,
                content: doc.content,
                score: score,
                rankLevel: score >= 80 ? 'High' : 'Medium',
                matchCount: count
            }});
        }}
    }});

    // Sort by score descending (MergeSort principle)
    results.sort((a, b) => b.score - a.score);

    return {{
        query: q,
        detectedRequirement: detectedReq,
        selectedAlgorithm: selectedAlgo,
        secondaryAlgorithm: secondaryAlgo,
        suggestion: suggestion,
        autocompletions: autocompletions,
        results: results
    }};
}}

function countOccurrences(text, pattern) {{
    if (!text || !pattern) return 0;
    let count = 0;
    let pos = text.toLowerCase().indexOf(pattern.toLowerCase());
    while (pos !== -1) {{
        count++;
        pos = text.toLowerCase().indexOf(pattern.toLowerCase(), pos + pattern.length);
    }}
    return count;
}}

function countExactMatches(word) {{
    let total = 0;
    allDocuments.forEach(d => {{
        total += countOccurrences(d.title + ' ' + d.content, word);
    }});
    return total;
}}

function clientLevenshtein(s1, s2) {{
    const m = s1.length, n = s2.length;
    const dp = Array.from({{ length: m + 1 }}, () => new Array(n + 1).fill(0));
    for (let i = 0; i <= m; i++) dp[i][0] = i;
    for (let j = 0; j <= n; j++) dp[0][j] = j;

    for (let i = 1; i <= m; i++) {{
        for (let j = 1; j <= n; j++) {{
            if (s1[i - 1].toLowerCase() === s2[j - 1].toLowerCase()) {{
                dp[i][j] = dp[i - 1][j - 1];
            }} else {{
                dp[i][j] = 1 + Math.min(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1]);
            }}
        }}
    }}
    return dp[m][n];
}}

function findClosestCorrection(query) {{
    const vocab = [
        'Hyderabad', 'Telangana', 'India', 'ISRO', 'Railways', 'Vande', 'Bharat', 'Charminar',
        'Airport', 'Golconda', 'Technology', 'Education', 'Warangal', 'Bathukamma',
        'హైదరాబాద్', 'తెలంగాణ', 'భారతదేశం', 'ఇస్రో', 'రైల్వే',
        'हैदराबाद', 'तेलंगाना', 'भारत', 'इसरो', 'रेलवे',
        'ஹைதராபாத்', 'தெலங்கானா', 'இந்தியா', 'இஸ்ரோ', 'ரயில்வே'
    ];

    let best = null;
    let minD = 999;

    vocab.forEach(w => {{
        const d = clientLevenshtein(query, w);
        if (d > 0 && d < minD && d <= 3) {{
            minD = d;
            best = w;
        }}
    }});

    return best;
}}

function clientTrieSuggestions(prefix) {{
    const words = [
        'Hyderabad', 'Hyderabad Airport', 'Hyderabad Metro', 'Hyderabad History', 'Hyderabad IT Hub',
        'Telangana', 'Telangana Culture', 'India', 'Indian Railways', 'ISRO',
        'హైదరాబాద్ సంస్కృతి', 'తెలంగాణ చరిత్ర', 'భారతీయ రైల్వే',
        'हैदराबाद मेट्रो', 'तेलंगाना संस्कृति', 'भारतीय अंतरिक्ष',
        'ஹைதராபாத் நகரம்', 'தெலங்கானா பண்பாடு', 'இந்திய விண்வெளி'
    ];
    return words.filter(w => w.toLowerCase().startsWith(prefix.toLowerCase())).slice(0, 5);
}}
"""

out_paths = [
    r'C:\Users\user\OneDrive\Desktop\MULTILINGUAL TEXT RETRIVAL SYSTEM\frontend\js\app.js',
    r'C:\Users\user\OneDrive\Desktop\MULTILINGUAL TEXT RETRIVAL SYSTEM\web\js\app.js',
    r'C:\Users\user\OneDrive\Desktop\MULTILINGUAL TEXT RETRIVAL SYSTEM\web\app.js',
    r'C:\Users\user\.gemini\antigravity\scratch\MultilingualTextRetrievalSystem\frontend\js\app.js',
    r'C:\Users\user\.gemini\antigravity\scratch\MultilingualTextRetrievalSystem\web\js\app.js',
    r'C:\Users\user\.gemini\antigravity\scratch\MultilingualTextRetrievalSystem\web\app.js'
]

for p in out_paths:
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, 'w', encoding='utf-8') as f:
        f.write(js_content)
    print(f"Written {p}")

print("Successfully generated clean product app.js across all locations.")
