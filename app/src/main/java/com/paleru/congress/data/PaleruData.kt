package com.paleru.congress.data

enum class AppLanguage { TELUGU, ENGLISH }

data class LocalizedText(val te: String, val en: String) {
    fun inLanguage(language: AppLanguage): String =
        if (language == AppLanguage.TELUGU) te else en
}

data class ElectionRecord(
    val year: Int,
    val era: LocalizedText,
    val congressCandidate: LocalizedText,
    val congressVotes: Int,
    val opponent: LocalizedText,
    val opponentParty: LocalizedText,
    val opponentVotes: Int,
    val won: Boolean,
    val margin: Int,
    val note: LocalizedText = LocalizedText("", "")
)

data class MinisterRecord(
    val name: LocalizedText,
    val period: LocalizedText,
    val portfolio: LocalizedText,
    val state: LocalizedText,
    val partyAtThatTime: LocalizedText,
    val note: LocalizedText,
    val current: Boolean = false
)

data class GramPanchayatRecord(
    val nameTe: String,
    val nameEn: String,
    val sarpanchOfficialName: String,
    val resultYear: Int = 2025,
    val declaredAge: Int? = null,
    val ageReferenceYear: Int? = null,
    val ageSourceUrl: String? = null,
    val photoUrl: String? = null,
    val photoSourceUrl: String? = null
) {
    val mapQuery: String get() = "$nameEn, Telangana, India"
}

data class MandalRecord(
    val id: String,
    val nameTe: String,
    val nameEn: String,
    val congressPresidentTe: String,
    val congressPresidentEn: String,
    val presidentEvidence: LocalizedText,
    val presidentSourceUrl: String,
    val presidentDeclaredAge: Int? = null,
    val presidentAgeReferenceYear: Int? = null,
    val presidentAgeSourceUrl: String? = null,
    val presidentPhotoUrl: String? = null,
    val gramPanchayats: List<GramPanchayatRecord>
) {
    val mapQuery: String get() = "$nameEn Mandal, Khammam, Telangana, India"
}

data class SourceRecord(
    val title: LocalizedText,
    val detail: LocalizedText,
    val url: String
)

data class LeaderProfile(
    val name: LocalizedText,
    val role: LocalizedText,
    val constituency: LocalizedText,
    val portfolio: LocalizedText,
    val photoUrl: String,
    val photoSourceUrl: String,
    val verifiedOn: String,
    val declaredAge: Int? = null,
    val ageReferenceYear: Int? = null,
    val ageSourceUrl: String? = null
)

object PaleruData {
    const val sarpanchResultsUrl = "https://tsec.gov.in/knowPRRural.se"
    const val mandalPresidentsUrl = "https://www.thehansindia.com/telangana/cong-appoints-new-mandal-town-presidents-in-khammam-district-1074157"
    const val panchayatDirectoryUrl = "https://khammam.telangana.gov.in/villages-panchayats/"
    const val eci2023DetailedResultsUrl = "https://www.eci.gov.in/eci-backend/public/all_files/full-statistical-reports/telangana/2023/Detailed_Results.pdf"

    val currentLeader = LeaderProfile(
        name = LocalizedText("పొంగులేటి శ్రీనివాస రెడ్డి", "Ponguleti Srinivas Reddy"),
        role = LocalizedText("పాలేరు ఎమ్మెల్యే మరియు తెలంగాణ మంత్రి", "Palair MLA and Telangana Minister"),
        constituency = LocalizedText("పాలేరు", "Palair"),
        portfolio = LocalizedText(
            "రెవెన్యూ, గృహ నిర్మాణం, సమాచార మరియు ప్రజాసంబంధాలు",
            "Revenue and Housing; Information & Public Relations"
        ),
        photoUrl = "https://www.telangana.gov.in/wp-content/uploads/2023/12/Sri-Ponguleti-Srinivas-Reddy-837x1024.jpg",
        photoSourceUrl = "https://khammam.telangana.gov.in/public-representatives/",
        verifiedOn = "2026-06-30",
        declaredAge = 58,
        ageReferenceYear = 2023,
        ageSourceUrl = eci2023DetailedResultsUrl
    )

