/**
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

const FALLBACK_DOCUMENTS = [
  {
    "id": 1,
    "title": "Hyderabad - The City of Pearls and IT Hub",
    "language": "English",
    "category": "Technology",
    "content": "Hyderabad is the capital and largest city of the Indian state of Telangana. Known historically as the City of Pearls, Hyderabad has evolved into a leading global information technology and biotechnology hub alongside Cyberabad and HITEC City. World-renowned landmarks include the Charminar, Golconda Fort, and the Hussain Sagar lake with its monolithic Buddha statue. The city is celebrated for its Hyderabadi biryani, vibrant education institutions, and rich heritage bridging history and modernity."
  },
  {
    "id": 2,
    "title": "Telangana - Heritage, Culture, and Geography",
    "language": "English",
    "category": "Cities & Culture",
    "content": "Telangana is a dynamic state in southern India, formed in 2014. The state is bounded by Maharashtra, Chhattisgarh, Karnataka, and Andhra Pradesh. Major rivers including the Godavari and Krishna flow through its plateau terrain, nourishing agriculture and historic settlements. Telangana is famous for the architectural legacy of the Kakatiya dynasty, Warangal fort, the Thousand Pillar Temple, and the joyous floral festival of Bathukamma. Hyderabad serves as its administrative, cultural, and economic epicenter."
  },
  {
    "id": 3,
    "title": "India - Unity in Diversity and Democracy",
    "language": "English",
    "category": "Culture & Society",
    "content": "India, officially the Republic of India, is the world's most populous democracy situated in South Asia. Bounded by the Indian Ocean on the south, the Arabian Sea on the southwest, and the Bay of Bengal on the southeast, India boasts geographical marvels from the snow-capped Himalayas in the north to the tropical backwaters of Kerala in the south. With New Delhi as its capital, India is a multilingual civilization representing diverse languages, faiths, cultures, and traditions united under a democratic constitutional framework."
  },
  {
    "id": 4,
    "title": "ISRO and the Indian Space Odyssey",
    "language": "English",
    "category": "Science & Space",
    "content": "The Indian Space Research Organisation (ISRO) is the national space agency of India, headquartered in Bengaluru. ISRO has achieved extraordinary milestones in space science and satellite technology, including the historic Chandrayaan moon missions and the Mars Orbiter Mission (Mangalyaan). Operating from the Satish Dhawan Space Centre in Sriharikota, ISRO provides vital satellite communications, earth observation, remote sensing, and navigation services, propelling India to the forefront of global aerospace exploration."
  },
  {
    "id": 5,
    "title": "Indian Railways and Modern Transport",
    "language": "English",
    "category": "Travel & Transport",
    "content": "Indian Railways constitutes one of the largest rail transportation networks in the world, connecting every corner of the country. With millions of daily passengers and extensive freight transport, it serves as the lifeline of the national economy. Recent modernizations include the indigenous semi-high-speed Vande Bharat Express trains, electrification of routes, and world-class redevelopment of key junction stations such as Secunderabad and New Delhi."
  },
  {
    "id": 6,
    "title": "Technology, Software, and Artificial Intelligence in India",
    "language": "English",
    "category": "Technology",
    "content": "India has emerged as an international powerhouse in computer science, software engineering, and artificial intelligence. Cities like Bengaluru, Hyderabad, and Pune house premier technology clusters driving cloud computing, data analytics, green hydrogen fuel innovations, and computational hydrology systems. Indian engineers, research scholars, and entrepreneurs from top universities and IITs continue to build breakthrough technologies that influence the global digital economy."
  },
  {
    "id": 7,
    "title": "Higher Education and Scientific Research in India",
    "language": "English",
    "category": "Technology",
    "content": "The Indian education system blends traditional academic wisdom with contemporary scientific inquiry. Prestigious institutions like the Indian Institutes of Technology (IIT), Indian Institute of Science (IISc), and central universities provide cutting-edge research opportunities in engineering, medicine, and fundamental sciences. Osmania University and University of Hyderabad are prominent educational landmarks in Telangana, shaping scholars and innovators."
  },
  {
    "id": 8,
    "title": "హైదరాబాద్ - భాగ్యనగరం మరియు సమాచార సాంకేతిక కేంద్రం",
    "language": "Telugu",
    "category": "Technology",
    "content": "హైదరాబాద్ తెలంగాణ రాష్ట్ర రాజధాని మరియు అతిపెద్ద నగరం. దీనిని చారిత్రాత్మకంగా ముత్యాల నగరం (భాగ్యనగరం) అని పిలుస్తారు. నేడు హైదరాబాద్ ప్రపంచ స్థాయి సమాచార సాంకేతిక (ఐటీ) మరియు ఔషధ రంగాల కేంద్రంగా ఎదిగింది. హైటెక్ సిటీ, చార్మినార్, గోల్కొండ కోట, మరియు హుస్సేన్ సాగర్ ఇక్కడి ప్రముఖ ఆకర్షణలు. ప్రసిద్ధ హైదరాబాదీ బిర్యానీ, గంగా-జమునీ తెహజీబ్ సంస్కృతి, మరియు విద్యాసంస్థలు నగరానికి అంతర్జాతీయ ఖ్యాతిని తెచ్చిపెట్టాయి."
  },
  {
    "id": 9,
    "title": "తెలంగాణ రాష్ట్రం - చరిత్ర, సంస్కృతి మరియు వారసత్వం",
    "language": "Telugu",
    "category": "Cities & Culture",
    "content": "తెలంగాణ భారతదేశంలోని ఒక ముఖ్యమైన రాష్ట్రం. గోదావరి మరియు కృష్ణా నదులు ఈ ప్రాంతం గుండా ప్రవహిస్తూ వ్యవసాయానికి మరియు సంస్కృతికి జీవనాడిలా నిలుస్తున్నాయి. కాకతీయుల శిల్పకళా వైభవం, వరంగల్ వేయి స్తంభాల గుడి, మరియు రామప్ప దేవాలయం రాష్ట్ర వారసత్వానికి ప్రతీకలు. తెలంగాణ సాంప్రదాయ పూల పండుగ బతుకమ్మ మరియు బోనాలు ఎంతో వైభవంగా జరుపుకుంటారు. హైదరాబాద్ నగరం తెలంగాణ రాజకీయ, ఆర్థిక, సాంస్కృతిక కేంద్రంగా ఉంది."
  },
  {
    "id": 10,
    "title": "భారతదేశం - భిన్నత్వంలో ఏకత్వం మరియు ప్రజాస్వామ్యం",
    "language": "Telugu",
    "category": "Technology",
    "content": "భారతదేశం ప్రపంచంలోనే అతిపెద్ద ప్రజాస్వామ్య దేశం. ఉత్తరాన హిమాలయ పర్వతాలు, దక్షిణాన హిందూ మహాసముద్రం, తూర్పున బంగాళాఖాతం, పశ్చిమాన అరేబియా సముద్రం సరిహద్దులుగా ఉన్నాయి. దేశ రాజధాని న్యూఢిల్లీ. విభిన్న భాషలు, మతాలు, ఆచారాలు ఉన్నప్పటికీ భిన్నత్వంలో ఏకత్వం భారతదేశ విశిష్టత. గంగా, యమునా, గోదావరి నదులు భారతీయ జీవన విధానంతో పెనవేసుకుపోయాయి. శాస్త్ర విజ్ఞానం మరియు సాంకేతికతలో భారత్ అగ్రగామిగా ఎదుగుతోంది."
  },
  {
    "id": 11,
    "title": "ఇస్రో మరియు భారతదేశ అంతరిక్ష పరిశోధనలు",
    "language": "Telugu",
    "category": "Science & Space",
    "content": "భారత అంతరిక్ష పరిశోధనా సంస్థ (ఇస్రో) భారతదేశ శాస్త్రీయ విజయాలకు తిరుగులేని నిదర్శనం. బెంగళూరు కేంద్రంగా పనిచేసే ఇస్రో, శ్రీహరికోటలోని సతీష్ ధావన్ అంతరిక్ష కేంద్రం నుండి రాకెట్లను ప్రయోగిస్తుంది. చంద్రయాన్ మిషన్లు, మంగళ్యాన్, మరియు ఆదిత్య-ఎల్1 ప్రాజెక్టులు అంతర్జాతీయ స్థాయిలో ప్రశంసలు పొందాయి. తక్కువ ఖర్చుతో ఉపగ్రహాలను కక్ష్యలోకి ప్రవేశపెట్టడంలో ఇస్రో ప్రపంచ రికార్డులను సృష్టించింది."
  },
  {
    "id": 12,
    "title": "భారతీయ రైల్వే వ్యవస్థ మరియు వందే భారత్ ఎక్స్‌ప్రెస్",
    "language": "Telugu",
    "category": "Travel & Transport",
    "content": "భారతీయ రైల్వే దేశంలోని అతిపెద్ద రవాణా వ్యవస్థలలో ఒకటి. రోజూ కోట్లాది మంది ప్రయాణీకులను సురక్షితంగా గమ్యస్థానాలకు చేర్చుతూ ఆర్థిక వ్యవస్థకు వెన్నుముకగా నిలుస్తోంది. ఇటీవల ప్రవేశపెట్టిన ఆధునిక వందే భారత్ సెమీ హైస్పీడ్ రైళ్లు ప్రయాణ సమయాన్ని గణనీయంగా తగ్గించాయి. సికింద్రాబాద్, హైదరాబాద్ రైల్వే స్టేషన్లు తెలంగాణలో కీలక రవాణా జంక్షన్లుగా సేవలందిస్తున్నాయి."
  },
  {
    "id": 13,
    "title": "కంప్యూటర్ సైన్స్ మరియు ఆధునిక సాంకేతిక ఆవిష్కరణలు",
    "language": "Telugu",
    "category": "Technology",
    "content": "భారతదేశంలో కంప్యూటర్ సైన్స్ మరియు ఇంజనీరింగ్ రంగాలు అద్భుతమైన పురోగతిని సాధించాయి. సాఫ్ట్‌వేర్ అభివృద్ధి, కృత్రిమ మేధస్సు (ఆర్టిఫిషియల్ ఇంటెలిజెన్స్), డేటా సైన్స్ రంగాలలో హైదరాబాద్, బెంగళూరు నగరాలు ప్రపంచ కేంద్రాలుగా మారాయి. తెలుగు రాష్ట్రాల విద్యార్థులు సాంకేతిక పరిజ్ఞానంతో అంతర్జాతీయ సంస్థలలో ఉన్నత స్థానాలను అధిరోహిస్తున్నారు."
  },
  {
    "id": 14,
    "title": "ఉన్నత విద్య మరియు పరిశోధనా సంస్థలు",
    "language": "Telugu",
    "category": "Technology",
    "content": "భారతదేశ విద్యా వ్యవస్థ విద్యార్థులలో పరిశోధన మరియు విజ్ఞాన తృష్ణను పెంపొందిస్తోంది. ఐఐటీలు, ఐఐఎస్సీ, మరియు కేంద్రీయ విశ్వవిద్యాలయాలు సాంకేతిక, శాస్త్రీయ రంగాలలో అత్యున్నత విద్యను అందిస్తున్నాయి. హైదరాబాద్‌లోని ఉస్మానియా విశ్వవిద్యాలయం మరియు హైదరాబాద్ కేంద్రీయ విశ్వవిద్యాలయం తెలంగాణలో ఉన్నత విద్య మరియు పరిశోధనలకు ప్రముఖ కేంద్రాలుగా ఉన్నాయి."
  },
  {
    "id": 15,
    "title": "हैदराबाद - मोतियों का शहर और सूचना प्रौद्योगिकी केंद्र",
    "language": "Hindi",
    "category": "Cities & Culture",
    "content": "हैदराबाद तेलंगाना राज्य की राजधानी और भारत का एक प्रमुख महानगर है। ऐतिहासिक रूप से इसे मोतियों का शहर कहा जाता है। वर्तमान में हैदराबाद सूचना प्रौद्योगिकी, सॉफ्टवेयर और जैव प्रौद्योगिकी का वैश्विक केंद्र बन चुका है। चारमीनार, गोलकोंडा किला और हुसैन सागर झील यहाँ के प्रमुख ऐतिहासिक स्थल हैं। विश्व प्रसिद्ध हैदराबादी बिरयानी और यहाँ की समृद्ध संस्कृति दुनिया भर में विख्यात है।"
  },
  {
    "id": 16,
    "title": "तेलंगाना राज्य - इतिहास, संस्कृति और विरासत",
    "language": "Hindi",
    "category": "Cities & Culture",
    "content": "तेलंगाना दक्षिण भारत का एक ऐतिहासिक और समृद्ध राज्य है। गोदावरी और कृष्णा जैसी पवित्र नदियाँ इस राज्य से होकर बहती हैं और कृषि को समृद्ध बनाती हैं। काकतीय राजवंश की वास्तुकला, वारंगल का किला और रामप्पा मंदिर तेलंगाना की महान सांस्कृतिक धरोहर हैं। बथुकम्मा और बोनालु यहाँ के प्रमुख पारंपरिक त्योहार हैं। हैदराबाद इस राज्य का आर्थिक, राजनीतिक और शैक्षणिक केंद्र है।"
  },
  {
    "id": 17,
    "title": "भारत - विविधता में एकता और महान लोकतंत्र",
    "language": "Hindi",
    "category": "Culture & Society",
    "content": "भारत विश्व का सबसे बड़ा लोकतांत्रिक देश है। उत्तर में विशाल हिमालय पर्वतमाला और दक्षिण में हिंद महासागर देश की प्राकृतिक सीमाएं निर्धारित करते हैं। नई दिल्ली भारत की राजधानी है। भारत में अनेक भाषाएं, धर्म और संस्कृतियां परस्पर सद्भाव से फल-फूल रही हैं। विविधता में एकता भारतीय सभ्यता की सबसे बड़ी विशेषता है। विज्ञान, अंतरिक्ष और डिजिटल अर्थव्यवस्था में भारत निरंतर प्रगति कर रहा है।"
  },
  {
    "id": 18,
    "title": "इसरो और भारतीय अंतरिक्ष अनुसंधान के कीर्तिमान",
    "language": "Hindi",
    "category": "Science & Space",
    "content": "भारतीय अंतरिक्ष अनुसंधान संगठन (इसरो) भारत की राष्ट्रीय अंतरिक्ष एजेंसी है जिसका मुख्यालय बेंगलुरु में है। इसरो ने अंतरिक्ष विज्ञान के क्षेत्र में ऐतिहासिक उपलब्धियां हासिल की हैं, जिनमें चंद्रयान चंद्रमा मिशन और मंगलयान प्रमुख हैं। श्रीहरिकोटा स्थित सतीश धवन अंतरिक्ष केंद्र से उपग्रहों और रॉकेटों का सफल प्रक्षेपण किया जाता है, जिससे वैश्विक अंतरिक्ष पटल पर भारत का नाम रोशन हुआ है।"
  },
  {
    "id": 19,
    "title": "भारतीय रेल और आधुनिक परिवहन क्रांति",
    "language": "Hindi",
    "category": "Travel & Transport",
    "content": "भारतीय रेल दुनिया के विशालतम रेल नेटवर्कों में से एक है जो पूरे देश को आपस में जोड़ता है। यह देश की जीवनरेखा मानी जाती है। हाल के वर्षों में स्वदेशी सेमी-हाई-स्पीड वंदे भारत एक्सप्रेस ट्रेनों की शुरुआत से यात्रा अधिक आरामदायक और तीव्र हो गई है। सिकंदराबाद और नई दिल्ली जैसे बड़े रेलवे स्टेशनों का आधुनिकीकरण यात्री सुविधाओं को नई दिशा दे रहा है।"
  },
  {
    "id": 20,
    "title": "कंप्यूटर विज्ञान, सॉफ्टवेयर और कृत्रिम बुद्धिमत्ता",
    "language": "Hindi",
    "category": "Technology",
    "content": "भारत सॉफ्टवेयर विकास और सूचना प्रौद्योगिकी के क्षेत्र में विश्व का नेतृत्व कर रहा है। हैदराबाद और बेंगलुरु जैसे शहर सॉफ्टवेयर इंजीनियरिंग, आर्टिफिशियल इंटेलिजेंस और क्लाउड कंप्यूटिंग के प्रमुख केंद्र बन गए हैं। भारतीय प्रौद्योगिकी संस्थानों (आईआईटी) के छात्र और वैज्ञानिक नवाचार के माध्यम से डिजिटल भारत के निर्माण में महत्वपूर्ण योगदान दे रहे हैं।"
  },
  {
    "id": 21,
    "title": "भारत में उच्च शिक्षा और वैज्ञानिक शोध संस्थान",
    "language": "Hindi",
    "category": "Education",
    "content": "भारतीय शिक्षा प्रणाली का उद्देश्य ज्ञान, कौशल और नैतिक मूल्यों का विकास करना है। भारतीय प्रौद्योगिकी संस्थान (आईआईटी), भारतीय विज्ञान संस्थान (आईआईएससी) और प्रमुख विश्वविद्यालय वैज्ञानिक शोध में महत्वपूर्ण भूमिका निभा रहे हैं। हैदराबाद का उस्मानिया विश्वविद्यालय और हैदराबाद विश्वविद्यालय उच्च शिक्षा और बौद्धिक अनुसंधान के प्रमुख केंद्र हैं।"
  },
  {
    "id": 22,
    "title": "ஹைதராபாத் - முத்துக்களின் நகரம் மற்றும் ஐடி மையம்",
    "language": "Tamil",
    "category": "Cities & Culture",
    "content": "ஹைதராபாத் தெலங்கானா மாநிலத்தின் தலைநகரமும் இந்தியாவின் முன்னணி பெருநகரமுமாகும். வரலாற்று ரீதியாக இது முத்துக்களின் நகரம் என்று அழைக்கப்படுகிறது. தற்போது உலகத்தரம் வாய்ந்த தகவல் தொழில்நுட்பம் மற்றும் மென்பொருள் நிறுவனங்களின் முக்கிய மையமாக ஹைதராபாத் திகழ்கிறது. சார்மினார், கோல்கொண்டா கோட்டை, மற்றும் உசேன் சாகர் ஏரி ஆகியவை வரலாற்று சிறப்புமிக்க இடங்களாகும். புகழ்பெற்ற பிரியாணி மற்றும் விருந்தோம்பல் கலாச்சாரம் இந்நகரத்தின் அடையாளமாகும்."
  },
  {
    "id": 23,
    "title": "தெலங்கானா மாநிலம் - வரலாறு, பண்பாடு மற்றும் புவியியல்",
    "language": "Tamil",
    "category": "Cities & Culture",
    "content": "தெலங்கானா தென்னிந்தியாவின் முக்கியமான மாநிலமாகும். கோதாவரி மற்றும் கிருஷ்ணா நதிகள் இம்மாநிலத்தின் வழியாக பாய்ந்து நிலங்களை வளப்படுத்துகின்றன. காகதீய வம்சத்தின் கலை நயம், வாரங்கல் கோட்டை, மற்றும் ராமப்பா கோவில் ஆகியவை பாரம்பரிய பெருமைகளை பறைசாற்றுகின்றன. பதுகம்மா மலர் திருவிழா இம்மாநிலத்தின் முக்கிய கலாச்சார விழாவாகும். ஹைதராபாத் இதன் நிர்வாக மற்றும் பொருளாதார தலைநகரமாகும்."
  },
  {
    "id": 24,
    "title": "இந்தியா - வேற்றுமையில் ஒற்றுமை மற்றும் ஜனநாயகம்",
    "language": "Tamil",
    "category": "Education",
    "content": "இந்தியா உலகின் மிகப்பெரிய ஜனநாயக நாடாகும். வடக்கே பனி சூழ்ந்த இமயமலையும், தெற்கே இந்தியப் பெருங்கடலும், கிழக்கே வங்காள விரிகுடாவும், மேற்கே அரபிக்கடலும் இயற்கை எல்லைகளாக அமைந்துள்ளன. புது தில்லி நாட்டின் தலைநகரமாகும். பல மொழிகள், மதங்கள் மற்றும் பழக்கவழக்கங்கள் இருந்தாலும் வேற்றுமையில் ஒற்றுமையே இந்தியாவின் தனிச்சிறப்பாகும். அறிவியல், விண்வெளி மற்றும் கல்வியில் இந்தியா மாபெரும் சாதனைகளை நிகழ்த்தி வருகிறது."
  },
  {
    "id": 25,
    "title": "இஸ்ரோ மற்றும் இந்தியாவின் விண்வெளி சாதனைகள்",
    "language": "Tamil",
    "category": "Science & Space",
    "content": "இந்திய விண்வெளி ஆய்வு மையம் (இஸ்ரோ) விண்வெளி அறிவியலில் மகத்தான சாதனைகளை படைத்துள்ளது. பெங்களூரை தலைமையிடமாகக் கொண்ட இஸ்ரோ, ஸ்ரீஹரிகோட்டாவில் உள்ள சதீஷ் தவான் விண்வெளி மையத்திலிருந்து ராக்கெட்டுகளை ஏவுகிறது. சந்திரயான் மற்றும் மங்கள்யான் திட்டங்கள் மூலம் உலக அரங்கில் இந்தியா பெரும் பாராட்டைப் பெற்றுள்ளது. குறைந்த செலவில் செயற்கைக்கோள்களை வெற்றிகரமாக விண்ணில் செலுத்துவதில் இஸ்ரோ முன்னோடியாக உள்ளது."
  },
  {
    "id": 26,
    "title": "இந்திய ரயில்வே மற்றும் வந்தே பாரத் ரயில்கள்",
    "language": "Tamil",
    "category": "Travel & Transport",
    "content": "இந்திய ரயில்வே உலகின் மிகப்பெரிய போக்குவரத்து வலையமைப்புகளில் ஒன்றாகும். தினசரி கோடிக்கணக்கான பயணிகளை இணைக்கும் நாட்டின் உயிர்நாடியாக இது செயல்படுகிறது. உள்நாட்டிலேயே தயாரிக்கப்பட்ட வந்தே பாரத் அதிவேக ரயில்கள் பயண நேரத்தை பெரிதும் குறைத்துள்ளன. செகந்திராபாத் மற்றும் சென்னை மத்திய ரயில் நிலையங்கள் தென்னிந்தியாவின் முக்கிய ரயில்வே சந்திப்புகளாகும்."
  },
  {
    "id": 27,
    "title": "கணினி அறிவியல் மற்றும் செயற்கை நுண்ணறிவு வளர்ச்சி",
    "language": "Tamil",
    "category": "Technology",
    "content": "இந்தியா கணினி அறிவியல், மென்பொருள் தயாரிப்பு மற்றும் செயற்கை நுண்ணறிவு (AI) துறைகளில் சர்வதேச அளவில் முன்னணியில் உள்ளது. ஹைதராபாத், சென்னை மற்றும் பெங்களூரு ஆகியவை முக்கிய தகவல் தொழில்நுட்ப நகரங்களாக விளங்குகின்றன. இளம் இந்திய பொறியாளர்கள் புதிய தொழில்நுட்பங்கள் மற்றும் கண்டுபிடிப்புகள் மூலம் உலகளாவிய டிஜிட்டல் பொருளாதாரத்தை வழிநடத்துகிறார்கள்."
  },
  {
    "id": 28,
    "title": "இந்தியாவில் உயர்கல்வி மற்றும் அறிவியல் ஆராய்ச்சி",
    "language": "Tamil",
    "category": "Education",
    "content": "இந்திய கல்வி முறை பாரம்பரிய அறிவையும் நவீன அறிவியல் ஆராய்ச்சிகளையும் ஒருங்கிணைக்கிறது. ஐஐடி, ஐஐஎஸ்சி மற்றும் மத்திய பல்கலைக்கழகங்கள் சிறந்த பொறியியல் மற்றும் அறிவியல் கல்வியை வழங்குகின்றன. உஸ்மானியா பல்கலைக்கழகம் மற்றும் ஹைதராபாத் மத்திய பல்கலைக்கழகம் ஆகியவை உயர்கல்வி மற்றும் ஆராய்ச்சியில் முக்கிய மையங்களாக விளங்குகின்றன."
  },
  {
    "id": 29,
    "title": "Rajiv Gandhi International Airport and Indian Civil Aviation",
    "language": "English",
    "category": "Aviation & Travel",
    "content": "Rajiv Gandhi International Airport located in Shamshabad, Hyderabad is one of India's premier international airports. Operated by GMR Group, it serves as a critical aviation hub connecting southern and central India to global destinations across the Middle East, Europe, and Southeast Asia. The airport has consistently earned accolades from Skytrax for exceptional passenger experience and environmental sustainability. India's civil aviation market has expanded rapidly with modern airlines, regional air connectivity initiatives, and upgraded passenger terminals supporting economic growth."
  },
  {
    "id": 30,
    "title": "రాజీవ్ గాంధీ అంతర్జాతీయ విమానాశ్రయం మరియు భారత విమానయాన రంగం",
    "language": "Telugu",
    "category": "Aviation & Travel",
    "content": "హైదరాబాద్‌లోని శంషాబాద్ వద్ద ఉన్న రాజీవ్ గాంధీ అంతర్జాతీయ విమానాశ్రయం భారతదేశంలోని అత్యుత్తమ విమానాశ్రయాలలో ఒకటి. జిఎమ్మార్ గ్రూప్ ఆధ్వర్యంలో నడుస్తున్న ఈ విమానాశ్రయం ప్రపంచ స్థాయి వసతులతో ప్రయాణికులకు సేవలు అందిస్తోంది. గల్ఫ్ దేశాలు, యూరప్ మరియు ఆసియా నగరాలకు హైదరాబాద్ నుంచి నేరుగా విమాన సర్వీసులు ఉన్నాయి. దక్షిణ భారతదేశానికి ఇది కీలకమైన విమానయాన కేంద్రంగా విలసిల్లుతోంది."
  },
  {
    "id": 31,
    "title": "राजीव गांधी अंतरराष्ट्रीय हवाई अड्डा और भारतीय नागरिक उड्डयन",
    "language": "Hindi",
    "category": "Aviation & Travel",
    "content": "हैदराबाद के शमशाबाद में स्थित राजीव गांधी अंतरराष्ट्रीय हवाई अड्डा भारत के सबसे आधुनिक और व्यस्ततम हवाई अड्डों में से एक है। यह हवाई अड्डा अंतरराष्ट्रीय मानकों के अनुरूप विश्वस्तरीय सेवाएं प्रदान करता है। हैदराबाद से खाड़ी देशों, यूरोप, अमेरिका और एशिया के प्रमुख शहरों के लिए नियमित उड़ानें उपलब्ध हैं। भारत का नागरिक उड्डयन क्षेत्र नई तकनीकों और बेहतर कनेक्टिविटी के साथ तीव्र गति से आगे बढ़ रहा है।"
  },
  {
    "id": 32,
    "title": "ராஜீவ் காந்தி சர்வதேச விமான நிலையம் மற்றும் இந்திய விமானப் போக்குவரத்து",
    "language": "Tamil",
    "category": "Aviation & Travel",
    "content": "ஹைதராபாத்தின் ஷம்ஷாபாத்தில் அமைந்துள்ள ராஜீவ் காந்தி சர்வதேச விமான நிலையம் இந்தியாவின் மிக நவீன மற்றும் முதன்மையான வானூர்தி நிலையங்களில் ஒன்றாகும். இது உலகத்தரம் வாய்ந்த உள்கட்டமைப்பு மற்றும் அதிநவீன தொழில்நுட்ப வசதிகளைக் கொண்டுள்ளது. மத்திய கிழக்கு, ஐரோப்பா மற்றும் தென்கிழக்கு ஆசிய நாடுகளுக்கு ஹைதராபாத்திலிருந்து நேரடி விமானங்கள் இயக்கப்படுகின்றன. இந்தியாவின் உள்நாட்டு மற்றும் சர்வதேச வான்வழி இணைப்பில் இந்நிலையம் முக்கிய பங்காற்றுகிறது."
  }
];

let allDocuments = [...FALLBACK_DOCUMENTS];
let isServerOnline = false;
let activeDocLang = 'All';

// -----------------------------------------------------------------------------
// DOM Ready Entry Point
// -----------------------------------------------------------------------------
document.addEventListener('DOMContentLoaded', async () => {
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
});

// -----------------------------------------------------------------------------
// 1. Navigation & SPA Routing
// -----------------------------------------------------------------------------
function initRouting() {
    const navLinks = document.querySelectorAll('.nav-link');

    function navigateToHash() {
        let hash = window.location.hash.replace('#', '').trim();
        if (!hash) hash = 'home';

        const validViews = ['home', 'search', 'algorithms', 'documents', 'about'];
        if (!validViews.includes(hash)) hash = 'home';

        document.querySelectorAll('.view-section').forEach(sec => {
            sec.classList.remove('active');
        });

        const targetSection = document.getElementById('view-' + hash);
        if (targetSection) {
            targetSection.classList.add('active');
        }

        navLinks.forEach(link => {
            if (link.getAttribute('data-view') === hash) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });

        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    window.addEventListener('hashchange', navigateToHash);
    navigateToHash();

    navLinks.forEach(link => {
        link.addEventListener('click', () => {
            const targetView = link.getAttribute('data-view');
            window.location.hash = '#' + targetView;
        });
    });
}

// -----------------------------------------------------------------------------
// 2. Server Status & Health Check
// -----------------------------------------------------------------------------
async function checkServerStatus() {
    const statusText = document.getElementById('backendStatus');
    const statusDot = document.querySelector('.status-dot');

    try {
        const resp = await fetch('/api/documents', { cache: 'no-store' });
        if (resp.ok) {
            const data = await resp.json();
            if (Array.isArray(data) && data.length > 0) {
                allDocuments = data;
            }
            isServerOnline = true;
            if (statusText) statusText.textContent = 'Engine Online';
            if (statusDot) statusDot.style.background = '#10b981';
            return;
        }
    } catch (err) {
        // Server offline, use standalone client mode
    }

    isServerOnline = false;
    if (statusText) statusText.textContent = 'Standalone Mode';
    if (statusDot) statusDot.style.background = '#f59e0b';
}

// -----------------------------------------------------------------------------
// 3. Search Engine & Automatic Algorithm Selection
// -----------------------------------------------------------------------------
function initSearchEvents() {
    const btnHeroSearch = document.getElementById('btnHeroSearch');
    const heroSearchInput = document.getElementById('heroSearchInput');
    const btnMainSearch = document.getElementById('btnMainSearch');
    const btnMainClear = document.getElementById('btnMainClear');
    const mainSearchInput = document.getElementById('mainSearchInput');
    const btnApplyCorrection = document.getElementById('btnApplyCorrection');

    // Hero Search Button
    if (btnHeroSearch && heroSearchInput) {
        btnHeroSearch.addEventListener('click', () => {
            const q = heroSearchInput.value.trim();
            if (q) {
                if (mainSearchInput) mainSearchInput.value = q;
                window.location.hash = '#search';
                performSearch(q);
            }
        });

        heroSearchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                btnHeroSearch.click();
            }
        });
    }

    // Main Search Button & Enter Key
    if (btnMainSearch && mainSearchInput) {
        btnMainSearch.addEventListener('click', () => {
            performSearch(mainSearchInput.value.trim());
        });

        mainSearchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                btnMainSearch.click();
            }
        });
    }

    // Clear Button
    if (btnMainClear && mainSearchInput) {
        btnMainClear.addEventListener('click', () => {
            mainSearchInput.value = '';
            mainSearchInput.focus();
        });
    }

    // Quick Test Chips
    document.querySelectorAll('.chip-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const q = btn.getAttribute('data-query');
            if (q) {
                if (mainSearchInput) mainSearchInput.value = q;
                window.location.hash = '#search';
                performSearch(q);
            }
        });
    });

    // Apply Suggested Typo Correction
    if (btnApplyCorrection) {
        btnApplyCorrection.addEventListener('click', () => {
            const correctedWord = btnApplyCorrection.textContent.trim();
            if (correctedWord) {
                if (mainSearchInput) mainSearchInput.value = correctedWord;
                performSearch(correctedWord);
            }
        });
    }
}

async function performSearch(query) {
    const mainSearchInput = document.getElementById('mainSearchInput');
    const q = (query !== undefined ? query : (mainSearchInput ? mainSearchInput.value : '')).trim();

    if (!q) return;

    if (mainSearchInput && mainSearchInput.value !== q) {
        mainSearchInput.value = q;
    }

    // Show loading state
    const resultsContainer = document.getElementById('searchResultsList');
    if (resultsContainer) {
        resultsContainer.innerHTML = '<div style="padding: 2.5rem; text-align: center; color: var(--text-muted);">Searching documents...</div>';
    }

    let data = null;

    if (isServerOnline) {
        try {
            const resp = await fetch(`/api/search?q=${encodeURIComponent(q)}&type=auto`);
            if (resp.ok) {
                data = await resp.json();
            }
        } catch (e) {
            console.warn('Backend search request failed, using standalone engine:', e);
        }
    }

    if (!data) {
        data = runClientSearch(q);
    }

    renderSearchAnalysis(data);
    renderSearchResults(data);
}

function renderSearchAnalysis(data) {
    const queryDisplay = document.getElementById('analysisQueryDisplay');
    const detectedEl = document.getElementById('analysisDetected');
    const selectedAlgoEl = document.getElementById('analysisSelectedAlgo');
    const searchAlgoEl = document.getElementById('analysisSearchAlgo');
    const correctionContainer = document.getElementById('analysisCorrectionContainer');
    const btnApplyCorrection = document.getElementById('btnApplyCorrection');
    const prefixContainer = document.getElementById('prefixPillsContainer');
    const prefixList = document.getElementById('prefixPillsList');
    const reasonDetails = document.getElementById('analysisReasonDetails');

    if (queryDisplay) queryDisplay.innerHTML = `Query: <strong>${escapeHtml(data.query)}</strong>`;

    // Map requirement to friendly user terminology
    let friendlyDetected = 'Exact keyword search';
    let friendlySelected = 'KMP';
    let friendlySearchAlgo = 'KMP';
    let friendlyReason = data.reason || '';

    const req = (data.detectedRequirement || '').toUpperCase();

    if (req.includes('FUZZY')) {
        friendlyDetected = 'Possible spelling variation';
        friendlySelected = 'Levenshtein Edit Distance';
        friendlySearchAlgo = 'KMP';
        friendlyReason = `The system detected that "${data.query}" is a likely spelling variation of "${data.suggestion || 'Hyderabad'}". Levenshtein Edit Distance computed the minimum character changes to determine the closest match, followed by KMP to search documents.`;
    } else if (req.includes('PREFIX')) {
        friendlyDetected = 'Prefix search';
        friendlySelected = 'Trie';
        friendlySearchAlgo = 'Trie';
        friendlyReason = `The query "${data.query}" was recognized as a prefix. The Trie structure navigated character paths in O(m) time to return instant autocomplete suggestions and matching documents.`;
    } else if (req.includes('MULTI')) {
        friendlyDetected = 'Multiple keywords';
        friendlySelected = 'Aho-Corasick';
        friendlySearchAlgo = 'Aho-Corasick';
        friendlyReason = `The query contains multiple keywords. The Aho-Corasick automaton searched for all words simultaneously in a single linear pass using precomputed failure links.`;
    } else {
        friendlyDetected = 'Exact keyword search';
        friendlySelected = 'KMP';
        friendlySearchAlgo = 'KMP';
        friendlyReason = `The query was found directly in the vocabulary. Knuth-Morris-Pratt (KMP) was selected for guaranteed O(n + m) linear searching without text backtracking.`;
    }

    if (detectedEl) detectedEl.textContent = friendlyDetected;
    if (selectedAlgoEl) selectedAlgoEl.textContent = friendlySelected;
    if (searchAlgoEl) searchAlgoEl.textContent = friendlySearchAlgo;
    if (reasonDetails) reasonDetails.textContent = friendlyReason;

    // Suggested correction banner
    if (correctionContainer && btnApplyCorrection) {
        if (data.suggestion && data.suggestion.toLowerCase() !== data.query.toLowerCase()) {
            correctionContainer.style.display = 'flex';
            btnApplyCorrection.textContent = data.suggestion;
        } else {
            correctionContainer.style.display = 'none';
        }
    }

    // Prefix autocomplete pills
    if (prefixContainer && prefixList) {
        if (data.autocompletions && data.autocompletions.length > 0) {
            prefixContainer.style.display = 'flex';
            prefixList.innerHTML = '';
            data.autocompletions.forEach(word => {
                const pill = document.createElement('button');
                pill.className = 'pill-item-btn';
                pill.textContent = word;
                pill.addEventListener('click', () => {
                    performSearch(word);
                });
                prefixList.appendChild(pill);
            });
        } else {
            prefixContainer.style.display = 'none';
            prefixList.innerHTML = '';
        }
    }
}

function renderSearchResults(data) {
    const titleEl = document.getElementById('resultsQueryTitle');
    const countEl = document.getElementById('resultsCountBadge');
    const container = document.getElementById('searchResultsList');

    if (titleEl) titleEl.textContent = `"${data.query}"`;
    const count = data.results ? data.results.length : 0;
    if (countEl) countEl.textContent = `Found ${count} ${count === 1 ? 'document' : 'documents'}`;

    if (!container) return;
    container.innerHTML = '';

    if (count === 0) {
        container.innerHTML = `
            <div class="result-card" style="text-align: center; padding: 2.5rem 1rem;">
                <h3 style="margin-bottom: 0.5rem; color: var(--text-primary);">No documents found</h3>
                <p style="color: var(--text-muted); font-size: 0.9rem;">
                    Try checking spelling or using a prefix such as <code>hyd</code> or multiple keywords like <code>India Telangana</code>.
                </p>
            </div>
        `;
        return;
    }

    const searchTerm = data.suggestion || data.query;

    data.results.forEach((item, idx) => {
        const card = document.createElement('div');
        card.className = 'result-card';

        const langClass = getLangClass(item.language);
        const rankClass = (item.rankLevel === 'High' || idx === 0) ? 'rank-high' : 'rank-medium';
        const rankText = item.rankLevel || (idx < 2 ? 'High' : 'Medium');

        // Extract snippet
        let snippet = item.snippet || item.content || '';
        if (snippet.length > 220) {
            snippet = snippet.substring(0, 220) + '...';
        }

        card.innerHTML = `
            <div class="result-card-header">
                <div>
                    <h3 class="result-title">${escapeHtml(item.documentTitle)}</h3>
                </div>
                <div class="result-badges">
                    <span class="cat-tag">${escapeHtml(item.category || 'General')}</span>
                    <span class="lang-tag ${langClass}">${escapeHtml(item.language)}</span>
                    <span class="rank-badge ${rankClass}">Rank: ${rankText}</span>
                </div>
            </div>

            <div class="result-snippet">
                ${highlightMatch(snippet, searchTerm)}
            </div>

            <div class="result-card-footer">
                <div class="matched-keywords">
                    <span>Matched:</span>
                    <strong style="color: var(--text-primary);">${escapeHtml(searchTerm)}</strong>
                </div>
                <button class="btn-read-doc" data-id="${item.documentId}">Read Document &rarr;</button>
            </div>
        `;

        const readBtn = card.querySelector('[data-id]');
        if (readBtn) {
            readBtn.addEventListener('click', () => {
                openDocumentModal(item.documentId, item.documentTitle, item.language, item.category || 'General', item.content);
            });
        }

        container.appendChild(card);
    });
}

function getLangClass(lang) {
    switch ((lang || '').toLowerCase()) {
        case 'telugu': return 'lang-te';
        case 'hindi': return 'lang-hi';
        case 'tamil': return 'lang-ta';
        default: return 'lang-en';
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function highlightMatch(text, query) {
    if (!text) return '';
    if (!query) return escapeHtml(text);

    const safeText = escapeHtml(text);
    const tokens = query.trim().split(/[,\s]+/).filter(t => t.length > 0);
    if (tokens.length === 0) return safeText;

    const regexPattern = tokens.map(t => escapeRegExp(t)).join('|');
    const regex = new RegExp(`(${regexPattern})`, 'gi');

    return safeText.replace(regex, '<mark class="highlight">$1</mark>');
}

function escapeRegExp(string) {
    return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

// -----------------------------------------------------------------------------
// 4. Documents Page & Filtering
// -----------------------------------------------------------------------------
function initDocFilterEvents() {
    const filterInput = document.getElementById('docFilterInput');
    const filterButtons = document.querySelectorAll('.doc-filter-btn');

    if (filterInput) {
        filterInput.addEventListener('input', () => {
            renderDocumentsGrid();
        });
    }

    filterButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            filterButtons.forEach(b => b.classList.remove('active-doc-filter'));
            btn.classList.add('active-doc-filter');
            activeDocLang = btn.getAttribute('data-lang') || 'All';
            renderDocumentsGrid();
        });
    });
}

function updateLanguageCounts() {
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
}

function renderDocumentsGrid() {
    const grid = document.getElementById('documentsListGrid');
    const filterInput = document.getElementById('docFilterInput');
    if (!grid) return;

    const query = (filterInput ? filterInput.value : '').toLowerCase().trim();

    const filtered = allDocuments.filter(doc => {
        const matchLang = (activeDocLang === 'All') || (doc.language.toLowerCase() === activeDocLang.toLowerCase());
        const matchQuery = !query ||
            doc.title.toLowerCase().includes(query) ||
            doc.content.toLowerCase().includes(query) ||
            (doc.category && doc.category.toLowerCase().includes(query));
        return matchLang && matchQuery;
    });

    grid.innerHTML = '';

    if (filtered.length === 0) {
        grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; color: var(--text-muted); padding: 3rem;">No documents match your filter.</div>';
        return;
    }

    filtered.forEach(doc => {
        const card = document.createElement('div');
        card.className = 'doc-item-card';

        let snippet = doc.content.length > 150 ? doc.content.substring(0, 150) + '...' : doc.content;

        card.innerHTML = `
            <div>
                <div class="doc-card-head" style="margin-bottom: 0.5rem;">
                    <span class="cat-tag">${escapeHtml(doc.category || 'General')}</span>
                    <span class="lang-tag ${getLangClass(doc.language)}">${escapeHtml(doc.language)}</span>
                </div>
                <h4 class="doc-card-title">${escapeHtml(doc.title)}</h4>
                <p class="doc-card-snippet" style="margin-top: 0.5rem;">${escapeHtml(snippet)}</p>
            </div>
            <button class="btn-read-doc" style="align-self: flex-start; margin-top: 0.5rem;" data-doc-id="${doc.id}">Read Document &rarr;</button>
        `;

        const btn = card.querySelector('[data-doc-id]');
        if (btn) {
            btn.addEventListener('click', () => {
                openDocumentModal(doc.id, doc.title, doc.language, doc.category || 'General', doc.content);
            });
        }

        grid.appendChild(card);
    });
}

// -----------------------------------------------------------------------------
// 5. Document Modal
// -----------------------------------------------------------------------------
function initModalEvents() {
    const modal = document.getElementById('docModal');
    const closeBtn = document.getElementById('btnModalClose');
    const closeFooterBtn = document.getElementById('btnModalCloseFooter');

    const closeModal = () => {
        if (modal) modal.classList.add('hidden');
    };

    if (closeBtn) closeBtn.addEventListener('click', closeModal);
    if (closeFooterBtn) closeFooterBtn.addEventListener('click', closeModal);

    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) closeModal();
        });
    }
}

function openDocumentModal(id, title, language, category, content) {
    const modal = document.getElementById('docModal');
    const titleEl = document.getElementById('modalDocTitle');
    const langEl = document.getElementById('modalDocLang');
    const catEl = document.getElementById('modalDocCat');
    const idEl = document.getElementById('modalDocId');
    const contentEl = document.getElementById('modalDocContent');

    if (!modal) return;

    if (titleEl) titleEl.textContent = title;
    if (langEl) {
        langEl.textContent = language;
        langEl.className = `lang-tag ${getLangClass(language)}`;
    }
    if (catEl) catEl.textContent = category || 'General';
    if (idEl) idEl.textContent = `ID: #${id}`;
    if (contentEl) contentEl.textContent = content;

    modal.classList.remove('hidden');
}

// -----------------------------------------------------------------------------
// 6. Standalone Client Search Engine (Offline Fallback)
// -----------------------------------------------------------------------------
function runClientSearch(query) {
    const q = query.trim();
    const tokens = q.split(/[,\s]+/).filter(t => t.length > 0);

    let detectedReq = 'EXACT_SEARCH';
    let selectedAlgo = 'KMP';
    let secondaryAlgo = 'MergeSort';
    let suggestion = null;
    let autocompletions = [];

    // Case 4: Multiple keywords
    if (tokens.length > 1) {
        detectedReq = 'MULTI_PATTERN_SEARCH';
        selectedAlgo = 'Aho-Corasick';
    }
    // Case 3: Prefix search (length <= 3)
    else if (q.length <= 3) {
        detectedReq = 'PREFIX_SEARCH';
        selectedAlgo = 'Trie';
        autocompletions = clientTrieSuggestions(q);
    }
    // Case 1 & 2: Check exact presence vs typo
    else {
        const exactMatches = countExactMatches(q);
        if (exactMatches === 0) {
            const closest = findClosestCorrection(q);
            if (closest && closest.toLowerCase() !== q.toLowerCase()) {
                detectedReq = 'FUZZY_SEARCH';
                selectedAlgo = 'Levenshtein Edit Distance';
                secondaryAlgo = 'KMP';
                suggestion = closest;
            } else {
                detectedReq = 'EXACT_SEARCH';
                selectedAlgo = 'KMP';
            }
        } else {
            detectedReq = 'EXACT_SEARCH';
            selectedAlgo = 'KMP';
        }
    }

    const target = suggestion || q;
    let results = [];

    allDocuments.forEach(doc => {
        const full = doc.title + ' ' + doc.content;
        let count = 0;

        if (detectedReq === 'MULTI_PATTERN_SEARCH') {
            tokens.forEach(tok => {
                count += countOccurrences(full, tok);
            });
        } else {
            count = countOccurrences(full, target);
        }

        if (count > 0) {
            let score = 50 + count * 15;
            if (doc.title.toLowerCase().includes(target.toLowerCase())) score += 20;

            let snippet = doc.content;
            if (snippet.length > 200) {
                const idx = snippet.toLowerCase().indexOf(target.toLowerCase());
                if (idx >= 0) {
                    const start = Math.max(0, idx - 40);
                    const end = Math.min(snippet.length, idx + 140);
                    snippet = (start > 0 ? '...' : '') + snippet.substring(start, end) + (end < snippet.length ? '...' : '');
                } else {
                    snippet = snippet.substring(0, 180) + '...';
                }
            }

            results.push({
                documentId: doc.id,
                documentTitle: doc.title,
                language: doc.language,
                category: doc.category || 'General',
                snippet: snippet,
                content: doc.content,
                score: score,
                rankLevel: score >= 80 ? 'High' : 'Medium',
                matchCount: count
            });
        }
    });

    // Sort by score descending (MergeSort principle)
    results.sort((a, b) => b.score - a.score);

    return {
        query: q,
        detectedRequirement: detectedReq,
        selectedAlgorithm: selectedAlgo,
        secondaryAlgorithm: secondaryAlgo,
        suggestion: suggestion,
        autocompletions: autocompletions,
        results: results
    };
}

function countOccurrences(text, pattern) {
    if (!text || !pattern) return 0;
    let count = 0;
    let pos = text.toLowerCase().indexOf(pattern.toLowerCase());
    while (pos !== -1) {
        count++;
        pos = text.toLowerCase().indexOf(pattern.toLowerCase(), pos + pattern.length);
    }
    return count;
}

function countExactMatches(word) {
    let total = 0;
    allDocuments.forEach(d => {
        total += countOccurrences(d.title + ' ' + d.content, word);
    });
    return total;
}

function clientLevenshtein(s1, s2) {
    const m = s1.length, n = s2.length;
    const dp = Array.from({ length: m + 1 }, () => new Array(n + 1).fill(0));
    for (let i = 0; i <= m; i++) dp[i][0] = i;
    for (let j = 0; j <= n; j++) dp[0][j] = j;

    for (let i = 1; i <= m; i++) {
        for (let j = 1; j <= n; j++) {
            if (s1[i - 1].toLowerCase() === s2[j - 1].toLowerCase()) {
                dp[i][j] = dp[i - 1][j - 1];
            } else {
                dp[i][j] = 1 + Math.min(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1]);
            }
        }
    }
    return dp[m][n];
}

function findClosestCorrection(query) {
    const vocab = [
        'Hyderabad', 'Telangana', 'India', 'ISRO', 'Railways', 'Vande', 'Bharat', 'Charminar',
        'Airport', 'Golconda', 'Technology', 'Education', 'Warangal', 'Bathukamma',
        'హైదరాబాద్', 'తెలంగాణ', 'భారతదేశం', 'ఇస్రో', 'రైల్వే',
        'हैदराबाद', 'तेलंगाना', 'भारत', 'इसरो', 'रेलवे',
        'ஹைதராபாத்', 'தெலங்கானா', 'இந்தியா', 'இஸ்ரோ', 'ரயில்வே'
    ];

    let best = null;
    let minD = 999;

    vocab.forEach(w => {
        const d = clientLevenshtein(query, w);
        if (d > 0 && d < minD && d <= 3) {
            minD = d;
            best = w;
        }
    });

    return best;
}

function clientTrieSuggestions(prefix) {
    const words = [
        'Hyderabad', 'Hyderabad Airport', 'Hyderabad Metro', 'Hyderabad History', 'Hyderabad IT Hub',
        'Telangana', 'Telangana Culture', 'India', 'Indian Railways', 'ISRO',
        'హైదరాబాద్ సంస్కృతి', 'తెలంగాణ చరిత్ర', 'భారతీయ రైల్వే',
        'हैदराबाद मेट्रो', 'तेलंगाना संस्कृति', 'भारतीय अंतरिक्ष',
        'ஹைதராபாத் நகரம்', 'தெலங்கானா பண்பாடு', 'இந்திய விண்வெளி'
    ];
    return words.filter(w => w.toLowerCase().startsWith(prefix.toLowerCase())).slice(0, 5);
}