    private fun gp(te: String, en: String, sarpanch: String) =
        GramPanchayatRecord(nameTe = te, nameEn = en, sarpanchOfficialName = sarpanch)

    val mandals = listOf(
        MandalRecord(
            id = "khammam-rural",
            nameTe = "ఖమ్మం రూరల్",
            nameEn = "Khammam Rural",
            congressPresidentTe = "తోట వీరభద్రం",
            congressPresidentEn = "Thota Veerabhadram",
            presidentEvidence = LocalizedText("మే 2026 ప్రజా నివేదిక", "Public report, May 2026"),
            presidentSourceUrl = mandalPresidentsUrl,
            gramPanchayats = listOf(
                gp("ఆరెకోడు", "Arekodu", "Arempula Ramdevi"),
                gp("ఆరెకోడు తండా", "Arekodu Thanda", "Gugulothu Mangamma"),
                gp("అరెంపుల", "Arempula", "Bandi Sateesh"),
                gp("చింతపల్లి", "Chintapally", "Arempula Mariyamma"),
                gp("దారేడు", "Daredu", "Bathula Venkateshwarlu"),
                gp("గొల్లపాడు", "Gollapadu", "Gundu Sravani"),
                gp("గూడూరుపాడు", "Gudurupadu", "Puchakayala Sridevi"),
                gp("కాచిరాజుగూడెం", "Kachirajugudem", "Hari Malothu"),
                gp("కామంచికల్", "Kamanchikal", "Boppi Prabhakar Rao"),
                gp("కాస్నా తండా", "Kasnathanda", "Banothu Papa"),
                gp("కొండాపురం", "Kondapuram", "Dasari Padma"),
                gp("మంగళగూడెం", "Mangalagudem", "Mekala Satyanarayana"),
                gp("ఎం. వెంకటాయపాలెం", "M. Venkatayapalem", "Vijaya Kumar Veginati"),
                gp("పడమటి తండా", "Padamati Thanda", "Boda Bheema"),
                gp("పల్లెగూడెం", "Pallegudem", "Chuduri Srujana"),
                gp("పోలేపల్లి", "Polepally", "Batini Mahesh"),
                gp("పోలిశెట్టిగూడెం", "Polisettigudem", "Kallem Jan Reddy"),
                gp("పొన్నెకల్", "Ponnekal", "Koti Srivasa Rao"),
                gp("తల్లంపాడు", "Tallampadu", "Kummari Ambedkar"),
                gp("తానగంపాడు", "Tanagampadu", "Aswini Jarpula"),
                gp("తీర్థాల", "Thirthala", "Bhukya Sailaja")
            )
        ),
        MandalRecord(
            id = "kusumanchi",
            nameTe = "కూసుమంచి",
            nameEn = "Kusumanchi",
            congressPresidentTe = "బజ్జూరి వెంకటరెడ్డి",
            congressPresidentEn = "Bajjuri Venkata Reddy",
            presidentEvidence = LocalizedText("మే 2026 ప్రజా నివేదిక", "Public report, May 2026"),
            presidentSourceUrl = mandalPresidentsUrl,
            gramPanchayats = listOf(
                gp("అగ్రహారం", "Agraharam", "Mallela Swathi"),
                gp("అజ్మీరా హీరామన్ తండా", "Azmeera Hiraman Thanda", "Azmeera Amani"),
                gp("భగత్ వీడు", "Bhagathveedu", "Vasantha Laxmi Bhukya"),
                gp("బోడియా తండా", "Bodiyathanda", "Boda Veeru"),
                gp("చాంద్యా తండా", "Chandyathanda", "Bonga Naik Banoth"),
                gp("చేగొమ్మ", "Chegomma", "Bathula Veeraswamy"),
                gp("చౌటపల్లి", "Chowtapalli", "Makka Rama Krishna"),
                gp("ధర్మా తండా", "Dharmathanda", "Jarpula Kiranmai"),
                gp("ఈశ్వరమాదారం", "Eswaramadaram", "Kolisetti Srinivas"),
                gp("గైగొల్లపల్లి", "Gaigollapalli", "Anusha Badavath"),
                gp("గంగబండ తండా", "Gangabandathanda", "Rajamma Vaditya"),
                gp("గట్టు సింగారం", "Gattusingaram", "Medepalli Vijaya"),
                gp("గోరీలపాడు తండా", "Goreelapaduthanda", "Banoth Saraswathi"),
                gp("గుర్వాయిగూడెం", "Guravaigudem", "Banothu Bixam"),
                gp("జక్కేపల్లి", "Jakkepalli", "Nallabolu Chandra Reddy"),
                gp("జక్కేపల్లి ఎస్సీ కాలనీ", "Jakkepalli SC Colony", "Tangella Laxmaiah"),
                gp("జీలచెరువు", "Jeellacheruvu", "Ithagani Venkataramana"),
                gp("జుజ్జులరావుపేట", "Jujjularaopeta", "Datla Satheesh"),
                gp("కేశవాపురం", "Keshavapuram", "Banoth Mamatha"),
                gp("కిష్టాపురం", "Kistapuram", "Konda Saidulu"),
                gp("కోక్యా తండా", "Kokyathanda", "Halavath Veeraiah"),
                gp("కొత్తూరు", "Kothuru", "Neelakantam Lodiga"),
                gp("కూసుమంచి", "Kusumanchi", "Konda Krishnaveni"),
                gp("లాల్‌సింగ్ తండా", "Lalsingh Thanda", "Bhukya Savitri"),
                gp("లింగారం తండా", "Lingaramthanda", "Pushpavathi Vaditya"),
                gp("లోక్యా తండా", "Lokyathanda", "Vadithya Venkatesh"),
                gp("మల్లాయిగూడెం", "Mallaigudem", "Badhavath Naresh"),
                gp("మల్లేపల్లి", "Mallepalli", "Tellamekala Bhavitha"),
                gp("మంగలి తండా", "Mangalthanda", "Madhavi Gugulothu"),
                gp("మునిగేపల్లి", "Munigepalli", "Ganga Sravanthi"),
                gp("ముత్యాలగూడెం", "Mutyalagudem", "Ashok Perelli"),
                gp("నాయకన్‌గూడెం", "Naikangudem", "Kanchari Saidamma"),
                gp("నరసింహులగూడెం", "Narasimhulagudem", "Kalikini Saritha"),
                gp("నేలపట్ల", "Nelapatla", "Nookala Shobhan Babu"),
                gp("పాలేరు", "Palair", "Banoth Nageswara Rao"),
                gp("పెరికసింగారం", "Perikasingaram", "Mangamma Kandunuri"),
                gp("పోచారం", "Pocharam", "Salavadi Gurmurthi"),
                gp("రాజుపేట", "Rajupeta", "Bhanoth Mahesh"),
                gp("రాజుపేట బజార్", "Rajupeta Bazar", "Bhukya Shirisha"),
                gp("తురకగూడెం", "Turakagudem", "Burra Krishna"),
                gp("ఎర్రగడ్డ తండా", "Yarragaddathanda", "Anasurya Jarpula")
            )
        ),
        MandalRecord(
            id = "nelakondapalle",
            nameTe = "నేలకొండపల్లి",
            nameEn = "Nelakondapalle",
            congressPresidentTe = "కోదాలి గోవిందరావు",
            congressPresidentEn = "Kodali Govinda Rao",
            presidentEvidence = LocalizedText("మే 2026 ప్రజా నివేదిక", "Public report, May 2026"),
            presidentSourceUrl = mandalPresidentsUrl,
            gramPanchayats = listOf(
                gp("ఆచర్లగూడెం", "Acharlagudem", "Kolikapongu Uppalama"),
                gp("అజయ్ తండా", "Ajay Thanda", "Tejavath Shivaji"),
                gp("అమ్ముగూడెం", "Ammugudem", "Laxmi Potta"),
                gp("అనసాగరం", "Anasagaram", "Boddu Rambabu"),
                gp("అప్పల నరసింహాపురం", "Appalanarasimha Puram", "Manne Raja Sri"),
                gp("ఆరెగూడెం", "Aregudem", "Vishala Vakkantula"),
                gp("భైరవునిపల్లి", "Bhairavunipally", "Laxmanarao Gundapaneni"),
                gp("బోదులబండ", "Bodulabanda", "Kattekola Nagarjunarao"),
                gp("బుద్ధారం", "Buddaram", "Mellacheruvu Satyam"),
                gp("చెన్నారం", "Chennaram", "Battapothula Nagamani"),
                gp("చెరువు మాదారం", "Cheruvumadharam", "Amaragani Yallaiah"),
                gp("గువ్వలగూడెం", "Guvvala Gudem", "Ravella Jyothi"),
                gp("కట్టు కాచారం", "Kattukacharam", "Bhanavathu Saidulu"),
                gp("కోనాయిగూడెం", "Konaigudem", "Koti Saida Reddy"),
                gp("కొంగర", "Kongara", "Mallempudi Krishna Kumari"),
                gp("కొరట్లగూడెం", "Koratlagudem", "Kothapalli Thriveni"),
                gp("కొత్త కొత్తూరు", "Kotha Kothuru", "Maloth Kalavathi"),
                gp("మండ్రాజుపల్లి", "Mandrajupally", "Lavuri Koteswara Rao"),
                gp("మంగాపురం తండా", "Mangapuramthanda", "Ashoka Rani Deeravathu"),
                gp("మోటాపురం", "Motapuram", "Eluri Ramarao"),
                gp("ముజ్జుగూడెం", "Mujjugudem", "Boddu Vasantha"),
                gp("నాచేపల్లి", "Nachepally", "Mounika Bhukya"),
                gp("నేలకొండపల్లి", "Nelakondapally", "Seelam Venkata Laxmi"),
                gp("పైనంపల్లి", "Painampalli", "Kukkala Nagaraju"),
                gp("రాజారాంపేట", "Rajaram Peta", "Rayapudi Ramarao"),
                gp("రాజేశ్వరపురం", "Rajeswarapuram", "Danda Rangaiah"),
                gp("రామచంద్రాపురం", "Ramachandrapuram", "Duddela Pavan"),
                gp("రవిగూడెం", "Ravigudem", "Boina Venu"),
                gp("సదాశివపురం", "Sadasivapuram", "Palapati Suresh"),
                gp("శంకరగిరి తండా", "Sankaragirithanda", "Dharavath Sarojini"),
                gp("సుర్దేపల్లి", "Surdepally", "Garidepalli Sujatha"),
                gp("తిరుమలాపురం తండా", "Thirumalapuram Thanda", "Kamadana Praveen")
            )
        ),
        MandalRecord(
            id = "thirumalayapalem",
            nameTe = "తిరుమలాయపాలెం",
            nameEn = "Thirumalayapalem",
            congressPresidentTe = "కొప్పుల అశోక్",
            congressPresidentEn = "Koppula Ashok",
            presidentEvidence = LocalizedText("మే 2026 ప్రజా నివేదిక", "Public report, May 2026"),
            presidentSourceUrl = mandalPresidentsUrl,
            gramPanchayats = listOf(
                gp("అజ్మీరా తండా", "Azmeera Thanda", "Azmeera Neelima"),
                gp("బచ్చోడు", "Bachodu", "Mallikarujn Nandipati"),
                gp("బచ్చోడు తండా", "Bachodu Thanda", "Bhukya Kumari"),
                gp("బాలాజీ నగర్ తండా", "Balaji Nagar Thanda", "Maloth Bhadru"),
                gp("బీరోలు", "Beerolu", "Gummadi Deva Biksham"),
                gp("చంద్రు తండా", "Chandruthanda", "Boda Bharathi"),
                gp("దమ్మాయిగూడెం", "Dammaigudem", "Veeradasu Vinoda"),
                gp("ఎద్దులచెరువు", "Eddula Chervu", "Arempala Vinoda"),
                gp("ఎలువరిగూడెం", "Eluvarigudem", "Guguloth Thulasi"),
                gp("గోల్ తండా", "Golthanda", "Banoth Sujatha"),
                gp("హస్నాబాద్", "Hasnabad", "Padma Kovuri"),
                gp("హైదర్‌సాయిపేట", "Hydersaipeta", "Banoth Chimla"),
                gp("ఇస్లావత్ తండా", "Islavath Thanda", "Islavath Sunitha"),
                gp("జల్లేపల్లి", "Jallepally", "Anneparthi Ravi"),
                gp("జోగులపాడు", "Jogulapadu", "Daravath Venkanna"),
                gp("జూపెడ", "Jupeda", "Vanavasam Narendar Reddy"),
                gp("కాకర్వాయి", "Kakarvai", "Guduru Upendar"),
                gp("కేశవాపురం", "Keshavapuram", "Eedula Mahesh"),
                gp("కొక్కిరేణి", "Kokkireni", "Rajender Prasad Karnati"),
                gp("లక్ష్మీదేవిపల్లి", "Laxmidevipalli", "Venkanna Mudu alias Venkat Nayak"),
                gp("మంగలి బండ తండా", "Mangali Banda Thanda", "Daravath Murali"),
                gp("మెడిదపల్లి", "Medidapally", "Mora Anil Reddy"),
                gp("మేకల తండా", "Mekala Thanda", "Banoth Laxmi"),
                gp("మహమ్మదాపురం", "Mohammadapuram", "Manju Boda"),
                gp("పడమటి తండా", "Padamati Thanda", "Gugulothu Kamala"),
                gp("పైనంపల్లి", "Painampally", "Dharavath Sarojini"),
                gp("పాతర్లపాడు", "Patharlapadu", "Jadala Sushma"),
                gp("పిండిప్రోలు", "Pindiprolu", "Kaamandal Suvartha"),
                gp("రఘునాథపాలెం", "Raghunadhapalem", "Chandraiah Gandu"),
                gp("రాజారం", "Rajaram", "Mandula Aruna"),
                gp("సోలిపురం", "Solipuram", "Nunavathu Sammakka"),
                gp("సుబ్లేడు", "Sublaid", "Swathi Sangabathula"),
                gp("సుద్దవాగు తండా", "Suddavagu Thanda", "Banoth Ravi"),
                gp("తెట్టెలపాడు", "Tettalapadu", "Chirra Narsamma"),
                gp("తల్లచెరువు", "Thallacheruvu", "Gadupudi Venkata Narayana"),
                gp("తిప్పారెడ్డిగూడెం", "Thippareddigudem", "Kumbham Upender"),
                gp("తిమ్మక్కపేట", "Timmakkapeta", "Repakula Subhadra"),
                gp("తిరుమలాయపాలెం", "Tirumalayapalem", "Daravath Sujatha"),
                gp("యేనకుంట తండా", "Yenakunta Thanda", "Banoth Sukya"),
                gp("ఎర్రగడ్డ", "Yerra Gadda", "Nallamalla Keerthi")
            )
        )
    )

    val gramPanchayats: List<Pair<MandalRecord, GramPanchayatRecord>> =
        mandals.flatMap { mandal -> mandal.gramPanchayats.map { mandal to it } }

    val elections = listOf(
        ElectionRecord(1962, LocalizedText("ఉమ్మడి ఆంధ్రప్రదేశ్", "United Andhra Pradesh"), LocalizedText("కత్తుల శాంతయ్య", "Kathula Santhaiah"), 21895, LocalizedText("నామవరపు పెద్దన్న", "Namavarapu Peddanna"), LocalizedText("కమ్యూనిస్టు పార్టీ", "Communist Party"), 19936, true, 1959),
        ElectionRecord(1967, LocalizedText("ఉమ్మడి ఆంధ్రప్రదేశ్", "United Andhra Pradesh"), LocalizedText("కె. శాంతయ్య", "K. Santhaiah"), 25149, LocalizedText("ఎస్. సుందరయ్య", "S. Sundaraiah"), LocalizedText("సీపీఐం(ఎం)", "CPI(M)"), 17324, true, 7825),
        ElectionRecord(1972, LocalizedText("ఉమ్మడి ఆంధ్రప్రదేశ్", "United Andhra Pradesh"), LocalizedText("కత్తుల శాంతయ్య", "Kathula Santhaiah"), 39477, LocalizedText("బాజీ హనుమంతు", "Baji Hanumanthu"), LocalizedText("సీపీఐం(ఎం)", "CPI(M)"), 14925, true, 24552),
        ElectionRecord(1978, LocalizedText("ఉమ్మడి ఆంధ్రప్రదేశ్", "United Andhra Pradesh"), LocalizedText("హుస్సేను పొట్ట పింజర", "Hussain Potta Pinjara"), 30107, LocalizedText("కోట గురుమూర్తి", "Kota Gurumurthy"), LocalizedText("జనతా పార్టీ", "Janata Party"), 24355, true, 5752),
        ElectionRecord(1983, LocalizedText("ఉమ్మడి ఆంధ్రప్రదేశ్", "United Andhra Pradesh"), LocalizedText("సంబాని చంద్రశేఖర్", "Sambani Chandrasekhar"), 27626, LocalizedText("భీమపాక భూపతిరావు", "Bhimapaka Bhupathi Rao"), LocalizedText("కమ్యూనిస్టు పార్టీ", "Communist Party"), 35915, false, 8289),
        ElectionRecord(1985, LocalizedText("ఉమ్మడి ఆంధ్రప్రదేశ్", "United Andhra Pradesh"), LocalizedText("సంబాని చంద్రశేఖర్", "Sambani Chandrasekhar"), 39249, LocalizedText("బాజీ హనుమంతు", "Baji Hanumanthu"), LocalizedText("సీపీఐం(ఎం)", "CPI(M)"), 40217, false, 968),
        ElectionRecord(1989, LocalizedText("ఉమ్మడి ఆంధ్రప్రదేశ్", "United Andhra Pradesh"), LocalizedText("సంబాని చంద్రశేఖర్", "Sambani Chandrasekhar"), 55845, LocalizedText("బాజీ హనుమంతు", "Baji Hanumanthu"), LocalizedText("సీపీఐం(ఎం)", "CPI(M)"), 51530, true, 4315),
        ElectionRecord(1994, LocalizedText("ఉమ్మడి ఆంధ్రప్రదేశ్", "United Andhra Pradesh"), LocalizedText("సంబాని చంద్రశేఖర్", "Sambani Chandrasekhar"), 53172, LocalizedText("సండ్ర వెంకట వీరయ్య", "Sandra Venkata Veeraiah"), LocalizedText("సీపీఐం(ఎం)", "CPI(M)"), 63328, false, 10156),
        ElectionRecord(1999, LocalizedText("ఉమ్మడి ఆంధ్రప్రదేశ్", "United Andhra Pradesh"), LocalizedText("సంబాని చంద్రశేఖర్", "Sambani Chandrasekhar"), 51638, LocalizedText("సండ్ర వెంకట వీరయ్య", "Sandra Venkata Veeraiah"), LocalizedText("సీపీఐం(ఎం)", "CPI(M)"), 40380, true, 11258),
        ElectionRecord(2004, LocalizedText("ఉమ్మడి ఆంధ్రప్రదేశ్", "United Andhra Pradesh"), LocalizedText("సంబాని చంద్రశేఖర్", "Sambani Chandrasekhar"), 78422, LocalizedText("సండ్ర వెంకట వీరయ్య", "Sandra Venkata Veeraiah"), LocalizedText("తెలుగుదేశం", "Telugu Desam"), 54500, true, 23922),
        ElectionRecord(2009, LocalizedText("ఉమ్మడి ఆంధ్రప్రదేశ్", "United Andhra Pradesh"), LocalizedText("రాంరెడ్డి వెంకటరెడ్డి", "Ramreddy Venkata Reddy"), 64555, LocalizedText("తమ్మినేని వీరభద్రం", "Tammineni Veerabhadram"), LocalizedText("సీపీఐం(ఎం)", "CPI(M)"), 58889, true, 5666),
        ElectionRecord(2014, LocalizedText("తెలంగాణ", "Telangana"), LocalizedText("రాంరెడ్డి వెంకటరెడ్డి", "Ramreddy Venkata Reddy"), 69707, LocalizedText("మద్దినేని బేబీ స్వర్ణకుమారి", "Maddineni Baby Swarna Kumari"), LocalizedText("తెలుగుదేశం", "Telugu Desam"), 47844, true, 21863),
        ElectionRecord(2016, LocalizedText("తెలంగాణ", "Telangana"), LocalizedText("రాంరెడ్డి సుచరిత", "Ramreddy Sucharitha"), 49258, LocalizedText("తుమ్మల నాగేశ్వరరావు", "Tummala Nageswara Rao"), LocalizedText("టీఆర్ఎస్", "TRS"), 94940, false, 45682, LocalizedText("ఉపఎన్నిక", "By-election")),
        ElectionRecord(2018, LocalizedText("తెలంగాణ", "Telangana"), LocalizedText("కందాళ ఉపేందర్ రెడ్డి", "Kandala Upender Reddy"), 89407, LocalizedText("తుమ్మల నాగేశ్వరరావు", "Tummala Nageswara Rao"), LocalizedText("టీఆర్ఎస్", "TRS"), 81738, true, 7669),
        ElectionRecord(2023, LocalizedText("తెలంగాణ", "Telangana"), LocalizedText("పొంగులేటి శ్రీనివాస రెడ్డి", "Ponguleti Srinivas Reddy"), 127820, LocalizedText("కందాళ ఉపేందర్ రెడ్డి", "Kandala Upender Reddy"), LocalizedText("బీఆర్ఎస్", "BRS"), 71170, true, 56650)
    )

    val ministers = listOf(
        MinisterRecord(LocalizedText("సంబాని చంద్రశేఖర్", "Sambani Chandrasekhar"), LocalizedText("ఉమ్మడి ఆంధ్రప్రదేశ్ కాలం", "United Andhra Pradesh period"), LocalizedText("ఆరోగ్య మరియు వైద్య వ్యవహారాలు", "Health and Medical Affairs"), LocalizedText("ఉమ్మడి ఆంధ్రప్రదేశ్", "United Andhra Pradesh"), LocalizedText("కాంగ్రెస్", "Congress"), LocalizedText("పాలేరు నుంచి ఎమ్మెల్యేగా మంత్రివర్గంలో పనిచేశారు.", "Served in the state cabinet while representing Palair.")),
        MinisterRecord(LocalizedText("రాంరెడ్డి వెంకటరెడ్డి", "Ramreddy Venkata Reddy"), LocalizedText("2010–2014", "2010–2014"), LocalizedText("ఉద్యానవనం మరియు పట్టు పరిశ్రమ", "Horticulture and Sericulture"), LocalizedText("ఉమ్మడి ఆంధ్రప్రదేశ్", "United Andhra Pradesh"), LocalizedText("కాంగ్రెస్", "Congress"), LocalizedText("పాలేరు ఎమ్మెల్యేగా ఉన్నప్పుడు మంత్రిగా సేవలందించారు.", "Served as a minister while MLA for Palair.")),
        MinisterRecord(LocalizedText("తుమ్మల నాగేశ్వరరావు", "Tummala Nageswara Rao"), LocalizedText("2016–2018", "2016–2018"), LocalizedText("రోడ్లు మరియు భవనాలు", "Roads and Buildings"), LocalizedText("తెలంగాణ", "Telangana"), LocalizedText("టీఆర్ఎస్", "TRS"), LocalizedText("చారిత్రక సంపూర్ణత కోసం చేర్చబడింది.", "Included for historical completeness.")),
        MinisterRecord(LocalizedText("పొంగులేటి శ్రీనివాస రెడ్డి", "Ponguleti Srinivas Reddy"), LocalizedText("2023 నుంచి ప్రస్తుతం", "2023–present"), LocalizedText("రెవెన్యూ, గృహ నిర్మాణం, సమాచార మరియు ప్రజాసంబంధాలు", "Revenue and Housing; Information & Public Relations"), LocalizedText("తెలంగాణ", "Telangana"), LocalizedText("కాంగ్రెస్", "Congress"), LocalizedText("ప్రస్తుత మంత్రివర్గ పట్టికలో పాలేరుకు ప్రాతినిధ్యం.", "Current cabinet listing identifies Palair as the constituency."), true)
    )

    val sources = listOf(
        SourceRecord(LocalizedText("తెలంగాణ రాష్ట్ర ఎన్నికల సంఘం — 2025 సర్పంచ్ ఫలితాలు", "Telangana State Election Commission — 2025 Sarpanch results"), LocalizedText("నాలుగు మండలాలలో 134 గ్రామపంచాయతీల ఎన్నికైన పేర్లు.", "Official elected names for 134 Gram Panchayats across four mandals."), sarpanchResultsUrl),
        SourceRecord(LocalizedText("ఖమ్మం జిల్లా — ప్రజాప్రతినిధులు", "Khammam District — Public Representatives"), LocalizedText("పాలేరు ఎమ్మెల్యే పేరు, పదవి, అధికారిక చిత్రం.", "Palair MLA name, office and official portrait."), "https://khammam.telangana.gov.in/public-representatives/"),
        SourceRecord(LocalizedText("తెలంగాణ మంత్రివర్గం", "Telangana Council of Ministers"), LocalizedText("ప్రస్తుత శాఖలు మరియు నియోజకవర్గం.", "Current portfolios and constituency."), "https://www.telangana.gov.in/Government/Council-of-Ministers/"),
        SourceRecord(LocalizedText("ఖమ్మం గ్రామపంచాయతీ డైరెక్టరీ", "Khammam Gram Panchayat directory"), LocalizedText("ఎల్‌జీడీ కోడ్లు, పంచాయతీ సెక్రటరీల ప్రజా డైరెక్టరీ.", "Public LGD codes and Panchayat Secretary directory."), panchayatDirectoryUrl),
        SourceRecord(LocalizedText("మండల కాంగ్రెస్ అధ్యక్షుల నియామక నివేదిక", "Reported Mandal Congress appointments"), LocalizedText("మే 2026లో ప్రచురించిన ప్రజా వార్తా నివేదిక; అధికారిక పార్టీ డైరెక్టరీ కాదు.", "Public report from May 2026; not an official party directory."), mandalPresidentsUrl),
        SourceRecord(LocalizedText("భారత ఎన్నికల సంఘం", "Election Commission of India"), LocalizedText("1962–2009 ఎన్నికల గణాంక నివేదికలు.", "Statistical reports for historical Assembly elections."), "https://www.eci.gov.in/statistical-report/statistical-reports"),
        SourceRecord(LocalizedText("భారత ఎన్నికల సంఘం — 2023 వివరణాత్మక ఫలితాలు", "Election Commission of India — 2023 detailed results"), LocalizedText("పొంగులేటి శ్రీనివాస రెడ్డి 2023 ఎన్నికల ప్రకటనలో తెలిపిన వయస్సు: 58.", "Ponguleti Srinivas Reddy's declared age in the 2023 election record: 58."), eci2023DetailedResultsUrl)
    )
}
